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
package org.netbeans.modules.micronaut.db;

import com.google.gson.JsonPrimitive;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CommandProvider.class)
public class GetContainerPathCommand implements CommandProvider {

    private static final String NBLS_GET_CONTAINER_PATH = "nbls.micronaut.get.container.path";

    @Override
    public Set<String> getCommands() {
        return Set.of(NBLS_GET_CONTAINER_PATH);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        final CompletableFuture<Object> future = new CompletableFuture<>();
        if (arguments.isEmpty()) {
            future.completeExceptionally(new IllegalStateException("Expecting project URL and property name as arguments to " + command));
        } else {
            try {
                String path = null;
                String uri = ((JsonPrimitive) arguments.get(0)).getAsString();
                FileObject fo = URLMapper.findFileObject(java.net.URI.create(uri).toURL());
                Project prj = fo != null ? FileOwnerQuery.getOwner(fo) : null;
                if (prj != null) {
                    NbMavenProject p = prj.getLookup().lookup(NbMavenProject.class);
                    MavenProject mvnp = p != null ? p.getMavenProject() : null;
                    if (mvnp != null) {
                        String image = (String) mvnp.getProperties().get("jib.docker.image");
                        String tag = (String) mvnp.getProperties().get("jib.docker.tag");
                        if (image != null && tag != null) {
                            path = image + ':' + tag;
                        }
                    } else {
                        BuildPropertiesSupport bps = BuildPropertiesSupport.get(prj);
                        if (bps != null) {
                            BuildPropertiesSupport.Property prop = bps.findTaskProperty("dockerBuild", "images");
                            if (prop != null) {
                                path = prop.getStringValue();
                                if (path != null && path.length() > 1) {
                                    path = path.substring(1, path.length() - 1);
                                }
                            }
                        }
                    }
                }
                future.complete(path);
            } catch (MalformedURLException ex) {
                future.completeExceptionally(ex);
            }
        }
        return future;
    }
}
