package com.winning.pbc.utils;

import com.winning.pbc.model.PBCArtifact;
import com.winning.pbc.model.TempPomGenerateContext;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils {


    public static File generateTempPomForPom(List<PBCArtifact> artifactList){
        try {
            File file = File.createTempFile("pom",".xml");
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
            cfg.setClassLoaderForTemplateLoading(Utils.class.getClassLoader(),"/");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            TempPomGenerateContext context = new TempPomGenerateContext();
            context.getDependencyList().addAll(artifactList);
            Template tmpl = cfg.getTemplate("tempPom.ftl");
            tmpl.setOutputEncoding("utf-8");
            try(Writer out = new FileWriter(file)){
                tmpl.process(context,out);
            }
            return file;
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("pom生成失败",e.getCause());
        }
    }

    public static File generateFile(String templateName,String writePath,Object context){
        try {
            File file = new File(writePath);
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
            cfg.setClassLoaderForTemplateLoading(Utils.class.getClassLoader(),"/");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            Template tmpl = cfg.getTemplate(templateName);
            tmpl.setOutputEncoding("utf-8");
            try(Writer out = new FileWriter(file)){
                tmpl.process(context,out);
            }
            return file;
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("文件生成失败",e.getCause());
        }
    }
    public static File generateFile(String templateName,File writeFile,Object context){
        try {
            if(!writeFile.getParentFile().exists()){
                writeFile.getParentFile().mkdirs();
            }
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
            cfg.setClassLoaderForTemplateLoading(Utils.class.getClassLoader(),"/");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            Template tmpl = cfg.getTemplate(templateName);
            tmpl.setOutputEncoding("utf-8");
            try(Writer out = new FileWriter(writeFile)){
                tmpl.process(context,out);
            }
            return writeFile;
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("文件生成失败",e.getCause());
        }
    }

    public static File generateTempPom(List<PBCArtifact> artifactList, Model model){
        List<Dependency> dependencyList = new ArrayList<>();
        for (PBCArtifact pbcArtifact : artifactList) {
            Dependency dependency = new Dependency();
            dependency.setGroupId(pbcArtifact.getGroupId());
            dependency.setArtifactId(pbcArtifact.getArtifactId());
            dependency.setVersion(pbcArtifact.getVersion());
            dependency.setType(pbcArtifact.getType());
            dependencyList.add(dependency);
        }
        model.setDependencies(dependencyList);
        File tempFile;
        try {
            tempFile = File.createTempFile("tempPom",".xml");
        } catch (IOException e) {
            return null;
        }
        try (OutputStream out = new FileOutputStream(tempFile)) {
            MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
            xpp3Writer.write(out, model);
        } catch (Throwable  e) {
            return null;
        }
        return tempFile;
    }

    public static Model readPomToModel(File pomFile){
        try(InputStream pomIn = new FileInputStream(pomFile)){
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            Model model = pomReader.read(pomIn);
            return model;
        } catch (Throwable e) {
            throw new RuntimeException("pomFile:"+pomFile.getAbsoluteFile()+" 读取失败",e);
        }
    }

    public static int getModuleSortScore(MavenProject mavenProject){
        switch(mavenProject.getArtifactId()){
            case "ccp.server":
                return -3;
            case "winex.appfw":
                return -2;
            case "winning-dtc-Coordinator":
                return -1;
            default:
                return 0;
        }
    }

    public static File getOrMkdirs(String path){
        File file= new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    public static File getOrMkdirs(File file){
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    public static boolean isSingleModuleProject(MavenSession session){
        int level =0;
        File rootDir = session.getTopLevelProject().getBasedir();
        for(MavenProject project : session.getAllProjects()){
            int levelcount = 0;
            File baseDirPoint = project.getBasedir();
            while(!baseDirPoint.equals(rootDir)){
                baseDirPoint = baseDirPoint.getParentFile();
                levelcount++;
            }
            level = Math.max(level ,levelcount);
        }
        return level<2;
    }

    public static Document readXmlDocument(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        if(file.exists())
            return reader.read(file);
        return null;
    }

    public static Element findIdeaComponent(Element module, String name) {
        return findElement(module, "component", name);
    }

    public static Element findElement(Element element, String elementName, String attributeName) {
        for (Iterator<Element> children = element.elementIterator(elementName); children.hasNext(); ) {
            Element child = children.next();
            if (attributeName.equals(child.attributeValue("name")))
                return child;
        }
        return null;
    }

    public static Element findElement(Element component, String name) {
        Element element = component.element(name);
        if (element == null)
            element = createElement(component, name);
        return element;
    }

    public static Element createElement(Element module, String name) {
        return module.addElement(name);
    }

    public static void writeXmlDocument(File file, Document document) throws IOException {
        XMLWriter writer = new IdeaXmlWriter(file);
        writer.write(document);
        writer.close();
    }
}
