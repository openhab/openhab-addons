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
package org.openhab.binding.mercedesme.internal.utils;

import java.util.Optional;

import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

/**
 * The {@link TokenWrapper} is holding, storing and restoring the token between lifecycles
 *
 * @author Bernd Weymann - Initial contribution
 */
public class TokenWrapper {
    Optional<AccessTokenResponse> atr = Optional.empty();

    public void setToken(AccessTokenResponse atr) {
        this.atr = Optional.of(atr);
    }

    public boolean isValid() {
        if (!atr.isEmpty()) {
            if (atr.get().getRefreshToken() != null) {
                return true;
            }
        }
        return false;
    }
}
