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
package org.openhab.binding.shelly.internal.config;

import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ShellyBindingConfiguration} maps binding configuration parameters directly from binding.cfg /
 * OSGi properties. This is a plain data class — no IP resolution, no port logic.
 * Use {@link ShellyBindingRuntimeConfig} to obtain a fully resolved runtime snapshot.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingConfiguration {
    public static final String CONFIG_DEF_HTTP_USER = "defaultUserId";
    public static final String CONFIG_DEF_HTTP_PWD = "defaultPassword";
    public static final String CONFIG_LOCAL_IP = "localIP";
    public static final String CONFIG_AUTOCOIOT = "autoCoIoT";

    private String defaultUserId = SHELLY2_DEFAULT_USERID;
    private String defaultPassword = SHELLY2_DEFAULT_PASSWORD;
    /** Admin-configured local IP override; empty string means "auto-detect via NetworkAddressService". */
    private String localIP = "";
    private boolean autoCoIoT = true;

    /** No-arg constructor: all fields use defaults. */
    public ShellyBindingConfiguration() {
    }

    /**
     * Returns a new instance with properties from {@code properties} applied on top of defaults.
     * Blank-string values for string fields are ignored (default is preserved).
     */
    public static ShellyBindingConfiguration fromProperties(@Nullable Map<String, Object> properties) {
        ShellyBindingConfiguration cfg = new ShellyBindingConfiguration();
        if (properties == null) {
            return cfg;
        }
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            Object value = e.getValue();
            switch (e.getKey()) {
                case CONFIG_DEF_HTTP_USER:
                    if (value instanceof String s && !s.isBlank()) {
                        cfg.defaultUserId = s;
                    }
                    break;
                case CONFIG_DEF_HTTP_PWD:
                    if (value instanceof String s && !s.isBlank()) {
                        cfg.defaultPassword = s;
                    }
                    break;
                case CONFIG_LOCAL_IP:
                    if (value instanceof String s && !s.isBlank()) {
                        cfg.localIP = s;
                    }
                    break;
                case CONFIG_AUTOCOIOT:
                    if (value instanceof String s) {
                        cfg.autoCoIoT = "true".equalsIgnoreCase(s);
                    } else if (value instanceof Boolean b) {
                        cfg.autoCoIoT = b;
                    }
                    break;
                default:
                    break;
            }
        }
        return cfg;
    }

    /**
     * Convenience overload accepting an OSGi {@link Dictionary} (e.g. from {@code ComponentContext}).
     */
    public static ShellyBindingConfiguration fromProperties(@Nullable Dictionary<String, Object> properties) {
        if (properties == null) {
            return new ShellyBindingConfiguration();
        }
        List<String> keys = Collections.list(properties.keys());
        Map<String, Object> map = keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
        return fromProperties(map);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getDefaultUserId() {
        return defaultUserId;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public String getLocalIP() {
        return localIP;
    }

    public boolean isAutoCoIoT() {
        return autoCoIoT;
    }
}
