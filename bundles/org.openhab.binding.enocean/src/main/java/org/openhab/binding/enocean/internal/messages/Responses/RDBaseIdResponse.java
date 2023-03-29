/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class RDBaseIdResponse extends Response {

    private byte[] baseId = new byte[0];
    private int remainingWriteCycles = 0;

    public RDBaseIdResponse(Response response) {
        this(response.getPayload().length, response.getOptionalPayload().length,
                Helper.concatAll(response.getPayload(), response.getOptionalPayload()));
    }

    RDBaseIdResponse(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, payload);

        if (this.data.length != 5 || this.optionalData.length != 1) {
            return;
        }

        baseId = getPayload(1, 4);
        remainingWriteCycles = optionalData[0] & 0xFF;

        isValid = true;
    }

    public final byte[] getBaseId() {
        return baseId;
    }

    public int getRemainingWriteCycles() {
        return remainingWriteCycles;
    }
}
