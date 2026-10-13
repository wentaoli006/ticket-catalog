package com.owen.api.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

/**
 * 拉取一个已落库项目的下级。不规定下级是场次、票品还是别的。
 */
@Getter
public class PullProjectChildrenCommand {

    @NotNull
    @Positive
    private final Long supplierId;

    @NotBlank
    private final String supplierProjectId;

    public PullProjectChildrenCommand(Long supplierId, String supplierProjectId) {
        this.supplierId = supplierId;
        this.supplierProjectId = supplierProjectId;
    }

}
