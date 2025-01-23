package com.oa.framework.aspectj;

import com.oa.common.annotation.MultiTransactional;
import com.oa.framework.support.MultiTransactionAspectSupport;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

@Aspect
@Component
public class MultiTransactionalAspect implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Setter
    private ApplicationContext applicationContext;

    @Around("@annotation(multiTransactional)")
    public Object around(ProceedingJoinPoint pjp, MultiTransactional multiTransactional) throws Throwable {
        Deque<MultiTransactionInfo> transactionInfos = new ArrayDeque<>();

        // push transactionInfos to current thread
        MultiTransactionAspectSupport.setCurrentTransationInfos(transactionInfos);

        try {
            if (!openTransaction(transactionInfos, multiTransactional)) {
                return null;
            }

            Object ret = pjp.proceed();

            commit(transactionInfos);

            return ret;
        } catch (Throwable e) {

            rollback(transactionInfos);

            logger.error(String.format("MultiTransactionalAspect, method:%s-%s occors error:",
                    pjp.getTarget().getClass().getSimpleName(), pjp.getSignature().getName()), e);
            throw e;
        }
    }

    private boolean openTransaction(Deque<MultiTransactionInfo> multiTransactionInfos,
                                    MultiTransactional multiTransactional) {
        String[] transactionMangerNames = multiTransactional.values();
        if (ArrayUtils.isEmpty(transactionMangerNames)) {
            return false;
        }
        for (String beanName : transactionMangerNames) {
            DataSourceTransactionManager dataSourceTransactionManager =
                    (DataSourceTransactionManager) applicationContext.getBean(beanName);
            MultiTransactionInfo info = new MultiTransactionInfo(beanName, dataSourceTransactionManager);
            multiTransactionInfos.addFirst(info);
        }
        return true;
    }

    private void commit(Queue<MultiTransactionInfo> multiTransactionInfos) {
        while (!multiTransactionInfos.isEmpty()) {
            MultiTransactionInfo info = multiTransactionInfos.poll();
            info.commit();
        }
    }

    private void rollback(Queue<MultiTransactionInfo> multiTransactionInfos) {
        while (!multiTransactionInfos.isEmpty()) {
            MultiTransactionInfo info = multiTransactionInfos.poll();
            info.rollback();
        }
    }

    public static class MultiTransactionInfo {

        @Getter
        private final String dataSourceManagerName;
        private final DataSourceTransactionManager dataSourceTransactionManager;
        private final TransactionStatus status;

        MultiTransactionInfo(String dataSourceManagerName,
                             DataSourceTransactionManager dataSourceTransactionManager) {
            this.dataSourceManagerName = dataSourceManagerName;
            this.dataSourceTransactionManager = dataSourceTransactionManager;
            this.status = this.dataSourceTransactionManager.getTransaction(new DefaultTransactionDefinition());
        }

        public void commit() {
            this.dataSourceTransactionManager.commit(this.status);
        }

        public void rollback() {
            this.dataSourceTransactionManager.rollback(this.status);
        }

        public void setRollbackOnly() {
            this.status.setRollbackOnly();
        }

    }

}