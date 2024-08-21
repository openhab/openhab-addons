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
package org.openhab.binding.enocean.internal.eep.Base;

import static org.openhab.binding.enocean.internal.messages.ESP3Packet.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;

/**
 *
 * @author Dominik Vorreiter - Initial contribution
 */
@NonNullByDefault
public class _4BSTeachInVariation3Response extends _4BSMessage {

    public _4BSTeachInVariation3Response(ERP1Message packet, boolean teachIn) {
        byte[] payload = packet.getPayload(ESP3_RORG_LENGTH, RORG._4BS.getDataLength());

        payload[3] = (byte) (teachIn ? 0xF0 : 0xD0); // telegram with EEP number and Manufacturer ID,
                                                     // EEP supported, Sender ID stored or deleted, Response

        setData(payload);
        setDestinationId(packet.getSenderId());
        setSuppressRepeating(false);
        setStatus((byte) 0x00);
    }
}
