package com.owen;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = "org.redisson.spring.starter.RedissonAutoConfigurationV2")
@Configurable
@EnableDubbo
@EnableApolloConfig
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }

}
