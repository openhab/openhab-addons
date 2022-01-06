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

import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class RDBaseIdResponse extends Response {

    private byte[] baseId = null;
    private int remainingWriteCycles = 0;

    public RDBaseIdResponse(Response response) {
        this(response.getPayload().length, response.getOptionalPayload().length,
                Helper.concatAll(response.getPayload(), response.getOptionalPayload()));
    }

    RDBaseIdResponse(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, payload);

        if (this.data == null || this.data.length != 5 || this.optionalData == null || this.optionalData.length != 1) {
            return;
        }

        baseId = getPayload(1, 4);
        remainingWriteCycles = optionalData[0] & 0xFF;

        _isValid = true;
    }

    public final byte[] getBaseId() {
        return baseId;
    }

    public int getRemainingWriteCycles() {
        return remainingWriteCycles;
    }
}
