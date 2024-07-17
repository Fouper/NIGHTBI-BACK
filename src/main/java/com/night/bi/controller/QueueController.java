package com.night.bi.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.night.bi.manager.RedisLimiterManager;
import com.night.bi.model.dto.chart.*;
import com.night.bi.model.entity.Chart;
import com.night.bi.model.entity.User;
import com.night.bi.model.vo.BiResponseVO;
import com.night.bi.service.ChartService;
import com.night.bi.service.UserService;
import com.night.bi.utils.ExcelUtils;
import com.night.bi.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 队列测试
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({ "dev", "local" })
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name) {
        CompletableFuture.runAsync(() -> {
            System.out.println("任务执行中" + name + ", 执行人：" + Thread.currentThread().getName());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get() {
        Map<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度", size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数", taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成任务", completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数", activeCount);
        return JSONUtil.toJsonStr(map);

    }
}
