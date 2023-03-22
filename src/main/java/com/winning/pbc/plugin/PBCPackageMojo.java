package com.winning.pbc.plugin;

import com.winning.pbc.utils.StartScriptCreater;
import com.winning.pbc.utils.WorkspaceRunManagerCreater;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

@Mojo(name = "package", threadSafe = true,
        requiresDependencyResolution = COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = COMPILE_PLUS_RUNTIME,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class PBCPackageMojo extends AbstractMojo {


    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(required = false,defaultValue = "",property = "outputPath")
    public boolean isCreateStartScript;

    @Parameter(required = true,defaultValue = "false",property = "skipAkso")
    public boolean isSkipAkso;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    }
}
