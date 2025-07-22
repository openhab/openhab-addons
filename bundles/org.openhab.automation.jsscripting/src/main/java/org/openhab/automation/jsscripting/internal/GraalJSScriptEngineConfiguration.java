/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.jsscripting.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes JavaScript Configuration Parameters.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GraalJSScriptEngineConfiguration {
    Logger logger = LoggerFactory.getLogger(GraalJSScriptEngineConfiguration.class);

    private static final String CFG_INJECTION_ENABLED = "injectionEnabledV2";
    private static final String CFG_INJECTION_CACHING_ENABLED = "injectionCachingEnabled";
    private static final String CFG_WRAPPER_ENABLED = "wrapperEnabled";
    private static final String CFG_DEPENDENCY_TRACKING_ENABLED = "dependencyTrackingEnabled";

    public static final int INJECTION_DISABLED = 0;
    public static final int INJECTION_ENABLED_FOR_NON_FILE_BASED_SCRIPTS = 2;
    public static final int INJECTION_ENABLED_FOR_ALL_SCRIPTS = 1;

    private int injectionEnabled = 2;
    private boolean injectionCachingEnabled = true;
    private boolean wrapperEnabled = true;
    private boolean dependencyTrackingEnabled = true;

    /**
     * Update configuration
     *
     * @param config configuration parameters to apply to JavaScript
     */
    void update(Map<String, ?> config) {
        logger.trace("JavaScript Script Engine Configuration: {}", config);

        this.injectionEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_ENABLED), Integer.class,
                INJECTION_ENABLED_FOR_NON_FILE_BASED_SCRIPTS);
        this.injectionCachingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_CACHING_ENABLED),
                Boolean.class, true);
        this.wrapperEnabled = ConfigParser.valueAsOrElse(config.get(CFG_WRAPPER_ENABLED), Boolean.class, true);
        this.dependencyTrackingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_DEPENDENCY_TRACKING_ENABLED),
                Boolean.class, true);
    }

    public boolean isInjection(int type) {
        return type == injectionEnabled;
    }

    public boolean isInjectionCachingEnabled() {
        return injectionCachingEnabled;
    }

    public boolean isWrapperEnabled() {
        return wrapperEnabled;
    }

    public boolean isDependencyTrackingEnabled() {
        return dependencyTrackingEnabled;
    }
}
