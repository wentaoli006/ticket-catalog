package com.owen.trigger.job.supplier;

import com.owen.api.supplier.PullSupplierCommand;
import com.owen.application.supplier.SupplierStaticPullAppService;
import com.owen.types.enums.SupplierEnum;
import com.owen.types.log.Logs;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 拉取上海大剧院已落库项目的场次和票品。不发现新项目，不申请 POI。
 */
@Component
public class ShanghaiGrandTheatreChildPullJob {

    private final SupplierStaticPullAppService appService;

    public ShanghaiGrandTheatreChildPullJob(SupplierStaticPullAppService appService) {
        this.appService = appService;
    }

    @XxlJob("shanghaiGrandTheatreChildPull")
    public void pullChildren() {
        Logs.info("拉取供应商场次和票品", "开始拉取上海大剧院已落库项目的场次和票品");
        appService.pullChildren(new PullSupplierCommand(SupplierEnum.SHANGHAI_GRAND_THEATRE.getId()));
        Logs.info("拉取供应商场次和票品", "上海大剧院已落库项目的场次和票品拉取结束");
    }

}
