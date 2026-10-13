package com.owen.trigger.job;

import com.owen.types.log.Logs;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Setter;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * XXL-Job
 */
@Component
public class XXLJob {

    @Setter(onMethod_ = @Autowired)
    private RocketMQTemplate rocketmqTemplate;

    @XxlJob("demoJobHandler")
    public void doJob() {
        // 可以在任务中，调用一些业务方法逻辑的实现，如定时扫描超时未支付订单为关单处理，恢复库存
        Logs.info("执行任务发送一条MQ消息", "demoJobHandler 发送到 ticket-catalog-mq");
        // 发送MQ消息
        rocketmqTemplate.convertAndSend("ticket-catalog-mq", "我是测试消息");
    }

}
