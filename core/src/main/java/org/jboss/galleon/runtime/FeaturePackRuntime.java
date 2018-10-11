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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.layout.FeaturePackLayout;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.FeaturePack;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeaturePackRuntime extends FeaturePackLayout implements FeaturePack<PackageRuntime> {

    private final Map<String, PackageRuntime> packages;
    private final Map<String, ResolvedFeatureSpec> featureSpecs;

    FeaturePackRuntime(FeaturePackRuntimeBuilder builder) throws ProvisioningException {
        super(builder.producer.getLocation().getFPID(), builder.getDir(), builder.getType());
        this.spec = builder.getSpec();
        this.featureSpecs = builder.featureSpecs;

        final Map<String, PackageRuntime> tmpPackages = new LinkedHashMap<>(builder.pkgOrder.size());
        int requiredTotal = 0;
        int referencedExcludedTotal = 0;
        int referencedIncludedTotal = 0;
        int includedTotal = 0;
        Set<Integer> visited = null;

        int i = builder.pkgOrder.size();
        List<PackageRuntime> pkgs = new ArrayList<>(i);
        while(--i >= 0) {
            final String pkgName = builder.pkgOrder.get(i);
            final PackageRuntime.Builder pkgBuilder = builder.pkgBuilders.get(pkgName);
            System.out.println(" - " + pkgName + " ");
            boolean include = true;
            switch(pkgBuilder.status) {
                case PackageRuntime.PKG_REFERENCED:
                    if(visited == null) {
                        visited = new HashSet<>();
                    }
                    final boolean required = pkgBuilder.isIncluded(visited);
                    visited.clear();
                    if(required) {
                        System.out.println("    INCLUDED");
                        pkgBuilder.include();
                        ++referencedIncludedTotal;
                    } else {
                        ++referencedExcludedTotal;
                        include = false;
                        System.out.println("    EXCLUDED");
                    }
                    break;
                case PackageRuntime.PKG_REQUIRED:
                    System.out.println("    required");
                    ++requiredTotal;
                    break;
                case PackageRuntime.PKG_INCLUDED:
                    System.out.println("    included");
                    ++includedTotal;
                    break;
                default:
                    throw new IllegalStateException("Unexpected status " + pkgBuilder.status);
            }
            if(include) {
                pkgs.add(pkgBuilder.build(this));
                //tmpPackages.put(pkgName, pkgBuilder.build(this));
            }
        }
        /*
        for(String pkgName : builder.pkgOrder) {
            final PackageRuntime.Builder pkgBuilder = builder.pkgBuilders.get(pkgName);
            System.out.println(" - " + pkgName + " ");
            boolean include = true;
            switch(pkgBuilder.status) {
                case PackageRuntime.PKG_REFERENCED:
                    //System.out.println("REFERENCED");
                    if(visited == null) {
                        visited = new LinkedHashSet<>();
                    }
                    final boolean required = pkgBuilder.isIncluded(visited);
                    visited.clear();
                    if(required) {
                        System.out.println("    INCLUDED");
                        pkgBuilder.include();
                        ++referencedIncludedTotal;
                    } else {
                        ++referencedExcludedTotal;
                        include = false;
                        System.out.println("    EXCLUDED");
                    }
                    break;
                case PackageRuntime.PKG_REQUIRED:
                    System.out.println("    required");
                    ++requiredTotal;
                    break;
                case PackageRuntime.PKG_INCLUDED:
                    System.out.println("    included");
                    ++includedTotal;
                    break;
                default:
                    throw new IllegalStateException("Unexpected status " + pkgBuilder.status);
            }
            if(include) {
                tmpPackages.put(pkgName, pkgBuilder.build(this));
            }
        }
        */
        i = pkgs.size();
        while(--i >= 0) {
            final PackageRuntime pkg = pkgs.get(i);
            tmpPackages.put(pkg.getName(), pkg);
        }

        System.out.println(getFPID() + " packages total: " + builder.pkgOrder.size());
        System.out.println("  required " + requiredTotal);
        System.out.println("  included " + includedTotal);
        System.out.println("  referenced included " + referencedIncludedTotal);
        System.out.println("  referenced excluded " + referencedExcludedTotal);

        packages = Collections.unmodifiableMap(tmpPackages);
    }

    @Override
    public boolean hasPackages() {
        return !packages.isEmpty();
    }

    @Override
    public boolean containsPackage(String name) {
        return packages.containsKey(name);
    }

    @Override
    public Set<String> getPackageNames() {
        return packages.keySet();
    }

    @Override
    public Collection<PackageRuntime> getPackages() {
        return packages.values();
    }

    @Override
    public PackageRuntime getPackage(String name) {
        return packages.get(name);
    }

    public Set<String> getFeatureSpecNames() {
        return featureSpecs.keySet();
    }

    public Collection<ResolvedFeatureSpec> getFeatureSpecs() {
        return featureSpecs.values();
    }

    public FeatureSpec getFeatureSpec(String name) throws ProvisioningException {
        if (featureSpecs.containsKey(name)) {
            return featureSpecs.get(name).xmlSpec;
        }
        return loadFeatureSpec(name);
    }

    public ResolvedFeatureSpec getResolvedFeatureSpec(String name) throws ProvisioningDescriptionException {
        return featureSpecs.get(name);
    }
}
