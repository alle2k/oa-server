package com.oa.core.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderAccountAgencySaveDto {

    @NotNull
    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date serviceBeginDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private Date serviceEndDate;

    @Min(0)
    @NotNull
    private BigDecimal amount;

    @NotEmpty
    private List<String> annexUrlList;
}
