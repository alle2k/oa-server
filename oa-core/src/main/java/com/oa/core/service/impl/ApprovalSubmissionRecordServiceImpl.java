package com.oa.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.core.domain.ApprovalSubmissionRecord;
import com.oa.core.mapper.master.ApprovalSubmissionRecordMapper;
import com.oa.core.service.IApprovalSubmissionRecordService;
import org.springframework.stereotype.Service;

@Service
public class ApprovalSubmissionRecordServiceImpl extends ServiceImpl<ApprovalSubmissionRecordMapper, ApprovalSubmissionRecord> implements IApprovalSubmissionRecordService {
}
