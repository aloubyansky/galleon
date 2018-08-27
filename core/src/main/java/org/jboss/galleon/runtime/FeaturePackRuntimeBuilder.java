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
package org.jboss.galleon.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.spec.FeaturePackSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.type.ParameterTypeProvider;
import org.jboss.galleon.type.builtin.BuiltInParameterTypeProvider;
import org.jboss.galleon.universe.FeaturePackLocation.FPID;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.LayoutUtils;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.xml.ConfigXmlParser;
import org.jboss.galleon.xml.FeatureGroupXmlParser;
import org.jboss.galleon.xml.FeatureSpecXmlParser;
import org.jboss.galleon.xml.PackageXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRuntimeBuilder implements FeaturePackLayout {

    final ProducerSpec producer;
    final Path dir;
    private final int type;
    final FeaturePackSpec spec;
    Map<String, ResolvedFeatureSpec> featureSpecs = null;
    private Map<String, FeatureGroup> fgSpecs = null;
    private Map<ConfigId, ConfigModel> configs = null;

    Map<String, PackageRuntime.Builder> pkgBuilders = Collections.emptyMap();
    List<String> pkgOrder = new ArrayList<>();

    private ParameterTypeProvider featureParamTypeProvider = BuiltInParameterTypeProvider.getInstance();

    FeaturePackRuntimeBuilder(FPID fpid, FeaturePackSpec spec, Path dir, int type) {
        this.producer = fpid.getProducer();
        this.dir = dir;
        this.spec = spec;
        this.type = type;
    }

    @Override
    public FPID getFPID() {
        return producer.getLocation().getFPID();
    }

    @Override
    public FeaturePackSpec getSpec() {
        return spec;
    }

    @Override
    public Path getDir() {
        return dir;
    }

    @Override
    public int getType() {
        return type;
    }

    boolean resolvePackage(String pkgName, ProvisioningRuntimeBuilder rt) throws ProvisioningException {
        if(pkgBuilders.containsKey(pkgName)) {
            return true;
        }

        final Path pkgDir = LayoutUtils.getPackageDir(dir, pkgName, false);
        if(!Files.exists(pkgDir)) {
            return false;
        }
        final Path pkgXml = pkgDir.resolve(Constants.PACKAGE_XML);
        if(!Files.exists(pkgXml)) {
            throw new ProvisioningDescriptionException(Errors.pathDoesNotExist(pkgXml));
        }

        final PackageRuntime.Builder pkgBuilder;
        try(BufferedReader reader = Files.newBufferedReader(pkgXml)) {
            pkgBuilder = PackageRuntime.builder(PackageXmlParser.getInstance().parse(reader), pkgDir);
        } catch (IOException | XMLStreamException e) {
            throw new ProvisioningException(Errors.parseXml(pkgXml), e);
        }
        if(!pkgBuilder.spec.getName().equals(pkgName)) {
            throw new ProvisioningDescriptionException("Feature-pack " + getFPID() + " package spec name " + pkgBuilder.spec.getName() + " does not match the requested package name " + pkgName);
        }
        pkgBuilders = CollectionUtils.put(pkgBuilders, pkgName, pkgBuilder);

        if(pkgBuilder.spec.hasPackageDeps()) {
            final FeaturePackRuntimeBuilder currentOrigin = rt.setOrigin(this);
            try {
                rt.processPackageDeps(pkgBuilder.spec);
            } catch(ProvisioningException e) {
                throw new ProvisioningDescriptionException(Errors.resolvePackage(producer.getLocation().getFPID(), pkgName), e);
            } finally {
                rt.setOrigin(currentOrigin);
            }
        }

        pkgOrder.add(pkgName);
        return true;
    }

    FeatureGroup getFeatureGroupSpec(String name) throws ProvisioningException {
        if(fgSpecs != null) {
            final FeatureGroup fgSpec = fgSpecs.get(name);
            if(fgSpec != null) {
                return fgSpec;
            }
        }
        final Path specXml = dir.resolve(Constants.FEATURE_GROUPS).resolve(name + ".xml");
        if (!Files.exists(specXml)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(specXml)) {
            final FeatureGroup fgSpec = FeatureGroupXmlParser.getInstance().parse(reader);
            if(!fgSpec.getName().equals(name)) {
                throw new ProvisioningDescriptionException("Feature-pack " + getFPID() + " feature group " + fgSpec.getName() + " does not match the requested feature group name " + name);
            }
            if (fgSpecs == null) {
                fgSpecs = new HashMap<>();
            }
            fgSpecs.put(name, fgSpec);
            return fgSpec;
        } catch (Exception e) {
            throw new ProvisioningException(Errors.parseXml(specXml), e);
        }
    }

    ConfigModel getConfig(ConfigId configId) throws ProvisioningException {
        if(configs != null) {
            final ConfigModel config = configs.get(configId);
            if(config != null) {
                return config;
            }
        }
        final Path p = LayoutUtils.getConfigXml(dir, configId, false);
        if (!Files.exists(p)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(p)) {
            final ConfigModel config = ConfigXmlParser.getInstance().parse(reader);
            if (configs == null) {
                configs = new HashMap<>();
            }
            configs.put(config.getId(), config);
            return config;
        } catch (Exception e) {
            throw new ProvisioningException(Errors.parseXml(p), e);
        }
    }

    ResolvedFeatureSpec getFeatureSpec(String name) throws ProvisioningException {
        if(featureSpecs != null) {
            final ResolvedFeatureSpec resolvedSpec = featureSpecs.get(name);
            if(resolvedSpec != null) {
                return resolvedSpec;
            }
        }
        final Path specXml = dir.resolve(Constants.FEATURES).resolve(name).resolve(Constants.SPEC_XML);
        if (!Files.exists(specXml)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(specXml)) {
            final FeatureSpec xmlSpec = FeatureSpecXmlParser.getInstance().parse(reader);
            if(!xmlSpec.getName().equals(name)) {
                throw new ProvisioningDescriptionException("Feature-pack " + getFPID() + " feature spec " + xmlSpec.getName() + " does not match the requested feature spec name " + name);
            }
            final ResolvedFeatureSpec resolvedSpec = new ResolvedFeatureSpec(new ResolvedSpecId(producer, xmlSpec.getName()),
                    featureParamTypeProvider, xmlSpec);
            if (featureSpecs == null) {
                featureSpecs = new HashMap<>();
            }
            featureSpecs.put(name, resolvedSpec);
            return resolvedSpec;
        } catch (Exception e) {
            throw new ProvisioningDescriptionException(Errors.parseXml(specXml), e);
        }
    }

    FeaturePackRuntime build() throws ProvisioningException {
        return new FeaturePackRuntime(this);
    }
}