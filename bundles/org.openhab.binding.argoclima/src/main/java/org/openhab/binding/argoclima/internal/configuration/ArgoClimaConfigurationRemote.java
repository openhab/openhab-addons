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

import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;
import org.openhab.binding.argoclima.internal.utils.PasswordUtils;

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
     * @return {@code ***}-masked string instead of the same length as configured password
     */
    private final String getPasswordMasked() {
        return PasswordUtils.maskPassword(password);
    }

    /**
     * Get MD5 hash of the configured password (for Basic auth)
     *
     * @return MD5 hash of password
     * @throws ArgoConfigurationException In case MD5 is not available in the security provider
     */
    public String getPasswordHashed() throws ArgoConfigurationException {
        try {
            return PasswordUtils.md5HashPassword(password);
        } catch (NoSuchAlgorithmException e) {
            throw ArgoConfigurationException.forInvalidParamValue(ArgoClimaBindingConstants.PARAMETER_PASSWORD,
                    PasswordUtils.maskPassword(password), i18nProvider, e); // User-provided value is likely NOT at
                                                                            // fault, but using this exception for
                                                                            // generic error messaging (cause will be
                                                                            // displayed anyway)
        }
    }

    @Override
    protected String getExtraFieldDescription() {
        return String.format("username=%s, password=%s", username, getPasswordMasked());
    }

    @Override
    protected void validateInternal() throws ArgoConfigurationException {
        if (username.isBlank()) {
            throw ArgoConfigurationException.forEmptyRequiredParam(ArgoClimaBindingConstants.PARAMETER_USERNAME,
                    i18nProvider);
        }
        if (password.isBlank()) {
            throw ArgoConfigurationException.forEmptyRequiredParam(ArgoClimaBindingConstants.PARAMETER_PASSWORD,
                    i18nProvider);
        }
    }
}
