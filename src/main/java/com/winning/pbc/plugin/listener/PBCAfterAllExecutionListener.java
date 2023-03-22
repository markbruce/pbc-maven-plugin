package com.winning.pbc.plugin.listener;

import com.winning.pbc.plugin.PBCGenerateHomeMojo;
import com.winning.pbc.plugin.PBCPackageMojo;
import com.winning.pbc.utils.PBCPackageRunner;
import com.winning.pbc.utils.WorkhomeInitialzier;
import org.apache.maven.execution.MojoExecutionEvent;
import org.apache.maven.execution.MojoExecutionListener;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.*;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;

@Named
@Component(role = MojoExecutionListener.class)
public class PBCAfterAllExecutionListener implements MojoExecutionListener {

    public PBCAfterAllExecutionListener(){
        System.out.println("create listener");
    }

    @Requirement
    private Logger logger;

    @Requirement
    private ProjectBuilder projectBuilder;

    public boolean isSkipAkso;
    private static boolean executingInitHome = false;

    private static boolean executingPackage = false;

    private static final AtomicInteger executingInitCount = new AtomicInteger(0);
    private static final AtomicInteger executingPackageCount = new AtomicInteger(0);

    @Override
    public void beforeMojoExecution(MojoExecutionEvent event) throws MojoExecutionException {
    }

    @Override
    public synchronized void afterMojoExecutionSuccess(MojoExecutionEvent event) throws MojoExecutionException {
        if(event.getMojo() instanceof PBCGenerateHomeMojo){
            executingInitHome = true;
            isSkipAkso = ((PBCGenerateHomeMojo) event.getMojo()).isSkipAkso;
        }
//        executingInitHome = executingInitHome || (event.getMojo() instanceof PBCGenerateHomeMojo);
        if(event.getMojo() instanceof PBCPackageMojo){
            executingPackage = true;
            isSkipAkso = ((PBCPackageMojo) event.getMojo()).isSkipAkso;
        }
//        executingPackage = executingPackage || (event.getMojo() instanceof PBCPackageMojo);
        doIfAllDone(event);
//        logger.info("mojo execution success");
    }

    @Override
    public synchronized void afterExecutionFailure(MojoExecutionEvent event) {
        doIfAllDone(event);
    }

    public void doIfAllDone(MojoExecutionEvent event){
        synchronized (Runtime.getRuntime()){
            int unfinishedProjectCount = 0;
            for (MavenProject allProject : event.getSession().getAllProjects()) {
                if(event.getSession().getResult().getBuildSummary(allProject) ==null){
                    unfinishedProjectCount++;
                }
            }
            if(executingInitHome){
                int initCount = Integer.parseInt(System.getProperty("executionInitCount","0"))+1;
                System.setProperty("executionInitCount", String.valueOf(initCount));
                logger.info("initCount:"+initCount);
                if(initCount == event.getSession().getAllProjects().size()){
                    logger.info("准备初始化工作空间");
                    WorkhomeInitialzier.initWorkHome(event.getSession(), projectBuilder, logger,isSkipAkso);
                }
            }
            if(executingPackage) {
//                int packageCount = executingPackageCount.incrementAndGet();
                int packageCount = Integer.parseInt(System.getProperty("packageCount","0"))+1;
                System.setProperty("packageCount", String.valueOf(packageCount));
                logger.info("packageCount:"+packageCount);
                if (packageCount == event.getSession().getAllProjects().size()) {
                    logger.info("准备生成制品");
                    try {
                        PBCPackageRunner.doPackage(event.getSession(), projectBuilder, logger,isSkipAkso);
                    } catch (Throwable e) {
                        logger.error("制品生成失败", e);
                    }
                }
            }
        }
    }
}
