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
package org.jboss.galleon.featurepack.uninstall.test;

import org.jboss.galleon.universe.galleon1.LegacyGalleon1Universe;
import org.jboss.galleon.FeaturePackLocation.FPID;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.PmUninstallFeaturePackTestBase;
import org.jboss.galleon.test.util.fs.state.DirState;

/**
 *
 * @author Alexey Loubyansky
 */
public class KeepExplicitlyInstalledFpDepsOfUninstalledFpTestCase extends PmUninstallFeaturePackTestBase {

    private static final FPID FP1_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp1", "1", "1.0.0.Final");
    private static final FPID FP2_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp2", "1", "1.0.0.Final");
    private static final FPID FP3_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp3", "1", "1.0.0.Final");
    private static final FPID FP4_100_GAV = LegacyGalleon1Universe.newFPID("org.jboss.pm.test:fp4", "1", "1.0.0.Final");

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {
        creator
            .newFeaturePack(FP1_100_GAV)
                .addDependency(FP2_100_GAV.getLocation())
                .addDependency(FP4_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp1/p1.txt", "fp1 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2", true)
                    .writeContent("fp1/p2.txt", "fp1 1.0.0.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp1/p3.txt", "fp1 1.0.0.Final p3")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP2_100_GAV)
                .addDependency(FP3_100_GAV.getLocation())
                .newPackage("p1", true)
                    .writeContent("fp2/p1.txt", "fp2 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2", true)
                    .writeContent("fp2/p2.txt", "fp2 1.0.0.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp2/p3.txt", "fp2 1.0.0.Final p3")
                    .getFeaturePack()
                .getCreator()
            .newFeaturePack(FP3_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp3/p1.txt", "fp3 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2", true)
                    .writeContent("fp3/p2.txt", "fp3 1.0.0.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp3/p3.txt", "fp3 1.0.0.Final p3")
                    .getFeaturePack()
                 .getCreator()
            .newFeaturePack(FP4_100_GAV)
                .newPackage("p1", true)
                    .writeContent("fp4/p1.txt", "fp4 1.0.0.Final p1")
                    .getFeaturePack()
                .newPackage("p2", true)
                    .writeContent("fp4/p2.txt", "fp4 1.0.0.Final p2")
                    .getFeaturePack()
                .newPackage("p3")
                    .writeContent("fp4/p3.txt", "fp4 1.0.0.Final p3")
                    .getFeaturePack()
                .getCreator()
            .install();
    }

    @Override
    protected ProvisioningConfig initialState() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(FP1_100_GAV.getLocation())
                        .excludePackage("p2")
                        .includePackage("p3")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(FP3_100_GAV.getLocation())
                        .excludePackage("p2")
                        .includePackage("p3")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(FP4_100_GAV.getLocation())
                        .excludePackage("p2")
                        .includePackage("p3")
                        .build())
                .build();
    }

    @Override
    protected FPID uninstallGav() throws ProvisioningDescriptionException {
        return FP1_100_GAV;
    }

    @Override
    protected ProvisioningConfig provisionedConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(FP3_100_GAV.getLocation())
                        .excludePackage("p2")
                        .includePackage("p3")
                        .build())
                .addFeaturePackDep(FeaturePackConfig.builder(FP4_100_GAV.getLocation())
                        .excludePackage("p2")
                        .includePackage("p3")
                        .build())
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningDescriptionException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP3_100_GAV)
                        .addPackage(ProvisionedPackage.newInstance("p1"))
                        .addPackage(ProvisionedPackage.newInstance("p3"))
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(FP4_100_GAV)
                        .addPackage(ProvisionedPackage.newInstance("p1"))
                        .addPackage(ProvisionedPackage.newInstance("p3"))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp3/p1.txt", "fp3 1.0.0.Final p1")
                .addFile("fp3/p3.txt", "fp3 1.0.0.Final p3")
                .addFile("fp4/p1.txt", "fp4 1.0.0.Final p1")
                .addFile("fp4/p3.txt", "fp4 1.0.0.Final p3")
                .build();
    }
}
