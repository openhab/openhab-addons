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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service reads addons.cfg and installs listed addons (= Karaf features).
 *
 * @author Kai Kreuzer
 */
public class FeatureInstaller {

    private static final String[] addonTypes = new String[] { "binding", "ui", "persistence", "action", "tts",
            "transformation", "io" };

    private final Logger logger = LoggerFactory.getLogger(FeatureInstaller.class);

    private FeaturesService featureService;

    protected void setFeaturesService(FeaturesService featureService) {
        this.featureService = featureService;
    }

    protected void unsetFeaturesService(FeaturesService featureService) {
        this.featureService = null;
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
            String name = Addon.PREFIX + type + "-" + addon.trim();
            try {
                featureService.installAddon(name);
            } catch (Exception e) {
                logger.error("Failed installing feature '{}'", name);
            }
        }
    }
}
