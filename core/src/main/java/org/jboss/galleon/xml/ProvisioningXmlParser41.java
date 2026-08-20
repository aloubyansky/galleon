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
package org.jboss.galleon.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 * Parser for provisioning XML schema version 4.1.
 * Adds support for the plugin-versions element.
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningXmlParser41 implements PlugableXmlParser<ProvisioningConfig.Builder> {

    public static final String NAMESPACE = "urn:jboss:galleon:provisioning:4.1";
    public static final QName ROOT = new QName(NAMESPACE, Element.INSTALLATION.getLocalName());

    enum Element implements XmlNameProvider {

        CONFIG("config"),
        DEFAULT_CONFIGS("default-configs"),
        EXCLUDE("exclude"),
        FEATURE_PACK("feature-pack"),
        INCLUDE("include"),
        INSTALLATION("installation"),
        OPTION("option"),
        OPTIONS("options"),
        ORIGIN("origin"),
        PACKAGES("packages"),
        PATCHES("patches"),
        PATCH("patch"),
        PLUGIN("plugin"),
        PLUGIN_VERSIONS("plugin-versions"),
        TRANSITIVE("transitive"),
        UNIVERSES("universes"),
        UNIVERSE("universe"),

        // default unknown element
        UNKNOWN(null);

        private static final Map<String, Element> elementsByLocal;

        static {
            elementsByLocal = new HashMap<>(18);
            elementsByLocal.put(CONFIG.name, CONFIG);
            elementsByLocal.put(DEFAULT_CONFIGS.name, DEFAULT_CONFIGS);
            elementsByLocal.put(EXCLUDE.name, EXCLUDE);
            elementsByLocal.put(FEATURE_PACK.name, FEATURE_PACK);
            elementsByLocal.put(INCLUDE.name, INCLUDE);
            elementsByLocal.put(INSTALLATION.name, INSTALLATION);
            elementsByLocal.put(OPTION.name, OPTION);
            elementsByLocal.put(OPTIONS.name, OPTIONS);
            elementsByLocal.put(ORIGIN.name, ORIGIN);
            elementsByLocal.put(PACKAGES.name, PACKAGES);
            elementsByLocal.put(PATCHES.name, PATCHES);
            elementsByLocal.put(PATCH.name, PATCH);
            elementsByLocal.put(PLUGIN.name, PLUGIN);
            elementsByLocal.put(PLUGIN_VERSIONS.name, PLUGIN_VERSIONS);
            elementsByLocal.put(TRANSITIVE.name, TRANSITIVE);
            elementsByLocal.put(UNIVERSE.name, UNIVERSE);
            elementsByLocal.put(UNIVERSES.name, UNIVERSES);
            elementsByLocal.put(null, UNKNOWN);
        }

        static Element of(String localName) {
            final Element element = elementsByLocal.get(localName);
            return element == null ? UNKNOWN : element;
        }

        private final String name;
        private final String namespace = NAMESPACE;

        Element(final String name) {
            this.name = name;
        }

        @Override
        public String getLocalName() {
            return name;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public QName getRoot() {
        return ROOT;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, ProvisioningConfig.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case UNIVERSES:
                            ProvisioningXmlParser40.readUniverses(reader, builder);
                            break;
                        case FEATURE_PACK:
                            ProvisioningXmlParser40.readFeaturePackDep(reader, builder);
                            break;
                        case DEFAULT_CONFIGS:
                            ProvisioningXmlParser40.parseDefaultConfigs(reader, builder);
                            break;
                        case CONFIG:
                            final ConfigModel.Builder config = ConfigModel.builder();
                            ConfigXml.readConfig(reader, config);
                            try {
                                builder.addConfig(config.build());
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to parse config element", reader.getLocation(), e);
                            }
                            break;
                        case TRANSITIVE:
                            readTransitive(reader, builder);
                            break;
                        case OPTIONS:
                            ProvisioningXmlParser40.readOptions(reader, builder);
                            break;
                        case PLUGIN_VERSIONS:
                            readPluginVersions(reader, builder);
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static void readTransitive(XMLExtendedStreamReader reader, ProvisioningConfig.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case FEATURE_PACK:
                            ProvisioningXmlParser40.readTransitiveFeaturePackDep(reader, builder);
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static void readPluginVersions(XMLExtendedStreamReader reader, ProvisioningConfig.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case PLUGIN:
                            readPluginVersion(reader, builder);
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static void readPluginVersion(XMLExtendedStreamReader reader, ProvisioningConfig.Builder builder) throws XMLStreamException {
        String location = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final ProvisioningXmlParser40.Attribute attribute = ProvisioningXmlParser40.Attribute.of(reader.getAttributeName(i).getLocalPart());
            switch (attribute) {
                case LOCATION:
                    location = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (location == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(ProvisioningXmlParser40.Attribute.LOCATION));
        }
        ParsingUtils.parseNoContent(reader);
        builder.addPluginVersion(location);
    }
}
