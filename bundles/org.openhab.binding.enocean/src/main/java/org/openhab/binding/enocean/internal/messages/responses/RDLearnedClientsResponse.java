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
package org.openhab.binding.enocean.internal.messages.responses;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class RDLearnedClientsResponse extends Response {

    public class LearnedClient {
        public byte[] clientId = new byte[0];
        public byte[] controllerId = new byte[0];
        public int mailboxIndex;
    }

    LearnedClient[] learnedClients = new LearnedClient[0];

    public RDLearnedClientsResponse(Response response) {
        this(response.getPayload().length, response.getOptionalPayload().length,
                Helper.concatAll(response.getPayload(), response.getOptionalPayload()));
    }

    RDLearnedClientsResponse(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, payload);

        if (payload.length == 0 || (payload.length - 1) % 9 != 0) {
            return;
        } else {
            isValid = true;
        }

        learnedClients = new LearnedClient[(payload.length - 1) / 9];
        for (int i = 0; i < learnedClients.length; i++) {
            LearnedClient client = new LearnedClient();
            client.clientId = Arrays.copyOfRange(payload, 1 + i * 9, 1 + i * 9 + 4);
            client.controllerId = Arrays.copyOfRange(payload, 5 + i * 9, 5 + i * 9 + 4);
            client.mailboxIndex = payload[9 + i * 9] & 0xFF;
            learnedClients[i] = client;
        }
    }

    public LearnedClient[] getLearnedClients() {
        return learnedClients;
    }
}
