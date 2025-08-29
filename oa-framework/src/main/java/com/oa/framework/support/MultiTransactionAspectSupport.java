package com.oa.framework.support;

import com.oa.framework.aspectj.MultiTransactionalAspect;
import org.apache.commons.lang3.StringUtils;

import java.util.Queue;


public class MultiTransactionAspectSupport {

    private static final ThreadLocal<Queue<MultiTransactionalAspect.MultiTransactionInfo>> transactionInfos = new ThreadLocal<>();

    public static void setCurrentTransationInfos(Queue<MultiTransactionalAspect.MultiTransactionInfo> multiTransactionInfos) {
        transactionInfos.set(multiTransactionInfos);
    }

    protected static Queue<MultiTransactionalAspect.MultiTransactionInfo> currentTransactionInfos() {
        return transactionInfos.get();
    }

    public static void setRollbackOnly() {
        setRollbackOnly(null);
    }

    public static void setRollbackOnly(String transactionManagerName) {
        Queue<MultiTransactionalAspect.MultiTransactionInfo> multiTransactionInfos = currentTransactionInfos();
        multiTransactionInfos.stream().filter(info -> {
            return StringUtils.isBlank(transactionManagerName)
                    || transactionManagerName.equals(info.getDataSourceManagerName());
        }).forEach(info -> {
            info.setRollbackOnly();
        });
    }

}
