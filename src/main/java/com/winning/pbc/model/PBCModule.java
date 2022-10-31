package com.winning.pbc.model;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.Set;

public class PBCModule {

    private MavenProject mavenProject;

    private Set<Artifact> classpathSet;

    private List<String> targetPathList;

    private List<MavenProject> subMavenProjectList;

    private List<PBCArtifact> subArtifactList;


    public PBCModule(MavenProject mavenProject, Set<Artifact> classpathSet, List<String> targetPathList, List<MavenProject> subMavenProjectList) {
        this.mavenProject = mavenProject;
        this.classpathSet = classpathSet;
        this.targetPathList = targetPathList;
        this.subMavenProjectList = subMavenProjectList;
    }

    public PBCModule(MavenProject mavenProject, Set<Artifact> classpathSet, List<String> targetPathList, List<MavenProject> subMavenProjectList, List<PBCArtifact> subArtifactList) {
        this.mavenProject = mavenProject;
        this.classpathSet = classpathSet;
        this.targetPathList = targetPathList;
        this.subMavenProjectList = subMavenProjectList;
        this.subArtifactList = subArtifactList;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public void setMavenProject(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    public Set<Artifact> getClasspathSet() {
        return classpathSet;
    }

    public void setClasspathSet(Set<Artifact> classpathSet) {
        this.classpathSet = classpathSet;
    }

    public List<String> getTargetPathList() {
        return targetPathList;
    }

    public void setTargetPathList(List<String> targetPathList) {
        this.targetPathList = targetPathList;
    }

    public List<PBCArtifact> getSubArtifactList() {
        return subArtifactList;
    }

    public void setSubArtifactList(List<PBCArtifact> subArtifactList) {
        this.subArtifactList = subArtifactList;
    }

    public List<MavenProject> getSubMavenProjectList() {
        return subMavenProjectList;
    }

    public void setSubMavenProjectList(List<MavenProject> subMavenProjectList) {
        this.subMavenProjectList = subMavenProjectList;
    }
}
