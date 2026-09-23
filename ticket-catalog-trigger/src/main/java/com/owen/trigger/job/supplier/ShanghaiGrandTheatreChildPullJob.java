package com.owen.trigger.job.supplier;

import com.owen.api.supplier.PullSupplierCommand;
import com.owen.application.supplier.SupplierStaticPullAppService;
import com.owen.types.enums.SupplierEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 拉取上海大剧院已落库项目的场次和票品。不发现新项目，不申请 POI。
 */
@Slf4j
@Component
public class ShanghaiGrandTheatreChildPullJob {

    private final SupplierStaticPullAppService appService;

    public ShanghaiGrandTheatreChildPullJob(SupplierStaticPullAppService appService) {
        this.appService = appService;
    }

    @XxlJob("shanghaiGrandTheatreChildPull")
    public void pullChildren() {
        log.info("开始拉取供应商场次和票品 supplier={}", SupplierEnum.SHANGHAI_GRAND_THEATRE.getName());
        appService.pullChildren(new PullSupplierCommand(SupplierEnum.SHANGHAI_GRAND_THEATRE.getId()));
        log.info("拉取供应商场次和票品结束 supplier={}", SupplierEnum.SHANGHAI_GRAND_THEATRE.getName());
    }

}
