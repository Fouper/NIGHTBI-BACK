package com.night.bi.manager;

import com.night.bi.common.ErrorCode;
import com.night.bi.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 提供 RedisLimiter 限流基础服务（通用能力）
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     *
     * @param key 区分不同的限流器，比如根据不同的用户 id 统计
     */
    public void doRateLimit(String key) {
        // OVERALL意味着限制是基于所有请求的总体频率，而不是针对单个用户或客户端。
        // 2：这是速率的数量。在这个例子中，它表示允许的最大请求数量。也就是令牌数
        // 1：这是速率的时间间隔。与RateIntervalUnit.SECONDS结合使用时，这表示在1秒钟内。
        // RateIntervalUnit.SECONDS：这是时间间隔的单位。在这里，它指的是秒。
        // 这里的限制是每秒最多2次
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        // 来了一个操作后请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        if (!canOp) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

    }
}
