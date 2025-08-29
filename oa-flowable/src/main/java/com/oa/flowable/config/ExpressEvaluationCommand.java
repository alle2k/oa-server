package com.oa.flowable.config;

import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.variable.api.delegate.VariableScope;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class ExpressEvaluationCommand implements Command<Boolean> {

    private final String conditionExpression;
    private Map<String, Object> data;
    private VariableScope variableScope;

    public ExpressEvaluationCommand(String conditionExpression, Map<String, Object> data) {
        this.conditionExpression = conditionExpression;
        this.data = data;
    }

    public ExpressEvaluationCommand(String conditionExpression, VariableScope variableScope) {
        this.conditionExpression = conditionExpression;
        this.variableScope = variableScope;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        log.info("表达式：{}，参数：{}", conditionExpression, variableScope.getVariables());
        ExpressionManager expressionManager = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager();
        Expression expression = expressionManager.createExpression(conditionExpression);
        VariableScope thisVariableScope = variableScope;
        if (Objects.isNull(thisVariableScope)) {
            thisVariableScope = new ExecutionEntityImpl();
            thisVariableScope.setTransientVariables(data);
        }
        Object value = expression.getValue(thisVariableScope);
        if (value instanceof String) {
            return !((String) expression.getValue(thisVariableScope)).contains("false");
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.FALSE;
    }
}
