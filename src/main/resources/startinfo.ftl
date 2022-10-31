<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!-- generate by winning-pbc maven plugin at ${.now} -->
<StartInfo>
    <tmts_home>${tmtsHome}</tmts_home>
    <#list moduleNameList as moduleName>
    <startModules>${moduleName}</startModules>
    </#list>
    <ProductCode>${productCode}</ProductCode>
</StartInfo>
