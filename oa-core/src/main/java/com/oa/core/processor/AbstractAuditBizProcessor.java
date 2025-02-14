package com.oa.core.processor;

import com.oa.core.enums.ApprovalSubmissionRecordStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractAuditBizProcessor {

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 根据业务ID获取其关联的审批编号
     *
     * @param bizId 业务ID
     * @return 审批编号
     */
    public abstract String getAuditNoByBizId(Long bizId);

    /**
     * 通过时
     */
    public abstract void whenPass(Long bizId);

    /**
     * 拒绝时
     */
    public abstract void whenReject(Long bizId);

    /**
     * 撤销时
     *
     * @param bizId      业务ID
     * @param statusEnum 审批状态
     */
    public abstract void whenRevoke(Long bizId, ApprovalSubmissionRecordStatusEnum statusEnum);
}
