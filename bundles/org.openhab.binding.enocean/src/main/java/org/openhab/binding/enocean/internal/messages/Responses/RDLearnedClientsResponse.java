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
package org.openhab.binding.enocean.internal.messages.Responses;

import java.util.Arrays;

import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class RDLearnedClientsResponse extends Response {

    public class LearnedClient {
        public byte[] clientId;
        public byte[] controllerId;
        public int mailboxIndex;
    }

    LearnedClient[] learnedClients;

    public RDLearnedClientsResponse(Response response) {
        this(response.getPayload().length, response.getOptionalPayload().length,
                Helper.concatAll(response.getPayload(), response.getOptionalPayload()));
    }

    RDLearnedClientsResponse(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, payload);

        if (payload == null || ((payload.length - 1) % 9) != 0) {
            return;
        } else {
            _isValid = true;
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
