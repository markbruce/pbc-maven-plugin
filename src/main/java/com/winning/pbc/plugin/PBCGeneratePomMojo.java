package com.winning.pbc.plugin;

import com.winning.pbc.model.PomGenerateContext;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

@Mojo(name = "init-pom",requiresProject = false,threadSafe = true)
public class PBCGeneratePomMojo extends AbstractMojo {

    @Parameter(required = true,defaultValue = "true",property = "genPom")
    public boolean isGenPom;

    @Parameter(required = true,defaultValue = "false",property = "overwrite")
    public boolean isOverwrite;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;


    public MavenProject project = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(this.getPluginContext().containsKey("project") &&this.getPluginContext().get("project") instanceof MavenProject ){
            project = (MavenProject) this.getPluginContext().get("project");
        }
        MavenProject currentProject = session.getCurrentProject();
        if(currentProject != session.getTopLevelProject()){
            return;
        }
        if(isGenPom){
            if(project!=null && project.getModel()!=null && project.getModel().getPomFile() !=null&&!isOverwrite){
                this.getLog().warn("项目下已包含pom,使用overwrite参数可覆盖现有pom");
                return;
            }
            try {
                doGeneratePom();
            } catch (MojoFailureException e) {
                this.getLog().error(e);
                throw e;
            }
        }
    }

    public void doGenerateExtensionsXml() throws MojoFailureException {
        File file = new File(".mvn");
        if(!file.exists()){
            file.mkdirs();
        }
        File xmlFile = new File(file,"extensions.xml");
        xmlFile = xmlFile.getAbsoluteFile();
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(),"/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        try {
            Template tmpl = cfg.getTemplate("extensions.ftl");
            tmpl.setOutputEncoding("utf-8");
            try (Writer out = new FileWriter(xmlFile)) {
                tmpl.process(null, out);
            }
        }catch(Throwable e){
            throw new MojoFailureException("extensions.xml生成失败",e.getCause());
        }
    }

    public void doGeneratePom() throws MojoFailureException {
        this.getLog().info("准备生成多工程pom文件");
        File file = new File("pom.xml");
        if(file.exists()){
            file.delete();
        }
        file = file.getAbsoluteFile();
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(),"/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        try {
            PomGenerateContext context = new PomGenerateContext();

            context.setProjectName(
                    file.getParentFile().getName().matches("^[a-zA-Z0-9_.-]{1,}$")
                    ? file.getParentFile().getName() : "pbc-multi-module-project");
            context.setModuleList(new ArrayList<>());
            for (File listFile : file.getParentFile().listFiles()) {
                if(listFile.isDirectory()&& !listFile.getName().startsWith(".")){
                    context.getModuleList().add(new PomGenerateContext.Module(listFile.getName()));
                }
            }
            Template tmpl = cfg.getTemplate("pom.ftl");
            tmpl.setOutputEncoding("utf-8");
            try(Writer out = new FileWriter(file)){
                tmpl.process(context,out);
            }
            this.getLog().info("pom文件生成:"+file.getAbsolutePath());
        } catch (IOException | TemplateException e) {
            throw new MojoFailureException("pom生成失败",e.getCause());
        }
    }
}
