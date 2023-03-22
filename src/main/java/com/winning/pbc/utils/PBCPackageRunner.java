package com.winning.pbc.utils;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.winning.mde.module.MSModuleType;
import com.winning.pbc.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PBCPackageRunner {

    public static void doPackage(MavenSession session, ProjectBuilder projectBuilder, Logger logger,boolean isSkipAkso) throws IOException {
        MavenProject topProject = session.getTopLevelProject();
        Map<MavenProject, List<MavenProject>> parentToChildrenProjectMapping = new ConcurrentHashMap<>();
        Map<MavenProject, File> projectToDirMapping = new ConcurrentHashMap<>();
        Map<File,MavenProject> dirToProjectMapping = new ConcurrentHashMap<>();
        boolean isSingleModuleProject = Utils.isSingleModuleProject(session);
        String frameworkVersion = null;
        Artifact launchArtifact = null;
        boolean haveCcp = false;
        boolean requireCcp = false;
        boolean haveAkso = false;
        boolean requireAkso = false;
        logger.info("创建 模块<---->目录 映射，检测框架版本");
        for (MavenProject mavenProject : session.getAllProjects()) {
            if (!isSingleModuleProject && mavenProject == topProject)
                continue;
            projectToDirMapping.putIfAbsent(mavenProject, mavenProject.getBasedir());
            dirToProjectMapping.putIfAbsent(mavenProject.getBasedir(), mavenProject);
            //顺便判定框架版本
            if (mavenProject.getGroupId().equals("com.winning") && mavenProject.getArtifactId().equals("ccp.server")) {
                haveCcp = true;
                if (frameworkVersion == null) {
                    frameworkVersion = mavenProject.getVersion();
                }
            }
            if (mavenProject.getGroupId().equals("com.winning") && mavenProject.getArtifactId().equals("winex.appfw")) {
                haveAkso = true;
                if (frameworkVersion == null) {
                    frameworkVersion = mavenProject.getVersion();
                }
            }
            for (Artifact artifact : mavenProject.getArtifacts()) {
                if(artifact.getGroupId().equals("com.winning") ){
                    if(artifact.getArtifactId().startsWith("ccp.server")){
                        requireCcp=true;
                        if(frameworkVersion==null){
                            logger.info(String.format("获取到工程%s的框架依赖项%s版本为%s,以此作为生成依据",mavenProject.getName(),artifact.getGroupId()+":"+artifact.getArtifactId(),artifact.getBaseVersion()));
                            frameworkVersion = artifact.getBaseVersion();
                        }
                    }else if(artifact.getArtifactId().startsWith("appfw")){
                        requireAkso = true;
                        if(frameworkVersion == null){
                            logger.info(String.format("获取到工程%s的框架依赖项%s版本为%s,以此作为生成依据",mavenProject.getName(),artifact.getGroupId()+":"+artifact.getArtifactId(),artifact.getBaseVersion()));
                            frameworkVersion = artifact.getBaseVersion();
                        }
                    }else if(artifact.getArtifactId().equals("launch")){
                        launchArtifact = artifact;
                    }
                }
            }
        }
        if(isSkipAkso){
            requireAkso = false;
        }
        logger.info("模块---->目录："+projectToDirMapping.size());
        logger.info("目录---->模块："+dirToProjectMapping.size());
        logger.info("框架版本:"+frameworkVersion);
        logger.info("依赖Middleware:"+requireCcp);
        logger.info("包含Middleware代码:"+haveCcp);
        logger.info("依赖Akso:"+requireAkso);
        logger.info("包含Akso代码:"+haveAkso);
        logger.info("获取顶层下的模块列表");
        projectToDirMapping.forEach((project ,file )->{
            if(!dirToProjectMapping.containsKey(file.getParentFile())){
                parentToChildrenProjectMapping.put(project,new ArrayList<>());
                logger.info("---->获取到一级模块:"+file.getAbsolutePath());
            }
        });
        logger.info("获取模块的递归子模块列表");
        projectToDirMapping.forEach((project ,file )->{
            if((!isSingleModuleProject && project == topProject) || parentToChildrenProjectMapping.containsKey(project))
                return;
            MavenProject projectP = project;
            File dirP = project.getBasedir();
            while(projectP!=null){
                MavenProject parentDirProject = dirToProjectMapping.get(dirP.getParentFile());
                if(parentDirProject==null){
                    logger.warn("奇怪这里是null，工程："+file.getAbsolutePath());
                    return;
                }
                if(parentToChildrenProjectMapping.containsKey(parentDirProject)){
                    parentToChildrenProjectMapping.get(parentDirProject).add(project);
                    logger.info("---->获取到模块"+parentDirProject.getArtifactId()+"的子模块"+project.getArtifactId());
                    break;
                }
                projectP = parentDirProject;
                dirP = parentDirProject.getBasedir();
            }
        });
        PBCPackagedModule ccpLoadResult = null;
        PBCPackagedModule aksoLoadResult = null;
        //优先处理框架
        if(!haveCcp && requireCcp){
            logger.info("处理Middleware依赖");
            ccpLoadResult = MavenUtils.resolveProjectAndDependencies("com.winning","ccp.server",frameworkVersion,session,projectBuilder,logger);

        }
        if(!haveAkso&& requireAkso){
            logger.info("处理akso依赖");
            aksoLoadResult = MavenUtils.resolveProjectAndDependencies("com.winning","winex.appfw",frameworkVersion,session,projectBuilder,logger);
        }
        File outputDir = Utils.getOrMkdirs(topProject.getBasedir()+"/.idea/target/modules");
        FileUtils.cleanDirectory(outputDir);
        Set<PBCArtifact> copyedSet = new ConcurrentHashSet<>();

        //优先处理没有代码的框架
        if(ccpLoadResult!=null ){
            File libDir = Utils.getOrMkdirs(outputDir.getAbsolutePath()+"/ccp.server/public/lib/");
            logger.info("处理ccp的包");
            for (Artifact artifact : ccpLoadResult.getChildModuleList()) {
                PBCArtifact toCopyArtifact = new PBCArtifact(artifact);
                if (copyedSet.add(toCopyArtifact)) {
                    FileUtils.copyFileToDirectory(artifact.getFile(), libDir);
                }
            }
            for (Artifact artifact : ccpLoadResult.getDependencyList()) {
                PBCArtifact toCopyArtifact = new PBCArtifact(artifact);
                if (copyedSet.add(toCopyArtifact)) {
                    FileUtils.copyFileToDirectory(artifact.getFile(), libDir);
                }
            }
            Utils.generateFile("module.ftl",new File(outputDir.getAbsolutePath()+"/ccp.server/META-INF/module.xml"),ccpLoadResult.getModuleXmlInModulesGenerateContext());
        }
        if(aksoLoadResult!=null ){
            File libDir = Utils.getOrMkdirs(outputDir.getAbsolutePath()+"/winex.appfw/public/lib/");
            logger.info("处理akso的包");
            for (Artifact artifact : aksoLoadResult.getChildModuleList()) {
                PBCArtifact toCopyArtifact = new PBCArtifact(artifact);
                if (copyedSet.add(toCopyArtifact)) {
                    FileUtils.copyFileToDirectory(artifact.getFile(), libDir);
                }
            }
            for (Artifact artifact : aksoLoadResult.getDependencyList()) {
                PBCArtifact toCopyArtifact = new PBCArtifact(artifact);
                if (copyedSet.add(toCopyArtifact)) {
                    FileUtils.copyFileToDirectory(artifact.getFile(), libDir);
                }
            }
            Utils.generateFile("module.ftl",new File(outputDir.getAbsolutePath()+"/winex.appfw/META-INF/module.xml"),aksoLoadResult.getModuleXmlInModulesGenerateContext());
        }
        //剩下的排序依次处理
        List<PBCPackagedModule> moduleList = new ArrayList<>();
        for (Map.Entry<MavenProject, List<MavenProject>> entry : parentToChildrenProjectMapping.entrySet()) {
            MavenProject parentProject = entry.getKey();
            List<MavenProject> childProjectList = entry.getValue();
            File libDir = Utils.getOrMkdirs(outputDir.getAbsolutePath() + "/" + parentProject.getArtifactId() + "/public/lib/");
            PBCPackagedModule module = new PBCPackagedModule(parentProject.getArtifactId());
            ModuleXmlInModulesGenerateContext moduleXmlGenerateContext = module.getModuleXmlInModulesGenerateContext();
            moduleXmlGenerateContext.setModuleName(parentProject.getName());
            for (MavenProject mavenProject : childProjectList) {
                //先查找各自的包拷贝到目标目录
                //直接按照artifactId-version.jar来取制品包
                if(Objects.equals("pom",mavenProject.getPackaging())) {
                    continue;
                }
                File targetJar = new File(mavenProject.getBasedir() + "/target/" + mavenProject.getArtifactId() + "-" + mavenProject.getVersion() + ".jar");
                if (targetJar.exists()) {
                    copyedSet.add(new PBCArtifact(mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion()));
                    FileUtils.copyFileToDirectory(targetJar, libDir);
                    moduleXmlGenerateContext.getCompList().add(new MSDevComp(mavenProject.getArtifactId(),"public",targetJar.getName(),null));
                }else{
                    logger.error("模块制品不存在:"+targetJar.getAbsolutePath());
                }
                module.getDependencyList().addAll(mavenProject.getArtifacts());
            }
            //设定order
            switch(parentProject.getArtifactId()){
                case "ccp.server":
                    moduleXmlGenerateContext.setOrder("1");
                    break;
                case "winex.appfw":
                    moduleXmlGenerateContext.setOrder("2");
                    break;
                case "winning-dtc-Coordinator":
                    moduleXmlGenerateContext.setOrder("3");
                    break;
                default:
                    moduleXmlGenerateContext.setOrder("60");
            }
            //设定模块类型
            String webAppPath = parentProject.getProperties().getProperty("project.manifest.webAppPath");
            String moduleType = parentProject.getProperties().getProperty("project.manifest.type");
            TmtsModuleGenerateContext.WebModule webModule  = null;
            //判断模块类型
            if (MSModuleType.SpringBootModule.name().equalsIgnoreCase(moduleType)) {
                moduleXmlGenerateContext.setModuleType(MSModuleType.SpringBootModule.name());
            } else if (MSModuleType.SpringBootWebModule.name().equalsIgnoreCase(moduleType)) {
                moduleXmlGenerateContext.setWebAppPath(webAppPath);
                moduleXmlGenerateContext.setModuleType(MSModuleType.SpringBootWebModule.name());
                //TODO 如果springbootweb也需要生成tmts_module的话，放开注释的部分
                webModule = new TmtsModuleGenerateContext.WebModule();
                webModule.setName(moduleXmlGenerateContext.getModuleName());
            } else if (webAppPath != null) {
                moduleXmlGenerateContext.setWebAppPath(webAppPath);
                moduleXmlGenerateContext.setModuleType(MSModuleType.WebModule.name());
                webModule = new TmtsModuleGenerateContext.WebModule();
                webModule.setName(moduleXmlGenerateContext.getModuleName());
            } else {
                moduleXmlGenerateContext.setModuleType(MSModuleType.ServiceModlue.name());
            }
            moduleList.add(module);
        }
        moduleList.sort(Comparator.comparingInt(m -> Integer.valueOf(m.getModuleXmlInModulesGenerateContext().getOrder())));
        for (PBCPackagedModule pbcPackagedModule : moduleList) {
            File libDir = Utils.getOrMkdirs(outputDir.getAbsolutePath() + "/" + pbcPackagedModule.getModuleName()+ "/public/lib/");
            //拷贝依赖部分
            boolean isAkso4Pbc = AksoPbcUtil.haveAkso4PbcFile(topProject.getBasedir().getAbsolutePath(), pbcPackagedModule.getModuleName());
            if(isAkso4Pbc){
                pbcPackagedModule.getAksoPbcModuleFileList().addAll(AksoPbcUtil.getCopyJars4Module(topProject.getBasedir().getAbsolutePath(),pbcPackagedModule.getModuleName()));
                for (File file : pbcPackagedModule.getAksoPbcModuleFileList()) {
                    FileUtils.copyFileToDirectory(file,libDir);
                }
            }else{
                for (Artifact artifact : pbcPackagedModule.getDependencyList()) {
                    if(copyedSet.add(new PBCArtifact(artifact))){
                        FileUtils.copyFileToDirectory(artifact.getFile(),libDir);
                    }
                }
            }
            //生成module.xml
            Utils.generateFile("module.ftl",
                    new File(outputDir.getAbsolutePath()+ "/" + pbcPackagedModule.getModuleName()+"/META-INF/module.xml"),pbcPackagedModule.getModuleXmlInModulesGenerateContext());
        }
        parentToChildrenProjectMapping.forEach((parentProject,children)->{
            logger.info(parentProject.getArtifactId());
            for(int i=0;i<children.size();i++){
                logger.info(((i+1 == children.size())?"└":"├")+"┈┈  "+children.get(i).getArtifactId());
            }
        });

    }
}
