package com.abyss.wsdl.analyzer.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lblsloveryy on 15-2-4.
 */
public class WsdlFileSupport {

    private static final String ROOT = "root";
    private static final String LEAF = "leaf";
    private static final String STEM = "stem";

    private String defFileType;

    private String defFileContent;

    private int ids;

    private int parentId;

    private final List<Integer> children = new ArrayList<>();

    //region est
    public List<Integer> getChildren() {


        return children;
    }

    public String getDefFileType() {
        return defFileType;
    }

    public void setDefFileType(String defFileType) {
        this.defFileType = defFileType;
    }

    public String getDefFileContent() {
        return defFileContent;
    }

    public void setDefFileContent(String defFileContent) {
        this.defFileContent = defFileContent;
    }

    public int getIds() {
        return ids;
    }

    public void setIds(int ids) {
        this.ids = ids;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
    //endregion
}
