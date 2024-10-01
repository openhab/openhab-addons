/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;

/**
 * The {@link ArgoClimaConfigurationLocal} class extends base configuration parameters with ones specific
 * to local connection (including a remote API stub / proxy)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoClimaConfigurationLocal extends ArgoClimaConfigurationBase {
    public enum ConnectionMode {
        LOCAL_CONNECTION,
        REMOTE_API_STUB,
        REMOTE_API_PROXY
    }

    public enum DeviceSidePasswordDisplayMode {
        NEVER,
        MASKED,
        CLEARTEXT
    }

    private String hostname = "";
    private ConnectionMode connectionMode = ConnectionMode.LOCAL_CONNECTION;
    private int hvacListenPort = 1001;
    private String localDeviceIP = "";
    private boolean useLocalConnection = true;
    private int stubServerPort = 8239; // Note the original Argo server listens on '80', but picking a non privileged
                                       // port (>1024) as a default, since this needs remapping on firewall, and openHAB
                                       // is typically listening on 80 or 8080
    private List<String> stubServerListenAddresses = List.of("0.0.0.0");
    private DeviceSidePasswordDisplayMode includeDeviceSidePasswordsInProperties = DeviceSidePasswordDisplayMode.NEVER;
    private boolean matchAnyIncomingDeviceIp = false;

    /**
     * Retrieves the *target* IP address of the LOCAL Argo device (from hostname and/or IP)
     *
     * @return The IP address of the Argo device (for use in local communication)
     * @throws ArgoConfigurationException if no IP address for the {@code hostname} could be found
     */
    public InetAddress getHostname() throws ArgoConfigurationException {
        try {
            return Objects.requireNonNull(InetAddress.getByName(hostname));
        } catch (UnknownHostException e) {
            throw ArgoConfigurationException.forInvalidParamValue(ArgoClimaBindingConstants.PARAMETER_HOSTNAME,
                    hostname, i18nProvider, e);
        }
    }

    /**
     * Retrieves the local IPv4 address of the Argo device (in its current subnet) - if available/known
     * <p>
     * If the device is behind NAT, this address will be different from the one determined from
     * {@link #getHostname() getHostname}
     *
     * @return Local IP address of the HVAC device (for use in matching remote responses to the device)
     * @throws ArgoConfigurationException if the {@code localDeviceIP} is invalid
     */
    public Optional<InetAddress> getLocalDeviceIP() throws ArgoConfigurationException {
        try {
            if (this.localDeviceIP.isBlank()) {
                return Optional.<InetAddress> empty();
            }
            return Optional.ofNullable(InetAddress.getByName(localDeviceIP)); // it's actually not Nullable, but
                                                                              // InetAddress doesn't have null
                                                                              // annotations... so this useless runtime
                                                                              // check spares us one compiler warning
                                                                              // (yay! ;))
        } catch (UnknownHostException e) {
            throw ArgoConfigurationException.forInvalidParamValue(ArgoClimaBindingConstants.PARAMETER_LOCAL_DEVICE_IP,
                    localDeviceIP, i18nProvider, e);
        }
    }

    /**
     * Returns the local Argo device port (1001 by default, unless re-mapped on firewall)
     *
     * @return device's local port
     */
    public int getHvacListenPort() {
        return this.hvacListenPort;
    }

    /**
     * Return the configured connection mode: local vs. remote API (with/without pass-through to Argo servers)
     *
     * @return The connection mode
     */
    public ConnectionMode getConnectionMode() {
        return this.connectionMode;
    }

    /**
     * Get the stub server listen port
     *
     * @return Stub server listen port or {@code -1} if N/A
     */
    public int getStubServerPort() {
        return this.stubServerPort;
    }

    /**
     * Get the stub server listen IP addresses (from hostnames)
     *
     * @return A set of listen addresses
     * @throws ArgoConfigurationException if at least one of the {@code stubServerListenAddresses} is a hostname and
     *             cannot be resolved to an IP address
     */
    public Set<InetAddress> getStubServerListenAddresses() throws ArgoConfigurationException {
        var addresses = new LinkedHashSet<InetAddress>();
        for (var t : stubServerListenAddresses) {
            try {
                addresses.add(Objects.requireNonNull(InetAddress.getByName(t)));
            } catch (UnknownHostException e) {
                throw ArgoConfigurationException.forInvalidParamValue(
                        ArgoClimaBindingConstants.PARAMETER_STUB_SERVER_LISTEN_ADDRESSES, t, i18nProvider, e);
            }
        }
        return addresses;
    }

    @Override
    public int getRefreshInterval() {
        if (!this.useLocalConnection) {
            return 0;
        }
        return super.getRefreshInterval();
    }

    /**
     * Returns information whether the device-side incoming passwords are to be shown as properties (and if so: in the
     * clear or replaced with ***)
     *
     * @return Configured value
     */
    public DeviceSidePasswordDisplayMode getIncludeDeviceSidePasswordsInProperties() {
        return this.includeDeviceSidePasswordsInProperties;
    }

    /**
     * Should the incoming (intercepted) device-side updates be a strict match to local IP (if provided) or hostname
     * (fallback)
     *
     * @return True - if requiring exact match, False - if IP mismatch is allowed
     */
    public boolean getMatchAnyIncomingDeviceIp() {
        return this.matchAnyIncomingDeviceIp;
    }

    @Override
    protected String getExtraFieldDescription() {
        return String.format(
                "hostname=%s, localDeviceIP=%s, hvacListenPort=%d, connectionMode=%s, useLocalConnection=%s, stubServerPort=%d, stubServerListenAddresses=%s, includeDeviceSidePasswordsInProperties=%s, matchAnyIncomingDeviceIp=%s",
                getOrDefault(this::getHostname), getOrDefault(this::getLocalDeviceIP), hvacListenPort, connectionMode,
                useLocalConnection, stubServerPort, getOrDefault(this::getStubServerListenAddresses),
                includeDeviceSidePasswordsInProperties, matchAnyIncomingDeviceIp);
    }

    @Override
    protected void validateInternal() throws ArgoConfigurationException {
        if (hostname.isEmpty()) {
            throw ArgoConfigurationException.forEmptyRequiredParam(ArgoClimaBindingConstants.PARAMETER_HOSTNAME,
                    i18nProvider);
        }

        if (!useLocalConnection && connectionMode == ConnectionMode.LOCAL_CONNECTION) {
            throw ArgoConfigurationException.forConflictingParams(
                    ArgoClimaBindingConstants.PARAMETER_USE_LOCAL_CONNECTION, "OFF",
                    ArgoClimaBindingConstants.PARAMETER_CONNECTION_MODE, ConnectionMode.LOCAL_CONNECTION, i18nProvider);
        }

        if (getRefreshInterval() == 0 && connectionMode == ConnectionMode.LOCAL_CONNECTION) {
            throw ArgoConfigurationException.forConflictingParams(ArgoClimaBindingConstants.PARAMETER_REFRESH_INTERNAL,
                    getRefreshInterval(), ArgoClimaBindingConstants.PARAMETER_CONNECTION_MODE,
                    ConnectionMode.LOCAL_CONNECTION, i18nProvider);
        }

        if (hvacListenPort < 0 || hvacListenPort > 65535) {
            throw ArgoConfigurationException.forParamOutOfRange(ArgoClimaBindingConstants.PARAMETER_HVAC_LISTEN_PORT,
                    hvacListenPort, i18nProvider, 0, 65535);
        }

        if (stubServerPort < 0 || stubServerPort > 65535) {
            throw ArgoConfigurationException.forParamOutOfRange(ArgoClimaBindingConstants.PARAMETER_STUB_SERVER_PORT,
                    stubServerPort, i18nProvider, 0, 65535);
        }

        // want the side-effect of these calls!
        getHostname();
        getStubServerListenAddresses();
        getLocalDeviceIP();
    }
}
