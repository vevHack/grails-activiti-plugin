/* Copyright 2010 the original author or authors.
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
 *
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
 *
 * @since 5.0.beta2
 */
 
includeTargets << grailsScript("Init")

target(uninstall: "Uninstall Vacation Request Sample Application") {
		String vacationRequestDir="vacationRequest"
		ant.delete file:"${basedir}/grails-app/conf/VacationRequestBootStrap.groovy"
		ant.delete file:"${basedir}/grails-app/conf/VacationRequest.bpmn20.xml"
		ant.delete dir:"${basedir}/grails-app/controllers/${vacationRequestDir}"
		ant.delete dir:"${basedir}/grails-app/domain/${vacationRequestDir}"
		ant.delete dir:"${basedir}/grails-app/views/${vacationRequestDir}"
		ant.delete dir:"${basedir}/src/groovy/${vacationRequestDir}"
}

setDefaultTarget(uninstall)