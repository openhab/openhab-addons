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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class SMACKTeachInResponse extends Response {

    // set response time to 250ms
    static final byte RESPONSE_TIME_HVALUE = 0;
    static final byte RESPONSE_TIME_LVALUE = (byte) 0xFA;

    static final byte TEACH_IN = 0x00;
    static final byte TEACH_OUT = 0x20;
    static final byte REPEATED_TEACH_IN = 0x01;
    static final byte NOPLACE_FOR_MAILBOX = 0x12;
    static final byte BAD_RSSI = 0x14;

    public SMACKTeachInResponse() {
        super(4, 0, new byte[] { Response.ResponseType.RET_OK.getValue(), RESPONSE_TIME_HVALUE, RESPONSE_TIME_LVALUE,
                TEACH_IN });
    }

    public void setTeachOutResponse() {
        data[3] = TEACH_OUT;
    }

    public boolean isTeachOut() {
        return data[3] == TEACH_OUT;
    }

    public void setRepeatedTeachInResponse() {
        data[3] = REPEATED_TEACH_IN;
    }

    public void setNoPlaceForFurtherMailbox() {
        data[3] = NOPLACE_FOR_MAILBOX;
    }

    public void setBadRSSI() {
        data[3] = BAD_RSSI;
    }

    public void setTeachIn() {
        data[3] = TEACH_IN;
    }

    public boolean isTeachIn() {
        return data[3] == TEACH_IN;
    }
}
