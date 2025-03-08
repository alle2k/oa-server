package com.oa.common.core.domain.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "数据权限DTO")
public class DataPermissionDto {

    @ApiModelProperty(value = "数据范围", example = "全部数据")
    private String dataScope;

    @ApiModelProperty(value = "部门ID列表", example = "[1, 2, 3]")
    private List<Long> deptIds;

    @ApiModelProperty(value = "登录用户ID", example = "123456")
    private Long loginUser;

    public DataPermissionDto(String dataScope) {
        this.dataScope = dataScope;
    }

    public DataPermissionDto(String dataScope, List<Long> deptIds) {
        this.dataScope = dataScope;
        this.deptIds = deptIds;
    }

    public DataPermissionDto(String dataScope, Long loginUser) {
        this.dataScope = dataScope;
        this.loginUser = loginUser;
    }
}
