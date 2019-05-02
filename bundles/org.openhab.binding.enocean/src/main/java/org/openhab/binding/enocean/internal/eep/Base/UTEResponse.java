/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

    public UTEResponse(ERP1Message packet) {

        int dataLength = packet.getPayload().length - SenderIdLength - RORGLength - StatusLength;

        setData(packet.getPayload(RORGLength, dataLength));
        bytes[0] = (byte) 0x91; // bidirectional communication, teach in accepted, teach in response

        setStatus((byte) 0x80);
        setSuppressRepeating(true);
        setDestinationId(packet.getSenderId());
    }
}
