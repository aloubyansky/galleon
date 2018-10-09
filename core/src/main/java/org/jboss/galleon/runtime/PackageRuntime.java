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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.galleon.Constants;
import org.jboss.galleon.ProvisioningDescriptionException;
import org.jboss.galleon.spec.PackageSpec;
import org.jboss.galleon.state.ProvisionedPackage;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class PackageRuntime implements ProvisionedPackage {

    static final int PKG_REFERENCED = 0;
    static final int PKG_REQUIRED = 1;
    static final int PKG_INCLUDED = 2;

    static class Builder {
        final Path dir;
        final PackageSpec spec;
        final int id;
        boolean parentIncluded;
        private List<PackageRuntime.Builder> optionalDeps = Collections.emptyList();
        private List<PackageRuntime.Builder> requiredDeps = Collections.emptyList();
        int status = PKG_REFERENCED;
        boolean skip;

        private Builder(PackageSpec spec, Path dir, int id) {
            this.dir = dir;
            this.spec = spec;
            this.id = id;
        }

        void addPackageRef(PackageRuntime.Builder dep, boolean optional ) {
            if(status == PKG_REFERENCED) {
                if (optional) {
                    optionalDeps = CollectionUtils.add(optionalDeps, dep);
                } else {
                    requiredDeps = CollectionUtils.add(requiredDeps, dep);
                }
            } else if(!dep.parentIncluded) {
                dep.parentIncluded = true;
            }
            System.out.println("REFERENCE " + spec.getName() + " -> " + dep.spec.getName() + " optional=" + optional);
        }

        void include() {
            if(status != PKG_REFERENCED) {
                return;
            }
            status = PKG_INCLUDED;
            parentIncluded = true;
            System.out.println("REFERENCED PACKAGE INCLUDED " + spec.getName());
            if(requiredDeps.isEmpty()) {
                return;
            }
            for(PackageRuntime.Builder dep : requiredDeps) {
                dep.include();
            }
        }

        boolean isIncluded(Set<Integer> visited) {
            if(skip) {
                return false;
            }
            if(status != PKG_REFERENCED) {
                return true;
            }
            if (!parentIncluded) {
                System.out.println("    " + spec.getName() + "'s parent(s) not included");
                skip = true;
                return false;
            }
            if(optionalDeps.isEmpty() && requiredDeps.isEmpty()) {
                System.out.println("    " + spec.getName() + " has no refs");
                skip = true;
                return false;
            }
            if(!visited.add(id)) {
                System.out.println("CIRCULAR PACKAGE DEP " + visited);
                return true;
            }
            for(PackageRuntime.Builder dep : optionalDeps) {
                if(!dep.isIncluded(visited)) {
                    System.out.println("    " + dep.spec.getName() + " is not required");
                    skip = true;
                    return false;
                }
            }
            for(PackageRuntime.Builder dep : requiredDeps) {
                if(!dep.isIncluded(visited)) {
                    System.out.println("    " + dep.spec.getName() + " is not required");
                    skip = true;
                    return false;
                }
            }
            visited.remove(id);
            return true;
        }


        PackageRuntime build(FeaturePackRuntime fp) {
            return new PackageRuntime(this, fp);
        }
    }

    static Builder builder(PackageSpec spec, Path dir, int id) {
        return new Builder(spec, dir, id);
    }

    private final FeaturePackRuntime fp;
    private final PackageSpec spec;
    private final Path layoutDir;

    private PackageRuntime(Builder builder, FeaturePackRuntime fp) {
        this.fp = fp;
        this.spec = builder.spec;
        this.layoutDir = builder.dir;
    }

    public FeaturePackRuntime getFeaturePackRuntime() {
        return fp;
    }

    public PackageSpec getSpec() {
        return spec;
    }

    @Override
    public String getName() {
        return spec.getName();
    }

    /**
     * Returns a resource path for a package.
     *
     * @param path  path to the resource relative to the package resources directory
     * @return  file-system path for the resource
     * @throws ProvisioningDescriptionException  in case the feature-pack or package were not found in the layout
     */
    public Path getResource(String... path) throws ProvisioningDescriptionException {
        if(path.length == 0) {
            throw new IllegalArgumentException("Resource path is null");
        }
        if(path.length == 1) {
            return layoutDir.resolve(path[0]);
        }
        Path p = layoutDir;
        for(String name : path) {
            p = p.resolve(name);
        }
        return p;
    }

    public Path getContentDir() {
        return layoutDir.resolve(Constants.CONTENT);
    }
}
