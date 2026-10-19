package com.owen.trigger.job.supplier;

import com.owen.api.supplier.PullSupplierCommand;
import com.owen.application.supplier.SupplierStaticPullAppService;
import com.owen.types.enums.SupplierEnum;
import com.owen.types.log.Logs;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 上海大剧院静态拉取。两个任务分开调度：项目不带下级，下级不发现新项目。
 */
@Component
public class ShanghaiGrandTheatrePullJob {

    private final SupplierStaticPullAppService appService;

    public ShanghaiGrandTheatrePullJob(SupplierStaticPullAppService appService) {
        this.appService = appService;
    }

    @XxlJob("shanghaiGrandTheatreProjectPull")
    public void pullProjects() {
        Logs.info("拉取供应商项目", "开始拉取上海大剧院全部项目");
        appService.pullProjects(new PullSupplierCommand(SupplierEnum.SHANGHAI_GRAND_THEATRE.getId()));
        Logs.info("拉取供应商项目", "上海大剧院全部项目拉取结束");
    }

    @XxlJob("shanghaiGrandTheatreChildPull")
    public void pullChildren() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        if (shardIndex < 0 || shardTotal <= 0 || shardIndex >= shardTotal) {
            throw new IllegalStateException("下级拉取分片参数无效: " + shardIndex + "/" + shardTotal);
        }
        Logs.info("拉取供应商场次和票品",
                "开始拉取上海大剧院已落库项目的场次和票品，分片 " + shardIndex + "/" + shardTotal);
        appService.pullChildren(new PullSupplierCommand(
                SupplierEnum.SHANGHAI_GRAND_THEATRE.getId(), shardIndex, shardTotal));
        Logs.info("拉取供应商场次和票品", "上海大剧院已落库项目的场次和票品拉取结束");
    }

}
