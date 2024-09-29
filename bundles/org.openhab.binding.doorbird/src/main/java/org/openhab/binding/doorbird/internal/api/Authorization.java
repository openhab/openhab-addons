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
package org.openhab.binding.doorbird.internal.api;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Authorization} is responsible for managing the host and
 * authorization strings used in Doorbird API calls.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Authorization {
    private final String host;
    private final String userId;
    private final String userPassword;
    private final String authorization;

    public Authorization(String host, String userId, String userPassword) {
        this.host = host;
        this.userId = userId;
        this.userPassword = userPassword;
        this.authorization = new String(Base64.getEncoder().encode((userId + ":" + userPassword).getBytes()),
                StandardCharsets.UTF_8);
    }

    public String getHost() {
        return host;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getAuthorization() {
        return authorization;
    }
}
