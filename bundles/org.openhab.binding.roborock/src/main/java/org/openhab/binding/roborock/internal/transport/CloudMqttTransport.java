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
package org.openhab.binding.roborock.internal.transport;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.roborock.internal.RoborockAccountHandler;

/**
 * Cloud-backed command transport using existing MQTT bridge behavior.
 *
 * @author Maciej Pham - Initial contribution
 */
@NonNullByDefault
public class CloudMqttTransport implements RoborockCommandTransport {

    private final RoborockAccountHandler accountHandler;
    private final String duid;
    private final byte[] nonce;
    private String localKey = "";
    private final boolean b01;
    private final boolean q7;
    private final boolean q10;

    public CloudMqttTransport(RoborockAccountHandler accountHandler, String duid, byte[] nonce, boolean b01, boolean q7,
            boolean q10) {
        this.accountHandler = accountHandler;
        this.duid = duid;
        this.nonce = Arrays.copyOf(nonce, nonce.length);
        this.b01 = b01;
        this.q7 = q7;
        this.q10 = q10;
    }

    @Override
    public int sendCommand(String method, String params, int requestId) throws UnsupportedEncodingException {
        if (b01 && q7) {
            return accountHandler.sendB01RPCCommand(method, params, duid, localKey, requestId);
        } else if (b01 && q10) {
            // q10 b01 vacuums don't use RPCCommands.
            return -1;
        }
        return accountHandler.sendRPCCommand(method, params, duid, localKey, nonce, requestId);
    }

    @Override
    public void updateContext(@Nullable String localKey, @Nullable String localHost, int localPort,
            @Nullable String endpointPrefix) {
        this.localKey = localKey == null ? "" : localKey;
    }
}
