package com.owen.trigger.job.supplier;

import com.owen.api.supplier.PullSupplierCommand;
import com.owen.application.supplier.SupplierStaticPullAppService;
import com.owen.types.enums.SupplierEnum;
import com.owen.types.log.Logs;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 拉取上海大剧院全部项目。不拉场次和票品。
 */
@Component
public class ShanghaiGrandTheatreProjectPullJob {

    private final SupplierStaticPullAppService appService;

    public ShanghaiGrandTheatreProjectPullJob(SupplierStaticPullAppService appService) {
        this.appService = appService;
    }

    @XxlJob("shanghaiGrandTheatreProjectPull")
    public void pullProjects() {
        Logs.info("拉取供应商项目", "开始拉取上海大剧院全部项目");
        appService.pullProjects(new PullSupplierCommand(SupplierEnum.SHANGHAI_GRAND_THEATRE.getId()));
        Logs.info("拉取供应商项目", "上海大剧院全部项目拉取结束");
    }

}
