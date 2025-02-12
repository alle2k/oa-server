package com.oa.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.constant.TransactionConstant;
import com.oa.common.utils.OrikaMapperUtils;
import com.oa.common.utils.SecurityUtils;
import com.oa.core.domain.BusinessOrder;
import com.oa.core.mapper.master.BusinessOrderMapper;
import com.oa.core.model.dto.BusinessOrderSaveDto;
import com.oa.core.service.IBusinessOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class BusinessOrderServiceImpl extends ServiceImpl<BusinessOrderMapper, BusinessOrder> implements IBusinessOrderService {

    @Transactional(TransactionConstant.MASTER)
    @Override
    public void add(BusinessOrderSaveDto saveDto) {
        Long userId = SecurityUtils.getUserId();
        BusinessOrder entity = OrikaMapperUtils.map(saveDto, BusinessOrder.class);
        List<String> annexUrlList = saveDto.getAnnexUrlList();
        if (!CollectionUtils.isEmpty(annexUrlList)) {
            entity.setAnnexUrl(String.join(",", annexUrlList));
        }
        List<String> paymentScreenshotList = saveDto.getPaymentScreenshotList();
        if (!CollectionUtils.isEmpty(paymentScreenshotList)) {
            entity.setPaymentScreenshot(String.join(",", paymentScreenshotList));
        }
        entity.setCreateUser(userId);
        entity.setUpdateUser(userId);
        save(entity);
    }
}
