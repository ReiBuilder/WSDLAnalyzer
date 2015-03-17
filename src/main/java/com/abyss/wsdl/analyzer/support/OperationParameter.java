package com.abyss.wsdl.analyzer.support;

/**
 * Created by lblsloveryy on 15-2-10.
 */
public class OperationParameter {

    private int operationId;
    private int parameterId;
    private String paramType;

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getParameterId() {
        return parameterId;
    }

    public void setParameterId(int parameterId) {
        this.parameterId = parameterId;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }
}
