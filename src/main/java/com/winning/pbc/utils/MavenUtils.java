package com.winning.pbc.utils;

import com.winning.mde.module.MSModuleType;
import com.winning.pbc.model.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenUtils {

    public static PBCPackagedModule resolveProjectAndDependencies(
            String groupId, String artifactId, String version,
            MavenSession session, ProjectBuilder projectBuilder, Logger logger){
        logger.info("开始初始化需单独下载的模块:");
        List<PBCArtifact> artifactList = new ArrayList<>();
        artifactList.add(new PBCArtifact(groupId,artifactId,version,"pom"));
        File pomFile = Utils.generateTempPomForPom(artifactList);
        ProjectBuildingRequest request = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        request.setResolveDependencies(true);
        try {
            ProjectBuildingResult buildingResult = projectBuilder.build(pomFile,request);
            for (Dependency dependency : buildingResult.getProject().getDependencies()) {
                logger.info("开始处理---->"+dependency.getGroupId()+":"+dependency.getArtifactId()+":"+dependency.getVersion());
//                    File currentModuleFolder = Utils.getOrMkdirs(new File(homeModulesDir,dependency.getArtifactId()));
                //构建模块获取pom
                Artifact artifact = buildingResult
                        .getProject()
                        .getArtifacts()
                        .stream()
                        .filter(art -> art.getGroupId().equals(dependency.getGroupId()) && art.getArtifactId().equals(dependency.getArtifactId()))
                        .findFirst().orElse(null);
                if(artifact!=null){
                    //获取pom的module列表,先以pom形式拉取子module的pom文件，然后根据packaging类型构建pom获取真实jar包
                    //step1 ，拉取子模块的pom文件
                    ProjectBuildingResult modulePomBuildResult =projectBuilder.build(artifact,request);
                    List<PBCArtifact> moduleArtifactList = new ArrayList<>();
                    for (String module : modulePomBuildResult.getProject().getModules()) {
                        moduleArtifactList.add(new PBCArtifact(dependency.getGroupId(),module,dependency.getVersion(),"pom"));
                    }
                    //将原pom中的module删除，装入dependencies中，以免版本管理失效
                    Model modulePomModel = Utils.readPomToModel(artifact.getFile());
                    modulePomModel.setModules(new ArrayList<>());
                    File pomFileForDownload = Utils.generateTempPom(moduleArtifactList,modulePomModel);
                    ProjectBuildingResult submodulePomResult = projectBuilder.build(pomFileForDownload,request);
                    //step2 ，从结果中获取并解析模块的pom文件，将packaging赋值给type，再次运行
                    for (PBCArtifact pbcArtifact : moduleArtifactList) {
                        File submodulePomFile = submodulePomResult
                                .getProject()
                                .getArtifacts()
                                .stream()
                                .filter(subArtifact -> subArtifact.getGroupId().equals(dependency.getGroupId())
                                        &&subArtifact.getArtifactId().equals(pbcArtifact.getArtifactId())
                                        && subArtifact.getType().equals("pom"))
                                .findFirst()
                                .get()
                                .getFile();

                        Model model = Utils.readPomToModel(submodulePomFile);
                        pbcArtifact.setType(model.getPackaging());

                    }
                    modulePomModel.getDependencies().clear();
                    pomFileForDownload = Utils.generateTempPom(moduleArtifactList,modulePomModel);
                    submodulePomResult = projectBuilder.build(pomFileForDownload,request);
//                    List<DualTuple<Artifact,Boolean>> buildClassPathList = new ArrayList<>();
//                    List<PBCLoadModuleResult> resultList = new ArrayList<>();
                    PBCPackagedModule result = new PBCPackagedModule(artifactId);
                    submodulePomResult.getProject().getArtifacts().forEach(moduleArtifact ->{
                        if(moduleArtifactList.contains(new PBCArtifact(moduleArtifact))){
                            result.getChildModuleList().add(moduleArtifact);
                        }else{
                            result.getDependencyList().add(moduleArtifact);
                        }
                    });
                    //同时构建无代码模块的module.xml的生成上下文
                    ModuleXmlInModulesGenerateContext moduleXmlGenerateContext = new ModuleXmlInModulesGenerateContext();
                    moduleXmlGenerateContext.setModuleName(dependency.getArtifactId());
                    Model currentModuleModel = Utils.readPomToModel(artifact.getFile());
                    //填充一部分module.xml文件的生成数据
                    String webAppPath = currentModuleModel.getProperties().getProperty("project.manifest.webAppPath");
                    String moduleType = currentModuleModel.getProperties().getProperty("project.manifest.type");
//                        moduleXmlGenerateContext.setWebAppPath(currentModuleModel.getProperties().getProperty("project.manifest.webAppPath",""));
                    switch(moduleXmlGenerateContext.getModuleName()){
                        case "ccp.server":
                            moduleXmlGenerateContext.setOrder("1");
                            break;
                        case "winex.appfw":
                            moduleXmlGenerateContext.setOrder("2");
                            break;
                        default:
                            moduleXmlGenerateContext.setOrder("60");
                    }
                    //判断模块类型
                    if (MSModuleType.SpringBootModule.name().equalsIgnoreCase(moduleType)) {
                        moduleXmlGenerateContext.setModuleType(MSModuleType.SpringBootModule.name());
                    } else if (MSModuleType.SpringBootWebModule.name().equalsIgnoreCase(moduleType)) {
                        moduleXmlGenerateContext.setWebAppPath(webAppPath);
                        moduleXmlGenerateContext.setModuleType(MSModuleType.SpringBootWebModule.name());
                    } else if (webAppPath != null) {
                        moduleXmlGenerateContext.setWebAppPath(webAppPath);
                        moduleXmlGenerateContext.setModuleType(MSModuleType.WebModule.name());
                    } else {
                        moduleXmlGenerateContext.setModuleType(MSModuleType.ServiceModlue.name());
                    }
                    for (PBCArtifact pbcArtifact : moduleArtifactList) {
                        MSDevComp moduleComp = new MSDevComp();
                        moduleComp.setId(pbcArtifact.getArtifactId());
                        moduleComp.setScope("public");
                        moduleComp.setJarName(pbcArtifact.getArtifactId()+"-"+pbcArtifact.getVersion()+".jar");
                        moduleXmlGenerateContext.getCompList().add(moduleComp);
                    }
                    result.setModuleXmlInModulesGenerateContext(moduleXmlGenerateContext);
                    return result;
                }
            }
        } catch (Throwable e) {
            logger.error("处理pom文件出错",e);
        }
        return null;
    }
}
