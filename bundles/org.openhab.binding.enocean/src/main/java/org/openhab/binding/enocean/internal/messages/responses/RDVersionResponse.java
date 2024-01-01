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
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class RDVersionResponse extends Response {

    protected String appVersion = "";
    protected String apiVersion = "";
    protected String chipId = "";
    protected String description = "";

    public RDVersionResponse(Response response) {
        this(response.getPayload().length, 0, response.getPayload());
    }

    RDVersionResponse(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, payload);

        if (payload.length < 33) {
            return;
        }

        try {
            appVersion = String.format("%d.%d.%d.%d", payload[1] & 0xff, payload[2] & 0xff, payload[3] & 0xff,
                    payload[4] & 0xff);
            apiVersion = String.format("%d.%d.%d.%d", payload[5] & 0xff, payload[6] & 0xff, payload[7] & 0xff,
                    payload[8] & 0xff);

            chipId = HexUtils.bytesToHex(Arrays.copyOfRange(payload, 9, 13));

            StringBuffer sb = new StringBuffer();
            for (int i = 17; i < payload.length; i++) {
                sb.append((char) (payload[i] & 0xff));
            }
            description = sb.toString();
            isValid = true;

        } catch (Exception e) {
            responseType = ResponseType.RET_ERROR;
        }
    }

    public String getAPPVersion() {
        return appVersion;
    }

    public String getAPIVersion() {
        return apiVersion;
    }

    public String getChipID() {
        return chipId;
    }

    public String getDescription() {
        return description;
    }
}
