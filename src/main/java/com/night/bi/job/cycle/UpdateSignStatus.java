package com.night.bi.job.cycle;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.night.bi.common.ErrorCode;
import com.night.bi.exception.ThrowUtils;
import com.night.bi.model.entity.Score;
import com.night.bi.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class UpdateSignStatus {

    @Resource
    private ScoreService scoreService;

    /**
     * 每天凌晨0点更新积分表的isSign为0
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void sendMessageToClient() {
        UpdateWrapper<Score> updateWrapper = new UpdateWrapper<>();
        //更新Score表中isSign为1的数据
        updateWrapper.set("isSign", 0)
                .eq("isSign", 1);
        boolean updateResult = scoreService.update(updateWrapper);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR);
        log.info("Check-in status has been updated!");
    }
}