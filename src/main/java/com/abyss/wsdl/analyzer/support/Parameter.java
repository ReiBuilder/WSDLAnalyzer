package com.abyss.wsdl.analyzer.support;

import java.util.Date;

/**
 * Created by lblsloveryy on 15-2-2.
 */
public class Parameter {

    private int id;

    private int ownerId;

    private String paramName;

    private String typeName;

    private Integer typeId;

    private String className;

    private Byte isArray;

    private Byte isRequired;

    private boolean processed;

    public boolean getProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Byte getIsArray() {
        return isArray;
    }

    public void setIsArray(Byte isArray) {
        this.isArray = isArray;
    }

    public Byte getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Byte isRequired) {
        this.isRequired = isRequired;
    }
}
