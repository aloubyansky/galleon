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
package org.jboss.galleon.config.feature.group.origin;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class OriginInsideFeatureGroupSpecTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP1_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");
    private static final FPID FP3_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP1_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "fp1"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeatureGroup(FeatureGroup.forGroup("fg2"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg2")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("id", "1"))
                    .build())
            .getCreator()
        .newFeaturePack(FP2_GAV)
            .addDependency(FP1_GAV.getLocation())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "fp2"))
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg2")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("id", "1"))
                    .build())
            .getCreator()
        .newFeaturePack(FP3_GAV)
            .addDependency(FP2_GAV.getLocation())
            .addConfig(ConfigModel.builder()
                    .addFeatureGroup(FeatureGroup.forGroup("fg1"))
                    .build())
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP3_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP1_GAV))
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP3_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP1_GAV, "specA", "id", "1"))
                                .setConfigParam("p1", "fp1")
                                .build())
                        .build())
                .build();
    }
}
