/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.mqttembeddedbroker.internal;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.moquette.spi.security.IAuthenticator;

/**
 * Provides a {@link IAuthenticator} for the Moquette server, that accepts given user name and password.
 * If ESH gains user credentials at some point, those should be accepted as well.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttEmbeddedBrokerUserAuthenticator implements IAuthenticator {
    final String username;
    final byte[] password;

    public MqttEmbeddedBrokerUserAuthenticator(String username, byte[] password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean checkValid(@Nullable String clientId, @Nullable String username, byte @Nullable [] password) {
        return this.username.equals(username) && Arrays.equals(this.password, password);
    }
}
