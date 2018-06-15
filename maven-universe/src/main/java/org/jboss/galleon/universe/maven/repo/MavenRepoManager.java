/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.galleon.universe.maven.repo;

import org.jboss.galleon.universe.maven.MavenUniverseConstants;
import org.jboss.galleon.universe.maven.MavenUniverseException;

import java.nio.file.Path;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repomanager.RepositoryArtifactResolver;
import org.jboss.galleon.universe.maven.MavenArtifact;

/**
 *
 * @author Alexey Loubyansky
 */
public interface MavenRepoManager extends RepositoryArtifactResolver {

    String REPOSITORY_ID = RepositoryArtifactResolver.ID_PREFIX + MavenUniverseConstants.MAVEN;

    @Override
    default String getRepositoryId() {
        return REPOSITORY_ID;
    }

    @Override
    default Path resolve(String location) throws ProvisioningException {
        final MavenArtifact artifact = MavenArtifact.fromString(location);
        resolve(artifact);
        return artifact.getPath();
    }

    void resolve(MavenArtifact artifact) throws MavenUniverseException;

    default void resolveLatestVersion(MavenArtifact artifact) throws MavenUniverseException {
        resolveLatestVersion(artifact, null);
    }

    void resolveLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException;

    default String getLatestVersion(MavenArtifact artifact) throws MavenUniverseException {
        return getLatestVersion(artifact, null);
    }

    String getLatestVersion(MavenArtifact artifact, String lowestQualifier) throws MavenUniverseException;

    void install(MavenArtifact artifact, Path path) throws MavenUniverseException;
}
