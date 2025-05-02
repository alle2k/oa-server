package com.oa.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @auther CodeGenerator
 * @create 2025-03-04 11:40:47
 * @describe 实体类
 */
@NoArgsConstructor
@Data
@TableName("business_order_item")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "BusinessOrderItem对象", description = "")
public class BusinessOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "PK")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("合同ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "业务类型")
    @TableField("biz_type")
    private Integer bizType;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty(value = "删除状态 0-未删除 1-已删除")
    @TableField("deleted")
    private Integer deleted;

    @ApiModelProperty(value = "创建人")
    @TableField("create_user")
    private Long createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty(value = "修改人")
    @TableField("update_user")
    private Long updateUser;

    @ApiModelProperty(value = "修改时间")
    @TableField("update_time")
    private Date updateTime;

    public BusinessOrderItem(Long orderId, Integer bizType, Long userId) {
        this.orderId = orderId;
        this.bizType = bizType;
        this.createUser = userId;
        this.updateUser = userId;
    }
}