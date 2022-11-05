package com.winning.pbc.utils;

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.os.OperatingSystemUtils;

import java.io.File;
import java.io.IOException;

public class StartScriptCreater {


    public static void generateStartScript(MavenProject project){
        if(OSUtils.isWindowsSystem()){
            generateWindowsStartScript(project);
        }else{
            generateMacosOrLinuxStartScript(project);
        }
    }


    public static void generateWindowsStartScript(MavenProject project){
        File workDir = project.getBasedir();
        File startupBatFile = new File(workDir,"startup.bat");
        if(startupBatFile.exists()){
            throw new RuntimeException("文件 startup.bat 已存在");
        }
        String batScript ="java -cp .idea/home/launcher/launch-0.0.2-SNAPSHOT.jar "
                +" -Xms1g -Xmx5g -Dtmts.runMode=develop "
                +" -Dorg.eclipse.jetty.server.Request.maxFormContentSize=10485760 "
                +" -Ddatabase.show.sql=false -Djava.awt.headless=true "
                +" com.winning.mde.loader.TmtsBootApplication \".idea/home\" \"./\"";
        try {
            FileUtils.write(startupBatFile,batScript,"utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void generateMacosOrLinuxStartScript(MavenProject project){
        File workDir = project.getBasedir();
        File startupShFile = new File(workDir,"startup.sh");
        if(startupShFile.exists()){
            throw new RuntimeException("文件 startup.sh 已存在");
        }
        String bashScript ="#!/bin/bash\n"
                +"java -cp .idea/home/launcher/launch-0.0.2-SNAPSHOT.jar \\\n"
                +" -Xms1g -Xmx5g -Dtmts.runMode=develop \\\n"
                +" -Dorg.eclipse.jetty.server.Request.maxFormContentSize=10485760 \\\n"
                +" -Ddatabase.show.sql=false -Djava.awt.headless=true \\\n"
                +" com.winning.mde.loader.TmtsBootApplication \".idea/home\" \"./\"";

        try {
            FileUtils.write(startupShFile,bashScript,"utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
