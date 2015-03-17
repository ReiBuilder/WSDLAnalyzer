package com.abyss.wsdl.analyzer.support;

/**
 * Created by lblsloveryy on 15-2-2.
 */
public class OperationObject {

    private int id;
    private int ownerId;
    private String operationName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
