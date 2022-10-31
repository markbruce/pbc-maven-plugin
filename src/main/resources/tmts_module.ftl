<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!-- generate by winning-pbc maven plugin at ${.now} -->
<Module>
    <#list webModuleList as webModule >
    <WebModule moduleName="${webModule.name}">
        <name>${webModule.name}</name>
        <source>${webModule.source}</source>
        <target>${webModule.target}</target>
    </WebModule>
    </#list>
</Module>
