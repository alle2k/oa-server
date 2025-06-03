package com.oa.core.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@ApiModel("用户信息")
@Data
public class UserShortVo {

    @ApiModelProperty(value = "id")
    private Long id;

    private Long deptId;

    @ApiModelProperty("所属部门ID列表")
    private String deptIdList;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("用户类型")
    private String type;

    @ApiModelProperty("性别 0-female 1-male")
    private Integer gender;

    @ApiModelProperty("职务")
    private String title;

    @ApiModelProperty("启用状态 0-否 1-是")
    private Integer enableStatus;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("创建时间")
    private Date createTime;

    public UserShortVo(String name, Long id, String phone, String account, Integer gender, String avatar, Date createTime, Integer enableStatus) {
        this.name = name;
        this.id = id;
        this.phone = phone;
        this.account = account;
        this.gender = gender;
        this.avatar = avatar;
        this.createTime = createTime;
        this.enableStatus = enableStatus;
    }
}
