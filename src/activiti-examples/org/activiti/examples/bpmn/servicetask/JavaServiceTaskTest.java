/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.activiti.examples.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiClassLoadingException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class JavaServiceTaskTest {
  @Rule public ActivitiRule activitiRule = new ActivitiRule();

  @Deployment
  @Test
  public void testJavaServiceDelegation() {
    RuntimeService runtimeService = activitiRule.getRuntimeService();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("javaServiceDelegation", CollectionUtil.singletonMap("input", "Activiti BPM Engine"));
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState")
      .singleResult();
    assertEquals("ACTIVITI BPM ENGINE", runtimeService.getVariable(execution.getId(), "input"));
  }
  
  @Deployment
  @Test
  public void testFieldInjection() {
    // Process contains 2 service-tasks using field-injection. One should use the exposed setter,
    // the other is using the private field.
    RuntimeService runtimeService = activitiRule.getRuntimeService();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("fieldInjection");
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState")
      .singleResult();
    
    assertEquals("HELLO WORLD", runtimeService.getVariable(execution.getId(), "var"));
    assertEquals("HELLO SETTER", runtimeService.getVariable(execution.getId(), "setterVar"));
  }
  
  @Deployment
  @Test
  public void testExpressionFieldInjection() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("name", "kermit");
    vars.put("gender", "male");
    vars.put("genderBean", new GenderBean());
    
    RuntimeService runtimeService = activitiRule.getRuntimeService();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("expressionFieldInjection", vars);
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState")
      .singleResult();
    
    assertEquals("timrek .rM olleH", runtimeService.getVariable(execution.getId(), "var2"));
    assertEquals("elam :si redneg ruoY", runtimeService.getVariable(execution.getId(), "var1"));
  }
  
  @Deployment
  @Test
  public void testUnexistingClassDelegation() {
    try {
    RuntimeService runtimeService = activitiRule.getRuntimeService();
      runtimeService.startProcessInstanceByKey("unexistingClassDelegation");
      fail();
    } catch (ActivitiException e) {
      assertTrue(e.getMessage().contains("couldn't instantiate class org.activiti.BogusClass"));
      assertNotNull(e.getCause());
      assertTrue(e.getCause() instanceof ActivitiClassLoadingException);
    }
  }

  @Test
  public void testIllegalUseOfResultVariableName() {
    try {
    RepositoryService repositoryService = activitiRule.getRepositoryService();
      repositoryService.createDeployment().addClasspathResource("org/activiti/examples/bpmn/servicetask/JavaServiceTaskTest.testIllegalUseOfResultVariableName.bpmn20.xml").deploy();
      fail();
    } catch (ActivitiException e) {
      assertTrue(e.getMessage().contains("resultVariable"));
    }
  }
  
  @Deployment
  @Test
  public void testExceptionHandling() {
    
    // If variable value is != 'throw-exception', process goes 
    // through service task and ends immidiately
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", "no-exception");
    RuntimeService runtimeService = activitiRule.getRuntimeService();
    runtimeService.startProcessInstanceByKey("exceptionHandling", vars);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    // If variable value == 'throw-exception', process executes
    // service task, which generates and catches exception,
    // and takes sequence flow to user task
    vars.put("var", "throw-exception");
    runtimeService.startProcessInstanceByKey("exceptionHandling", vars);
    TaskService taskService = activitiRule.getTaskService();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Fix Exception", task.getName());
  }

}
