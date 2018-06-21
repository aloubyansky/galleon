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

package org.jboss.galleon.featurepack.location.test;

import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.UniverseSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackSourceStringTestCase {

    @Test
    public void testCompleteSourceToString() throws Exception {
        Assert.assertEquals("producer@factory/location:channel/frequency#build",
                new FeaturePackLocation(
                        new UniverseSpec("factory", "location"),
                        "producer", "channel", "frequency", "build")
        .toString());
    }

    @Test
    public void testCompleteSourceFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory/location:channel/frequency#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("factory", parsedSrc.getUniverse().getFactory());
        Assert.assertEquals("location", parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedSrc.getProducer());
        Assert.assertEquals("channel", parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testSourceWithoutUniverseLocationToString() throws Exception {
        Assert.assertEquals("producer@factory:channel/frequency#build",
                new FeaturePackLocation(
                        new UniverseSpec("factory", null),
                        "producer", "channel", "frequency", "build")
        .toString());
    }

    @Test
    public void testSourceWithoutUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedSrc = FeaturePackLocation.fromString("producer@factory:channel/frequency#build");
        Assert.assertNotNull(parsedSrc);
        Assert.assertEquals("factory", parsedSrc.getUniverse().getFactory());
        Assert.assertNull(parsedSrc.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedSrc.getProducer());
        Assert.assertEquals("channel", parsedSrc.getChannelName());
        Assert.assertEquals("frequency", parsedSrc.getFrequency());
        Assert.assertEquals("build", parsedSrc.getBuild());
    }

    @Test
    public void testChannelSourceWithUniverseLocationAndFrequencyToString() throws Exception {
        final FeaturePackLocation.ChannelSpec channel = new FeaturePackLocation(
                new UniverseSpec("factory", "location"),
                "producer", "channel", "frequency", "build")
        .getChannel();
        Assert.assertEquals("producer@factory/location:channel", channel.toString());
    }

    @Test
    public void testChannelWithUniverseLocationAndFrequencyFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory/location:channel/frequency");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("location", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducer());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertEquals("frequency", parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testChannelWithoutUniverseLocationToString() throws Exception {
        final FeaturePackLocation.ChannelSpec channel = new FeaturePackLocation(
                new UniverseSpec("factory", null),
                "producer", "channel", "frequency", "build")
        .getChannel();
        Assert.assertEquals("producer@factory:channel", channel.toString());
    }

    @Test
    public void testChannelWithoutUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory:channel/frequency");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertNull(parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducer());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertEquals("frequency", parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testChannelWithoutFrequencyToString() throws Exception {
        final FeaturePackLocation.ChannelSpec channel = new FeaturePackLocation(
                new UniverseSpec("factory", "location"),
                "producer", "channel", null, "build")
        .getChannel();
        Assert.assertEquals("producer@factory/location:channel", channel.toString());
    }

    @Test
    public void testChannelWithoutFrequencyFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory/location:channel");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("location", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducer());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertNull(parsedCoords.getBuild());
    }

    @Test
    public void testFeaturePackIdWithUniverseLocationToString() throws Exception {
        final FeaturePackLocation.FPID fpid = new FeaturePackLocation(
                new UniverseSpec("factory", "location"),
                "producer", "channel", "frequency", "build").getFPID();
        Assert.assertEquals("producer@factory/location:channel#build", fpid.toString());
    }

    @Test
    public void testFeaturePackIdWithUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory/location:channel#build");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("location", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducer());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertEquals("build", parsedCoords.getBuild());
    }

    @Test
    public void testFeaturePackIdWithoutUniverseLocationToString() throws Exception {
        final FeaturePackLocation.FPID fpid = new FeaturePackLocation(
                new UniverseSpec("factory", null), "producer", "channel", "frequency", "build").getFPID();
        Assert.assertEquals("producer@factory:channel#build", fpid.toString());
    }

    @Test
    public void testFeaturePackIdWithoutUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("producer@factory:channel#build");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("factory", parsedCoords.getUniverse().getFactory());
        Assert.assertNull(parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("producer", parsedCoords.getProducer());
        Assert.assertEquals("channel", parsedCoords.getChannelName());
        Assert.assertNull(parsedCoords.getFrequency());
        Assert.assertEquals("build", parsedCoords.getBuild());
    }

    @Test
    public void testMavenUniverseLocationFromString() throws Exception {
        final FeaturePackLocation parsedCoords = FeaturePackLocation.fromString("wildfly@maven/org.jboss.galleon-universe:jboss-galleon-universe:1.0.0.Final:14/beta#14.0.0.Beta1-SNAPSHOT");
        Assert.assertNotNull(parsedCoords);
        Assert.assertEquals("maven", parsedCoords.getUniverse().getFactory());
        Assert.assertEquals("org.jboss.galleon-universe:jboss-galleon-universe:1.0.0.Final", parsedCoords.getUniverse().getLocation());
        Assert.assertEquals("wildfly", parsedCoords.getProducer());
        Assert.assertEquals("14", parsedCoords.getChannelName());
        Assert.assertEquals("beta", parsedCoords.getFrequency());
        Assert.assertEquals("14.0.0.Beta1-SNAPSHOT", parsedCoords.getBuild());
    }
}
