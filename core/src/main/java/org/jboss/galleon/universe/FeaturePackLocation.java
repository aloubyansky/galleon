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

package org.jboss.galleon.universe;

import org.jboss.galleon.ProvisioningDescriptionException;

/**
 * Complete feature-pack location incorporates two things: the feature-pack
 * identity and its origin.
 *
 * The identity is used to check whether the feature-pack is present in
 * the installation, check version compatibility, etc.
 *
 * The origin is used to obtain the feature-pack and later after it has been
 * installed to check for version updates.
 *
 * The string format for the complete location is producer[@universe]:channel[/frequency]#build
 *
 * Producer may represent a product or a project.
 *
 * Universe is a set of producers.
 *
 * Channel represents a stream of backward compatible version updates.
 *
 * Frequency is an optional classifier for feature-pack builds that are
 * streamed through the channel, e.g. DR, Alpha, Beta, CR, Final, etc. It is
 * basically the channel's feature-pack build filter.
 *
 * Build is an ID or version of the feature-pack which must be unique in the scope of the channel.
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackLocation {

    public class FPID {

        private final int hash;

        private FPID() {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + getChannel().hashCode();
            hash = prime * hash + (build == null ? 0 : build.hashCode());
            this.hash = hash;
        }

        public ChannelSpec getChannel() {
            return FeaturePackLocation.this.getChannel();
        }

        public String getBuild() {
            return build;
        }

        public FeaturePackLocation getLocation() {
            return FeaturePackLocation.this;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FPID other = (FPID) obj;
            Object thisField = getChannel();
            Object otherField = other.getChannel();
            if (thisField == null) {
                if (otherField != null)
                    return false;
            } else if (!thisField.equals(otherField))
                return false;
            thisField = getBuild();
            otherField = other.getBuild();
            if (thisField == null) {
                if (otherField != null)
                    return false;
            } else if (!thisField.equals(otherField))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return FeaturePackLocation.toString(universeSrc, producer, channel, null, build);
        }
    }

    public class ChannelSpec {

        private final int hash;

        private ChannelSpec() {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + (channel == null ? 0 : channel.hashCode());
            hash = prime * hash + producer.hashCode();
            hash = prime * hash + universeSrc.hashCode();
            this.hash = hash;
        }

        public UniverseSpec getUniverseSource() {
            return universeSrc;
        }

        public String getProducer() {
            return producer;
        }

        public String getChannel() {
            return channel;
        }

        public FeaturePackLocation getLocation() {
            return FeaturePackLocation.this;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ChannelSpec other = (ChannelSpec) obj;
            Object otherField = other.getChannel();
            if (channel == null) {
                if (otherField != null)
                    return false;
            } else if (!channel.equals(otherField))
                return false;
            otherField = other.getProducer();
            if (producer == null) {
                if (otherField != null)
                    return false;
            } else if (!producer.equals(otherField))
                return false;
            otherField = other.getUniverseSource();
            if (universeSrc == null) {
                if (otherField != null)
                    return false;
            } else if (!universeSrc.equals(otherField))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return FeaturePackLocation.toString(universeSrc, producer, channel, null, null);
        }
    }

    /**
     * Creates feature-pack source from its string representation.
     *
     * @param str  string representation of a feature-pack location
     * @return  feature-pack source
     * @throws ProvisioningDescriptionException  in case the string is not following the syntax
     */
    public static FeaturePackLocation fromString(String str) {
        if(str == null) {
            throw new IllegalArgumentException("str is null");
        }

        int buildSep = str.lastIndexOf('#');
        if(buildSep < 0) {
            buildSep = str.length();
        }
        int universeEnd = buildSep;
        int channelNameEnd = buildSep;
        loop: while(universeEnd > 0) {
            switch(str.charAt(--universeEnd)) {
                case '/':
                    channelNameEnd = universeEnd;
                    break;
                case ':':
                    break loop;
            }
        }
        if(universeEnd <= 0) {
            throw unexpectedFormat(str);
        }
        int producerEnd = 0;
        while(producerEnd < universeEnd) {
            if(str.charAt(producerEnd) == '@') {
                break;
            }
            ++producerEnd;
        }
        if(producerEnd == 0) {
            throw unexpectedFormat(str);
        }
        return new FeaturePackLocation(
                producerEnd == universeEnd ? null : UniverseSpec.fromString(str.substring(producerEnd + 1, universeEnd)),
                str.substring(0, producerEnd),
                str.substring(universeEnd + 1, channelNameEnd),
                channelNameEnd == buildSep ? null : str.substring(channelNameEnd + 1, buildSep),
                buildSep == str.length() ? null : str.substring(buildSep + 1)
                );
    }

    private static IllegalArgumentException unexpectedFormat(String str) {
        return new IllegalArgumentException(str + " does not follow format producer[@factory[/location]]:channel[/frequency]#build");
    }

    private static String toString(UniverseSpec universeSrc, String producer, String channel, String frequency, String build) {
        final StringBuilder buf = new StringBuilder();
        buf.append(producer).append('@').append(universeSrc).append(':').append(channel);
        if(frequency != null) {
            buf.append('/').append(frequency);
        }
        if(build != null) {
            buf.append('#').append(build);
        }
        return buf.toString();
    }

    private final UniverseSpec universeSrc;
    private final String producer;
    private final String channel;
    private final String frequency;
    private final String build;

    private ChannelSpec channelSrc;
    private FPID fpid;

    private final int hash;

    public FeaturePackLocation(UniverseSpec universeSrc, String producer, String channelName, String frequency,
            String build) {
        this.universeSrc = universeSrc;
        this.producer = producer;
        this.channel = channelName;
        this.frequency = frequency;
        this.build = build;

        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((build == null) ? 0 : build.hashCode());
        hash = prime * hash + ((channel == null) ? 0 : channel.hashCode());
        hash = prime * hash + ((frequency == null) ? 0 : frequency.hashCode());
        hash = prime * hash + producer.hashCode();
        hash = prime * hash + universeSrc.hashCode();
        this.hash = hash;
    }

    public UniverseSpec getUniverse() {
        return universeSrc;
    }

    public String getProducer() {
        return producer;
    }

    public String getChannelName() {
        return channel;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getBuild() {
        return build;
    }

    public ChannelSpec getChannel() {
        return channelSrc == null ? channelSrc = new ChannelSpec() : channelSrc;
    }

    public FPID getFPID() {
        return fpid == null ? fpid = new FPID() : fpid;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeaturePackLocation other = (FeaturePackLocation) obj;
        if (build == null) {
            if (other.build != null)
                return false;
        } else if (!build.equals(other.build))
            return false;
        if (channel == null) {
            if (other.channel != null)
                return false;
        } else if (!channel.equals(other.channel))
            return false;
        if (frequency == null) {
            if (other.frequency != null)
                return false;
        } else if (!frequency.equals(other.frequency))
            return false;
        if (producer == null) {
            if (other.producer != null)
                return false;
        } else if (!producer.equals(other.producer))
            return false;
        if (universeSrc == null) {
            if (other.universeSrc != null)
                return false;
        } else if (!universeSrc.equals(other.universeSrc))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return toString(universeSrc, producer, channel, frequency, build);
    }
}
