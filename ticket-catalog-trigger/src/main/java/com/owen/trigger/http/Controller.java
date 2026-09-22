package com.owen.trigger.http;

import com.owen.domain.xxx.service.XxxService;
import com.owen.types.common.Constants;
import com.owen.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@RestController
public class Controller {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private XxxService xxxService;

    /**
     * http://localhost:8090/success
     */
    @RequestMapping("/success")
    public Response<String> success() {
        log.info("测试调用");
        try {
            // 随机休眠
            Thread.sleep(new Random().nextInt(1000));
            // 开启线程
            threadPoolExecutor.execute(() -> {
                log.info("开启线程");
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (new Random().nextInt(100) == 1) throw new RuntimeException("异常");
        return Response.<String>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .data("查询用户信息")
                .build();
    }

    /**
     * http://localhost:8091/apollo/demo
     * 经领域服务读动态配置，不在 trigger 里直接碰 Apollo。
     */
    @RequestMapping("/apollo/demo")
    public Response<String> apolloDemo() {
        return Response.<String>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .data(xxxService.queryWelcome())
                .build();
    }

}
