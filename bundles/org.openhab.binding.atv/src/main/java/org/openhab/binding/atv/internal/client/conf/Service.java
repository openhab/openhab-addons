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
package org.openhab.binding.atv.internal.client.conf;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Mutable {@link BaseService} implementation used when creating and adding services to a configuration.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class Service implements BaseService {

    private final @Nullable String identifier;
    private final Protocol protocol;
    private final int port;
    private final Map<String, String> properties;
    private @Nullable String credentials;
    private @Nullable String password;
    private boolean requiresPassword;
    private PairingRequirement pairing;
    private boolean enabled;

    /**
     * Creates a new service.
     *
     * @param identifier unique service identifier or {@code null}
     * @param protocol protocol type
     * @param port service port number
     * @param properties Zeroconf properties or {@code null}
     */
    public Service(@Nullable String identifier, Protocol protocol, int port, @Nullable Map<String, String> properties) {
        this(identifier, protocol, port, properties, null, null, false, PairingRequirement.Unsupported, true);
    }

    /**
     * Creates a new service.
     *
     * @param identifier unique service identifier or {@code null}
     * @param protocol protocol type
     * @param port service port number
     * @param properties Zeroconf properties or {@code null}
     * @param credentials credentials or {@code null}
     * @param password password or {@code null}
     * @param requiresPassword if a password is required to access the service
     * @param pairing pairing requirement
     * @param enabled if the service is enabled
     */
    public Service(@Nullable String identifier, Protocol protocol, int port, @Nullable Map<String, String> properties,
            @Nullable String credentials, @Nullable String password, boolean requiresPassword,
            PairingRequirement pairing, boolean enabled) {
        this.identifier = identifier;
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.port = port;
        this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
        this.credentials = credentials;
        this.password = password;
        this.requiresPassword = requiresPassword;
        this.pairing = Objects.requireNonNull(pairing, "pairing");
        this.enabled = enabled;
    }

    /**
     * Creates a deep copy of another service.
     *
     * @param other service to copy
     * @return new independent copy
     */
    public static Service copyOf(BaseService other) {
        return new Service(other.identifier().orElse(null), other.protocol(), other.port(), other.properties(),
                other.credentials().orElse(null), other.password().orElse(null), other.requiresPassword(),
                other.pairing(), other.enabled());
    }

    @Override
    public Optional<String> identifier() {
        return Optional.ofNullable(identifier);
    }

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public Optional<String> credentials() {
        return Optional.ofNullable(credentials);
    }

    @Override
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public Optional<String> password() {
        return Optional.ofNullable(password);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean requiresPassword() {
        return requiresPassword;
    }

    /**
     * Sets whether a password is required to access the service.
     *
     * @param requiresPassword new state
     */
    public void setRequiresPassword(boolean requiresPassword) {
        this.requiresPassword = requiresPassword;
    }

    @Override
    public PairingRequirement pairing() {
        return pairing;
    }

    /**
     * Sets the pairing requirement of the service.
     *
     * @param pairing new pairing requirement
     */
    public void setPairing(PairingRequirement pairing) {
        this.pairing = Objects.requireNonNull(pairing, "pairing");
    }

    @Override
    public void merge(BaseService other) {
        other.credentials().ifPresent(value -> credentials = value);
        other.password().ifPresent(value -> password = value);
        properties.putAll(other.properties());
    }

    @Override
    public Map<String, Object> settings() {
        Map<String, Object> settings = new HashMap<>();
        String currentCredentials = credentials;
        if (currentCredentials != null) {
            settings.put("credentials", currentCredentials);
        }
        String currentPassword = password;
        if (currentPassword != null) {
            settings.put("password", currentPassword);
        }
        return settings;
    }

    @Override
    public void apply(Map<String, Object> settings) {
        Object newCredentials = settings.get("credentials");
        if (newCredentials != null) {
            credentials = newCredentials.toString();
        }
        Object newPassword = settings.get("password");
        if (newPassword != null) {
            password = newPassword.toString();
        }
    }

    @Override
    public String toString() {
        return "Protocol: " + protocol + ", Port: " + port + ", Credentials: " + credentials + ", Requires Password: "
                + requiresPassword + ", Password: " + password + ", Pairing: " + pairing
                + (enabled ? "" : " (Disabled)");
    }
}
