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
package org.openhab.automation.pythonscripting.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes Python Configuration Parameters.
 *
 * @author Holger Hees - Initial contribution
 */
@NonNullByDefault
public class PythonScriptEngineConfiguration {

    private final Logger logger = LoggerFactory.getLogger(PythonScriptEngineConfiguration.class);

    private static final String CFG_INJECTION_ENABLED = "injectionEnabled";
    private static final String CFG_HELPER_ENABLED = "helperEnabled";
    private static final String CFG_SCOPE_ENABLED = "scopeEnabled";
    private static final String CFG_DEPENDENCY_TRACKING_ENABLED = "dependencyTrackingEnabled";
    private static final String CFG_CACHING_ENABLED = "cachingEnabled";
    private static final String CFG_JYTHON_EMULATION = "jythonEmulation";

    public static final int INJECTION_DISABLED = 0;
    public static final int INJECTION_ENABLED_FOR_ALL_SCRIPTS = 1;
    public static final int INJECTION_ENABLED_FOR_NON_FILE_BASED_SCRIPTS = 2;
    private int injectionEnabled = 0;
    private boolean helperEnabled = false;
    private boolean scopeEnabled = false;
    private boolean dependencyTrackingEnabled = false;
    private boolean cachingEnabled = false;
    private boolean jythonEmulation = false;

    /**
     * Update configuration
     *
     * @param config Configuration parameters to apply to ScriptEngine
     */
    void update(Map<String, ?> config) {
        logger.trace("Python Script Engine Configuration: {}", config);

        this.scopeEnabled = ConfigParser.valueAsOrElse(config.get(CFG_SCOPE_ENABLED), Boolean.class, true);
        this.helperEnabled = ConfigParser.valueAsOrElse(config.get(CFG_HELPER_ENABLED), Boolean.class, true);
        this.injectionEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_ENABLED), Integer.class,
                INJECTION_ENABLED_FOR_NON_FILE_BASED_SCRIPTS);
        this.dependencyTrackingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_DEPENDENCY_TRACKING_ENABLED),
                Boolean.class, true);
        this.cachingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_CACHING_ENABLED), Boolean.class, true);
        this.jythonEmulation = ConfigParser.valueAsOrElse(config.get(CFG_JYTHON_EMULATION), Boolean.class, false);
    }

    public boolean isScopeEnabled() {
        return scopeEnabled;
    }

    public boolean isHelperEnabled() {
        return helperEnabled;
    }

    public boolean isInjection(int type) {
        return injectionEnabled == type;
    }

    public boolean isDependencyTrackingEnabled() {
        return dependencyTrackingEnabled;
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public boolean isJythonEmulation() {
        return jythonEmulation;
    }
}
