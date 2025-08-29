package com.oa.core.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AuditFormBusinessOrderDto {

    @NotNull
    @Min(0)
    private BigDecimal performance;
}
