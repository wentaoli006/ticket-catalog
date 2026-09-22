package com.owen.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 接收消息
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = "ticket-catalog-mq", consumerGroup = "ticket-catalog-group")
public class MQListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {
        log.info("接收到RocketMQ消息 {}", s);
    }

}
