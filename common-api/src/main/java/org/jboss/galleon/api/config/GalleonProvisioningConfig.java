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
package org.jboss.galleon.api.config;


import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 * The configuration of the installation to be provisioned.
 *
 * @author Alexey Loubyansky
 */
public class GalleonProvisioningConfig extends GalleonFeaturePackDepsConfig {

    public static class Builder extends GalleonFeaturePackDepsConfigBuilder<Builder> {

        private Map<String, String> options = Collections.emptyMap();
        private Map<String, String> pluginVersions = Collections.emptyMap();

        protected Builder() {
        }

        protected Builder(GalleonProvisioningConfig original) throws ProvisioningDescriptionException {
            if(original == null) {
                return;
            }
            if(original.hasOptions()) {
                addOptions(original.getOptions());
            }
            if(original.hasPluginVersions()) {
                addPluginVersions(original.getPluginVersions());
            }
            for (GalleonFeaturePackConfig fp : original.getFeaturePackDeps()) {
                addFeaturePackDep(original.originOf(fp.getLocation().getProducer()), fp);
            }
            if (original.hasTransitiveDeps()) {
                for (GalleonFeaturePackConfig fp : original.getTransitiveDeps()) {
                    addFeaturePackDep(original.originOf(fp.getLocation().getProducer()), fp);
                }
            }
            initUniverses(original);
            initConfigs(original);
        }

        public Builder addOption(String name, String value) {
            options = CollectionUtils.put(options, name, value);
            return this;
        }

        public Builder removeOption(String name) {
            options = CollectionUtils.remove(options, name);
            return this;
        }

        public Builder clearOptions() {
            options = Collections.emptyMap();
            return this;
        }


        public Builder addOptions(Map<String, String> options) {
            this.options = CollectionUtils.putAll(this.options, options);
            return this;
        }

        public Builder addPluginVersion(String location) {
            final String ga = extractGroupArtifact(location);
            pluginVersions = CollectionUtils.put(pluginVersions, ga, location);
            return this;
        }

        public Builder addPluginVersions(Map<String, String> pluginVersions) {
            this.pluginVersions = CollectionUtils.putAll(this.pluginVersions, pluginVersions);
            return this;
        }

        public GalleonProvisioningConfig build() throws ProvisioningDescriptionException {
            return new GalleonProvisioningConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Allows to build a provisioning configuration starting from the passed in
     * initial configuration.
     *
     * @param provisioningConfig  initial state of the configuration to be built
     * @return  this builder instance
     * @throws ProvisioningDescriptionException  in case the config couldn't be built
     */
    public static Builder builder(GalleonProvisioningConfig provisioningConfig) throws ProvisioningDescriptionException {
        return new Builder(provisioningConfig);
    }

    private final Map<String, String> options;
    private final Map<String, String> pluginVersions;

    private GalleonProvisioningConfig(Builder builder) throws ProvisioningDescriptionException {
        super(builder);
        this.options = CollectionUtils.unmodifiable(builder.options);
        this.pluginVersions = CollectionUtils.unmodifiable(builder.pluginVersions);
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    public String getOption(String name) {
        return options.get(name);
    }

    public boolean hasPluginVersions() {
        return !pluginVersions.isEmpty();
    }

    public Map<String, String> getPluginVersions() {
        return pluginVersions;
    }

    public static String extractGroupArtifact(String location) {
        final int firstColon = location.indexOf(':');
        if (firstColon < 0) {
            return location;
        }
        final int secondColon = location.indexOf(':', firstColon + 1);
        if (secondColon < 0) {
            return location;
        }
        return location.substring(0, secondColon);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        result = prime * result + ((pluginVersions == null) ? 0 : pluginVersions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        GalleonProvisioningConfig other = (GalleonProvisioningConfig) obj;
        if (options == null) {
            if (other.options != null)
                return false;
        } else if (!options.equals(other.options))
            return false;
        if (pluginVersions == null) {
            if (other.pluginVersions != null)
                return false;
        } else if (!pluginVersions.equals(other.pluginVersions))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append('[');
        append(buf);
        if(!options.isEmpty()) {
            buf.append("options=");
            StringUtils.append(buf, options.entrySet());
        }
        if(!pluginVersions.isEmpty()) {
            buf.append("plugin-versions=");
            StringUtils.append(buf, pluginVersions.entrySet());
        }
        return buf.append(']').toString();
    }
}
