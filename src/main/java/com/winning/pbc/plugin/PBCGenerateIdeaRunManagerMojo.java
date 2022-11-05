package com.winning.pbc.plugin;

import com.winning.pbc.utils.StartScriptCreater;
import com.winning.pbc.utils.Utils;
import com.winning.pbc.utils.WorkspaceRunManagerCreater;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;

import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

//@Mojo(name = "init-home",
//        threadSafe = true,
//        requiresDependencyResolution = COMPILE_PLUS_RUNTIME,
//        requiresDependencyCollection = COMPILE_PLUS_RUNTIME,
//        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@Mojo(name = "init-run", threadSafe = true)
public class PBCGenerateIdeaRunManagerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(required = true,defaultValue = "false",property = "createStartScript")
    public boolean isCreateStartScript;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MavenProject project = session.getCurrentProject();
        if(project == session.getTopLevelProject()){
            try{
                WorkspaceRunManagerCreater.generateRunManagerToWorkspace(project);
            }catch(Throwable e){
                this.getLog().error("向workspace.xml写入启动配置失败",e);
            }
            if(this.isCreateStartScript){
                try{
                    this.getLog().info("开始生成启动脚本文件");
                    StartScriptCreater.generateStartScript(project);
                }catch(Throwable e){
                    this.getLog().warn("生成启动脚本失败:"+e.getMessage());
                    this.getLog().warn(e);
                }
            }
        }
    }

}
