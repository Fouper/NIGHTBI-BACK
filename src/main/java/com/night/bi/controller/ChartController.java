package com.night.bi.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.night.bi.annotation.AuthCheck;
import com.night.bi.common.BaseResponse;
import com.night.bi.common.DeleteRequest;
import com.night.bi.common.ErrorCode;
import com.night.bi.common.ResultUtils;
import com.night.bi.constant.CommonConstant;
import com.night.bi.constant.UserConstant;
import com.night.bi.exception.BusinessException;
import com.night.bi.exception.ThrowUtils;
import com.night.bi.manager.AiManager;
import com.night.bi.manager.RedisCacheManager;
import com.night.bi.manager.RedisLimiterManager;
import com.night.bi.manager.TaskManager;
import com.night.bi.model.dto.chart.*;
import com.night.bi.model.entity.Chart;
import com.night.bi.model.entity.User;
import com.night.bi.model.vo.BiResponseVO;
import com.night.bi.mq.MessageProducer;
import com.night.bi.service.ChartService;
import com.night.bi.service.ScoreService;
import com.night.bi.service.UserService;
import com.night.bi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图表接口
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@RestController // Controller和ResponseBody
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @Resource
    private UserService userService;

    @Resource
    private ScoreService scoreService;

    @Resource
    private TaskManager taskManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private MessageProducer messageProducer;

    @Resource
    private RedisCacheManager redisCacheManager;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        log.info(String.valueOf(chartQueryRequest));
        String cacheKey = "listMyChartByPage_" + chartQueryRequest.getId();
        log.info(cacheKey);
        Page<Chart> cachedChartPage = redisCacheManager.getCachedResult(cacheKey);
        if (cachedChartPage != null) {
            // 缓存命中，比较缓存数据与数据库数据是否一致
            Page<Chart> databaseChartPage = chartService.page(new Page<>(current, size),
                    chartService.getQueryWrapper(chartQueryRequest));
            if (!chartService.isSamePage(cachedChartPage, databaseChartPage)) {
                redisCacheManager.putCachedResult(cacheKey, databaseChartPage);
            }
            return ResultUtils.success(cachedChartPage);
        } else {
            // 缓存未命中，从数据库中查询数据，并放入缓存
            Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                    chartService.getQueryWrapper(chartQueryRequest));
            redisCacheManager.asyncPutCachedResult(cacheKey, chartPage);
            return ResultUtils.success(chartPage);
        }
    }


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/genC")
    public BaseResponse<BiResponseVO> genChartByAi(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 用户校验
        User loginUser = userService.getLoginUser(request);
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        // 校验文件大小及后缀
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "csv", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        // 分析需求：
        // 分析网站用户的增长情况
        // 原始数据：
        // 日期,用户数
        // 6号,50
        // 7号,80
        // 8号,100

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        String csvData;
        if (!suffix.equals("csv")) {
            csvData = ExcelUtils.excelToCsv(multipartFile);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            try (InputStream inputStream = multipartFile.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
                // 逐行读取CSV文件内容，并将每行添加到字符串构建器中
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            csvData = stringBuilder.toString();
        }
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");

        scoreService.deductPoints(loginUser.getId(),1L);

        String result = aiManager.getContents(CommonConstant.BI_MODEL_ID, userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // Echarts代码过滤 "var option ="
        if (genChart.startsWith("var option =")) {
            // 去除 "var option ="
            genChart = genChart.replaceFirst("var\\s+option\\s*=\\s*", "");
        }

        JsonObject chartJson = JsonParser.parseString(genChart).getAsJsonObject();
        // 加入下载按钮
        JsonObject toolbox = new JsonObject();
        toolbox.addProperty("show", true);
        JsonObject saveAsImage = new JsonObject();
        saveAsImage.addProperty("show", true);
        saveAsImage.addProperty("excludeComponents", "['toolbox']");
        saveAsImage.addProperty("pixelRatio", 2);
        JsonObject feature = new JsonObject();
        feature.add("saveAsImage", saveAsImage);
        toolbox.add("feature", feature);
        chartJson.add("toolbox", toolbox);
        chartJson.remove("title");
        String updatedGenChart = chartJson.toString();

        // 插入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(updatedGenChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus(3);
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setChartId(chart.getId());
        biResponseVO.setGenChart(updatedGenChart);
        biResponseVO.setGenResult(genResult);

        return ResultUtils.success(biResponseVO);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/genC/async")
    public BaseResponse<BiResponseVO> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 用户校验
        User loginUser = userService.getLoginUser(request);
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        // 校验文件大小及后缀
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "csv", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        // 分析需求：
        // 分析网站用户的增长情况
        // 原始数据：
        // 日期,用户数
        // 6号,50
        // 7号,80
        // 8号,100

        scoreService.deductPoints(loginUser.getId(),1L);
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        String csvData;
        if (!suffix.equals("csv")) {
            csvData = ExcelUtils.excelToCsv(multipartFile);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            try (InputStream inputStream = multipartFile.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                // 逐行读取CSV文件内容，并将每行添加到字符串构建器中
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            csvData = stringBuilder.toString();
        }
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");

        // 插入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus(1);
        chart.setUserId(loginUser.getId());

        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setChartId(chart.getId());

        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为“执行中”，执行成功 / 失败后修改，保存执行结果
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            JsonObject chartJson;
            updateChart.setStatus(2);
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图表执行状态出错");
                return;
            }
            // 调用 AI
            String result = aiManager.getContents(CommonConstant.BI_MODEL_ID, userInput.toString());
            String[] splits = result.split("【【【【【");
            if (splits.length < 3) {
                handleChartUpdateError(chart.getId(), "AI 生成错误");
                return;
            }

            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            String updatedGenChart = "";
            try {
                chartJson = JsonParser.parseString(genChart).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "json代码解析异常");
            }
            // Echarts代码过滤 "var option ="
            if (genChart.startsWith("var option =")) {
                // 去除 "var option ="
                genChart = genChart.replaceFirst("var\\s+option\\s*=\\s*", "");
            }
            // 更新
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus(3);
            // 加入下载按钮
            JsonObject toolbox = new JsonObject();
            toolbox.addProperty("show", true);
            JsonObject saveAsImage = new JsonObject();
            saveAsImage.addProperty("show", true);
            saveAsImage.addProperty("excludeComponents", "['toolbox']");
            saveAsImage.addProperty("pixelRatio", 2);
            JsonObject feature = new JsonObject();
            feature.add("saveAsImage", saveAsImage);
            toolbox.add("feature", feature);
            chartJson.add("toolbox", toolbox);
            chartJson.remove("title");
            updatedGenChart = chartJson.toString();
            updateChartResult.setGenChart(updatedGenChart);
            boolean res = chartService.updateById(updateChartResult);
            if (!res) {
                handleChartUpdateError(updateChart.getId(), "更新图表成功状态出错");
                return;
            }
            biResponseVO.setGenChart(updatedGenChart);
            biResponseVO.setGenResult(genResult);
        }, threadPoolExecutor);
        return ResultUtils.success(biResponseVO);
    }

    /**
     * 智能分析（消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/genC/async_mq")
    public BaseResponse<BiResponseVO> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 用户校验
        User loginUser = userService.getLoginUser(request);
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        // 校验文件大小及后缀
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "csv", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        scoreService.deductPoints(loginUser.getId(),1L);

        // 插入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setStatus(1);
        chart.setUserId(loginUser.getId());
        taskManager.startTaskTimer(chart.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        long newChartId = chart.getId();
        messageProducer.sendMessage(String.valueOf(newChartId));
        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setChartId(chart.getId());
        return ResultUtils.success(biResponseVO);
    }

    /**
     * 分析错误重试
     *
     * @param id
     * @return
     */
    @GetMapping("/genC/async/mq_retry")
    public BaseResponse<BiResponseVO> retryGenChartByAiAsyncMq(Long id, HttpServletRequest request) {
        log.info(String.valueOf(id));
        Chart retrychart = chartService.getById(id);
        String Goal = retrychart.getGoal();
        String userData = retrychart.getChartData();
        String chartType = retrychart.getChartType();
        int userStatus = retrychart.getStatus();
        // 防止多次重试
        if (userStatus == 1)
            return null;

        // 插入数据库
        Chart chart = new Chart();
        chart.setStatus(1);
        chart.setId(id);
        taskManager.startTaskTimer(id);
        boolean saveResult = chartService.updateById(chart);
        if (!saveResult)
            handleChartUpdateError(chart.getId(), "图表状态更新失败");
        long newChartId = chart.getId();
        messageProducer.sendMessage(String.valueOf(newChartId));
        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setChartId(newChartId);
        return ResultUtils.success(biResponseVO);
    }
    
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus(0);
        updateChartResult.setExecMessage(execMessage);
        boolean res = chartService.updateById(updateChartResult);
        if (!res) {
            log.error("更新图表失败状态出错" + chartId + "." + execMessage);
        }
    }

}
