package com.abyss.wsdl.analyzer.support;

import javax.wsdl.Definition;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lblsloveryy on 15-2-4.
 */
public class WsdlFileSupport {

    public static final String ROOT = "root";
    public static final String LEAF = "leaf";
    public static final String STEM = "stem";

    private String defFileType;

    private String fileContentTag;

    private String fileName;

    private Definition defFileContent;

    private int id;

    private int parentId;

    private final List<String> children = new ArrayList<>();

    public String getDefFileType() {
        return defFileType;
    }

    public void setDefFileType(String defFileType) {
        this.defFileType = defFileType;
    }

    public String getFileContentTag() {
        return fileContentTag;
    }

    public void setFileContentTag(String fileContentTag) {
        this.fileContentTag = fileContentTag;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public List<String> getChildren() {
        return children;
    }
}
