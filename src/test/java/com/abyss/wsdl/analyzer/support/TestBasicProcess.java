package com.abyss.wsdl.analyzer.support;

import java.util.*;

import java.io.IOException;
import java.util.Map.Entry;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.wsdl.ImportImpl;

import org.junit.Ignore;
import org.junit.Test;


/**
 * Created by lblsloveryy on 15-2-2.
 */
public class TestBasicProcess {
    private static final Long serialVersionUID = 1L;
    private static final String PROTOCOL_IMPL_PREFIX = "com.ibm.wsdl.extensions";
    public static final String FAIL_EXIST = "failExist";
    public static final String FAIL_PROTOCOL = "failProtocol";
    public static final String NULL = "null";
    public int counter = 0;

    private static final String BINDING = "binding";
    private static final String SERVICE = "service";
    private static final String TYPE = "type";
    private static final String PORTTYPE = "portType";
    private static final String MESSAGE = "messsage";

    private final Map<String, Object> definitionMap = new HashMap<String, Object>();

    private final List<WsdlFileSupport> wsdlFiles = new ArrayList<>();

    @Ignore("Temporary")
    @Test
    public void testCopyFolder() {
        String url = "http://172.16.206.32:8080/mdb_service_web/webservice/"
                + "JzgXzzwModifyAPI/updateJzgXzzwByCondition?wsdl";
        String url2 = "http://172.16.206.32:8080/mdb_service_web/webservice/"
                + "JzgXzzwModifyAPI/updateJzgXzzwByCondition?wsdl=JzgXzzwModifyAPI.wsdl";
        try {
            System.out.print(BasicHttpTools.readContentFromGet(url2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Ignore("Temporary")
    @Test
    public void testGetNameSpace() throws WSDLException {
//        String url = "http://172.16.206.32:8081/mdb_service_web/webservice/jzgJbxxQueryAPI/getJzgJbxxList?wsdl";
//        String url2 = "http://msg.njau.edu.cn/message/services/ExtCommandService?wsdl";
//        String url3 = "http://172.16.206.32:8080/mdb_service_web/webservice/jzgZpGetAPI/getJzgZpByZgh?wsdl";
//
//        WSDLFactory factory = WSDLFactory.newInstance();
//        WSDLReader reader = factory.newWSDLReader();
//        reader.setFeature("javax.wsdl.verbose", true);
//        reader.setFeature("javax.wsdl.importDocuments", false);

//        Definition def = reader.readWSDL(url2);
        Definition def = getDefinitionInstanse("type");

        String nameSpace = def.getTargetNamespace();
        System.out.println("targetNamespace is " + nameSpace);
        /*
        Map<String, String> namespaceMap = def.getNamespaces();

		Set<Map.Entry<String, String>> set = namespaceMap.entrySet();
		for (Iterator<Map.Entry<String, String>> it = set.iterator(); it
				.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it
					.next();
			System.out.println(entry.getKey() + "--->" + entry.getValue());
		}

        System.out.println("---------------------------");

        Map<Object, Object> serviceMap = def.getServices();
        Set<Map.Entry<Object, Object>> set1 = serviceMap.entrySet();
        for (Iterator<Map.Entry<Object, Object>> it = set1.iterator(); it
                .hasNext();) {
            Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) it
                    .next();
//			System.out.println("the key:" + entry.getKey().getClass().getName());
            System.out.println("ServiceName : " + ((QName)entry.getKey()).getLocalPart());
            ServiceImpl serviceImpl = (ServiceImpl)entry.getValue();



            Map<Object,Object> map = serviceImpl.getPorts();
            Set<Map.Entry<Object, Object>> set2 = map.entrySet();
            for (Iterator<Map.Entry<Object, Object>> it2 = set2.iterator();
                 it2.hasNext();)
            {
                Map.Entry<Object, Object> entry2 =
                        (Map.Entry<Object, Object>) it2.next();
                System.out.println("the key:" + entry2.getKey());
//				PortImpl port = (PortImpl)entry2.getValue();

            }


        }
        */

    }

    private boolean setDefinitionMap(String url) throws WSDLException {

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", true);
        reader.setFeature("javax.wsdl.importDocuments", false);

        /*
        the Definition has several tags, include:
        types, message, portType, binding, service
         */
        LinkedList<String> urlList = new LinkedList<String>();
        LinkedList<Integer> idList = new LinkedList<Integer>();
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


            Map imports = def.getImports();

            if (imports.entrySet().size() > 0) {

                if (parentId == 0) {
                    wsdlFile.setDefFileType(WsdlFileSupport.ROOT);
                } else {
                    wsdlFile.setDefFileType(WsdlFileSupport.STEM);
                }

                for (Object item : imports.entrySet()) {
                    Vector v = (Vector) ((Entry) item).getValue();
                    for (int i = 0; i < v.size(); i++) {
                        ImportImpl ii = (ImportImpl) v.get(i);
                        String importUrl = ii.getLocationURI();
                        idStore.push(id);
                        urlList.push(importUrl);
                    }
                }

            } else {
                wsdlFile.setDefFileType(WsdlFileSupport.LEAF);
            }

            wsdlFiles.add(wsdlFile);

            fillDefinitionMap(def);
        }

        return checkDefinitionMap();

    }

    private void fillDefinitionMap(Definition def)
            throws WSDLException {


        Object temp = def.getBindings();
        if (null != temp) {
            definitionMap.put(BINDING, def);
        }

        temp = def.getMessages();
        if (null != temp) {
            definitionMap.put(MESSAGE, def);
        }

        temp = def.getServices();
        if (null != temp) {
            definitionMap.put(SERVICE, def);
        }

        temp = def.getAllPortTypes();
        if (null != temp) {
            definitionMap.put(PORTTYPE, def);
        }

        temp = def.getTypes();
        if (null != temp) {
            definitionMap.put(TYPE, def);
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


    private Definition getDefinitionInstanse(String type) throws WSDLException {

        String url = "http://172.16.206.32:8080/mdb_service_web/webservice/jzgZpGetAPI/getJzgZpByZgh?wsdl";

        String path = "/Users/lblsloveryy/GitProject/WSDLAnalyzer/src/test/resources/wsdl2";
        String first = path + "/getJzgZpByZgh-root.xml";// condition
        String second = path + "/getJzgZpByZgh.xml";
        String third = path + "/ExtCommandService.xml";
        String fourth = path + "/regWebService.xml";

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", true);
        reader.setFeature("javax.wsdl.importDocuments", false);

        setDefinitionMap(fourth);

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

    //    @Ignore("Temporary")
    @Test
    public void testGetTypes() throws WSDLException, NoSuchFieldException,
            SecurityException {
        List<Node> elementList = new ArrayList<Node>();
        List<Node> typeList = new ArrayList<Node>();
        List<ClassProperty> cpList = new ArrayList<ClassProperty>();

        // get from java_base_type table by JavaBaseTypeService
        HashMap<String, Integer> baseType = new HashMap<String, Integer>();
        baseType.put("xs:string", 1);
        baseType.put("xs:int", 2);
        baseType.put("xsd:string", 3);
        baseType.put("xsd:int", 4);
        baseType.put("xsd:long", 5);
        baseType.put("xsd:boolean", 6);

        Definition def = getDefinitionInstanse("type");

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
                // xs:element//xs:complexType
                // the class identity:
                // org.apache.xerces.dom.DeferredElementNSImpl
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

            System.out.println("the size of elementList is "
                    + elementList.size());
            // to every element node, do the traverse
            for (Node node : elementList) {
                makeClassPropertyObject_New(node, typeList, cpList, baseType);
            }

            System.out.println("the size of cpList is " + cpList.size());

            for (ClassProperty node : cpList) {
                System.out.println("the " + node.getId() + " is "
                        + node.getParamName() + ". its owner_id is "
                        + node.getOwnerId() + ". its typeName is "
                        + node.getTypeName() + ". its className is "
                        + node.getClassName() + ".");
            }
        }
    }

    private void makeClassPropertyObject(Node node, List<Node> typeList,
                                         List<ClassProperty> cpList, HashMap<String, Integer> baseType) {
        LinkedList<Integer> workList = new LinkedList<Integer>();
        LinkedList<Integer> ownerIdList = new LinkedList<Integer>();
        LinkedList<Integer> ownerIdStore = new LinkedList<Integer>();

        // get the target type node
        ownerIdList.push(-1);// class_property:owner_id

        String typeName = getTypeNameInElementNode(node);// class_property:class_name
        int target = getTargetTypeNodeIndexByName(typeName, typeList);
        workList.push(target);

        while (workList.size() > 0) {
            Node targetNode = typeList.get(workList.pop());
            NodeList sequenceContent = targetNode.getChildNodes();
            Node sequence = sequenceContent.item(1);
            NodeList children = sequence.getChildNodes();

            int currentOwnerId = ownerIdList.pop().intValue();

            for (int i = 0; i < children.getLength(); i++) {
                if (null != children.item(i).getLocalName()) {
                    ownerIdStore.push(currentOwnerId);
                }
            }
            for (int i = 0; i < children.getLength(); i++) {
                if (null != children.item(i).getLocalName()) {
                    makeClassPropertyWorker(children.item(i), cpList, typeList,
                            workList, ownerIdList, ownerIdStore, baseType);
                }
            }
        }


    }

    private void makeClassPropertyObject_New(Node node, List<Node> typeList,
                                             List<ClassProperty> cpList, HashMap<String, Integer> baseType) {
        LinkedList<Integer> workList = new LinkedList<Integer>();
        LinkedList<Integer> ownerIdList = new LinkedList<Integer>();
        LinkedList<Integer> ownerIdStore = new LinkedList<Integer>();

        ownerIdList.push(-1);

        String typeName = getTypeName_New(node);

        if (null != typeName)//the old style
        {
            int target = getTargetTypeNodeIndexByName(typeName, typeList);
            workList.push(target);

            while (workList.size() > 0) {
                Node targetNode = typeList.get(workList.pop());

                complexTypeWorker(targetNode, cpList,
                        typeList, workList,
                        ownerIdList, ownerIdStore,
                        baseType);
            }
        } else//the new style
        {
            //the first complex node is from element node.
            //process this node alone, and others are be processed as before.
            Node complex = getComplexTypeNode(node);

            complexTypeWorker(complex, cpList,
                    typeList, workList,
                    ownerIdList, ownerIdStore,
                    baseType);

            while (workList.size() > 0) {

                Node targetNode = typeList.get(workList.pop());

                complexTypeWorker(targetNode, cpList,
                        typeList, workList,
                        ownerIdList, ownerIdStore,
                        baseType);
            }


        }


    }

    private String getTypeName_New(Node node) {
        String typeName = null;
        Node typeNode = node.getAttributes().getNamedItem("type");
        if (null != typeNode) {
            typeName = typeNode.getTextContent().split(":")[1];
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

    private void complexTypeWorker(Node node, List<ClassProperty> cpList,
                                   List<Node> typeList, LinkedList<Integer> workList,
                                   LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore,
                                   HashMap<String, Integer> baseType) {
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
            sequenceWorker(target, cpList, typeList, workList, ownerIdList,
                    ownerIdStore, baseType);
        } else {
            complexContentWorker(target, cpList, typeList, workList, ownerIdList,
                    ownerIdStore, baseType);
        }
    }

    private void sequenceWorker(Node node, List<ClassProperty> cpList,
                                List<Node> typeList, LinkedList<Integer> workList,
                                LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore,
                                HashMap<String, Integer> baseType) {
        NodeList children = node.getChildNodes();

        int currentOwnerId = ownerIdList.pop().intValue();

        for (int i = 0; i < children.getLength(); i++) {
            if (null != children.item(i).getLocalName()) {
                ownerIdStore.push(currentOwnerId);
            }
        }
        for (int i = 0; i < children.getLength(); i++) {
            if (null != children.item(i).getLocalName()) {
                makeClassPropertyWorker(children.item(i), cpList, typeList,
                        workList, ownerIdList, ownerIdStore, baseType);
            }
        }
    }

    private void complexContentWorker(Node node, List<ClassProperty> cpList,
                                      List<Node> typeList, LinkedList<Integer> workList,
                                      LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore,
                                      HashMap<String, Integer> baseType) {
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
                    sequenceWorker(item, cpList, typeList, workList, ownerIdList,
                            ownerIdStore, baseType);
                    break;
                }
            }
        }

        int id = cpList.size();
        int is_required = 1;
        boolean is_del = false;
        int is_array = 0;
        int owner_id = ownerId;

        String param_name = "父类";
        String type_name = "xs:complexType";
        int type_id = 15;
        String comment = "";
        String class_name = typeName;

        int index = getTargetTypeNodeIndexByName(typeName, typeList);
        workList.push(index);
        ownerIdList.push(id);

        ClassProperty cp = new ClassProperty();
        cp.setId(id);
        cp.setIsArray((byte) is_array);
        cp.setIsRequired((byte) is_required);
        cp.setIsDel(is_del);
        cp.setComment(comment);
        cp.setParamName(param_name);
        cp.setTypeName(type_name);
        cp.setClassName(class_name);
        cp.setTypeId(type_id);
        cp.setOwnerId(owner_id);

        cpList.add(cp);
        //get sequence element and handle as makeClassPropertyWorker

    }


    private void makeClassPropertyWorker(Node node, List<ClassProperty> cpList,
                                         List<Node> typeList, LinkedList<Integer> workList,
                                         LinkedList<Integer> ownerIdList, LinkedList<Integer> ownerIdStore,
                                         HashMap<String, Integer> baseType) {
        int id = cpList.size();
        int is_required = 1;
        boolean is_del = false;
        int is_array;
        int owner_id = ownerIdStore.pop();

        if (node.getAttributes().getNamedItem("maxOccurs") == null) {
            is_array = 0;
        } else {
            is_array = 1;
        }

        String param_name = node.getAttributes().getNamedItem("name")
                .getTextContent();
        String type_name = node.getAttributes().getNamedItem("type")
                .getTextContent();

        Integer type_id_integer = baseType.get(type_name);
        String class_name = null;
        String comment = "";
        String[] splitedStr = type_name.split(":");
        int type_id;
        if (type_id_integer == null) {
            if (getTargetTypeNodeIndexByName(splitedStr[1], typeList) != -1) {
                type_id = 15;
                class_name = splitedStr[1];
                type_name = "xs:complexType";

                String target = getTypeNameInElementNode(node);
                int index = getTargetTypeNodeIndexByName(target, typeList);
                workList.push(index);
                ownerIdList.push(id);
            } else {
                type_id = 16;
                class_name = splitedStr[1];
                type_name = "xs:newType";
            }
        } else {
            class_name = type_name;
            type_id = type_id_integer.intValue();
        }

        ClassProperty cp = new ClassProperty();

        cp.setId(id);
        cp.setIsArray((byte) is_array);
        cp.setIsRequired((byte) is_required);
        cp.setIsDel(is_del);
        cp.setComment(comment);
        cp.setParamName(param_name);
        cp.setTypeName(type_name);
        cp.setClassName(class_name);
        cp.setTypeId(type_id);
        cp.setOwnerId(owner_id);

        cpList.add(cp);
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

    @Ignore("Temporary")
    @Test
    public void testGetOperationNameAndParamType() throws WSDLException {
        Definition def = getDefinitionInstanse("second");

        Map<String, PortType> portTypes = def.getPortTypes();
        if (portTypes.isEmpty()) {
            System.out.println("the portTypes is empty!!");
        }
        PortType tempPortType;
        List<Operation> operations;
        Element tempDocElem;

        Input tempIn;
        Message tempInputMsg;
        Part tempInputPart;
        Map<String, Part> tempInputParts;

        Output tempOut;
        Message tempOutputMsg;
        Part tempOutputPart;
        Map<String, Part> tempOutputParts;

        String serviceName = null;

        List<OperationMessageElementObject> myBridge = new ArrayList<OperationMessageElementObject>();

        for (Map.Entry<String, PortType> entryPortType : portTypes.entrySet()) {
            tempPortType = (PortType) entryPortType.getValue();
            serviceName = tempPortType.getQName().getLocalPart();

            System.out.println("the service name is " + serviceName);// test

            operations = tempPortType.getOperations();
            for (int i = 0; i < operations.size(); i++) {
                String operationName = operations.get(i).getName();

                // process input part
                tempIn = operations.get(i).getInput();
                String inputMessageName = tempIn.getMessage().getQName()
                        .getLocalPart();
                tempInputMsg = def.getMessage(tempIn.getMessage().getQName());
                tempInputParts = tempInputMsg.getParts();
                for (Map.Entry<String, Part> entryPart : tempInputParts
                        .entrySet()) {
                    tempInputPart = (Part) entryPart.getValue();
                    String elementName = tempInputPart.getElementName()
                            .getLocalPart();
                    OperationMessageElementObject item = new OperationMessageElementObject();
                    item.setOperationName(operationName);
                    item.setMessageName(inputMessageName);
                    item.setElementName(elementName);
                    item.setParamType("input");
                    myBridge.add(item);

                    System.out.println(operationName + " ; " + inputMessageName
                            + " ; " + elementName + " ; " + "input");
                }

                tempOut = operations.get(i).getOutput();
                String outputMessageName = tempOut.getMessage().getQName()
                        .getLocalPart();
                tempOutputMsg = def.getMessage(tempOut.getMessage().getQName());
                tempOutputParts = tempOutputMsg.getParts();
                for (Map.Entry<String, Part> entryPart : tempOutputParts
                        .entrySet()) {
                    tempOutputPart = (Part) entryPart.getValue();
                    String elementName = tempOutputPart.getElementName()
                            .getLocalPart();
                    OperationMessageElementObject item = new OperationMessageElementObject();
                    item.setOperationName(operationName);
                    item.setMessageName(outputMessageName);
                    item.setElementName(elementName);
                    item.setParamType("output");
                    myBridge.add(item);

                    System.out.println(operationName + " ; "
                            + outputMessageName + " ; " + elementName + " ; "
                            + "output");
                }
            }

        }
    }

    public class OperationMessageElementObject {
        private String operationName;
        private String messageName;
        private String paramType;
        private String elementName;
        private int id;

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

        public String getMessageName() {
            return messageName;
        }

        public void setMessageName(String messageName) {
            this.messageName = messageName;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public String getElementName() {
            return elementName;
        }

        public void setElementName(String elementName) {
            this.elementName = elementName;
        }
    }

//    @Test
//    public void testGetImportURL() throws WSDLException, NoSuchFieldException,
//            SecurityException {
//
//
//    }

    public class ClassProperty {
        private int id;

        private String paramName;

        private int ownerId;

        private String typeName;

        private String comment;

        private String className;

        private Byte isArray;

        private Date createTime;

        private Date updateTime;

        private Boolean isDel;

        private Integer userId;

        private Byte isRequired;

        private Integer typeId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName == null ? null : paramName.trim();
        }

        public int getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(int ownerId) {
            this.ownerId = ownerId;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName == null ? null : typeName.trim();
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment == null ? null : comment.trim();
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className == null ? null : className.trim();
        }

        public Byte getIsArray() {
            return isArray;
        }

        public void setIsArray(Byte isArray) {
            this.isArray = isArray;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }

        public Boolean getIsDel() {
            return isDel;
        }

        public void setIsDel(Boolean isDel) {
            this.isDel = isDel;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Byte getIsRequired() {
            return isRequired;
        }

        public void setIsRequired(Byte isRequired) {
            this.isRequired = isRequired;
        }

        public Integer getTypeId() {
            return typeId;
        }

        public void setTypeId(Integer typeId) {
            this.typeId = typeId;
        }
    }

    public class WsdlFileSupport {

        private static final String ROOT = "root";
        private static final String LEAF = "leaf";
        private static final String STEM = "stem";

        private String defFileType;

        private String fileContentTag;


        private Definition defFileContent;

        private int id;

        private int parentId;

        private final List<Integer> children = new ArrayList<>();

        public String getDefFileType() {
            return defFileType;
        }

        public void setDefFileType(String defFileType) {
            this.defFileType = defFileType;
        }

        public Definition getDefFileContent() {
            return defFileContent;
        }

        public void setDefFileContent(Definition defFileContent) {
            this.defFileContent = defFileContent;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getParentId() {
            return parentId;
        }

        public void setParentId(int parentId) {
            this.parentId = parentId;
        }

        public List<Integer> getChildren() {
            return children;
        }

        public String getFileContentTag() {
            return fileContentTag;
        }

        public void setFileContentTag(String fileContentTag) {
            this.fileContentTag = fileContentTag;
        }
    }
}
