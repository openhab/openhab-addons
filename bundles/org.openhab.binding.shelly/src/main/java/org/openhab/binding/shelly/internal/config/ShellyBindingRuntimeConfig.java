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
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.openhab.core.net.NetworkAddressService;

/**
 * Runtime snapshot derived from {@link ShellyBindingConfiguration} plus
 * runtime binding configuration derived from {@link ShellyBindingConfiguration}.
 *
 * Resolves the local IP address (config override wins; falls back to
 * {@link NetworkAddressService}) and carries the HTTP port once the OSGi HTTP
 * service has started.
 *
 * Thread-safe (mutable object with synchronized access, updated in-place).
 * Held as volatile in {@link ShellyHandlerFactory} and {@link ShellyBaseHandler} to
 * guarantee safe publication after @Modified callbacks.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingRuntimeConfig {

    // All access must be guarded by "this"
    private String defaultUserId;

    // All access must be guarded by "this"
    private String defaultPassword;

    // All access must be guarded by "this"
    private String localIP;

    // All access must be guarded by "this"
    private int httpPort; // -1 = use DEFAULT_LOCAL_PORT sentinel

    // All access must be guarded by "this"
    private boolean autoCoIoT;

    /**
     * Build a runtime config from raw binding properties and the network address service.
     * If the raw config contains a non-blank {@code localIP} override it is used as-is;
     * otherwise the primary IPv4 address from {@code nas} is used (link-local addresses
     * are discarded).
     */
    public ShellyBindingRuntimeConfig(ShellyBindingConfiguration config, int httpPort, NetworkAddressService nas) {
        this.defaultUserId = config.getDefaultUserId();
        this.defaultPassword = config.getDefaultPassword();
        this.autoCoIoT = config.isAutoCoIoT();
        this.httpPort = httpPort;
        String cfgIp = config.getLocalIP();
        this.localIP = cfgIp.isBlank() ? resolveLocalIP(nas) : cfgIp;
    }

    /**
     * Updates this runtime configuration based on the specified binding configuration.
     *
     * @param config the binding configuration.
     * @param nas the {@link NetworkAddressService} used to resolve the local IP.
     * @return {@code true} if at least one value was changed, {@code false} if no change occurred.
     */
    public synchronized boolean update(ShellyBindingConfiguration config, NetworkAddressService nas) {
        boolean result = false;
        String s = config.getDefaultUserId();
        if (!s.equals(this.defaultUserId)) {
            this.defaultUserId = s;
            result = true;
        }
        if (!(s = config.getDefaultPassword()).equals(this.defaultPassword)) {
            this.defaultPassword = s;
            result = true;
        }
        if (this.autoCoIoT != config.isAutoCoIoT()) {
            this.autoCoIoT = !this.autoCoIoT;
            result = true;
        }
        s = config.getLocalIP();
        if (s.isBlank()) {
            s = resolveLocalIP(nas);
        }
        if (!s.equals(this.localIP)) {
            this.localIP = s;
            result = true;
        }
        return result;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public synchronized String getDefaultUserId() {
        return defaultUserId;
    }

    public synchronized String getDefaultPassword() {
        return defaultPassword;
    }

    public synchronized String getLocalIP() {
        return localIP;
    }

    /**
     * Returns the configured HTTP port, or
     * {@link org.openhab.binding.shelly.internal.ShellyBindingConstants#DEFAULT_LOCAL_PORT}
     * if no explicit port has been set.
     */
    public synchronized int getHttpPort() {
        int httpPort = this.httpPort;
        return httpPort != -1 ? httpPort : DEFAULT_LOCAL_PORT;
    }

    public synchronized boolean isAutoCoIoT() {
        return autoCoIoT;
    }

    // ── Setters ──────────────────────────────────────────────────────────────

    /**
     * Sets the HTTP port to use. {@code -1} means "use default".
     *
     * @param httpPort the new HTTP port value.
     */
    public synchronized void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String resolveLocalIP(NetworkAddressService nas) {
        @Nullable
        String ip = nas.getPrimaryIpv4HostAddress();
        return (ip != null && !ip.isBlank() && !ip.startsWith("169.254")) ? ip : "";
    }
}
