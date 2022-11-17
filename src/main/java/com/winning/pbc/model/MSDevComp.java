package com.winning.pbc.model;

public class MSDevComp {
        private String id;
        private String scope;
        private String jarName;
        private String jarPath;

        public MSDevComp(){

        }

    /**
     *
     * @param id
     * @param scope
     * @param jarName
     * @param jarPath
     */
        public MSDevComp(String id, String scope, String jarName, String jarPath) {
            this.id = id;
            this.scope = scope;
            this.jarName = jarName;
            this.jarPath = jarPath;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getJarName() {
            return jarName;
        }

        public void setJarName(String jarName) {
            this.jarName = jarName;
        }

        public String getJarPath() {
            return jarPath;
        }

        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }
    }
