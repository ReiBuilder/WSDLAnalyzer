package com.abyss.wsdl.analyzer.support;

import java.util.List;

/**
 * Created by lblsloveryy on 15-2-2.
 */
public class Service {

    private String serviceName;
    private String targetNameSpace;
    private String portName;
    private int id;
    private List<Integer> OperationList;

    public List<Integer> getOperationList() {
        return OperationList;
    }

    public void setOperationList(List<Integer> operationList) {
        OperationList = operationList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTargetNameSpace() {
        return targetNameSpace;
    }

    public void setTargetNameSpace(String targetNameSpace) {
        this.targetNameSpace = targetNameSpace;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }
}
