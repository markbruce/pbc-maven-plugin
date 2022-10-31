<?xml version="1.0" encoding="UTF-8"?>
<!-- generate by winning-pbc maven plugin at ${.now} -->
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.winning.wxp</groupId>
    <artifactId>winning-pbc-multi-modules-project</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>winning-pbc-multi-modules-project</name>
    <distributionManagement>
        <repository>
            <id>winning-releases</id>
            <url><#noparse>${</#noparse>maven.host<#noparse>}</#noparse>/repository/winning-releases/</url>
        </repository>
        <snapshotRepository>
            <id>winning-snapshots</id>
            <url><#noparse>${</#noparse>maven.host<#noparse>}</#noparse>/repository/winning-snapshots/</url>
        </snapshotRepository>
    </distributionManagement>
    <properties>
        <maven.compiler.target><#noparse>${</#noparse>java.version<#noparse>}</#noparse></maven.compiler.target>
        <java.version>1.8</java.version>
        <maven.compiler.source><#noparse>${</#noparse>java.version<#noparse>}</#noparse></maven.compiler.source>
        <project.manifest.gene>project.manifest.gene</project.manifest.gene>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.host>http://nexus.winning.com.cn:8081</maven.host>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>
    <repositories>
        <repository>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <id>winning-local-nexus</id>
            <url><#noparse>${</#noparse>maven.host<#noparse>}</#noparse>/repository/maven-public/</url>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <id>winning-local-nexus</id>
            <url><#noparse>${</#noparse>maven.host<#noparse>}</#noparse>/repository/maven-public/</url>
        </pluginRepository>
    </pluginRepositories>
    <dependencies>
        <#list dependencyList as dependency>
            <dependency>
                <groupId>${dependency.groupId}</groupId>
                <artifactId>${dependency.artifactId}</artifactId>
                <version>${dependency.version}</version>
                <#if dependency.type == 'pom'><type>pom</type></#if>
            </dependency>
        </#list>
    </dependencies>
    <#--  <build>-->
    <#--    <plugins>-->
    <#--      <plugin>-->
    <#--        <artifactId></artifactId>-->
    <#--        <groupId></groupId>-->
    <#--        <version></version>-->
    <#--        <excutions></excutions>-->
    <#--      </plugin>-->
    <#--    </plugins>-->
    <#--  </build>-->
</project>
