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
package org.jboss.galleon.config.capability.dynamic.collection;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
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
public class ProvidedByDefaultParamValueTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .providesCapability("cap.$col")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.builder("col").setType("[String]").setDefaultValue("[1, 2]").build())
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .requiresCapability("cap.$req")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.create("req"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("req", "1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a1"))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b2")
                            .setParam("req", "2"))
                    .build())
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forFPID(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "a", "a1")).setConfigParam("col", "[1, 2]").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "b", "b1")).setConfigParam("req", "1").build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "b", "b2")).setConfigParam("req",  "2").build())
                        .build())
                .build();
    }
}
