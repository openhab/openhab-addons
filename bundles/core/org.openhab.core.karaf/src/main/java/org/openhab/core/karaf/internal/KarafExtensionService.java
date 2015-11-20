/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.karaf.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.eclipse.smarthome.core.extension.Extension;
import org.eclipse.smarthome.core.extension.ExtensionService;
import org.eclipse.smarthome.core.extension.ExtensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is an implementation of an ESH {@link ExtensionService} using the Karaf
 * features service. This exposes all openHAB addons through the rest api and allows
 * UIs to dynamically install and uninstall them.
 *
 * @author Kai Kreuzer
 *
 */
public class KarafExtensionService implements ExtensionService {

    private final Logger logger = LoggerFactory.getLogger(FeatureInstaller.class);

    private FeaturesService featuresService;

    protected void setFeaturesService(FeaturesService featuresService) {
        this.featuresService = featuresService;
    }

    protected void unsetFeaturesService(FeaturesService featureService) {
        this.featuresService = null;
    }

    @Override
    public List<Extension> getExtensions(Locale locale) {
        List<Extension> extensions = new LinkedList<>();
        try {
            for (Feature feature : featuresService.listFeatures()) {
                if (feature.getName().startsWith(FeatureInstaller.PREFIX)
                        && Arrays.asList(FeatureInstaller.addonTypes).contains(getType(feature.getName()))) {
                    Extension extension = getExtension(feature);
                    extensions.add(extension);
                }
            }
        } catch (Exception e) {
            logger.error("Exception while retrieving features: {}", e.getMessage());
            return Collections.emptyList();
        }
        return extensions;
    }

    @Override
    public Extension getExtension(String id, Locale locale) {
        Feature feature;
        try {
            feature = featuresService.getFeature(FeatureInstaller.PREFIX + id);
            return getExtension(feature);
        } catch (Exception e) {
            logger.error("Exception while querying feature '{}'", id);
            return null;
        }
    }

    private Extension getExtension(Feature feature) {
        String extId = getType(feature.getName()) + "-" + getName(feature.getName());
        String type = getType(feature.getName());
        String label = feature.getDescription();
        String version = feature.getVersion();
        boolean installed = featuresService.isInstalled(feature);
        return new Extension(extId, type, label, version, installed);
    }

    @Override
    public List<ExtensionType> getTypes(Locale locale) {
        List<ExtensionType> typeList = new ArrayList<>(6);
        typeList.add(new ExtensionType("binding", "Bindings"));
        typeList.add(new ExtensionType("ui", "User Interface"));
        typeList.add(new ExtensionType("persistence", "Persistence Service"));
        typeList.add(new ExtensionType("action", "Actions"));
        typeList.add(new ExtensionType("transformation", "Transformations"));
        typeList.add(new ExtensionType("misc", "Misc"));
        return typeList;
    }

    @Override
    public void install(String id) {
        try {
            featuresService.installFeature(FeatureInstaller.PREFIX + id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uninstall(String id) {
        try {
            featuresService.uninstallFeature(FeatureInstaller.PREFIX + id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getType(String name) {
        if (name.startsWith(FeatureInstaller.PREFIX)) {
            name = name.substring(FeatureInstaller.PREFIX.length());
            return StringUtils.substringBefore(name, "-");
        }
        return "";
    }

    private String getName(String name) {
        if (name.startsWith(FeatureInstaller.PREFIX)) {
            name = name.substring(FeatureInstaller.PREFIX.length());
            return StringUtils.substringAfter(name, "-");
        }
        return name;
    }

}
