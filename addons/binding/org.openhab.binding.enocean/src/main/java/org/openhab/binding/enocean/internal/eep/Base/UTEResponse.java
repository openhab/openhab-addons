/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

        setDestinationId(packet.getSenderId());
    }
}
