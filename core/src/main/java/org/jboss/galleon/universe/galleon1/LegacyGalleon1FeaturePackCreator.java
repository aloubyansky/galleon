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

package org.jboss.galleon.universe.galleon1;

import java.nio.file.Path;

import org.jboss.galleon.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.creator.UniverseFeaturePackCreator;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.universe.Universe;

/**
 *
 * @author Alexey Loubyansky
 */
public class LegacyGalleon1FeaturePackCreator implements UniverseFeaturePackCreator {

    @Override
    public String getUniverseFactoryId() {
        return LegacyGalleon1UniverseFactory.ID;
    }

    @Override
    public void install(Universe<?> universe, FeaturePackSpec spec, Path fpContentDir) throws ProvisioningException {
        final LegacyGalleon1Universe mvnUni = (LegacyGalleon1Universe) universe;
        final FPID fpid = spec.getFPID();
        final LegacyGalleon1Producer producer = mvnUni.getProducer(fpid.getProducer());
        // make sure the channel exists
        producer.getChannel(fpid.getChannelName());

        mvnUni.artifactResolver.install(LegacyGalleon1Universe.toArtifactCoords(fpid.getLocation()), fpContentDir);
    }
}
