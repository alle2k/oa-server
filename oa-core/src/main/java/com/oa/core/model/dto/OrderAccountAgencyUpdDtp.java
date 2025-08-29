package com.oa.core.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderAccountAgencyUpdDtp extends OrderAccountAgencySaveDto {

    @NotNull
    private Long id;
}
