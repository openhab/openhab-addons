/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.apache.karaf.features;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The service managing features repositories.
 */
public class FeaturesServiceImpl implements FeaturesService {

    private final Logger logger = LoggerFactory.getLogger(FeaturesServiceImpl.class);

    private Map<String, FeatureImpl> features;
    private Set<FeatureImpl> installedFeatures;

    public FeaturesServiceImpl() {
        this.features = new HashMap<>();
        this.installedFeatures = new HashSet<>();

        for (int i = 0; i < 20; i++) {
            FeatureImpl feature = new FeatureImpl("oh" + i, "openhab2-addon-binding-feature" + i,
                    "This is a nice feature", "2.0.0");
            features.put("openhab2-addon-binding-feature" + i, feature);
        }
        features.put("some-other-feature",
                new FeatureImpl("oh" + 20, "some-other-feature", "This is some other feature", "2.0.0"));
    }

    @Override
    public void installFeature(String name) throws Exception {
        FeatureImpl feature = features.get(name);
        if (feature != null) {
            installedFeatures.add(feature);
            logger.info("Installed feature '{}'", name);
        }
    };

    @Override
    public void installFeature(String name, String version) throws Exception {
        installFeature(name);
    };

    @Override
    public void uninstallFeature(String name) throws Exception {
        Feature feature = features.get(name);
        if (feature != null) {
            installedFeatures.remove(feature);
            logger.info("Uninstalled feature '{}'", name);
        }
    };

    @Override
    public void uninstallFeature(String name, String version) throws Exception {
        uninstallFeature(name);
    };

    @Override
    public Feature[] listFeatures() throws Exception {
        return features.values().toArray(new FeatureImpl[features.values().size()]);
    };

    @Override
    public Feature[] listInstalledFeatures() throws Exception {
        return installedFeatures.toArray(new FeatureImpl[installedFeatures.size()]);
    };

    @Override
    public boolean isInstalled(Feature f) {
        return installedFeatures.contains(f);
    };

    @Override
    public Feature getFeature(String name, String version) throws Exception {
        return getFeature(name);
    };

    @Override
    public Feature getFeature(String name) throws Exception {
        return features.get(name);
    };

}
