<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!-- generate by winning-pbc maven plugin at ${.now} -->
<Module moduleName="${moduleName}" moduleType="${moduleType}" order="${order}">
    <#if webAppPath != ''><webAppPath>${webAppPath}</webAppPath></#if>
    <#list compList as comp>
    <MSComp id="${comp.id}" scope="${comp.scope}">
        <jarName>${comp.jarName}</jarName>
    </MSComp>
    </#list>
</Module>
