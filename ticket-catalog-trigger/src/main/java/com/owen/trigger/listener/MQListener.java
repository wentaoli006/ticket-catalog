package com.owen.trigger.listener;

import com.owen.types.log.Logs;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 接收消息
 */
@Component
@RocketMQMessageListener(topic = "ticket-catalog-mq", consumerGroup = "ticket-catalog-group")
public class MQListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {
        Logs.info("接收到RocketMQ消息", "ticket-catalog-mq " + s);
    }

}
