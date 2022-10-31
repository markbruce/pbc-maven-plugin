package com.winning.pbc.utils;

import com.winning.mde.module.MSModuleType;
import com.winning.pbc.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.*;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class WorkhomeInitialzier {
    public static void initWorkHome(MavenSession session, ProjectBuilder projectBuilder, Logger logger){
        logger.info("开始构建启动环境");
        boolean isSingleModuleProject = Utils.isSingleModuleProject(session);
        logger.info("判定框架版本");
        String version = null;
        Artifact launchArtifact = null;
        boolean haveCcpProject = false;
        boolean requireCcp = false;
        boolean haveAksoProject = false;
        boolean requireAkso = false;
        outer:  for (MavenProject allProject : session.getAllProjects()) {
            if(allProject.getGroupId().equals("com.winning")&& allProject.getArtifactId().equals("ccp.server")){
                haveCcpProject = true;
                if(version == null){
                    version = allProject.getVersion();
                }
            }
            if(allProject.getGroupId().equals("com.winning")&& allProject.getArtifactId().equals("winex.appfw")){
                haveAksoProject = true;
                if(version == null){
                    version = allProject.getVersion();
                }
            }
            if(haveCcpProject && haveAksoProject && launchArtifact!=null){
                break;
            }
            if(requireAkso && requireCcp && launchArtifact!=null){
                continue;
            }
            for (Artifact artifact : allProject.getArtifacts()) {
                if(artifact.getGroupId().equals("com.winning") ){
                    if(artifact.getArtifactId().startsWith("ccp.server")){
                        requireCcp=true;
                        if(version==null){
                            logger.info(String.format("获取到工程%s的框架依赖项%s版本为%s,以此作为生成依据",allProject.getName(),artifact.getGroupId()+":"+artifact.getArtifactId(),artifact.getBaseVersion()));
                            version = artifact.getBaseVersion();
                        }
                    }else if(artifact.getArtifactId().startsWith("appfw")){
                        requireAkso = true;
                        if(version == null){
                            logger.info(String.format("获取到工程%s的框架依赖项%s版本为%s,以此作为生成依据",allProject.getName(),artifact.getGroupId()+":"+artifact.getArtifactId(),artifact.getBaseVersion()));
                            version = artifact.getBaseVersion();
                        }
                    }else if(artifact.getArtifactId().equals("launch")){
                        launchArtifact = artifact;
                    }
                }
            }
        }
        logger.info("框架版本:"+version);
        logger.info("初始化目录结构");
        //初始化home目录
        File workspaceDir = new File("").getAbsoluteFile();
        File ideaDir = Utils.getOrMkdirs(new File(workspaceDir,".idea"));
        File homeDir = Utils.getOrMkdirs(new File(ideaDir,"home"));
        File homeConfigureDir = Utils.getOrMkdirs(new File(homeDir,"configure"));
        File homeLauncherDir = Utils.getOrMkdirs(new File(homeDir,"launcher"));
        File homeModulesDir = Utils.getOrMkdirs(new File(homeDir,"modules"));
        File homeMs_devDir = Utils.getOrMkdirs(new File(homeDir,"ms_dev"));
        File homeStartinfoDir = Utils.getOrMkdirs(new File(homeDir,"startinfo"));
        try{
            logger.info("清理modules目录");
            FileUtils.cleanDirectory(homeModulesDir);
            logger.info("清理ms_dev目录");
            FileUtils.cleanDirectory(homeMs_devDir);
            logger.info("清理startinfo目录");
            FileUtils.cleanDirectory(homeStartinfoDir);

        }catch(Throwable e){
            logger.warn("清理失败",e);
        }
        String pbcModulesStr = session.getTopLevelProject().getProperties().getProperty("pbc.modules","");
        //定义目录到元组的map，用来存储工程/无工程模块的 maven工程、依赖、代码路径等
        //moduleFolder -> (mavenproject,classpath set,target path set,子工程artifact);
        Map<File, PBCModule> moduleFolderToModuleMap =new HashMap<>();
        if(requireCcp&&!haveCcpProject){
            logger.info("依赖ccp.server模块但缺少工程,加入下载列表");
            pbcModulesStr = "com.winning:ccp.server:"+version+";"+pbcModulesStr;
        }
        if(requireAkso&&!haveAksoProject){
            logger.info("依赖winex.appfw模块但缺少工程,加入下载列表");
            pbcModulesStr = "com.winning:winex.appfw:"+version+";"+pbcModulesStr;
        }
        //处理无代码模块
        if(StringUtils.isNotBlank(pbcModulesStr)){
            logger.info("开始初始化需单独下载的模块:");
            List<PBCArtifact> artifactList = new ArrayList<>();
            String[] pbcModuleStrArr = pbcModulesStr.split(";");
            for (String s : pbcModuleStrArr) {
                if(StringUtils.isBlank(s))
                    continue;
                logger.info("--------> "+s);
                String[] artifactStrArr = s.split(":");
                artifactList.add(new PBCArtifact(artifactStrArr[0],artifactStrArr[1],artifactStrArr[2],"pom"));
            }
            File pomFile = Utils.generateTempPomForPom(artifactList);
            ProjectBuildingRequest request = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
            request.setResolveDependencies(true);
            try {
                ProjectBuildingResult result = projectBuilder.build(pomFile,request);
                for (Dependency dependency : result.getProject().getDependencies()) {
                    logger.info("开始处理---->"+dependency.getGroupId()+":"+dependency.getArtifactId()+":"+dependency.getVersion());
                    File currentModuleFolder = Utils.getOrMkdirs(new File(homeModulesDir,dependency.getArtifactId()));
                    //构建模块获取pom
                    Artifact artifact = result
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
                        List<Artifact> buildClassPathList = new ArrayList<>();
                        submodulePomResult.getProject().getArtifacts().forEach(moduleArtifact ->{
                            buildClassPathList.add(moduleArtifact);
                        });
                        moduleFolderToModuleMap.put(currentModuleFolder,new PBCModule(modulePomBuildResult.getProject(),new HashSet<>(),null,null,moduleArtifactList));
                        moduleFolderToModuleMap.get(currentModuleFolder).getClasspathSet().addAll(buildClassPathList);
                        logger.info("写入无工程模块的module.xml文件");
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
                        Utils.generateFile("module.ftl",new File(homeModulesDir+"/"+moduleXmlGenerateContext.getModuleName()+"/META-INF/module.xml"),moduleXmlGenerateContext);
                        logger.info("写入"+moduleXmlGenerateContext.getModuleName()+"/META-INF/module.xml 完成");
                    }
                }
            } catch (Throwable e) {
                logger.error("处理pom文件出错",e);
                e.printStackTrace();
            }

        }
        //处理工程模块
        logger.info("开始处理工程依赖");
        File toplevelFolder = session.getTopLevelProject().getBasedir();

        //首先删选模块级别工程，初始化classpathMap，防止异常的子模块列表
        logger.info("筛选模块级别目录");
        if(isSingleModuleProject){
            MavenProject moduleProject = session.getTopLevelProject();
            moduleFolderToModuleMap.put(moduleProject.getBasedir(),new PBCModule(moduleProject,new HashSet<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<PBCArtifact>()));
        }else{
            for (int i = 0; i < session.getAllProjects().size(); i++) {
                MavenProject currProject = session.getAllProjects().get(i);
                File moduleFolder = currProject.getBasedir();
                if (moduleFolder.getParentFile().equals(toplevelFolder)) {

                    moduleFolderToModuleMap.put(moduleFolder,new PBCModule(currProject,new HashSet<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<PBCArtifact>()));
                    logger.info("---->获取到模块:"+currProject.getGroupId()+":"+currProject.getArtifactId()+":"+currProject.getVersion());
                    for (Artifact artifact : currProject.getArtifacts()) {
                        if(artifact.getFile().getName().endsWith(".jar")){
                            moduleFolderToModuleMap.get(moduleFolder).getClasspathSet().add(artifact);
                        }
                    }
                }
            }
        }
        //处理下级工程
        logger.info("处理模块子集");
        for (int i=0;i<session.getAllProjects().size();i++){
            MavenProject currProject = session.getAllProjects().get(i);
            File moduleFolder = currProject.getBasedir();
            File levelPoint = moduleFolder;
            while(levelPoint!=null){
                if(moduleFolderToModuleMap.containsKey(levelPoint)){
                    logger.info("---->获取到二级模块:"+currProject.getGroupId()+":"+currProject.getArtifactId()+":"+currProject.getVersion());
                    for (Artifact artifact : currProject.getArtifacts()) {
                        if(artifact.getFile().getName().endsWith(".jar")){
                            moduleFolderToModuleMap.get(levelPoint).getClasspathSet().add(artifact);
                        }
                    }
                    moduleFolderToModuleMap.get(levelPoint).getSubArtifactList().add(new PBCArtifact(currProject.getGroupId(),currProject.getArtifactId(),currProject.getVersion(),currProject.getPackaging()));
                    moduleFolderToModuleMap.get(levelPoint).getTargetPathList().add(moduleFolder.getAbsolutePath()+"/target/classes");
                    moduleFolderToModuleMap.get(levelPoint).getSubMavenProjectList().add(currProject);
                    break;
                }
                levelPoint = levelPoint.getParentFile();
            }
        }
        logger.info("处理完成,开始写入依赖文件,构建启动列表");
        /**
         * 遍历模块，删除本模块在其他模块中的依赖引用，目的是将自己模块的包留在自己模块的classpath中
         * 例如模块com.winning:ccp.server 子模块ccp.server.data所生成的jar包 cpp.server.data-a.b.c-SNAPSHOT.jar 会存在于其他模块
         * 的依赖列表中，执行下面逻辑以实现不重复引入，并且将ccp.server.data留在ccp.server中
         *
         * 如果本模块是工程模块，也需要从自身依赖中移除掉自己的jar包
         */
        logger.info("--->去除重复依赖");
        moduleFolderToModuleMap.keySet().stream().sorted((k1,k2) ->{
            int k1Score =Utils.getModuleSortScore(moduleFolderToModuleMap.get(k1).getMavenProject());
            int k2Score = Utils.getModuleSortScore(moduleFolderToModuleMap.get(k2).getMavenProject());
            return k1Score - k2Score;
        }).forEach(moduleFolder -> {
            PBCModule pbcModule = moduleFolderToModuleMap.get(moduleFolder);
            MavenProject currentMavenProject = pbcModule.getMavenProject();
            moduleFolderToModuleMap.keySet().forEach(otherModuleFolder ->{
                if(moduleFolder == otherModuleFolder && pbcModule.getSubMavenProjectList()==null)
                    return;
                PBCModule otherPbcModule = moduleFolderToModuleMap.get(otherModuleFolder);
                MavenProject otherMavenProject = otherPbcModule.getMavenProject();
                Iterator<Artifact> otherArtifactsIt = otherPbcModule.getClasspathSet().iterator();
                while(otherArtifactsIt.hasNext()){
                    Artifact otherArtifact = otherArtifactsIt.next();
                    if(pbcModule.getSubArtifactList().contains(PBCArtifact.toPbcArtifact(otherArtifact))){
                        otherArtifactsIt.remove();
                    }
                }
            });
        });
        Set<String> classPathDistinctSet = new HashSet<>();
        StartInfoGenerateContext startInfoContext = new StartInfoGenerateContext();
        startInfoContext.setTmtsHome(homeDir.getAbsolutePath());
        startInfoContext.setProductCode(workspaceDir.getName());
        TmtsModuleGenerateContext tmtsModuleContext = new TmtsModuleGenerateContext();
        //开始写入classpath + ms_dev + modules
        moduleFolderToModuleMap.keySet().stream().sorted((k1,k2) ->{
            int k1Score =Utils.getModuleSortScore(moduleFolderToModuleMap.get(k1).getMavenProject());
            int k2Score = Utils.getModuleSortScore(moduleFolderToModuleMap.get(k2).getMavenProject());
            return k1Score - k2Score;
        }).forEach((moduleFolder) ->{
            List<String > classPathToWriteList = new ArrayList<>();
            PBCModule pbcModule = moduleFolderToModuleMap.get(moduleFolder);
            for (Artifact s : pbcModule.getClasspathSet()) {
                if(!classPathDistinctSet.contains(s.getFile().getAbsolutePath())){
                    classPathDistinctSet.add(s.getFile().getAbsolutePath());
                    classPathToWriteList.add(s.getFile().getAbsolutePath());
                }
            }
            try {
                FileUtils.write(
                        new File(Utils.getOrMkdirs(new File(homeModulesDir,pbcModule.getMavenProject().getArtifactId()).getAbsolutePath()+"/public/lib"),"classpath.txt")
                        ,StringUtils.join(classPathToWriteList,File.pathSeparator + System.lineSeparator()), Charset.forName("UTF-8"));
                logger.info("---->写入依赖文件"+new File(homeModulesDir,pbcModule.getMavenProject().getArtifactId()).getAbsolutePath()+"/public/lib/classpath.txt");
                startInfoContext.getModuleNameList().add(pbcModule.getMavenProject().getArtifactId());
                logger.info("---->加入启动列表:"+pbcModule.getMavenProject().getArtifactId());
                if(pbcModule.getSubMavenProjectList()!=null && pbcModule.getSubMavenProjectList().size()>0){
                    logger.info("---->计算模块的ms_dev");
                    MSDevGenerateContext msDevGenerateContext = new MSDevGenerateContext();
                    ModuleXmlInModulesGenerateContext moduleXmlGenerateContext = new ModuleXmlInModulesGenerateContext();
                    String moduleName = pbcModule.getMavenProject().getArtifactId();
                    String order = "";
                    switch(moduleName){
                        case "ccp.server":
                            order = "1";
                            break;
                        case "winex.appfw":
                            order = "2";
                            break;
                        default:
                            order = "60";
                    }
                    //设置ms_dev生成的模块名称及顺序
                    msDevGenerateContext.setModuleName(pbcModule.getMavenProject().getArtifactId());
                    msDevGenerateContext.setOrder(order);
                    String webAppPath = pbcModule.getMavenProject().getProperties().getProperty("project.manifest.webAppPath");
                    String moduleType = pbcModule.getMavenProject().getProperties().getProperty("project.manifest.type");
                    TmtsModuleGenerateContext.WebModule webModule  = null;
                    //判断模块类型
                    if (MSModuleType.SpringBootModule.name().equalsIgnoreCase(moduleType)) {
                        msDevGenerateContext.setModuleType(MSModuleType.SpringBootModule.name());
                    } else if (MSModuleType.SpringBootWebModule.name().equalsIgnoreCase(moduleType)) {
                        msDevGenerateContext.setWebAppPath(webAppPath);
                        msDevGenerateContext.setModuleType(MSModuleType.SpringBootWebModule.name());
                        webModule = new TmtsModuleGenerateContext.WebModule();
                        webModule.setName(msDevGenerateContext.getModuleName());
                    } else if (webAppPath != null) {
                        msDevGenerateContext.setWebAppPath(webAppPath);
                        msDevGenerateContext.setModuleType(MSModuleType.WebModule.name());
                        webModule = new TmtsModuleGenerateContext.WebModule();
                        webModule.setName(msDevGenerateContext.getModuleName());
                    } else {
                        msDevGenerateContext.setModuleType(MSModuleType.ServiceModlue.name());
                    }
                    //填充ms_dev中的组件列表，同时针对web模块获取其路径
                    List<MSDevComp> devCompList = new ArrayList<>();
                    for (int i = 0; i < pbcModule.getSubMavenProjectList().size(); i++) {
                        MavenProject mavenProject = pbcModule.getSubMavenProjectList().get(i);
                        MSDevComp comp = new MSDevComp();
                        comp.setId(mavenProject.getArtifactId());
                        comp.setScope("public");
                        comp.setJarName(mavenProject.getArtifactId()+"-"+mavenProject.getVersion()+".jar");
                        comp.setJarPath(pbcModule.getTargetPathList().get(i));
                        devCompList.add(comp);
                        File compWebappPath = new File(comp.getJarPath(),"webapp");
                        if(webModule!=null&&StringUtils.isBlank(webModule.getSource())&& compWebappPath.exists()){
                            logger.info("---->找到web目录:"+compWebappPath.getAbsolutePath());
                            webModule.setSource(compWebappPath.getAbsolutePath());
                            webModule.setTarget(new File(homeModulesDir,pbcModule.getMavenProject().getArtifactId()).getAbsolutePath()+"/webapp");
                            Utils.getOrMkdirs(webModule.getTarget());
                            tmtsModuleContext.getWebModuleList().add(webModule);
                        }
                    }
                    msDevGenerateContext.setCompList(devCompList);
                    logger.info("---->生成模块"+moduleName+"的ms_dev数据");
                    Utils.generateFile("ms_dev.ftl",homeMs_devDir+"/"+moduleName+".xml",msDevGenerateContext);
                }
            } catch (IOException e) {
                logger.error("写入工程依赖文件异常",e);
            }
        });

        logger.info("开始写入启动列表数据(startinfo)");
        Utils.generateFile("startinfo.ftl",new File(homeStartinfoDir,"startinfo.xml").getAbsolutePath(),startInfoContext);
        logger.info("写入启动列表数据完成");
        if(tmtsModuleContext.getWebModuleList().size()>0){
            logger.info("开始写入web工程列表");
            Utils.generateFile("tmts_module.ftl",new File(homeStartinfoDir,"tmts_module.xml"),tmtsModuleContext);
            logger.info("web工程列表写入完成");
        }
        logger.info("拷贝启动包");
        if(FileUtils.deleteQuietly(homeLauncherDir)){
            logger.info("----> 清理launcher目录，开始拷贝");
            homeLauncherDir.mkdirs();
            try {
                FileUtils.copyFileToDirectory(launchArtifact.getFile(),homeLauncherDir);
            } catch (IOException e) {
                logger.error("拷贝launch异常",e);
                throw new RuntimeException(e);
            }
        }
        logger.info("拷贝结束");
        logger.info("环境构建结束");
    }
}
