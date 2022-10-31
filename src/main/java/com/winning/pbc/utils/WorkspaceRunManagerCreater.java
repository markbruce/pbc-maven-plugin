package com.winning.pbc.utils;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;

public class WorkspaceRunManagerCreater {

    public static void generateRunManagerToWorkspace(MavenProject project) throws MojoFailureException, DocumentException, IOException {
        File workspaceFile = new File(project.getBasedir().getAbsolutePath()+"/.idea/workspace.xml");
        if(!workspaceFile.exists()){
            throw new MojoFailureException(".idea目录下没有找到workspace.xml 文件，无法添加运行配置，可尝试在idea中刷新工程，或执行 mvn idea:idea 命令生成相关文件");
        }
        try{
            Document workspaceDoc = Utils.readXmlDocument(workspaceFile);
            Element rootElement = workspaceDoc.getRootElement();
            Element runManagerElement = Utils.findIdeaComponent(rootElement,"RunManager");
            if(runManagerElement==null){
                runManagerElement = Utils.createElement(rootElement,"component");
                runManagerElement.addAttribute("name","RunManager");
            }
            Element listElem = Utils.findElement(runManagerElement,"list");
            boolean needWriteXml = false;
            //生成Application
            Element applicationConfigurationElem = Utils.findElement(runManagerElement,"configuration","PBCAppStarter");
            if(applicationConfigurationElem==null){
                needWriteXml = true;
                applicationConfigurationElem = Utils.createElement(runManagerElement,"configuration");
                applicationConfigurationElem.addAttribute("name","PBCAppStarter");
                applicationConfigurationElem.addAttribute("type","Application");
                applicationConfigurationElem.addAttribute("factoryName","Application");
                applicationConfigurationElem.addAttribute("nameIsGenerated","true");
                {
                    //MAIN_CLASS_NAME
                    Element optionElem = Utils.createElement(applicationConfigurationElem,"option");
                    optionElem.addAttribute("name","MAIN_CLASS_NAME");
                    optionElem.addAttribute("value","com.winning.mde.loader.TmtsBootApplication");
                }
                {
                    //PROGRAM_PARAMETERS
                    Element optionElem = Utils.createElement(applicationConfigurationElem,"option");
                    optionElem.addAttribute("name","PROGRAM_PARAMETERS");
                    optionElem.addAttribute("value","\"$PROJECT_DIR$/.idea/home\" \"$PROJECT_DIR\"");
                }
                {
                    //VM_PARAMETERS
                    Element optionElem = Utils.createElement(applicationConfigurationElem,"option");
                    optionElem.addAttribute("name","VM_PARAMETERS");
                    optionElem.addAttribute("value","-cp $PROJECT_DIR$/.idea/home/launcher/launch-0.0.2-SNAPSHOT.jar -Xms1g -Xmx5g -Dtmts.runMode=develop -Dorg.eclipse.jetty.server.Request.maxFormContentSize=10485760 -Ddatabase.show.sql=false -Djava.awt.headless=true\" ");
                }
                {
                    //method
                    Element optionElem = Utils.createElement(applicationConfigurationElem,"method");
                    optionElem.addAttribute("v","2");
                    Element methodOptionElem = Utils.createElement(optionElem,"option");
                    methodOptionElem.addAttribute("name","Make");
                    methodOptionElem.addAttribute("enabled","true");
                }
                Element itemElem = Utils.createElement(listElem,"item");
                itemElem.addAttribute("itemvalue","Application.PBCAppStarter");

            }
            //生成JarApplication
            Element jarConfigurationElem = Utils.findElement(runManagerElement,"configuration","PBCJarStarter");
            if(jarConfigurationElem==null){
                needWriteXml = true;
                jarConfigurationElem = Utils.createElement(runManagerElement,"configuration");
                jarConfigurationElem.addAttribute("name","PBCJarStarter");
                jarConfigurationElem.addAttribute("type","JarApplication");
                {
                    //JAR_PATH
                    Element optionElem = Utils.createElement(jarConfigurationElem,"option");
                    optionElem.addAttribute("name","JAR_PATH");
                    optionElem.addAttribute("value","$PROJECT_DIR$/.idea/home/launcher/launch-0.0.2-SNAPSHOT.jar");
                }
                {
                    //VM_PARAMETERS
                    Element optionElem = Utils.createElement(jarConfigurationElem,"option");
                    optionElem.addAttribute("name","VM_PARAMETERS");
                    optionElem.addAttribute("value","-cp $PROJECT_DIR$/.idea/home/launcher/launch-0.0.2-SNAPSHOT.jar -Xms1g -Xmx5g -Dtmts.runMode=develop -Dorg.eclipse.jetty.server.Request.maxFormContentSize=10485760 -Ddatabase.show.sql=false -Djava.awt.headless=true\" ");
                }
                {
                    //PROGRAM_PARAMETERS
                    Element optionElem = Utils.createElement(jarConfigurationElem,"option");
                    optionElem.addAttribute("name","PROGRAM_PARAMETERS");
                    optionElem.addAttribute("value","\"$PROJECT_DIR$/.idea/home\" \"$PROJECT_DIR$\"");
                }
//                {
//                    //WORKING_DIRECTORY
//                    Element optionElem = Utils.createElement(jarConfigurationElem,"option");
//                    optionElem.addAttribute("name","WORKING_DIRECTORY");
//                    optionElem.addAttribute("value","com.winning.mde.loader.TmtsBootApplication");
//                }
                {
                    //method
                    Element optionElem = Utils.createElement(jarConfigurationElem,"method");
                    optionElem.addAttribute("v","2");
                }
                Element itemElem = Utils.createElement(listElem,"item");
                itemElem.addAttribute("itemvalue","JAR Application.PBCJarStarter");
            }
            if(needWriteXml){
                Utils.writeXmlDocument(workspaceFile,workspaceDoc);
            }
        }catch(Throwable e){
            throw e;
        }
    }
}
