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
package org.openhab.binding.netatmo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;

/**
 * The {@link ApiHandlerConfiguration} is responsible for holding configuration
 * information needed to access Netatmo API and general binding behavior setup
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiHandlerConfiguration {
    public class Credentials {
        public final String clientId, clientSecret, username, password;

        private Credentials(@Nullable String clientId, @Nullable String clientSecret, @Nullable String username,
                @Nullable String password) throws NetatmoException {
            this.clientSecret = checkMandatory(clientSecret, "@text/conf-error-no-client-secret");
            this.username = checkMandatory(username, "@text/conf-error-no-username");
            this.password = checkMandatory(password, "@text/conf-error-no-password");
            this.clientId = checkMandatory(clientId, "@text/conf-error-no-client-id");
        }

        private String checkMandatory(@Nullable String value, String error) throws NetatmoException {
            if (value == null || value.isBlank()) {
                throw new NetatmoException(error);
            }
            return value;
        }

        @Override
        public String toString() {
            return "Credentials [clientId=" + clientId + ", username=" + username
                    + ", password=******, clientSecret=******]";
        }
    }

    private @Nullable String clientId;
    private @Nullable String clientSecret;
    private @Nullable String username;
    private @Nullable String password;
    public @Nullable String webHookUrl;
    public int reconnectInterval = 300;

    public Credentials getCredentials() throws NetatmoException {
        return new Credentials(clientId, clientSecret, username, password);
    }
}
