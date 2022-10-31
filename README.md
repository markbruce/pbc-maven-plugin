# PBC MAVEN 插件使用手册
## 命令及开关列表
### mvn pbc:init-pom -Doverwrite
### mvn pbc:init-home
### mvn pbc:init-run

## init-pom 使用方式
1.  新建工程目录，例如 outpatient-aaio
2. 进入目录中
3. 将本地需要启动的工程从仓库中克隆下来，完成后目录结构应类似:
```
    outpatient-aaio
       - module-project-1
       - module-project-2
       - module-project-3
```
4. 在outpatient-aaio目录中执行命令
``` bash
    mvn pbc:init-pom
```
执行完毕后将会生成一个包括目录中工程的pom，形如：
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.winning.wxp</groupId>
  <artifactId>outpatient-aaio</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>pom</packaging>
  <name>winning-pbc-multi-modules-project</name>
  <modules>
    <module>module-project-1</module>
    <module>module-project-2</module>
    <module>module-project-3</module>
  </modules>
  <distributionManagement>
    <repository>
      <id>winning-releases</id>
      <url>${maven.host}/repository/winning-releases/</url>
    </repository>
    <snapshotRepository>
      <id>winning-snapshots</id>
      <url>${maven.host}/repository/winning-snapshots/</url>
    </snapshotRepository>
  </distributionManagement>
  <properties>
    <maven.compiler.target>${java.version}</maven.compiler.target>
    <java.version>1.8</java.version>
    <maven.compiler.source>${java.version}</maven.compiler.source>
    <project.manifest.gene>project.manifest.gene</project.manifest.gene>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.host>${mavenHost}</maven.host>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <!-- 下面的配置用来定义要启动的非工程模块，例如1.3.1.0版本的winning-winex-person模块。如果有多个可以用分号分割，不可换行。 -->
    <!--<pbc.modules>com.winning.person:winning-winex-person:1.3.1.0-SNAPSHOT;</pbc.modules>-->
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
      <url>${maven.host}/repository/maven-public/</url>
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
      <url>${maven.host}/repository/maven-public/</url>
    </pluginRepository>
  </pluginRepositories>
</project>
```
ps. 如果已经生成过一次，可以使用-Doverwrite 覆盖生成
## init-home 使用方式

```bash
    mvn pbc:init-home
```
此命令会进行以下动作
1. 在工程目录下生成以下目录结构:

```
    outpatient-aaio
        - .idea
            - configure
            - launcher
            - module
            - ms_dev
            - startinfo
```
2. 解析当前工程的依赖
3. 读取pom.xml中 properties 内的属性<span><pbc.modules></pbc.modules></span>,此属性用于下载
目录中不存在代码的模块，格式为 groupId:artifactId:version , 多个时使用分号<span>;</span>分割;
4. 解析工程依赖，判断工程对pbc框架的依赖版本，若当前工程不包含框架的源码，则将框架的 pom信息也加入到步骤3
的解析结果中
5. 下载步骤 3、4 中解析到的模块
6. 结合工程的依赖与下载的模块在 modules中按模块生成依赖列表
7. 根据框架版本拉取启动器jar包放入launcher目录中
8. 根据目录中的工程按模块生成ms_dev中的工程模块列表
9. 结合工程及下载的模块在startinfo中生成启动列表
10. 在 .idea/workspace.xml中生成idea的启动配置项

## init-run 使用方式

```bash
    mvn pbc:init-run
```

此命令将在.idea/workspace.xml中生成idea的启动配置，与init-home的步骤10 相同

