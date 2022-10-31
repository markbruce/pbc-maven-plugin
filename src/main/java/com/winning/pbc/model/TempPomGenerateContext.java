package com.winning.pbc.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TempPomGenerateContext {

    private List<PBCArtifact> dependencyList = new ArrayList<>();

    public String getCreatedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date());
    }

    public List<PBCArtifact> getDependencyList() {
        return dependencyList;
    }

    public void setDependencyList(List<PBCArtifact> dependencyList) {
        this.dependencyList = dependencyList;
    }

}
