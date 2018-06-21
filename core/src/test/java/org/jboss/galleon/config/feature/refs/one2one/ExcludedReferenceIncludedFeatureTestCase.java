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
package org.jboss.galleon.config.feature.refs.one2one;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureReferenceSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.test.PmInstallFeaturePackTestBase;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExcludedReferenceIncludedFeatureTestCase extends PmInstallFeaturePackTestBase {

    private static final FPID FP_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("a", "aOne"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("b", false))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .addFeatureRef(FeatureReferenceSpec.builder("specA")
                            .setName("specA")
                            .setNillable(false)
                            .mapParam("a", "name")
                            .setInclude(true)
                            .build())
                    .build())
            .addFeatureGroup(FeatureGroup.builder("fg1")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "b")
                            .setParam("a", "a"))
                    .build())
            .addConfig(ConfigModel.builder().setName("config1")
                    .addFeatureGroup(FeatureGroup.builder("fg1")
                            .excludeFeature(FeatureId.create("specA", "name", "a"))
                            .build())
                    .build())
            .getCreator()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forLocation(FP_GAV.getLocation());
    }

    @Override
    protected void pmSuccess() {
        Assert.fail("There should be an unsatisfied reference");
    }

    @Override
    protected void pmFailure(Throwable e) {
        Assert.assertEquals("Failed to build config named config1", e.getMessage());
        e = (ProvisioningException) e.getCause();
        Assert.assertNotNull(e);
        Assert.assertEquals("{org.jboss.pm.test:fp1@universe.factory.galleon1:1}specB:name=b has unresolved dependency on {org.jboss.pm.test:fp1@universe.factory.galleon1:1}specA:name=a", e.getMessage());
    }
}
