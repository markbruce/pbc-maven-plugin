package com.winning.pbc.model;

import java.util.ArrayList;
import java.util.List;

public class TmtsModuleGenerateContext {

    private List<WebModule> webModuleList = new ArrayList<>();

    public List<WebModule> getWebModuleList() {
        return webModuleList;
    }

    public void setWebModuleList(List<WebModule> webModuleList) {
        this.webModuleList = webModuleList;
    }

    public static class WebModule{
        private String name;

        private String source;

        private String target;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }
}

