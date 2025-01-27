package com.night.bi.mq;

import com.night.bi.common.ErrorCode;
import com.night.bi.exception.BusinessException;
import com.night.bi.model.entity.Chart;
import com.night.bi.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DeadMessageConsumer {

    @Resource
    private ChartService chartService;

    /**
     * @param message     接收到的消息内容。
     * @param channel     与 RabbitMQ 通信的通道对象。
     * @param deliveryTag 消息的交付标签，用来唯一标识消息的标签,用于手动确认消息。初始值为1
     */
    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {MqConstant.DEAD_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (StringUtils.isEmpty(message)) {
            channel.basicAck(deliveryTag, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        log.info("Dead message：{}", message);
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            // 如果找不到图表，记录错误并确认消息
            log.error("死信队列中的图表ID {} 不存在", chartId);
            channel.basicAck(deliveryTag, false);
            return;
        }
        chart.setId(chartId);
        chart.setStatus(0);
        boolean b = chartService.updateById(chart);
        if (!b)
            log.error("更新图表失败状态错误" + chartId);
        channel.basicAck(deliveryTag, false);
    }
}
