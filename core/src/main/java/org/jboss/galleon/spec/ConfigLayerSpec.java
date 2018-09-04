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

package org.jboss.galleon.spec;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroupBuilderSupport;
import org.jboss.galleon.config.FeatureGroupSupport;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigLayerSpec extends FeatureGroupSupport {

    public static class Builder extends FeatureGroupBuilderSupport<Builder> {

        private Set<String> layerDeps = Collections.emptySet();

        protected Builder() {
            super();
        }

        protected Builder(String layerName) {
            super(layerName);
        }

        protected Builder(String layerName, boolean inheritFeatures) {
            super(layerName);
            this.inheritFeatures = inheritFeatures;
        }

        public Builder addLayerDep(String layerName) {
            layerDeps = CollectionUtils.addLinked(layerDeps, layerName);
            return this;
        }

        public ConfigLayerSpec build() throws ProvisioningDescriptionException {
            return new ConfigLayerSpec(this);
        }
    }

    public static Builder builder() {
        return new Builder(null, true);
    }

    public static Builder builder(boolean inheritFeatures) {
        return new Builder(null, inheritFeatures);
    }

    public static Builder builder(String layerName) {
        return builder(layerName, true);
    }

    public static Builder builder(String layerName, boolean inheritFeatures) {
        return new Builder(layerName, inheritFeatures);
    }

    private final Set<String> layerDeps;

    protected ConfigLayerSpec(Builder builder) throws ProvisioningDescriptionException {
        super(builder);
        layerDeps = CollectionUtils.unmodifiable(builder.layerDeps);
    }

    public boolean hasLayerDeps() {
        return !layerDeps.isEmpty();
    }

    public Set<String> getLayerDeps() {
        return layerDeps;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((layerDeps == null) ? 0 : layerDeps.hashCode());
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
        ConfigLayerSpec other = (ConfigLayerSpec) obj;
        if (layerDeps == null) {
            if (other.layerDeps != null)
                return false;
        } else if (!layerDeps.equals(other.layerDeps))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[name=").append(getName());
        if(layerDeps.isEmpty()) {
            buf.append(" layer-deps=");
            StringUtils.append(buf, layerDeps);
        }
        if(!isInheritFeatures()) {
            buf.append(" inherit-features=false");
        }
        if(!hasIncludedSpecs()) {
            buf.append(" includedSpecs=");
            StringUtils.append(buf, getIncludedSpecs());
        }
        if(!hasExcludedSpecs()) {
            buf.append(" exlcudedSpecs=");
            StringUtils.append(buf, getExcludedSpecs());
        }
        if(!hasIncludedFeatures()) {
            buf.append(" includedFeatures=[");
            final Iterator<Map.Entry<FeatureId, FeatureConfig>> i = getIncludedFeatures().entrySet().iterator();
            Map.Entry<FeatureId, FeatureConfig> entry = i.next();
            buf.append(entry.getKey());
            if(entry.getValue() != null) {
                buf.append("->").append(entry.getValue());
            }
            while(i.hasNext()) {
                entry = i.next();
                buf.append(';').append(entry.getKey());
                if(entry.getValue() != null) {
                    buf.append("->").append(entry.getValue());
                }
            }
            buf.append(']');
        }
        if(!hasExcludedFeatures()) {
            buf.append(" exlcudedFeatures=");
            StringUtils.append(buf, getExcludedFeatures().keySet());
        }

        if(!items.isEmpty()) {
            buf.append(" items=");
            StringUtils.append(buf, items);
        }
        return buf.append(']').toString();
    }
}
