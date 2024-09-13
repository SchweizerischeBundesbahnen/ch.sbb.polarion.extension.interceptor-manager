# Polarion ALM extension to execute validation during save/delete actions

This Polarion extension provides possibility to run custom Java code (via hooks), before Polarion saves/deletes a Work Item, Document, Plan or Test Run.

## Build

This extension can be produced using maven:
```bash
mvn clean package
```

## Installation to Polarion

To install extension to Polarion `ch.sbb.polarion.extension.interceptor-manager-<version>.jar`
should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.interceptor-manager/eclipse/plugins`.
It can be done manually or automated using maven build:
```bash
mvn clean install -P install-to-local-polarion
```
For automated installation with maven env variable `POLARION_HOME` should be defined and point to folder where Polarion is installed.

Changes only take effect after restart of Polarion.

## Polarion configuration

### Interceptor Manager settings/customization
Use Administration -> Interceptor Manager -> Settings page to enable/disable or set specific properties for each existing hook.


### Hooks installation
#### Standalone jar hooks
Copy hook jar to hooks folder (`<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.interceptor-manager/eclipse/plugins/hooks`) and enforce hooks reloading from the settings page or restart Polarion.
By default, newly added hooks are disabled and must be enabled manually.
#### Hooks from Another Polarion Extension
Hooks can be located in another Polarion extension. In this case, the extension should register the hooks as an OSGi service using the IActionHook interface.
For more details, refer to the example at https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.interceptor-manager.hook-samples/tree/main/hook-samples-osgi.

## Upgrade

### Upgrade from version 2.x.x to 3.0.0
Version 3.0.0 has received significant change - project+package rename. Verify you're using the new subfolder name in the `extensions` folder.
Also, this change prevents previous hooks versions from working. They must be replaced with the new ones built especially for v.3.0.0+. Note that doing this will reset hooks settings tho their default values, so do not forget to backup your actual settings to restore them after update. 

### Upgrade from version 1.x.x to 2.0.0
Version 2.0.0 requires the new hooks model therefore hooks built for 1.x.x will stop working. They must be replaced with the new ones built especially for v.2.0.0+.
Also the new version introduced some significant internal settings model changes/improvements which have made old settings data incompatible. This means that during the first run all settings will be reset to their default values (unfortunately revisions history will be lost too).
