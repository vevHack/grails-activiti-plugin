/* Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 /**
 * This event script improved the TestApp.groovy and DeployBar.groovy script 
 * by generating activiti.properties from activiti configurations in Config.groovy.
 * User of this plugin no longer need to maintain activiti.properties file separately. 
  * 
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
  *
 * @since 5.0.alpha3
  * 
 */
import grails.util.BuildSettingsHolder as build
import groovy.xml.MarkupBuilder
 
includeTargets << grailsScript("_GrailsPackage")

CONFIG_FILE = "activiti.cfg.xml"

eventTestPhasesStart = {
	ant.echo "eventTestPhasesStart invoked."
	ensureAllGeneratedFilesDeleted()	  
	createActivitiConfigFile(build.settings.resourcesDir.toString())
}

eventTestPhasesEnd = { 
  ant.echo "eventTestPhasesEnd invoked."
	ant.delete file:"${build.settings.resourcesDir}/${CONFIG_FILE}" 
}

eventTestPhaseStart = { phase ->
	ant.echo "eventTestPhaseStart invoked. phase = $phase"
	if (phase == 'unit') {
		ant.mkdir dir:"${build.settings.testClassesDir.absolutePath}/${phase}"
		rootLoader.addURL new File("${build.settings.testClassesDir.absolutePath}/${phase}").toURL()
	}
}

eventDeployBarStart = { 
	ant.echo "eventDeployBarStart invoked."
	ensureAllGeneratedFilesDeleted()
  createActivitiConfigFile("${activitiPluginDir}/grails-app/conf")
}
 
eventDeployBarEnd = { 
	ant.echo "eventDeployBarEnd invoked."
	ant.delete file:"${activitiPluginDir}/grails-app/conf/${CONFIG_FILE}" 
}

private void ensureAllGeneratedFilesDeleted() {
	if (new File("${build.settings.resourcesDir}/${CONFIG_FILE}").exists()) {
		  ant.delete file:"${build.settings.resourcesDir}/${CONFIG_FILE}" 
	}
	if (new File("${activitiPluginDir}/grails-app/conf/${CONFIG_FILE}").exists()) {
		  ant.delete file:"${activitiPluginDir}/grails-app/conf/${CONFIG_FILE}" 
	}	
}

private void createActivitiConfigFile(String activitiConfigFilePath) {
	createConfig()
	def activitiConfigFile = new File(activitiConfigFilePath, CONFIG_FILE)
	activitiConfigFile.withWriter {
		it.writeLine """<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans" 
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans   http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="processEngineConfiguration" class="org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration">
  
    <property name="databaseType" value="${config.activiti.databaseType}" />
    <property name="jdbcUrl" value="${config.dataSource.url}" />
    <property name="jdbcDriver" value="${config.dataSource.driverClassName}" />
    <property name="jdbcUsername" value="${config.dataSource.username}" />
    <property name="jdbcPassword" value="${config.dataSource.password}" />
    
    <!-- Database configurations -->
    <property name="databaseSchemaUpdate" value="${config.activiti.databaseSchemaUpdate}" />
    
    <!-- job executor configurations -->
    <property name="jobExecutorActivate" value="${config.activiti.jobExecutorActivate}" />
    
    <!-- mail server configurations -->
    <property name="mailServerPort" value="${config.activiti.mailServerPort}" />    
  </bean>

</beans>	
"""
	}
	ant.echo "Content of generated ${activitiConfigFile.absolutePath} file:"
  println activitiConfigFile.text
}	

eventCreateWarStart = {warname, stagingDir ->
  if (grailsEnv == "production") {
      ant.echo "Remove unnecessary JAR files..."
	  ["subethasmtp-smtp-1.2.jar", 
		  "subethasmtp-wiser-1.2.jar", 
		  "antlr-2.7.7.jar",
		  "geronimo-jta_1.1_spec-1.1.1.jar",
		  "mockito-core-1.8.2.jar",
		  "objenesis-1.0.jar",
		  "persistence-api-1.0.jar"
		 ].each { jar ->
      ant.delete file:"${stagingDir}/WEB-INF/lib/$jar"
      }
    }
}
