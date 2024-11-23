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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.model.GetsessionDTO;
import org.openhab.binding.doorbird.internal.model.GetsessionDTO.GetsessionBha;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link DoorbirdSession} holds information about the Doorbird session,
 * including the v2 decryption key for notification events.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DoorbirdSession {
    private @Nullable String returnCode;
    private @Nullable String sessionId;
    private @Nullable String decryptionKey;

    public DoorbirdSession(String infoJson) throws JsonSyntaxException {
        GetsessionDTO session = DoorbirdAPI.fromJson(infoJson, GetsessionDTO.class);
        if (session != null) {
            GetsessionBha bha = session.bha;
            returnCode = bha.returnCode;
            sessionId = bha.sessionId;
            decryptionKey = bha.decryptionKey;
        }
    }

    public @Nullable String getReturnCode() {
        return returnCode;
    }

    public @Nullable String getSessionId() {
        return sessionId;
    }

    public @Nullable String getDecryptionKey() {
        return decryptionKey;
    }
}
