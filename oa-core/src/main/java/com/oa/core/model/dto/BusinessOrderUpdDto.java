package com.oa.core.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessOrderUpdDto extends BusinessOrderSaveDto {

    /**
     * PK
     */
    @NotNull
    private Long id;
}
