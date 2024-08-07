/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.project;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo.ModuleType;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.api.JpaSupport;

/**
 * An implementation of the <code>JPAModuleInfo</code>
 * for Web proejcts.
 * 
 * @author Erno Mononen
 */
class WebJPAModuleInfo implements JPAModuleInfo{
    
    private final WebProject project;
    
    /** Creates a new instance of WebJpaModuleInfo */
    WebJPAModuleInfo(WebProject project) {
        this.project = project;
    }
    
    @Override
    public ModuleType getType() {
        return JPAModuleInfo.ModuleType.WEB;
    }

    @Override
    public String getVersion() {
        return project.getWebModule().getModuleVersion();
    }

    @Override
    public Boolean isJPAVersionSupported(String version) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eePlatform platform  = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
        
        if (platform == null) {
            return null;
        }
        JpaSupport support = JpaSupport.getInstance(platform);
        JpaProvider provider = support.getDefaultProvider();
        if (provider != null) {
            return (Persistence.VERSION_3_2.equals(version) && provider.isJpa32Supported())
                    || (Persistence.VERSION_3_1.equals(version) && provider.isJpa31Supported())
                    || (Persistence.VERSION_3_0.equals(version) && provider.isJpa30Supported())
                    || (Persistence.VERSION_2_2.equals(version) && provider.isJpa22Supported())
                    || (Persistence.VERSION_2_1.equals(version) && provider.isJpa21Supported())
                    || (Persistence.VERSION_2_0.equals(version) && provider.isJpa2Supported())
                    || (Persistence.VERSION_1_0.equals(version) && provider.isJpa1Supported());
        }
        return null;
    }
}
