package com.winning.pbc.plugin.listener;

import com.winning.pbc.plugin.PBCGenerateHomeMojo;
import com.winning.pbc.utils.WorkhomeInitialzier;
import org.apache.maven.execution.MojoExecutionEvent;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.*;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
@Component(role = org.apache.maven.execution.MojoExecutionListener.class)
public class MojoExecutionListener implements org.apache.maven.execution.MojoExecutionListener {

    @Requirement
    private Logger logger;

    @Requirement
    private ProjectBuilder builder2;

    private static boolean executingInit = false;

    @Override
    public void beforeMojoExecution(MojoExecutionEvent event) throws MojoExecutionException {
    }

    @Override
    public synchronized void afterMojoExecutionSuccess(MojoExecutionEvent event) throws MojoExecutionException {
        executingInit= executingInit || (event.getMojo() instanceof PBCGenerateHomeMojo);
        doIfAllDone(event);
//        logger.info("mojo execution success");
    }

    @Override
    public synchronized void afterExecutionFailure(MojoExecutionEvent event) {
        doIfAllDone(event);
    }

    public void doIfAllDone(MojoExecutionEvent event){
        synchronized (event.getSession()){
            int nullcount = 0;
            for (MavenProject allProject : event.getSession().getAllProjects()) {
                if(event.getSession().getResult().getBuildSummary(allProject) ==null){
                    nullcount++;
                }
            }
            if(nullcount==1 && executingInit){
                logger.info("准备初始化工作空间");
                WorkhomeInitialzier.initWorkHome(event.getSession(), builder2, logger);
            }
        }

    }


}
