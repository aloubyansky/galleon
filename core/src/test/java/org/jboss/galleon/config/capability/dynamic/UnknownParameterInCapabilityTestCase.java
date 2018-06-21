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
package org.jboss.galleon.config.capability.dynamic;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedSpecId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnknownParameterInCapabilityTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .providesCapability("cap.a")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .requiresCapability("cap.$a")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .build())
            .addConfig(ConfigModel.builder()
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "a1"))
                    .build())
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected void pmSuccess() {
        Assert.fail("There is no cap.a provider");
    }

    @Override
    protected void pmFailure(Throwable e) {
        Assert.assertEquals("Failed to build config", e.getMessage());
        e = e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals("Failed to resolve capability cap.$a for {org.jboss.pm.test:fp1@universe.factory.galleon1:1}specB:b=b1", e.getMessage());
        e = e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals(Errors.unknownFeatureParameter(new ResolvedSpecId(FP_GAV.getChannel(), "specB"), "a"), e.getMessage());
    }
}
