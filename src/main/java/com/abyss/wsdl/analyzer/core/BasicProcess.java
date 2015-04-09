package com.abyss.wsdl.analyzer.core;

import com.abyss.wsdl.analyzer.support.*;
import com.abyss.wsdl.analyzer.support.OperationObject;
import com.abyss.wsdl.analyzer.support.Service;
import com.ibm.wsdl.BindingImpl;
import com.ibm.wsdl.ImportImpl;
import com.ibm.wsdl.PortImpl;
import com.ibm.wsdl.ServiceImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.*;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by lblsloveryy on 15-2-9.
 */
public class BasicProcess {

    //process control flag
    private boolean processed;
    private boolean processDefinition;
    private boolean serviceInfoGotten;


    //some constant
    private static final String BINDING = "binding";
    private static final String SERVICE = "service";
    private static final String TYPE = "type";
    private static final String PORTTYPE = "portType";
    private static final String MESSAGE = "messsage";

    //the results would be return to caller
    private Service service;
    private List<OperationObject> operationList;
    private List<Parameter> parametersList;
    private List<OperationParameter> opList;

    //support to make results
    private List<Bridge> bridgeList;

    //the input parameters
    private final String originalUrl;
    private Map<String,Integer> baseTypeMap;



    private final Map<String, Object> definitionMap = new HashMap<String, Object>();

    private final List<WsdlFileSupport> wsdlFiles = new ArrayList<>();


    public BasicProcess(String url, Map<String,Integer> BaseTypeMap)
    {
        originalUrl = url;

        service = new Service();

        operationList = new ArrayList<>();

        parametersList = new ArrayList<>();

        bridgeList = new ArrayList<>();

        opList = new ArrayList<>();

        //init process flags
        processed = false;
        processDefinition = false;
        serviceInfoGotten = false;

        baseTypeMap = BaseTypeMap;
        if(null == baseTypeMap) {
            baseTypeMap = new HashMap<>();
            baseTypeMap.put("string", 1);
            baseTypeMap.put("int", 2);
            baseTypeMap.put("boolean", 3);
            baseTypeMap.put("complexType", 4);
        }
    }

    public Map<String,Object> Process()
    {
        Map<String,Object> results = new HashMap<>();

        try {
            if(null != originalUrl)
            {
                if(!setDefinitionMap(originalUrl))
                {
                    results.put("Result","ERROR");
                    results.put("Message","The wsdl is not intact!");
                    return results;
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
            results.put("Result","ERROR");
            results.put("Message","IOException in setDefinitionMap()!");
            return results;
        } catch (WSDLException e) {
//            e.printStackTrace();
            results.put("Result","ERROR");
            results.put("Message","WSDLException in setDefinitionMap()!");
            return results;
        }

        fillServiceInfo();

        fillBridgeInfo(service.getId());


        try {
            fillOperationAndParameters();
        } catch (WSDLException e) {
//            e.printStackTrace();
            results.put("Result","ERROR");
            results.put("Message","WSDLException in fillOperationAndParameters()!");
            return results;
        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
            results.put("Result","ERROR");
            results.put("Message","NoSuchFieldException in fillOperationAndParameters()!");
            return results;
        } catch (IOException e) {
//            e.printStackTrace();
            results.put("Result","ERROR");
            results.put("Message","IOException in fillOperationAndParameters()!");
            return results;
        }

        processed = true;

        results.put("Result","SUCCESS");
        return results;
    }

    /**
     * analyser the wsdl based on original url, get the mapping of wsdl's content and wsdl tags.
     * for example, a wsdl file may contain message, types and portType. and other one import this may
     * contain service and binding tag.
     * @param url
     * @return
     * @throws WSDLException
     * @throws IOException
     */
    private boolean setDefinitionMap(String url) throws WSDLException, IOException {

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", true);
        reader.setFeature("javax.wsdl.importDocuments", false);

        /*
        the Definition has several tags, include:
        types, message, portType, binding, service
         */
        LinkedList<String> urlList = new LinkedList<String>();
        LinkedList<Integer> idStore = new LinkedList<Integer>();
        urlList.push(url);
        int count = 1;
        while (urlList.size() > 0) {
            WsdlFileSupport wsdlFile = new WsdlFileSupport();

            //make id
            int id = count;
            count++;

            //make parentId
            int parentId;
            if (idStore.size() == 0) {
                parentId = 0;
            } else {
                parentId = idStore.pop();
            }

            //make definition
            String targetUrl = urlList.pop();
            Definition def = reader.readWSDL(targetUrl);

            wsdlFile.setId(id);
            wsdlFile.setParentId(parentId);
            wsdlFile.setDefFileContent(def);
            wsdlFile.setFileContentTag(fillDefinitionMap(def));

            Map imports = def.getImports();

            if (imports.entrySet().size() > 0) {

                if (parentId == 0) {
                    wsdlFile.setDefFileType(WsdlFileSupport.ROOT);
                } else {
                    wsdlFile.setDefFileType(WsdlFileSupport.STEM);
                }

                for (Object item : imports.entrySet()) {
                    Vector v = (Vector) ((Map.Entry) item).getValue();
                    for (int i = 0; i < v.size(); i++) {
                        ImportImpl ii = (ImportImpl) v.get(i);
                        String importUrl = ii.getLocationURI();
                        idStore.push(id);
                        urlList.push(importUrl);
                    }
                }

            } else {
                if(wsdlFiles.size()>0)
                {
                    wsdlFile.setDefFileType(WsdlFileSupport.LEAF);
                }
                else
                {
                    //when a wsdl has no import tag, and files list size equals to 0,
                    //it means the wsdl is the only one for the web service.
                    wsdlFile.setDefFileType(WsdlFileSupport.ROOT);
                }
            }
            wsdlFiles.add(wsdlFile);
        }
        if(checkDefinitionMap())
        {
            processDefinition = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean checkDefinitionMap() {
        Set set = definitionMap.entrySet();
        if (set.size() == 5) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * get what content the wsdl file contains and present it by string.
     * the sign and tag mapping:
     * b ----> binding
     * s ----> service
     * t ----> types
     * p ----> portType
     * m ----> message
     * these five tags cover all element types which are Definition's direct children.
     *
     * @param def
     * @return
     * @throws WSDLException
     */
    private String fillDefinitionMap(Definition def)
            throws WSDLException {

        String contentTag = "";

        int size1 = 0;
        Map<QName,BindingImpl> temp1 = def.getAllBindings();
        for ( Object item : temp1.entrySet()) {
            Map.Entry test = (Map.Entry<QName,BindingImpl>)item;
            BindingImpl test2 = (BindingImpl)test.getValue();
            size1 = test2.getBindingOperations().size();
        }
        if (null != temp1 && size1 > 0) {
            definitionMap.put(BINDING, def);
            contentTag += "b";
        }

        Map temp2 = def.getMessages();
        int size2 = temp2.entrySet().size();
        if (null != temp2 && size2 > 0) {
            definitionMap.put(MESSAGE, def);
            contentTag += "m";
        }

        Map temp3 = def.getServices();
        int size3 = temp3.entrySet().size();
        if (null != temp3 && size3 > 0) {
            definitionMap.put(SERVICE, def);
            contentTag += "s";
        }

        Map<String, PortType> temp4 = def.getPortTypes();
        Message massage = null;
        for (Map.Entry<String, PortType> entryPortType : temp4.entrySet()) {
            PortType portType = (PortType)entryPortType.getValue();
            List<javax.wsdl.Operation> operations = portType.getOperations();
            for (int i = 0; i < operations.size(); i++) {
                Input tempIn = operations.get(i).getInput();
                massage = tempIn.getMessage();
                break;
            }
            break;
        }
        int size4 = temp4.entrySet().size();
        if (null != temp4 && size4 > 0 && null != massage) {
            definitionMap.put(PORTTYPE, def);
            contentTag += "p";
        }

        Types temp5 = def.getTypes();
        if (null != temp5) {
            definitionMap.put(TYPE, def);
            contentTag += "t";
        }

        return contentTag;
    }

    /**
     * get definition instance from definitionMap
     * @param type
     * @return
     */
    private Definition getDefinitionInstance(String type)
    {
        switch (type) {
            case BINDING: {
                return (Definition) definitionMap.get(BINDING);
            }
            case SERVICE: {
                return (Definition) definitionMap.get(SERVICE);
            }
            case TYPE: {
                return (Definition) definitionMap.get(TYPE);
            }
            case PORTTYPE: {
                return (Definition) definitionMap.get(PORTTYPE);
            }
            case MESSAGE: {
                return (Definition) definitionMap.get(MESSAGE);
            }
            default: {
                return null;
            }
        }
    }

    /**
     * get service info from definition.
     *
     * @return
     */
    private void fillServiceInfo()
    {
        Definition def = getDefinitionInstance(SERVICE);

        service.setId(1);//prepare for multiple threads word

        String targetNameSpace = def.getTargetNamespace();
        service.setTargetNameSpace(targetNameSpace);

        String nameSpace = null;
        Map<String,String> nameSpaces = def.getNamespaces();
        for ( Object item : nameSpaces.entrySet()) {
            Map.Entry itemObject = (Map.Entry<String,String>)item;
            String key = (String)itemObject.getKey();
            if(key.equals("ns1"))
            {
                nameSpace = (String)itemObject.getValue();
                service.setNameSpace(nameSpace);
                break;
            }
        }
        if(null == nameSpace)
        {
            service.setNameSpace(targetNameSpace);
        }

        Map<Object, Object> serviceMap = def.getServices();
        Set<Map.Entry<Object, Object>> set1 = serviceMap.entrySet();
        Iterator<Map.Entry<Object, Object>> it = set1.iterator();
        while(it.hasNext())
        {
            Map.Entry<Object, Object> entry = it.next();
            String serviceName = ((QName) entry.getKey()).getLocalPart();
            if(null != serviceName)
            {
                service.setServiceName(serviceName);
            }

            ServiceImpl serviceImpl = (ServiceImpl)entry.getValue();
            Map<Object,Object> map = serviceImpl.getPorts();
            Set<Map.Entry<Object, Object>> set2 = map.entrySet();
            Iterator<Map.Entry<Object, Object>> it2 = set2.iterator();
            while(it2.hasNext())
            {
                Map.Entry<Object, Object> entry2 = it2.next();
                PortImpl port = (PortImpl)entry2.getValue();
                String portName = port.getName();
                if(null != portName)
                {
                    service.setPortName(portName);
                    break;
                }
            }
        }
        serviceInfoGotten = true;
    }

    /**
     * one basic function of WSDLAnalyzer
     *
     * @param basePath
     * @return
     */
    public Map<String,Object> writeProcessedWsdlToDisk(String basePath)
    {
        Map<String, Object> result = new HashMap<>();

        if(!modifyDefImportAndMakeFileName())
        {
            result.put("Result","ERROR");
            result.put("Message","The basic process isn't finished!");
            return result;
        }

        try {
            result = writeProcessedWsdlToDiskWorker(basePath);
        } catch (IOException e) {
//            e.printStackTrace();
            result.put("Result","ERROR");
            result.put("Message","The IOException in writeProcessedWsdlToDisk()!");
            return result;
        } catch (WSDLException e) {
//            e.printStackTrace();
            result.put("Result","ERROR");
            result.put("Message","The WSDLException in writeProcessedWsdlToDisk()!");
            return result;
        }
        return result;
    }

    private Map<String, Object> writeProcessedWsdlToDiskWorker(String path) throws IOException, WSDLException {

        Map<String, Object> result = new HashMap<>();

        String serviceName = service.getServiceName();

        if(null == serviceName
                || serviceName.isEmpty()
                || serviceName.trim().length() == 0)
        {
            result.put("Result","ERROR");
            result.put("Message","The service name is fault!");
            return result;
        }
        else
        {
            if(!path.endsWith(File.separator))
            {
                path += File.separator + serviceName;
            }
            else
            {
                path += serviceName;
            }
        }

        for(int i = 0, j = wsdlFiles.size(); i < j ; i++) {
            WsdlFileSupport temp = wsdlFiles.get(i);
            String fileName = path + File.separator + temp.getFileName();
            if(!makeDirsForFile(fileName))
            {
                result.put("Result","ERROR");
                result.put("Message","The director made failed!");
                return result;
            }
            File f = new File(fileName);
            WSDLFactory factory = WSDLFactory.newInstance();
            FileWriter fw = new FileWriter(f);
            WSDLWriter writer = factory.newWSDLWriter();
            writer.writeWSDL(temp.getDefFileContent(), fw);
            fw.close();
        }

        result.put("Result","SUCCESS");
        return result;
    }

    /**
     * support writeProcessedWsdlToDiskWorker
     *
     * @param fileName
     * @return
     */
    private boolean makeDirsForFile(String fileName){
        boolean ret = true;
        File file = new File(fileName);
        File parent = file.getParentFile();
        if(parent!=null&&!parent.exists()){
            ret = parent.mkdirs();
        }
        return ret;
    }

    /**
     * when the users reuse these files off-line, they import root file,
     * and would not find imported file from root's import element.
     * because the import element uri point to a address on Internet.
     * so, I modify the default import element in these wsdl files
     * (Definition instances in code).
     *
     * @return
     */
    private boolean modifyDefImportAndMakeFileName()
    {
        if(!serviceInfoGotten)
        {
            return false;
        }

        int length = wsdlFiles.size();

        for(int i = 0; i < length; i++)
        {
            WsdlFileSupport temp = wsdlFiles.get(i);
            if(temp.getDefFileType().equals(WsdlFileSupport.ROOT))
            {
                makeFileName(temp);
            }
            else
            {
                fillParentWsdFile(temp);
            }
        }

        for(int i = 0; i < length; i++)
        {
            WsdlFileSupport temp = wsdlFiles.get(i);
            modifyImport(temp);
        }

        return true;
    }

    /**
     * add wsdl's new name to its parent children attribute.
     *
     * @param file
     */
    private void fillParentWsdFile(WsdlFileSupport file)
    {
        String fileName = makeFileName(file);
        WsdlFileSupport parent = getParentById(file.getParentId());
        parent.getChildren().add(fileName);
    }

    /**
     * make file name based on serviceName, fileContentTag and defFileType.
     *
     * @param file
     * @return
     */
    private String makeFileName(WsdlFileSupport file)
    {
        String fileName = "";
        fileName += service.getServiceName() + "_" + file.getFileContentTag() + "_" +
                file.getDefFileType() + ".wsdl";
        file.setFileName(fileName);
        return fileName;
    }

    /**
     * modify the definition-instance's import element.
     *
     * @param temp
     */
    private void modifyImport(WsdlFileSupport temp)
    {
        Definition def = temp.getDefFileContent();
        Map imports = def.getImports();
        List<String> fileNames = temp.getChildren();
        for (Object item : imports.entrySet())
        {
            Vector v = (Vector) ((Map.Entry) item).getValue();
            for(int i=0; i<v.size(); i++)
            {
                ImportImpl ii = (ImportImpl)v.get(i);
                ii.setLocationURI(fileNames.get(i));
            }
        }
    }

    private WsdlFileSupport getParentById(int id)
    {
        int length = wsdlFiles.size();
        for(int i = 0; i < length; i++)
        {
            WsdlFileSupport temp = wsdlFiles.get(i);
            if(temp.getId() ==  id)
            {
                return temp;
            }
        }
        return null;
    }

    /**
     * the WSDLAnalyzer uses Bridge to link operations and types.
     * the bridgeList is uesing in fillOperationAndParameters function.
     *
     * @param ServiceId
     */
    private void fillBridgeInfo(int ServiceId)
    {

        int serviceId = ServiceId;

        int bridgeCounter = 0;

        int operationCounter = 0;

        Definition def_portType = getDefinitionInstance(PORTTYPE);

        Definition def_message = getDefinitionInstance(MESSAGE);

        Map<String, PortType> portTypes = def_portType.getPortTypes();

        List<javax.wsdl.Operation> operations;

        Input tempIn;

        Message tempInputMsg;

        Part tempInputPart;

        Map<String, Part> tempInputParts;

        Output tempOut;

        Message tempOutputMsg;

        Part tempOutputPart;

        Map<String, Part> tempOutputParts;

        for (Map.Entry<String, PortType> entryPortType : portTypes.entrySet()) {

            operations = entryPortType.getValue().getOperations();

            for (int i = 0; i < operations.size(); i++) {

                String operationName = operations.get(i).getName();

                OperationObject operation = new OperationObject();
                operation.setId(operationCounter);
                operation.setOwnerId(serviceId);
                operation.setOperationName(operationName);
                operationList.add(operation);



                // process input part
                tempIn = operations.get(i).getInput();

                String inputMessageName = tempIn.getMessage().getQName().getLocalPart();

                tempInputMsg = def_message.getMessage(tempIn.getMessage().getQName());

                tempInputParts = tempInputMsg.getParts();

                for (Map.Entry<String, Part> entryPart : tempInputParts.entrySet()) {

                    tempInputPart = entryPart.getValue();
                    String elementName = tempInputPart.getElementName()
                            .getLocalPart();
                    Bridge item = new Bridge();
                    item.setOperationName(operationName);
                    item.setMessageName(inputMessageName);
                    item.setElementName(elementName);
                    item.setParamType("input");
                    item.setId(bridgeCounter);
                    item.setOwnerId(serviceId);
                    item.setOperationId(operationCounter);
                    bridgeList.add(item);

                    bridgeCounter++;

                }

                tempOut = operations.get(i).getOutput();

                String outputMessageName = tempOut.getMessage().getQName().getLocalPart();

                tempOutputMsg = def_message.getMessage(tempOut.getMessage().getQName());

                tempOutputParts = tempOutputMsg.getParts();

                for (Map.Entry<String, Part> entryPart : tempOutputParts.entrySet()) {

                    tempOutputPart = entryPart.getValue();
                    String elementName = tempOutputPart.getElementName()
                            .getLocalPart();
                    Bridge item = new Bridge();
                    item.setOperationName(operationName);
                    item.setMessageName(outputMessageName);
                    item.setElementName(elementName);
                    item.setParamType("output");
                    item.setId(bridgeCounter);
                    item.setOwnerId(serviceId);
                    item.setOperationId(operationCounter);
                    bridgeList.add(item);

                    bridgeCounter++;
                }
                operationCounter++;
            }

        }
    }

    private void fillOperationAndParameters() throws WSDLException, NoSuchFieldException,
            SecurityException, IOException {
        List<Node> elementList = new ArrayList<>();
        List<Node> typeList = new ArrayList<>();
//        List<Parameter> pList = new ArrayList<>();

        // get from java_base_type table by JavaBaseTypeService
        Definition def = getDefinitionInstance("type");

        Types myTypes = def.getTypes();
        Schema schema = null;
        for (Object e : myTypes.getExtensibilityElements()) {
            if (e instanceof Schema) {
                schema = (Schema) e;
                break;
            }
        }

        if (schema != null) {
            Element schemaElement = schema.getElement();
            NodeList list = schemaElement.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node tempNode = list.item(i);
                String node_type = tempNode.getLocalName();
                if (null != node_type) {
                    if (node_type.equals("element")) {
                        elementList.add(tempNode);
                    } else if (node_type.equals("complexType")) {
                        typeList.add(tempNode);
                    }
                }
            }
            for (Node element : elementList) {
                makeClassPropertyObject_New(element, typeList);
            }
        }
    }

    private void makeClassPropertyObject_New(Node elementNode, List<Node> typeList) {
        LinkedList<Integer> workList = new LinkedList<Integer>();
        LinkedList<Integer> ownerIdList = new LinkedList<Integer>();
        LinkedList<Integer> ownerIdStore = new LinkedList<Integer>();

        ownerIdList.push(-1);

        String typeName = getTypeName_New(elementNode);

        if (null != typeName)//the old style
        {
            int target = getTargetTypeNodeIndexByName(typeName, typeList);
            workList.push(target);

            while (workList.size() > 0) {
                Node targetNode = typeList.get(workList.pop());

                complexTypeWorker(elementNode,targetNode,
                        typeList, workList,
                        ownerIdList, ownerIdStore);
            }
        } else//the new style
        {
            //the first complex node is from element node.
            //process this node alone, and others are be processed as before.
            Node complex = getComplexTypeNode(elementNode);

            complexTypeWorker(elementNode,complex,
                    typeList, workList,
                    ownerIdList, ownerIdStore);

            while (workList.size() > 0) {

                Node targetNode = typeList.get(workList.pop());

                complexTypeWorker(elementNode,targetNode,
                        typeList, workList,
                        ownerIdList, ownerIdStore);
            }
        }
    }

    private String getTypeName_New(Node node) {
        String typeName = null;
        Node typeNode = node.getAttributes().getNamedItem("type");
        if (null != typeNode) {
            typeName = typeNode.getTextContent().split(":")[1];//.getTextContent().split(":")[1]//.getLocalName()
        }
        return typeName;
    }

    /**
     * get complexType node from input node.
     * always, one node has a only one complexType node as direct child.
     *
     * @param node
     * @return
     */
    private Node getComplexTypeNode(Node node) {
        NodeList children = node.getChildNodes();
        if (children.getLength() > 0) {
            for (int i = 0, j = children.getLength(); i < j; i++) {
                Node temp = children.item(i);
                if (null != temp.getLocalName()) {
                    return temp;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private void complexTypeWorker(Node elementNode,Node node,
                                   List<Node> typeList, LinkedList<Integer> workList,
                                   LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore) {
        NodeList children = node.getChildNodes();
        Node target = null;
        boolean sequenceFlag = false;

        for (int i = 0, j = children.getLength(); i < j; i++) {
            target = children.item(i);
            String name = target.getLocalName();
            if (null != name) {
                if (name.equals("sequence")) {
                    sequenceFlag = true;
                    break;
                }
                if (name.equals("complexContent")) {
                    break;
                }
            }
        }

        if (sequenceFlag) {
            sequenceWorker(elementNode,target, typeList, workList, ownerIdList, ownerIdStore);
        } else {
            complexContentWorker(elementNode,target, typeList, workList, ownerIdList, ownerIdStore);
        }
    }

    private void sequenceWorker(Node elementNode,Node node,
                                List<Node> typeList, LinkedList<Integer> workList,
                                LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore) {
        NodeList children = node.getChildNodes();

        int currentOwnerId = ownerIdList.pop().intValue();

        for (int i = 0; i < children.getLength(); i++) {
            if (null != children.item(i).getLocalName()) {
                ownerIdStore.push(currentOwnerId);
            }
        }
        for (int i = 0; i < children.getLength(); i++) {
            if (null != children.item(i).getLocalName()) {
                makeClassPropertyWorker(elementNode,children.item(i), typeList,
                        workList, ownerIdList, ownerIdStore);
            }
        }
    }

    private void complexContentWorker(Node elementNode,Node node,
                                      List<Node> typeList, LinkedList<Integer> workList,
                                      LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore) {
        //get extension element
        Node extension = null;
        NodeList extensionChildren = node.getChildNodes();
        for (int i = 0, j = extensionChildren.getLength(); i < j; i++) {
            extension = extensionChildren.item(i);
            String name = extension.getLocalName();
            if (null != name) {
                if (name.equals("extension")) {
                    break;
                }
            }
        }

        String baseContent = extension.getAttributes().getNamedItem("base").getTextContent();
        String typeName = baseContent.split(":")[1];

        int ownerId = ownerIdList.pop().intValue();
        ownerIdList.push(ownerId);

        NodeList children = extension.getChildNodes();//extension//node
        for (int i = 0, j = children.getLength(); i < j; i++) {
            Node item = children.item(i);
            String name = item.getLocalName();
            if (null != name) {
                if (name.equals("sequence")) {
                    sequenceWorker(elementNode,item, typeList, workList, ownerIdList,
                            ownerIdStore);
                    break;
                }
            }
        }

        int id = parametersList.size();
        int is_required = 1;
        int is_array = 0;
        int owner_id = ownerId;

        String param_name = "父类";
        String class_name = typeName;
        String type_name = "complexType";
        int type_id = baseTypeMap.get(type_name);

        int index = getTargetTypeNodeIndexByName(class_name, typeList);
        workList.push(index);
        ownerIdList.push(id);

        Parameter param = new Parameter();
        param.setId(id);
        param.setIsArray((byte) is_array);
        param.setIsRequired((byte) is_required);
        param.setParamName(param_name);
        param.setTypeName(type_name);
        param.setClassName(class_name);
        param.setTypeId(type_id);
        param.setOwnerId(owner_id);
        param.setProcessed(false);

        parametersList.add(param);
        //get sequence element and handle as makeClassPropertyWorker

        if (owner_id == -1)
        {
            makeOperationParameter(elementNode, id);
        }
    }

    private void makeClassPropertyWorker(Node elementNode, Node node,
                                         List<Node> typeList, LinkedList<Integer> workList,
                                         LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore) {
        int id = parametersList.size();
        int is_required = 1;
        int is_array;
        int owner_id = ownerIdStore.pop();
        String class_name = null;

        if (node.getAttributes().getNamedItem("maxOccurs") == null) {
            is_array = 0;
        } else {
            is_array = 1;
        }

        String param_name = node.getAttributes().getNamedItem("name")
                .getTextContent();

        String type_name = node.getAttributes().getNamedItem("type")
                .getTextContent().split(":")[1];

        Integer type_id_integer = baseTypeMap.get(type_name);

        int type_id;
        if (type_id_integer == null) {
            if (getTargetTypeNodeIndexByName(type_name, typeList) != -1) {
                type_id = baseTypeMap.get("complexType");
                class_name = type_name;
                type_name = "complexType";
                String target = getTypeNameInElementNode(node);
                int index = getTargetTypeNodeIndexByName(target, typeList);
                workList.push(index);
                ownerIdList.push(id);
            } else {
                type_id =baseTypeMap.entrySet().size() + 1;
                class_name = type_name;
                baseTypeMap.put(type_name,type_id);
            }
        } else {
            class_name = type_name;
            type_id = type_id_integer.intValue();
        }

        Parameter param = new Parameter();

        param.setId(id);
        param.setIsArray((byte) is_array);
        param.setIsRequired((byte) is_required);
        param.setParamName(param_name);
        param.setTypeName(type_name);
        param.setClassName(class_name);
        param.setTypeId(type_id);
        param.setOwnerId(owner_id);
        param.setProcessed(false);

        parametersList.add(param);

        if (owner_id == -1)
        {
            makeOperationParameter(elementNode, id);
        }
    }

    private void makeOperationParameter(Node elementNode, int parameterID)
    {
        String currentElementName = elementNode.getAttributes()
                .getNamedItem("name").getTextContent();

        for(Bridge item: bridgeList)
        {
            if(item.getElementName().equals(currentElementName))
            {
                OperationParameter op = new OperationParameter();
                op.setOperationId(item.getOperationId());
                op.setParameterId(parameterID);
                op.setParamType(item.getParamType());
                opList.add(op);
            }
        }
    }

    private int getTargetTypeNodeIndexByName(String name, List<Node> typeList) {
        int i = 0;
        boolean exist = false;
        for (; i < typeList.size(); i++) {
            String currentTypeName = getTypeNameInTypeNode(typeList.get(i));
            if (name.equals(currentTypeName)) {
                exist = true;
                break;
            }
        }

        if (exist) {
            return i;
        } else {
            return -1;
        }
    }

    private String getTypeNameInElementNode(Node node) {
        String[] typeNameInfo = node.getAttributes().getNamedItem("type")
                .getTextContent().split(":");
        return typeNameInfo[1];
    }

    private String getTypeNameInTypeNode(Node node) {
        return node.getAttributes().getNamedItem("name").getTextContent();
    }

    public Map<String,Integer> getBaseTypeMap()
    {
        if(processed)
        {
            if (baseTypeMap.entrySet().size() > 0) {
                return baseTypeMap;
            }
            return null;
        }
        else
        {
            return null;
        }
    }

    public Service getService()
    {
        if(processed)
        {
            return service;
        }
        else
        {
            return null;
        }
    }

    public List<OperationObject> getOperationList()
    {
        if(processed)
        {
            return operationList;
        }
        else
        {
            return null;
        }
    }

    public List<OperationParameter> getOperationParameterList()
    {
        if(processed)
        {
            return opList;
        }
        else
        {
            return null;
        }
    }

    public List<Parameter> getParametersList()
    {
        if(processed)
        {
            return parametersList;
        }
        else
        {
            return null;
        }
    }
}
