package com.oa.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("test")
@Data
public class Test {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("remark")
    private String remark;
}
