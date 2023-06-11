/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschindego.internal;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Session for storing Bosch Indego context information.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoSession {

    private static final Duration DEFAULT_EXPIRATION_PERIOD = Duration.ofSeconds(10);

    private String contextId;
    private String serialNumber;
    private Instant expirationTime;

    public IndegoSession() {
        this("", "", Instant.MIN);
    }

    public IndegoSession(String contextId, String serialNumber, Instant expirationTime) {
        this.contextId = contextId;
        this.serialNumber = serialNumber;
        this.expirationTime = expirationTime.equals(Instant.MIN) ? Instant.now().plus(DEFAULT_EXPIRATION_PERIOD)
                : expirationTime;
    }

    /**
     * Get context id for HTTP requests (headers "x-im-context-id: <contextId>" and
     * "Cookie: BOSCH_INDEGO_SSO=<contextId>").
     * 
     * @return current context id
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Get serial number of device.
     * 
     * @return serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Get expiration time of session as {@link Instant}.
     * 
     * @return expiration time
     */
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Check if session is initialized, i.e. has serial number.
     * 
     * @see #isValid()
     * @return true if session is initialized
     */
    public boolean isInitialized() {
        return !serialNumber.isEmpty();
    }

    /**
     * Check if session is valid, i.e. has not yet expired.
     *
     * @return true if session is still valid
     */
    public boolean isValid() {
        return !contextId.isEmpty() && expirationTime.isAfter(Instant.now());
    }

    /**
     * Invalidate session.
     */
    public void invalidate() {
        contextId = "";
        expirationTime = Instant.MIN;
    }

    @Override
    public String toString() {
        return String.format("%s (serialNumber %s, expirationTime %s)", contextId, serialNumber, expirationTime);
    }
}
