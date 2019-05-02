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
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;

/**
 *
 * @author Dominik Vorreiter - Initial contribution
 */
public class _4BSTeachInVariation3Response extends _4BSMessage {

    public _4BSTeachInVariation3Response(ERP1Message packet) {
        byte[] payload = packet.getPayload(RORGLength, RORG._4BS.getDataLength());

        payload[3] = (byte) 0xF0; // telegram with EEP number and Manufacturer ID,
                                  // EEP supported, Sender ID stored, Response

        setData(payload);
        setDestinationId(packet.getSenderId());
        setSuppressRepeating(false);
        setStatus((byte) 0x00);
    }
}
