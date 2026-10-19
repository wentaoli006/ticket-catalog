package com.owen.api.supplier;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

/**
 * 拉取一个已落库项目的下级。入参是 api_project 主键，不携带对方协议字段。
 */
@Getter
public class PullProjectChildrenCommand {

    @NotNull
    @Positive
    private final Long projectId;

    public PullProjectChildrenCommand(Long projectId) {
        this.projectId = projectId;
    }

}
