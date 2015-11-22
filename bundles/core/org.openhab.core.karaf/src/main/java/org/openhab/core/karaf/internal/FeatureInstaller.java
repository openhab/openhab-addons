/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.karaf.internal;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.karaf.features.FeaturesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service reads addons.cfg and installs listed addons (= Karaf features).
 *
 * @author Kai Kreuzer
 */
public class FeatureInstaller {

    public static final String PREFIX = "openhab-";

    public static final String[] addonTypes = new String[] { "binding", "ui", "persistence", "action", "tts",
            "transformation", "misc" };

    private final Logger logger = LoggerFactory.getLogger(FeatureInstaller.class);

    private FeaturesService featuresService;

    protected void setFeaturesService(FeaturesService featuresService) {
        this.featuresService = featuresService;
    }

    protected void unsetFeaturesService(FeaturesService featuresService) {
        this.featuresService = null;
    }

    protected void activate(final Map<String, Object> config) {
        ExecutorService scheduler = Executors.newSingleThreadExecutor();
        scheduler.execute(new Runnable() {
            @Override
            public void run() {
                for (String type : addonTypes) {
                    Object install = config.get(type);
                    if (install instanceof String) {
                        installFeatures(type, (String) install);
                    }
                }
            }
        });
    }

    private void installFeatures(String type, String install) {
        for (String addon : install.split(",")) {
            String name = PREFIX + type + "-" + addon.trim();
            try {
                featuresService.installFeature(name);
            } catch (Exception e) {
                logger.error("Failed installing feature '{}'", name);
            }
        }
    }
}
