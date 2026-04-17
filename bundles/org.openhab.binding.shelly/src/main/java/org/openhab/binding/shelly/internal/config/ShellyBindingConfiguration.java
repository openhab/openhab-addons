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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.DEFAULT_LOCAL_PORT;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.net.NetworkAddressService;

/**
 * The {@link ShellyBindingConfiguration} class contains fields mapping binding configuration parameters.
 * Instances are immutable after construction; use {@link #fromProperties} to apply configuration overrides
 * and {@link #withHttpPort} to set the HTTP port after the OSGi HTTP service has started.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingConfiguration {
    // Binding Configuration Properties
    public static final String CONFIG_DEF_HTTP_USER = "defaultUserId";
    public static final String CONFIG_DEF_HTTP_PWD = "defaultPassword";
    public static final String CONFIG_LOCAL_IP = "localIP";
    public static final String CONFIG_AUTOCOIOT = "autoCoIoT";

    private final String defaultUserId;
    private final String defaultPassword;
    private final String localIP;
    private final int httpPort; // -1 means "use DEFAULT_LOCAL_PORT"
    private final boolean autoCoIoT;

    /** No-arg constructor: empty localIP, all other fields use defaults. */
    public ShellyBindingConfiguration() {
        this("", "admin", "admin", -1, true);
    }

    /**
     * Resolve the local IP from the NetworkAddressService; all other fields use defaults.
     * Combine with {@link #fromProperties} to apply binding.cfg overrides on top.
     */
    public ShellyBindingConfiguration(NetworkAddressService networkAddressService) {
        this(resolveLocalIP(networkAddressService), "admin", "admin", -1, true);
    }

    private ShellyBindingConfiguration(String localIP, String defaultUserId, String defaultPassword, int httpPort,
            boolean autoCoIoT) {
        this.localIP = localIP;
        this.defaultUserId = defaultUserId;
        this.defaultPassword = defaultPassword;
        this.httpPort = httpPort;
        this.autoCoIoT = autoCoIoT;
    }

    /**
     * Returns a new instance with the given property map overlaid on top of default values.
     * The {@code localIP} parameter supplies the base IP (typically from
     * {@link ShellyBindingConfiguration#ShellyBindingConfiguration(NetworkAddressService)}) so that
     * callers control whether a network-resolved address or an empty placeholder is used.
     */
    public static ShellyBindingConfiguration fromProperties(String localIP, Map<String, Object> properties) {
        return new ShellyBindingConfiguration(localIP, "admin", "admin", -1, true).withOverrides(properties);
    }

    /**
     * Convenience overload accepting an OSGi {@link Dictionary} (e.g. from {@code ComponentContext}).
     */
    public static ShellyBindingConfiguration fromProperties(String localIP,
            @Nullable Dictionary<String, Object> properties) {
        if (properties == null) {
            return new ShellyBindingConfiguration(localIP, "admin", "admin", -1, true);
        }
        List<String> keys = Collections.list(properties.keys());
        Map<String, Object> map = keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
        return fromProperties(localIP, map);
    }

    /**
     * Returns a new instance identical to this one except with the given HTTP port.
     * Used by {@link org.openhab.binding.shelly.internal.ShellyHandlerFactory} after the OSGi
     * HTTP service port becomes known.
     */
    public ShellyBindingConfiguration withHttpPort(int httpPort) {
        return new ShellyBindingConfiguration(localIP, defaultUserId, defaultPassword, httpPort, autoCoIoT);
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

    /**
     * Returns the configured HTTP port, or
     * {@link org.openhab.binding.shelly.internal.ShellyBindingConstants#DEFAULT_LOCAL_PORT}
     * if no explicit port has been set via {@link #withHttpPort}.
     */
    public int getHttpPort() {
        return httpPort != -1 ? httpPort : DEFAULT_LOCAL_PORT;
    }

    public boolean isAutoCoIoT() {
        return autoCoIoT;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String resolveLocalIP(NetworkAddressService networkAddressService) {
        String ip = networkAddressService.getPrimaryIpv4HostAddress();
        return (ip != null && !ip.isBlank() && !ip.startsWith("169.254")) ? ip : "";
    }

    private ShellyBindingConfiguration withOverrides(Map<String, Object> properties) {
        String uid = defaultUserId;
        String pwd = defaultPassword;
        String ip = localIP;
        boolean coiot = autoCoIoT;

        for (Map.Entry<String, Object> e : properties.entrySet()) {
            switch (e.getKey()) {
                case CONFIG_DEF_HTTP_USER:
                    uid = (String) e.getValue();
                    break;
                case CONFIG_DEF_HTTP_PWD:
                    pwd = (String) e.getValue();
                    break;
                case CONFIG_LOCAL_IP:
                    ip = (String) e.getValue();
                    break;
                case CONFIG_AUTOCOIOT:
                    Object value = e.getValue();
                    if (value instanceof String stringValue) {
                        // support config through shelly.cfg
                        coiot = "true".equalsIgnoreCase(stringValue);
                    } else {
                        coiot = (boolean) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return new ShellyBindingConfiguration(ip, uid, pwd, httpPort, coiot);
    }
}
