package com.owen.trigger.http.admin;

import com.owen.api.supplier.PullProjectChildrenCommand;
import com.owen.application.supplier.SupplierStaticPullAppService;
import com.owen.types.common.Constants;
import com.owen.types.model.Response;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API 原始项目管理后台。手动拉取只提交任务，接口返回不表示拉取完成。
 */
@Validated
@RestController
@RequestMapping("/admin/api-projects")
public class ApiProjectAdminController {

    private final SupplierStaticPullAppService appService;

    public ApiProjectAdminController(SupplierStaticPullAppService appService) {
        this.appService = appService;
    }

    @PostMapping("/children")
    public Response<Void> pullProjectChildren(@RequestParam @NotNull @Positive Long supplierId,
                                               @RequestParam @NotBlank String supplierProjectId) {
        appService.pullProjectChildren(new PullProjectChildrenCommand(supplierId, supplierProjectId));
        return Response.<Void>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .build();
    }

}
