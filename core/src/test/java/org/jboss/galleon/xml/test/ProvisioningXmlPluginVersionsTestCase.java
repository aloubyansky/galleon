/*
 * Copyright 2016-2026 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.xml.test;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.test.util.XmlParserValidator;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.xml.ProvisioningXmlParser;
import org.jboss.galleon.xml.ProvisioningXmlWriter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for provisioning XML plugin-versions support (schema 4.1).
 */
public class ProvisioningXmlPluginVersionsTestCase {

    private static final XmlParserValidator<ProvisioningConfig> validator = new XmlParserValidator<>(
            Paths.get("src/main/resources/schema/galleon-provisioning-4_1.xsd"), ProvisioningXmlParser.getInstance());

    private static final Locale defaultLocale = Locale.getDefault();

    @BeforeClass
    public static void setLocale() {
        Locale.setDefault(Locale.US);
    }

    @AfterClass
    public static void resetLocale() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void readPluginVersions() throws Exception {
        ProvisioningConfig found = validator
                .validateAndParse("xml/provisioning/provisioning-plugin-versions.xml", null, null);
        ProvisioningConfig expected = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackLocation.fromString("fp1@maven(universe):0#0.0.1"))
                .addOption("name1", "value1")
                .addPluginVersion("org.wildfly.galleon-plugins:wildfly-galleon-plugins:jar:8.0.0.Final")
                .addPluginVersion("org.example:other-plugin:jar:2.0.0")
                .build();
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testPluginVersionsRoundTrip() throws Exception {
        ProvisioningConfig original = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackLocation.fromString("fp1@maven(universe):0#0.0.1"))
                .addOption("opt1", "val1")
                .addPluginVersion("org.wildfly.galleon-plugins:wildfly-galleon-plugins:jar:8.0.0.Final")
                .build();

        final Path tmpFile = Files.createTempFile("provisioning-test", ".xml");
        try {
            ProvisioningXmlWriter.getInstance().write(original, tmpFile);
            final String xml = new String(Files.readAllBytes(tmpFile));

            Assert.assertTrue("Written XML should contain plugin-versions element",
                    xml.contains("plugin-versions"));
            Assert.assertTrue("Written XML should use 4.1 namespace",
                    xml.contains("urn:jboss:galleon:provisioning:4.1"));
            Assert.assertFalse("Written XML should not contain 4.0 namespace references",
                    xml.contains("urn:jboss:galleon:provisioning:4.0"));

            validator.validateAndParse(tmpFile, null, null);

            ProvisioningConfig parsed = ProvisioningXmlParser.getInstance().parse(new StringReader(xml));
            Assert.assertEquals(original, parsed);
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

    @Test
    public void testExtractGroupArtifact() {
        Assert.assertEquals("org.wildfly.galleon-plugins:wildfly-galleon-plugins",
                ProvisioningConfig.extractGroupArtifact("org.wildfly.galleon-plugins:wildfly-galleon-plugins:jar:8.0.0.Final"));
        Assert.assertEquals("org.example:plugin",
                ProvisioningConfig.extractGroupArtifact("org.example:plugin:jar:1.0"));
    }

    @Test
    public void testNoPluginVersionsUses40Namespace() throws Exception {
        ProvisioningConfig config = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackLocation.fromString("fp1@maven(universe):0#0.0.1"))
                .build();

        final StringWriter strWriter = new StringWriter();
        ProvisioningXmlWriter.getInstance().write(config, strWriter);
        final String xml = strWriter.toString();

        Assert.assertTrue("Config without plugin-versions should use 4.0 namespace",
                xml.contains("urn:jboss:galleon:provisioning:4.0"));
        Assert.assertFalse("Config without plugin-versions should not contain plugin-versions element",
                xml.contains("plugin-versions"));
    }
}
