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

package org.jboss.galleon.universe.location.test;

import static org.jboss.galleon.universe.TestConstants.GROUP_ID;

import java.nio.file.Path;

import org.jboss.galleon.ArtifactCoords;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.repomanager.FeaturePackBuilder;
import org.jboss.galleon.repomanager.FeaturePackRepositoryManager;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseRepoTestBase;
import org.jboss.galleon.universe.UniverseResolver;
import org.jboss.galleon.universe.maven.MavenArtifact;
import org.jboss.galleon.universe.maven.MavenErrors;
import org.jboss.galleon.universe.maven.MavenProducerInstaller;
import org.jboss.galleon.universe.maven.MavenUniverseFactory;
import org.jboss.galleon.universe.maven.MavenUniverseInstaller;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackLocationResolverTestCase extends UniverseRepoTestBase {

    private static final String PRODUCER1_ARTIFACT_ID = "producer1-artifact";
    private static final String PRODUCER1_FP_ARTIFACT_ID = "producer1-feature-pack-artifact";
    private static final String FP_GROUP_ID = "test.group";

    private FeaturePackRepositoryManager fpRepo;
    private UniverseResolver resolver;
    private MavenArtifact universeArt;

    @Override
    public void doInit() throws Exception {

        fpRepo = FeaturePackRepositoryManager.newInstance(repoHome);

        final MavenArtifact artifact = new MavenArtifact().
                setGroupId(GROUP_ID).
                setArtifactId(PRODUCER1_ARTIFACT_ID).
                setVersion("1.0.0.Final");
        MavenProducerInstaller producerInstaller = new MavenProducerInstaller("producer1", repo, artifact, FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID);
        producerInstaller.addChannel("5", "[5.0-alpha,6.0-alpha)");
        producerInstaller.addFrequencies("alpha", "beta");
        producerInstaller.install();

        universeArt = new MavenArtifact().
                setGroupId(GROUP_ID).
                setArtifactId("universe1-artifact").
                setVersion("1.0.0.Final");
        final MavenUniverseInstaller universeInstaller = new MavenUniverseInstaller(repo, universeArt);
        universeInstaller.addProducer("producer1", GROUP_ID, PRODUCER1_ARTIFACT_ID, "[1.0.0,2.0.0)");
        universeInstaller.install();

        resolver = UniverseResolver.builder().addArtifactResolver(repo).build();
    }


    @Test
    public void testMain() throws Exception {

        try {
            resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5/alpha"));
            Assert.fail("Artifact does not exist");
        } catch(ProvisioningException e) {
            final MavenArtifact artifact = new MavenArtifact();
            artifact.setGroupId(FP_GROUP_ID);
            artifact.setArtifactId(PRODUCER1_FP_ARTIFACT_ID);
            Assert.assertEquals(MavenErrors.artifactNotFound(artifact, repoHome).getLocalizedMessage(), e.getLocalizedMessage());
        }

        FeaturePackBuilder.newInstance().setGav(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "5.1.0.Alpha1")).build(fpRepo);

        try {
            resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5"));
            Assert.fail("No final releases yet");
        } catch(ProvisioningException e) {
            // ignore
        }

        MavenArtifact artifact = new MavenArtifact().
                setGroupId(FP_GROUP_ID).
                setArtifactId(PRODUCER1_FP_ARTIFACT_ID).
                setExtension("zip").
                setVersion("5.1.0.Alpha1");
        Path path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5/alpha"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        FeaturePackBuilder.newInstance().setGav(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "4.1.0.Beta2")).build(fpRepo);
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5/alpha"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        FeaturePackBuilder.newInstance().setGav(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "5.2.0.Final")).build(fpRepo);
        artifact.setVersion("5.2.0.Final");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5/alpha"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        FeaturePackBuilder.newInstance().setGav(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "5.2.1.Beta1")).build(fpRepo);
        artifact.setVersion("5.2.1.Beta1");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5/alpha"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5/beta"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5"));
        artifact.setVersion("5.2.0.Final");
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        FeaturePackBuilder.newInstance().setGav(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "6.0.0.Alpha1")).build(fpRepo);
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5/alpha"));
        artifact.setVersion("5.2.1.Beta1");
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        FeaturePackBuilder.newInstance().setGav(ArtifactCoords.newGav(FP_GROUP_ID, PRODUCER1_FP_ARTIFACT_ID, "6.0.0.Final")).build(fpRepo);
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5"));
        artifact.setVersion("5.2.0.Final");
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        artifact.setVersion("5.1.0.Alpha1");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5#5.1.0.Alpha1"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());

        artifact.setVersion("4.1.0.Beta2");
        path = resolver.resolve(FeaturePackLocation.fromString("producer1@" + MavenUniverseFactory.ID + '/' + universeArt.getCoordsAsString() + ":5#4.1.0.Beta2"));
        Assert.assertEquals(artifact.getArtifactFileName(), path.getFileName().toString());
    }
}
