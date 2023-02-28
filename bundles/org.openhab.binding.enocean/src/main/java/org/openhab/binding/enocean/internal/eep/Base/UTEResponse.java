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
package org.openhab.binding.enocean.internal.eep.Base;

import static org.openhab.binding.enocean.internal.messages.ESP3Packet.*;

import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class UTEResponse extends _VLDMessage {

    public static final byte TeachIn_MASK = 0x3f;
    public static final byte CommunicationType_MASK = (byte) 0x80;
    public static final byte ResponseNeeded_MASK = 0x40;
    public static final byte TeachIn_NotSpecified = 0x20;

    public UTEResponse(ERP1Message packet, boolean teachIn) {
        int dataLength = packet.getPayload().length - ESP3_SENDERID_LENGTH - ESP3_RORG_LENGTH - ESP3_STATUS_LENGTH;

        setData(packet.getPayload(ESP3_RORG_LENGTH, dataLength));
        bytes[0] = (byte) (teachIn ? 0x91 : 0xA1); // bidirectional communication, teach in accepted or teach out, teach
                                                   // in response

        setStatus((byte) 0x80);
        setSuppressRepeating(true);
        setDestinationId(packet.getSenderId());
    }
}
