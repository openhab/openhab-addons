/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;

/**
 * The {@link ArgoClimaConfigurationLocal} class extends base configuration parameters with ones specific
 * to local connection (including a remote API stub / proxy)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoClimaConfigurationLocal extends ArgoClimaConfigurationBase {
    public static enum ConnectionMode {
        LOCAL_CONNECTION,
        REMOTE_API_STUB,
        REMOTE_API_PROXY
    }

    /**
     * Argo configuration parameters specific to local connection & API stub
     * These names are defined in thing-types.xml and get injected on instantiation
     * through {@link org.openhab.core.thing.binding.BaseThingHandler#getConfigAs getConfigAs}
     */
    private String hostname = "";
    private String localDeviceIP = "";
    private int localDevicePort = 1001;
    private ConnectionMode connectionMode = ConnectionMode.LOCAL_CONNECTION;
    private boolean useLocalConnection = true;
    private int stubServerPort = -1;
    private List<String> stubServerListenAddresses = List.of();
    private boolean showCleartextPasswords = false;
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
            throw new ArgoConfigurationException("Invalid hostname configuration", hostname, e);
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
            throw new ArgoConfigurationException("Invalid localDeviceIP configuration", this.localDeviceIP, e);
        }
    }

    /**
     * Returns the local Argo device port (1001 by default, unless re-mapped on firewall)
     *
     * @return device's local port
     */
    public int getLocalDevicePort() {
        return this.localDevicePort;
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
                throw new ArgoConfigurationException(
                        "Invalid Stub server listen address configuration: " + e.getMessage(), t, e);
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
     * Returns information whether the passwords are to be shown in the clear or replaced with ***
     *
     * @return Configured value
     */
    public boolean getShowCleartextPasswords() {
        return this.showCleartextPasswords;
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
                "hostname=%s, localDeviceIP=%s, localDevicePort=%d, connectionMode=%s, useLocalConnection=%s, stubServerPort=%d, stubServerListenAddresses=%s, showCleartextPasswords=%s, matchAnyIncomingDeviceIp=%s",
                getOrDefault(this::getHostname), getOrDefault(this::getLocalDeviceIP), localDevicePort, connectionMode,
                useLocalConnection, stubServerPort, getOrDefault(this::getStubServerListenAddresses),
                showCleartextPasswords, matchAnyIncomingDeviceIp);
    }

    @Override
    protected void validateInternal() throws ArgoConfigurationException {
        if (hostname.isEmpty()) {
            throw new ArgoConfigurationException(
                    "Hostname is empty. Must be set to Argo Air Conditioner's local address");
        }

        if (!useLocalConnection && connectionMode == ConnectionMode.LOCAL_CONNECTION) {
            throw new ArgoConfigurationException(
                    "Cannot set Use Local Connection to OFF, when connection mode is LOCAL_CONNECTION");
        }

        if (getRefreshInterval() == 0 && connectionMode == ConnectionMode.LOCAL_CONNECTION) {
            throw new ArgoConfigurationException(
                    "Cannot set refresh interval to 0, when connection mode is LOCAL_CONNECTION");
        }

        if (localDevicePort < 0 || localDevicePort >= 65536) {
            throw new ArgoConfigurationException("Local Device Port must be in range [0..65536]");
        }

        if (stubServerPort < 0 || stubServerPort >= 65536) {
            throw new ArgoConfigurationException("Stub server port must be in range [0..65536]");
        }

        // want the side-effect of these calls!
        getHostname();
        getStubServerListenAddresses();
        getLocalDeviceIP();
    }
}
