package com.night.bi.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.night.bi.common.BaseResponse;
import com.night.bi.common.ErrorCode;
import com.night.bi.common.ResultUtils;
import com.night.bi.constant.FileConstant;
import com.night.bi.exception.BusinessException;
import com.night.bi.model.dto.file.UploadFileRequest;
import com.night.bi.model.entity.User;
import com.night.bi.model.enums.FileUploadBizEnum;
import com.night.bi.service.UserService;

import java.io.*;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            // 调用上传方法并获取响应
            String response = uploadImageToPicgo(file);
            // 返回可访问地址
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("文件上传失败, 文件路径 = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("文件删除失败, 文件路径 = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long TEN_M = 10 * 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > TEN_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 10M");
            }
            if (!Arrays.asList("jpeg", "jpg", "bmp", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    /**
     * 图片上传
     *
     * @param file
     * @return
     */
    public String uploadImageToPicgo(File file) {
        // 获取图床api和key
        String apiUrl = FileConstant.COS_HOST;
        String apiKey = FileConstant.API_KEY;

        try {
            // 创建HTTP请求对象
            HttpRequest request = HttpRequest.post(apiUrl)
                    .form("key", apiKey)  // 添加API密钥参数
                    .form("source", file);
            String response = request.execute().body();

            // 解析JSON响应并提取图片URL
            JSONObject jsonObject = JSONUtil.parseObj(response).getJSONObject("image");
            String imageUrl = jsonObject.getStr("url");
            return imageUrl;
        } catch (HttpException e) {
            // HTTP请求异常处理
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "网络可能出现异常，请稍后再试");
        } catch (Exception e) {
            // 其他异常处理
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

    }

}
