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
package org.openhab.binding.broadlink.internal.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;

/**
 * Remote blaster handler, "generation" 4
 *
 * These devices place a 6-byte preamble before their payload.
 * The format is:
 * Byte 00 01 02 03 04 05
 * PLl PLh CMD 00 00 00
 *
 * Where PL is the 16-bit unsigned length of the payload PLUS 4 BYTES
 * so PLl is the lower byte and PLh is the high byte
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModel4MiniHandler extends BroadlinkRemoteHandler {

    public BroadlinkRemoteModel4MiniHandler(Thing thing,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider,
            StorageService storageService) {
        super(thing, commandDescriptionProvider, storageService);
    }

    @Override
    protected void getStatusFromDevice() throws BroadlinkStatusException, IOException {
        // These devices use a 2-byte preamble to the normal protocol;
        // https://github.com/mjg59/python-broadlink/blob/0bd58c6f598fe7239246ad9d61508febea625423/broadlink/__init__.py#L666
        byte[] response = sendCommand((byte) 0x24, "RM4 device status"); // Status check is now Ox24, not 0x01 as in
        // earlier devices
        if (response == null) {
            throw new BroadlinkStatusException(
                    "response from RM4 device was null, did you configure the right address for the device?");
        }
        byte decodedPayload[] = extractResponsePayload(response);

        // Temps and humidity get divided by 100 now, not 10
        double temperature = ((decodedPayload[0] * 100 + decodedPayload[1]) / 100D);
        updateTemperature(temperature);
        double humidity = ((decodedPayload[2] * 100 + decodedPayload[3]) / 100D);
        updateHumidity(humidity);
    }

    // These devices use a 6-byte sendCode preamble instead of the previous 4:
    // https://github.com/mjg59/python-broadlink/blob/822b3c326631c1902b5892a83db126291acbf0b6/broadlink/remote.py#L78
    @Override
    protected ByteArrayOutputStream buildCommandMessage(byte commandByte, byte[] codeBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] preamble = new byte[6];
        int length = codeBytes.length + 4;
        preamble[0] = (byte) (length & 0xFF);
        preamble[1] = (byte) ((length >> 8) & 0xFF);
        preamble[2] = commandByte;
        outputStream.write(preamble);
        if (codeBytes.length > 0) {
            outputStream.write(codeBytes);
        }

        return outputStream;
    }

    // Interesting stuff begins at the 6th byte, and runs for the length indicated
    // in the first two bytes of the response (little-endian) + 2, as opposed to
    // whatever the "natural" decrypted length is
    @Override
    protected byte[] extractResponsePayload(byte[] responseBytes) throws IOException {
        byte decryptedResponse[] = decodeDevicePacket(responseBytes);
        int lsb = decryptedResponse[0] & 0xFF;
        int msb = decryptedResponse[1] & 0xFF;
        int payloadLength = (msb << 8) + lsb;
        if ((payloadLength + 2) > decryptedResponse.length) {
            logger.warn("Received incomplete message, expected length: {}, received: {}", payloadLength + 2,
                    decryptedResponse.length);
            payloadLength = decryptedResponse.length - 2;
        }
        return Utils.slice(decryptedResponse, 6, payloadLength + 2);
    }
}
