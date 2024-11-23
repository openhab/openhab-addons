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

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.core.library.types.StringType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class RDRepeaterResponse extends Response {

    protected String repeaterLevel = "";

    public RDRepeaterResponse(Response response) {
        this(response.getPayload().length, 0, response.getPayload());
    }

    RDRepeaterResponse(int dataLength, int optionalDataLength, byte[] payload) {
        super(dataLength, optionalDataLength, payload);

        if (payload.length < 3) {
            return;
        }

        if (payload[1] == 0) {
            repeaterLevel = REPEATERMODE_OFF;
        } else if (payload[1] == 1 || payload[1] == 2) {
            switch (payload[2]) {
                case 1:
                    repeaterLevel = REPEATERMODE_LEVEL_1;
                    break;
                case 2:
                    repeaterLevel = REPEATERMODE_LEVEL_2;
                    break;
                case 0:
                    repeaterLevel = REPEATERMODE_OFF;
                    break;
                default:
                    return;
            }

            isValid = true;
        }
    }

    public StringType getRepeaterLevel() {
        return StringType.valueOf(repeaterLevel);
    }
}
