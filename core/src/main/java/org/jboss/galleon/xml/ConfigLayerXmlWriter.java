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
package org.jboss.galleon.xml;

import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.xml.ConfigLayerXml.Attribute;
import org.jboss.galleon.xml.ConfigLayerXml.Element;
import org.jboss.galleon.xml.util.ElementNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigLayerXmlWriter extends BaseXmlWriter<ConfigModel> {

    private static final ConfigLayerXmlWriter INSTANCE = new ConfigLayerXmlWriter();

    public static ConfigLayerXmlWriter getInstance() {
        return INSTANCE;
    }

    private ConfigLayerXmlWriter() {
    }

    protected ElementNode toElement(ConfigModel layer) {
        return toElement(layer, ConfigLayerXml.NAMESPACE_1_0);
    }

    protected ElementNode toElement(ConfigModel layer, String ns) {
        final ElementNode configE = addElement(null, Element.CONFIG_LAYER_SPEC.getLocalName(), ns);
        if(layer.getModel() != null) {
            addAttribute(configE, Attribute.MODEL, layer.getModel());
        }
        addAttribute(configE, Attribute.NAME, layer.getName());

        if(layer.hasLayerDeps()) {
            final ElementNode layers = addElement(configE, Element.LAYERS.getLocalName(), ns);
            for(String layerName : layer.getLayerDeps()) {
                final ElementNode layerE = addElement(layers, Element.LAYER.getLocalName(), ns);
                addAttribute(layerE, Attribute.NAME, layerName);
            }
        }

        FeatureGroupXmlWriter.addFeatureGroupDepBody(layer, configE, ns);
        return configE;
    }
}
