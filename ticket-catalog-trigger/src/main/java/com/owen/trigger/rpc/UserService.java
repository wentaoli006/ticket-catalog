package com.owen.trigger.rpc;

import com.owen.api.IUserService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0.0", timeout = 450)
public class UserService implements IUserService {

    @Override
    public String queryUserInfo(String req) {
        return "查询用户信息";
    }

}
