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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;
import org.slf4j.LoggerFactory;

/**
 * Remote blaster handler, "generation" 4
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModel4Handler extends BroadlinkRemoteHandler {

    public BroadlinkRemoteModel4Handler(Thing thing) {
        super(thing, LoggerFactory.getLogger(BroadlinkRemoteModel4Handler.class));
    }

    protected boolean onBroadlinkDeviceBecomingReachable() {
        return getStatusFromDevice();
    }

    protected boolean getStatusFromDevice() {
        try {
            // These devices use a 2-byte preamble to the normal protocol;
            // https://github.com/mjg59/python-broadlink/blob/0bd58c6f598fe7239246ad9d61508febea625423/broadlink/__init__.py#L666

            byte payload[] = new byte[16];
            payload[0] = 0x04;
            payload[1] = 0x00;
            payload[2] = 0x24; // Status check is now Ox24, not 0x01 as in earlier devices
            byte message[] = buildMessage((byte) 0x6a, payload);
            byte response[] = sendAndReceiveDatagram(message, "RM4 device status");
            byte decodedPayload[] = decodeDevicePacket(response);
            // Temps and humidity get divided by 100 now, not 10
            // Temperature and Humidity response fields are 2 bytes further into the response,
            // mirroring the request
            float temperature = (float) ((double) (decodedPayload[6] * 100 + decodedPayload[7]) / 100D);
            updateState(BroadlinkBindingConstants.CHANNEL_TEMPERATURE, new DecimalType(temperature));
            float humidity = (float) ((double) (decodedPayload[8] * 100 + decodedPayload[9]) / 100D);
            updateState(BroadlinkBindingConstants.CHANNEL_HUMIDITY, new DecimalType(humidity));
            return true;
        } catch (Exception e) {
            thingLogger.logError("Could not get status: ", e);
            return false;
        }
    }

    protected void sendCode(byte code[]) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // These devices use a 6-byte sendCode preamble instead of the previous 4
            // https://github.com/mjg59/python-broadlink/blob/0.13.0/broadlink/__init__.py#L50 add RM4 list

            byte[] preamble = new byte[6];
            preamble[0] = (byte) 0xd0;
            preamble[2] = 2;
            outputStream.write(preamble);
            outputStream.write(code);

            byte[] padded = Utils.padTo(outputStream.toByteArray(), 16);
            byte[] message = buildMessage((byte) 0x6a, padded);
            sendAndReceiveDatagram(message, "remote code");
        } catch (IOException e) {
            thingLogger.logError("Exception while sending code", e);
        }
    }
}
