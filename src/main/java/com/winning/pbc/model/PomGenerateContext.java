package com.winning.pbc.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class PomGenerateContext{
    private String projectName;

    private List<Module> moduleList;

    private String mavenHost =  "http://nexus.winning.com.cn:8081";

    private String createdAt;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<Module> getModuleList() {
        return moduleList;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
    }

    public String getMavenHost() {
        return mavenHost;
    }

    public void setMavenHost(String mavenHost) {
        this.mavenHost = mavenHost;
    }

    public String getCreatedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date());
    }

    public static class Module {
        private String moduleName;

        public Module(String moduleName){
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }
    }



}
