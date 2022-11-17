package com.winning.pbc.model;

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PBCPackagedModule {
    String moduleName;
    String type;
    List<Artifact> childModuleList;
    List<Artifact> dependencyList;
    List<File> aksoPbcModuleFileList;
    ModuleXmlInModulesGenerateContext moduleXmlInModulesGenerateContext;

    public PBCPackagedModule(String moduleName) {
        this.moduleName = moduleName;
        this.childModuleList = new CopyOnWriteArrayList<>();
        this.dependencyList = new CopyOnWriteArrayList<>();
        this.aksoPbcModuleFileList = new CopyOnWriteArrayList<>();
        this.moduleXmlInModulesGenerateContext = new ModuleXmlInModulesGenerateContext();
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Artifact> getChildModuleList() {
        return childModuleList;
    }

    public void setChildModuleList(List<Artifact> childModuleList) {
        this.childModuleList = childModuleList;
    }

    public List<Artifact> getDependencyList() {
        return dependencyList;
    }

    public void setDependencyList(List<Artifact> dependencyList) {
        this.dependencyList = dependencyList;
    }

    public ModuleXmlInModulesGenerateContext getModuleXmlInModulesGenerateContext() {
        return moduleXmlInModulesGenerateContext;
    }

    public void setModuleXmlInModulesGenerateContext(ModuleXmlInModulesGenerateContext moduleXmlInModulesGenerateContext) {
        this.moduleXmlInModulesGenerateContext = moduleXmlInModulesGenerateContext;
    }

    public List<File> getAksoPbcModuleFileList() {
        return aksoPbcModuleFileList;
    }

    public void setAksoPbcModuleFileList(List<File> aksoPbcModuleFileList) {
        this.aksoPbcModuleFileList = aksoPbcModuleFileList;
    }
}
