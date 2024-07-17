package com.night.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.night.bi.common.ErrorCode;
import com.night.bi.common.ResultUtils;
import com.night.bi.exception.BusinessException;
import com.night.bi.exception.ThrowUtils;
import com.night.bi.mapper.ScoreMapper;
import com.night.bi.model.entity.Score;
import com.night.bi.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 积分服务实现
 *
 * @author WL丶Night
 * @from WL丶Night
 */
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    @Override
    public boolean addScore(Long userId) {
        Score scoreUser = new Score();
        // 未签到
        scoreUser.setIsSign(0);
        // 初始积分10分
        scoreUser.setScoreTotal(10L);
        scoreUser.setUserId(userId);
        boolean scoreResult = this.save(scoreUser);
        if (!scoreResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册积分异常");
        }
        return scoreResult;
    }

    @Override
    public void checkIn(Long userId) {
        QueryWrapper<Score> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        Score score = this.getOne(queryWrapper);
        ThrowUtils.throwIf(score == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(score.getIsSign() == 1, ErrorCode.PARAMS_ERROR, "领取失败，今日已领取");
        Long scoreTotal = score.getScoreTotal();
        UpdateWrapper<Score> updateWrapper = new UpdateWrapper();
        updateWrapper
                //此处暂时写死签到积分
                .eq("userId", userId)
                .set("scoreTotal", scoreTotal + 1)
                .set("isSign", 1);
        boolean res = this.update(updateWrapper);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "更新签到数据失败");
    }

    @Override
    public void deductPoints(Long userId, Long points) {
        QueryWrapper<Score> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        Score score = this.getOne(queryWrapper);
        ThrowUtils.throwIf(score.getScoreTotal() < points, ErrorCode.OPERATION_ERROR, "积分不足，请联系管理员！");
        Long scoreTotal = score.getScoreTotal();
        UpdateWrapper<Score> updateWrapper = new UpdateWrapper();
        updateWrapper
                .eq("userId", userId)
                .set("scoreTotal", scoreTotal - points);
        boolean res = this.update(updateWrapper);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
    }



    @Override
    public Long getUserPoints(Long userId) {
        QueryWrapper<Score> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        Score score = this.getOne(queryWrapper);
        return score.getScoreTotal();
    }

    @Override
    public int getIsSign(Long userId) {
        QueryWrapper<Score> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        Score score = this.getOne(queryWrapper);
        return score.getIsSign();
    }

    @Override
    public boolean updateScore(Score score) {
        Long userId = score.getUserId();
        Long scoreTotal = score.getScoreTotal();
        UpdateWrapper<Score> updateWrapper = new UpdateWrapper();
        updateWrapper
                .eq("userId", userId)
                .set("scoreTotal", scoreTotal);
        boolean res = this.update(updateWrapper);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return res;
    }
}



