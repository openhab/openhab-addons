/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.concurrent.TimeUnit;

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
    private static final String CFG_DEBUGGER_ENABLED = "debuggerEnabled";
    private static final String CFG_DEBUGGER_PORT = "debuggerPort";
    private static final String CFG_LOCK_ACQUISITION_TIMEOUT = "lockAcquisitionTimeout";

    private static final int INJECTION_ENABLED_FOR_SCRIPT_MODULES_ONLY = 1;
    private static final int INJECTION_ENABLED_FOR_SCRIPT_MODULES_AND_TRANSFORMATIONS = 2;
    private static final int INJECTION_ENABLED_FOR_ALL_SCRIPTS = 3;

    private static final int DEBUGGER_PORT_DEFAULT = 9229;

    /** The default lock acquisition timeout in seconds */
    private static final long LOCK_ACQUISITION_TIMEOUT_DEFAULT = 5L;

    private int injectionEnabled = INJECTION_ENABLED_FOR_ALL_SCRIPTS;
    private boolean injectionCachingEnabled = true;
    private boolean scriptConditionWrapperEnabled = false;
    private boolean eventConversionEnabled = true;
    private boolean dependencyTrackingEnabled = true;
    private boolean debuggerEnabled = false;
    private int debuggerPort = DEBUGGER_PORT_DEFAULT;
    private long lockAcquisitionTimeout = TimeUnit.SECONDS.toMillis(LOCK_ACQUISITION_TIMEOUT_DEFAULT);

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
        boolean oldDebuggerEnabled = debuggerEnabled;
        int oldDebuggerPort = debuggerPort;
        long oldLockAcquisitionTimeout = lockAcquisitionTimeout;

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
        if (oldDebuggerEnabled != debuggerEnabled) {
            logger.warn("{} debugger for JavaScript Scripting. Restart openHAB to apply this change.",
                    debuggerEnabled ? "Enabled" : "Disabled");
        } else if (oldDebuggerPort != debuggerPort) {
            logger.warn("Reconfigured debugger for JavaScript Scripting. Restart openHAB to apply this change.");
        }
        if (oldLockAcquisitionTimeout != lockAcquisitionTimeout) {
            logger.warn(
                    "JavaScript Scripting lock acquisition timeout changed from {} to {} milliseconds. Rules created with JavaScript scripts might need to be reloaded for the changes to apply.",
                    oldLockAcquisitionTimeout, lockAcquisitionTimeout);
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
                INJECTION_ENABLED_FOR_ALL_SCRIPTS);
        injectionCachingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_INJECTION_CACHING_ENABLED), Boolean.class,
                true);
        scriptConditionWrapperEnabled = ConfigParser.valueAsOrElse(config.get(CFG_SCRIPT_CONDITION_WRAPPER_ENABLED),
                Boolean.class, false);
        eventConversionEnabled = ConfigParser.valueAsOrElse(config.get(CFG_EVENT_CONVERSION_ENABLED), Boolean.class,
                true);
        dependencyTrackingEnabled = ConfigParser.valueAsOrElse(config.get(CFG_DEPENDENCY_TRACKING_ENABLED),
                Boolean.class, true);
        debuggerEnabled = ConfigParser.valueAsOrElse(config.get(CFG_DEBUGGER_ENABLED), Boolean.class, false);
        debuggerPort = ConfigParser.valueAsOrElse(config.get(CFG_DEBUGGER_PORT), Integer.class, DEBUGGER_PORT_DEFAULT);
        lockAcquisitionTimeout = TimeUnit.SECONDS.toMillis(ConfigParser
                .valueAsOrElse(config.get(CFG_LOCK_ACQUISITION_TIMEOUT), Long.class, LOCK_ACQUISITION_TIMEOUT_DEFAULT));
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

    public boolean isDebuggerEnabled() {
        return debuggerEnabled;
    }

    public int getDebuggerPort() {
        return debuggerPort;
    }

    /**
     * @return The log acquisition timeout in milliseconds.
     */
    public long getLockAcquisitionTimeout() {
        return lockAcquisitionTimeout;
    }
}
