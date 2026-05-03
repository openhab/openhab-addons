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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.net.NetworkAddressService;

/**
 * Runtime binding configuration derived from {@link ShellyBindingConfiguration}.
 * Resolves the local IP address (config override wins; falls back to
 * {@link NetworkAddressService}) and carries the HTTP port once the OSGi HTTP
 * service has started. Immutable after construction; use {@link #withHttpPort}
 * to produce an updated copy.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingRuntimeConfig {
    private final String defaultUserId;
    private final String defaultPassword;
    private final String localIP;
    private final int httpPort; // -1 = use DEFAULT_LOCAL_PORT sentinel
    private final boolean autoCoIoT;

    /**
     * Build a runtime config from raw binding properties and the network address service.
     * If the raw config contains a non-blank {@code localIP} override it is used as-is;
     * otherwise the primary IPv4 address from {@code nas} is used (link-local addresses
     * are discarded).
     */
    public ShellyBindingRuntimeConfig(ShellyBindingConfiguration config, NetworkAddressService nas) {
        this.defaultUserId = config.getDefaultUserId();
        this.defaultPassword = config.getDefaultPassword();
        this.autoCoIoT = config.isAutoCoIoT();
        this.httpPort = -1;
        String cfgIp = config.getLocalIP();
        this.localIP = cfgIp.isBlank() ? resolveLocalIP(nas) : cfgIp;
    }

    private ShellyBindingRuntimeConfig(String localIP, String defaultUserId, String defaultPassword, int httpPort,
            boolean autoCoIoT) {
        this.localIP = localIP;
        this.defaultUserId = defaultUserId;
        this.defaultPassword = defaultPassword;
        this.httpPort = httpPort;
        this.autoCoIoT = autoCoIoT;
    }

    /**
     * Returns a new instance identical to this one but with the given HTTP port.
     * Called by {@link org.openhab.binding.shelly.internal.ShellyHandlerFactory} once the OSGi
     * HTTP service port is known.
     */
    public ShellyBindingRuntimeConfig withHttpPort(int httpPort) {
        return new ShellyBindingRuntimeConfig(localIP, defaultUserId, defaultPassword, httpPort, autoCoIoT);
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

    private static String resolveLocalIP(NetworkAddressService nas) {
        @Nullable
        String ip = nas.getPrimaryIpv4HostAddress();
        return (ip != null && !ip.isBlank() && !ip.startsWith("169.254")) ? ip : "";
    }
}
