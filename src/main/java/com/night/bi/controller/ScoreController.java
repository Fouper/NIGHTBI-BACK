package com.night.bi.controller;

import com.night.bi.annotation.AuthCheck;
import com.night.bi.common.BaseResponse;
import com.night.bi.common.ErrorCode;
import com.night.bi.common.ResultUtils;
import com.night.bi.constant.UserConstant;
import com.night.bi.exception.BusinessException;
import com.night.bi.exception.ThrowUtils;
import com.night.bi.model.dto.score.ScoreUpdateRequest;
import com.night.bi.model.dto.user.UserUpdateRequest;
import com.night.bi.model.entity.Score;
import com.night.bi.model.entity.User;
import com.night.bi.service.ScoreService;
import com.night.bi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/score")
@Slf4j
public class ScoreController {

    @Resource
    private UserService userService;

    @Resource
    private ScoreService scoreService;


    /**
     * 添加新用户积分
     *
     * @param userId
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addScore(Long userId, HttpServletRequest request) {
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Score scoreRes = new Score();
        scoreRes.setUserId(userId);
        boolean scoreResult = scoreService.addScore(userId);
        ThrowUtils.throwIf(!scoreResult, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用于签到时添加积分
     *
     * @param request
     * @return
     */
    @PostMapping("/checkIn")
    public BaseResponse<String> getScoreCheckIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        scoreService.checkIn(loginUser.getId());
        return ResultUtils.success("签到成功");
    }

    /**
     * 查询积分
     *
     * @param request
     * @return
     */
    @GetMapping("/get/my")
    public BaseResponse<Long> getMyScoreById(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long totalPoints = scoreService.getUserPoints(loginUser.getId());
        return ResultUtils.success(totalPoints);
    }

    /**
     * 根据ID查询积分（管理员）
     *
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> getScoreById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userScore = scoreService.getUserPoints(id);
        return ResultUtils.success(userScore);
    }

    /**
     * 查询签到状态
     *
     * @param request
     * @return
     */
    @GetMapping("/getSign")
    public BaseResponse<Integer> getSignById(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        int isSign = scoreService.getIsSign(loginUser.getId());
        return ResultUtils.success(isSign);
    }

    /**
     * 更新用户积分
     *
     * @param scoreUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateScoreById(@RequestBody ScoreUpdateRequest scoreUpdateRequest, HttpServletRequest request) {
        if (scoreUpdateRequest == null || scoreUpdateRequest.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Score score = new Score();
        BeanUtils.copyProperties(scoreUpdateRequest, score);
        boolean result = scoreService.updateScore(score);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

}
