/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;

/**
 * Supports quirks in V44057 firmware.
 *
 * @author Stewart Cossey - Initial contribution
 * @author Anton Jansen - revised based on community feedback
 */

@NonNullByDefault
public class BroadlinkRemoteModel3V44057Handler extends BroadlinkRemoteHandler {

    public BroadlinkRemoteModel3V44057Handler(Thing thing,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider,
            StorageService storageService) {
        super(thing, commandDescriptionProvider, storageService);
    }

    @Override
    protected byte @Nullable [] sendCommand(byte commandByte, byte[] codeBytes, String purpose) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // We start using an unsigned int (2 bytes) that indicates the size of the command (4 bytes) and the length
            // of the codeBytes
            int length = codeBytes.length + 4;
            length = length & 0xffff; // truncate, ensure we have an unsigned short int value
            outputStream.write((byte) (length & 0xFF)); // We have an unsigned int with little Endian
            outputStream.write((byte) ((length >> 8) & 0xFF)); // So the larger part goes later
            buildCommandMessage(commandByte, codeBytes).writeTo(outputStream);
            byte[] padded = Utils.padTo(outputStream.toByteArray(), 16);
            byte[] message = buildMessage((byte) 0x6a, padded);
            return sendAndReceiveDatagram(message, purpose);
        } catch (IOException e) {
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                    new StringType("Error found during when entering IR learning mode"));
            logger.warn("Exception while sending command", e);
        }

        return null;
    }

    @Override
    protected byte[] extractResponsePayload(byte[] responseBytes) throws IOException {
        byte decryptedResponse[] = decodeDevicePacket(responseBytes);
        // Interesting stuff begins at the sixth byte, as we now have the extra short unsigned int in the response
        // as compared to the "standard" devices
        return Utils.slice(decryptedResponse, 6, decryptedResponse.length);
    }
}
