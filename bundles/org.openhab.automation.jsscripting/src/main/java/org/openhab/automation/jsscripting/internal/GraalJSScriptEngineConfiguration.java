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
    private static final String CFG_SCRIPT_CONDITION_WRAPPER_ENABLED = "scriptConditionWrapperEnabled";
    private static final String CFG_EVENT_CONVERSION_ENABLED = "eventConversionEnabled";
    private static final String CFG_DEPENDENCY_TRACKING_ENABLED = "dependencyTrackingEnabled";

    private static final int INJECTION_ENABLED_FOR_SCRIPT_MODULES_ONLY = 1;
    private static final int INJECTION_ENABLED_FOR_SCRIPT_MODULES_AND_TRANSFORMATIONS = 2;
    private static final int INJECTION_ENABLED_FOR_ALL_SCRIPTS = 3;

    private int injectionEnabled = INJECTION_ENABLED_FOR_ALL_SCRIPTS;
    private boolean injectionCachingEnabled = true;
    private boolean scriptConditionWrapperEnabled = false;
    private boolean eventConversionEnabled = true;
    private boolean dependencyTrackingEnabled = true;

    /**
     * Create a new configuration instance from the given parameters.
     * 
     * @param config configuration parameters to apply to JavaScript
     */
    public GraalJSScriptEngineConfiguration(Map<String, ?> config) {
        update(config);
    }

    /**
     * To be called when the configuration is modified.
     *
     * @param config configuration parameters to apply to JavaScript
     */
    void modified(Map<String, ?> config) {
        boolean oldInjectionEnabledForUiBasedScript = isInjectionEnabledForScriptModules();
        boolean oldDependencyTrackingEnabled = dependencyTrackingEnabled;
        boolean oldScriptConditionWrapperEnabled = scriptConditionWrapperEnabled;
        boolean oldEventConversionEnabled = eventConversionEnabled;

        this.update(config);

        if (oldInjectionEnabledForUiBasedScript != isInjectionEnabledForScriptModules()
                && !isInjectionEnabledForScriptModules() && isEventConversionEnabled()) {
            logger.warn(
                    "Injection disabled for UI-based scripts, but event conversion is enabled. Event conversion will not work.");
        }
        if (oldDependencyTrackingEnabled != dependencyTrackingEnabled) {
            logger.info(
                    "{} dependency tracking for JavaScript Scripting. Please resave your scripts to apply this change.",
                    dependencyTrackingEnabled ? "Enabled" : "Disabled");
        }
        if (oldScriptConditionWrapperEnabled != scriptConditionWrapperEnabled) {
            logger.info(
                    "{} script condition wrapper for JavaScript Scripting. Please resave your rules with JavaScript script conditions to apply this change.",
                    scriptConditionWrapperEnabled ? "Enabled" : "Disabled");
        }
        if (oldEventConversionEnabled != eventConversionEnabled) {
            if (eventConversionEnabled && !isInjectionEnabledForScriptModules()) {
                logger.warn(
                        "Enabled event conversion for UI-based scripts, but auto-injection is disabled. Event conversion will not work.");
            }
            if (!eventConversionEnabled) {
                logger.info(
                        "Disabled event conversion for JavaScript Scripting. Please resave your scripts to apply this change.");
            }
        }
    }

    /**
     * Update configuration
     *
     * @param config configuration parameters to apply to JavaScript
     */
    private void update(Map<String, ?> config) {
        logger.debug("JavaScript Script Engine Configuration: {}", config);

        injectionEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_ENABLED), Integer.class,
                INJECTION_ENABLED_FOR_SCRIPT_MODULES_ONLY);
        injectionCachingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_CACHING_ENABLED), Boolean.class,
                true);
        scriptConditionWrapperEnabled = ConfigParser.valueAsOrElse(config.get(CFG_SCRIPT_CONDITION_WRAPPER_ENABLED),
                Boolean.class, false);
        eventConversionEnabled = ConfigParser.valueAsOrElse(config.get(CFG_EVENT_CONVERSION_ENABLED), Boolean.class,
                true);
        dependencyTrackingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_DEPENDENCY_TRACKING_ENABLED),
                Boolean.class, true);
    }

    /**
     * Whether injection is enabled for script modules, i.e. scripts executed by an implementation of
     * {@link org.openhab.core.automation.module.script.internal.handler.AbstractScriptModuleHandler}.
     * 
     * @return whether injection is enabled for script modules
     */
    public boolean isInjectionEnabledForScriptModules() {
        return injectionEnabled >= INJECTION_ENABLED_FOR_SCRIPT_MODULES_ONLY;
    }

    /**
     * Whether injection is enabled for transformations, i.e. scripts executed by the
     * {@link org.openhab.core.automation.module.script.ScriptTransformationService}.
     * 
     * @return whether injection is enabled for transformations
     */
    public boolean isInjectionEnabledForTransformations() {
        return injectionEnabled >= INJECTION_ENABLED_FOR_SCRIPT_MODULES_AND_TRANSFORMATIONS;
    }

    /**
     * Whether injection is enabled for all scripts, i.e. script modules, transformations and script files.
     * 
     * @return whether injection is enabled for all scripts
     */
    public boolean isInjectionEnabledForAllScripts() {
        return injectionEnabled == INJECTION_ENABLED_FOR_ALL_SCRIPTS;
    }

    public boolean isInjectionCachingEnabled() {
        return injectionCachingEnabled;
    }

    /**
     * Whether the wrapper is enabled for script conditions (see
     * {@link org.openhab.core.automation.module.script.internal.handler.ScriptConditionHandler}).
     * 
     * @return whether the wrapper is enabled for script conditions
     */
    public boolean isScriptConditionWrapperEnabled() {
        return scriptConditionWrapperEnabled;
    }

    public boolean isEventConversionEnabled() {
        return eventConversionEnabled;
    }

    public boolean isDependencyTrackingEnabled() {
        return dependencyTrackingEnabled;
    }
}
