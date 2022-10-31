package com.winning.pbc.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ModuleXmlInModulesGenerateContext {

    private String moduleName;

    private String moduleType;

    private String order;

    private String webAppPath;

    private List<MSDevComp> compList = new ArrayList<>();

    public ModuleXmlInModulesGenerateContext() {
    }

    public ModuleXmlInModulesGenerateContext(String moduleName, String moduleType, String order) {
        this.moduleName = moduleName;
        this.moduleType = moduleType;
        this.order = order;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public List<MSDevComp> getCompList() {
        return compList;
    }

    public void setCompList(List<MSDevComp> compList) {
        this.compList = compList;
    }

    public String getWebAppPath() {
        return StringUtils.isBlank(webAppPath)?"":webAppPath;
    }

    public void setWebAppPath(String webAppPath) {
        this.webAppPath = webAppPath;
    }
}
