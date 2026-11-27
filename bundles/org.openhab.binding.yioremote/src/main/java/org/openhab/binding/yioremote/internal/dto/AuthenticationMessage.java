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
package org.openhab.binding.yioremote.internal.dto;

import static org.openhab.binding.yioremote.internal.YIOremoteBindingConstants.MESSAGE_TYPE_AUTH;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AuthenticationMessage} is sent to the YIO Dock to authenticate the connection.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class AuthenticationMessage {
    public final String type = MESSAGE_TYPE_AUTH;
    public final String token;

    public AuthenticationMessage(String token) {
        this.token = token;
    }
}
