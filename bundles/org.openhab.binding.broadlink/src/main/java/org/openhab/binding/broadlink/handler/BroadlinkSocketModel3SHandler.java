/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * Smart power socket handler with power consumption measurement.
 * Despite its name, it is more closely related to the SP2 than the SP3,
 * as it doesn't have a nightlight.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkSocketModel3SHandler extends BroadlinkSocketModel2Handler {

    public BroadlinkSocketModel3SHandler(Thing thing) {
        super(thing, true);
    }

    // The SP3S device needs to be explicitly
    // asked to obtain the power consumption (as opposed to the SP2S)
    // and it has other quirks too
    // @see
    // https://github.com/mjg59/python-broadlink/blob/822b3c326631c1902b5892a83db126291acbf0b6/broadlink/switch.py#L247
    @Override
    double derivePowerConsumption(byte[] unusedPayload) throws IOException {
        byte payload[] = { 8, 0, (byte) 254, 1, 5, 1, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0 };
        byte message[] = buildMessage((byte) 0x6a, payload);
        byte response[] = sendAndReceiveDatagram(message, "SP3s power consumption byte");
        if (response != null) {
            byte consumptionResponsePayload[] = decodeDevicePacket(response);
            return deriveSP3sPowerConsumption(consumptionResponsePayload);
        }
        return 0D;
    }

    double deriveSP3sPowerConsumption(byte[] consumptionResponsePayload) {
        if (consumptionResponsePayload.length > 7) {
            ByteBuffer bb = ByteBuffer.wrap(consumptionResponsePayload).order(ByteOrder.BIG_ENDIAN); // Big-endian
                                                                                                     // (unlike SP2S)
            int intValue = bb.getInt(4);
            return (double) intValue / 100; // Instead of 1000 in SP2S
        }
        return 0D;
    }
}
