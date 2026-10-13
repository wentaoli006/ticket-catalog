package com.owen.trigger.http;

import com.owen.types.common.Constants;
import com.owen.types.log.Logs;
import com.owen.types.model.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
public class Controller {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * http://localhost:8090/success
     */
    @RequestMapping("/success")
    public Response<String> success() {
        Logs.info("测试调用", "请求 /success");
        try {
            // 随机休眠
            Thread.sleep(new Random().nextInt(1000));
            // 开启线程
            threadPoolExecutor.execute(() -> {
                Logs.info("开启线程", "请求 /success 后开启线程");
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

}
