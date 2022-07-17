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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;

/**
 * The {@link ArgoClimaConfigurationRemote} class contains fields mapping thing configuration parameters
 * for a remote Argo device (comms via Argo servers)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoClimaConfigurationRemote extends ArgoClimaConfigurationBase {

    /**
     * The duration after which the device would be considered non-responsive (and taken OFFLINE)
     */
    public static final Duration LAST_SEEN_UNAVAILABILITY_THRESHOLD = Duration.ofMinutes(20);

    /**
     * Argo configuration parameters specific to remote connection
     * These names are defined in thing-types.xml and get injected on instantiation
     * through {@link org.openhab.core.thing.binding.BaseThingHandler#getConfigAs getConfigAs}
     */
    private String username = "";
    private String password = "";

    /**
     * Get the username (login) to use in authenticating to Argo server
     *
     * @return username (as configured by the user)
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Get the masked password used in authenticating to Argo server (for logging)
     *
     * @implNote Password length is preserved (which may be considered a security weakness, but is useful for
     *           troubleshooting
     *           and given state of Argo API's security... likely is an overkill already :)
     * @return {@code ***}-masked string instead of the same length as configured password
     */
    public String getPasswordMasked() {
        return this.password.replaceAll(".", "*");
    }

    /**
     * Get MD5 hash of the configured password (for Basic auth)
     *
     * @return MD5 hash of password
     * @throws ArgoConfigurationException In case MD5 is not available in the security provider
     */
    public String getPasswordMD5Hash() throws ArgoConfigurationException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new ArgoConfigurationException("Unable to calculate MD5 hash of password", getPasswordMasked(), e);
        }
    }

    @Override
    protected String getExtraFieldDescription() {
        return String.format("username=%s, password=%s", username, getPasswordMasked());
    }

    @Override
    protected void validateInternal() throws ArgoConfigurationException {
        if (username.isBlank()) {
            throw new ArgoConfigurationException("Username is empty. Must be set to Argo login");
        }
        if (password.isBlank()) {
            throw new ArgoConfigurationException("Password is empty. Must be set to Argo password");
        }
    }
}
