package com.winning.pbc.plugin;

import com.winning.mde.module.MSModuleType;
import com.winning.pbc.model.*;
import com.winning.pbc.utils.StartScriptCreater;
import com.winning.pbc.utils.Utils;
import com.winning.pbc.utils.WorkspaceRunManagerCreater;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.*;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
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

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MavenProject project = session.getCurrentProject();
        if(project == session.getTopLevelProject()){
            try{
                WorkspaceRunManagerCreater.generateRunManagerToWorkspace(project);
            }catch(Throwable e){
                this.getLog().warn("???workspace.xml???????????????????????????"+e.getMessage());
                this.getLog().warn("????????????????????????home????????????????????????????????????????????? mvn winning-pbc:init-run ?????????????????????????????????,????????????????????????");
                this.getLog().error(e);
            }
            if(this.isCreateStartScript){
                try{
                    this.getLog().info("??????????????????????????????");
                    StartScriptCreater.generateStartScript(project);
                }catch(Throwable e){
                    this.getLog().warn("????????????????????????:"+e.getMessage());
                    this.getLog().warn(e);
                }
            }
        }
    }





}
