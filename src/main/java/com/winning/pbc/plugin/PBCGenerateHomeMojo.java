package com.winning.pbc.plugin;

import com.winning.pbc.utils.StartScriptCreater;
import com.winning.pbc.utils.WorkspaceRunManagerCreater;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

@Mojo(name = "init-home",
        threadSafe = true,
        requiresDependencyResolution = COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = COMPILE_PLUS_RUNTIME,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class PBCGenerateHomeMojo extends AbstractMojo {


    public static AtomicInteger count = new AtomicInteger(0);

    @Component
    private ProjectBuilder projectBuilder;

    @Parameter(required = true,defaultValue = "false",property = "createStartScript")
    public boolean isCreateStartScript;

    @Parameter(required = true,defaultValue = "false",property = "skipAkso")
    public boolean isSkipAkso;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MavenProject project = session.getCurrentProject();
        if(project == session.getTopLevelProject()){
            try{
                WorkspaceRunManagerCreater.generateRunManagerToWorkspace(project);
            }catch(Throwable e){
                this.getLog().warn("向workspace.xml写入启动配置失败："+e.getMessage());
                this.getLog().warn("这个问题不会影响home生成，处理该问题后，后续可通过 mvn winning-pbc:init-run 或当前命令再次尝试生成,具体异常信息如下");
                this.getLog().error(e);
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
