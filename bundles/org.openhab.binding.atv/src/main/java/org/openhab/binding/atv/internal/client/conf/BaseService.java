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

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * A protocol service belonging to a device, e.g. the MRP or AirPlay endpoint.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface BaseService {

    /**
     * Returns the unique identifier associated with this service.
     *
     * @return service identifier if available
     */
    Optional<String> identifier();

    /**
     * Returns the protocol type.
     *
     * @return protocol type
     */
    Protocol protocol();

    /**
     * Returns the service port number.
     *
     * @return port number
     */
    int port();

    /**
     * Returns if the service is enabled.
     *
     * @return {@code true} if enabled
     */
    boolean enabled();

    /**
     * Changes whether the service is enabled or not.
     *
     * @param enabled new enabled state
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the service Zeroconf properties.
     *
     * @return Zeroconf properties
     */
    Map<String, String> properties();

    /**
     * Returns the credentials used to access the service.
     *
     * @return credentials if available
     */
    Optional<String> credentials();

    /**
     * Sets the credentials used to access the service.
     *
     * @param credentials new credentials or {@code null}
     */
    void setCredentials(String credentials);

    /**
     * Returns the password used to access the service.
     *
     * @return password if available
     */
    Optional<String> password();

    /**
     * Sets the password used to access the service.
     *
     * @param password new password or {@code null}
     */
    void setPassword(String password);

    /**
     * Returns if a password is required to access the service.
     *
     * @return {@code true} if a password is required
     */
    boolean requiresPassword();

    /**
     * Returns if pairing is required by the service.
     *
     * @return pairing requirement
     */
    PairingRequirement pairing();

    /**
     * Merges with another service of the same type. Merge will only include credentials, password and properties.
     *
     * @param other service to merge from
     */
    void merge(BaseService other);

    /**
     * Returns a snapshot of settings and their values (keys {@code credentials} and {@code password}).
     *
     * @return settings snapshot; values may be {@code null}
     */
    Map<String, Object> settings();

    /**
     * Applies settings to the service.
     *
     * <p>
     * Expects the same format as returned by {@link #settings()}. Unknown properties are silently ignored.
     * Settings with a {@code null} value are also ignored (keeps original value).
     *
     * @param settings settings to apply
     */
    void apply(Map<String, Object> settings);
}
