package com.night.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.night.bi.model.entity.Score;

public interface ScoreService extends IService<Score> {

    /**
     * 添加新用户积分
     *
     * @param userId
     * @return
     */
    boolean addScore(Long userId);

    /**
     * 签到
     *
     * @param userId
     * @return
     */
    void checkIn(Long userId);

    /**
     * 消耗积分
     *
     * @param userId
     * @param points 积分数
     * @return
     */
    void deductPoints(Long userId, Long points);

    /**
     * 获取积分
     *
     * @param userId
     * @return
     */
    Long getUserPoints(Long userId);

    /**
     * 获取是否签到状态
     *
     * @param userId
     * @return
     */
    int getIsSign(Long userId);

    /**
     * 添加新用户积分
     *
     * @param score
     * @return
     */
    boolean updateScore(Score score);
}