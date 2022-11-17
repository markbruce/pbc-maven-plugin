package com.winning.pbc.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;

import java.util.Objects;

public class PBCArtifact {
        private String groupId;

        private String artifactId;

        private String version;

        private String type;

        public PBCArtifact(String groupId, String artifactId, String version) {
            this(groupId,artifactId,version,"jar");
        }

        public PBCArtifact(String groupId, String artifactId, String version, String type) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.type = type;
        }

        public PBCArtifact(Artifact artifact){
            this.groupId = artifact.getGroupId();
            this.artifactId = artifact.getArtifactId();
            this.version = artifact.getBaseVersion();
            this.type = artifact.getType();
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getType() {
            return StringUtils.isBlank(type)?"jar":type;
        }

        public void setType(String type) {
            this.type  = (StringUtils.isBlank(type)?"jar":type);
        }

        public static PBCArtifact toPbcArtifact(Artifact artifact){
            return new PBCArtifact(artifact.getGroupId(),artifact.getArtifactId(),artifact.getBaseVersion(),artifact.getType());
        }

        @Override
        public boolean equals(Object other){
            if(other == null || !(other instanceof PBCArtifact))
                return false;
            PBCArtifact other1 = (PBCArtifact) other;
            return Objects.equals(this.groupId,other1.getGroupId())
                    && Objects.equals(this.artifactId,other1.getArtifactId())
                    && Objects.equals(this.version,other1.getVersion())
                    && Objects.equals(this.type,other1.getType());
        }

        @Override
        public int hashCode() {
            return (this.groupId+":"+this.artifactId+":"+this.version+":"+this.type).hashCode();
        }
    }
