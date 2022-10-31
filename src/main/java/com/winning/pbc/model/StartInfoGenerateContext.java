package com.winning.pbc.model;

import java.util.ArrayList;
import java.util.List;

public class StartInfoGenerateContext {

    private String tmtsHome;

    private String productCode;

    private List<String> moduleNameList = new ArrayList<>();

    public StartInfoGenerateContext() {
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getTmtsHome() {
        return tmtsHome;
    }

    public void setTmtsHome(String tmtsHome) {
        this.tmtsHome = tmtsHome;
    }

    public List<String> getModuleNameList() {
        return moduleNameList;
    }

    public void setModuleNameList(List<String> moduleNameList) {
        this.moduleNameList = moduleNameList;
    }
}
