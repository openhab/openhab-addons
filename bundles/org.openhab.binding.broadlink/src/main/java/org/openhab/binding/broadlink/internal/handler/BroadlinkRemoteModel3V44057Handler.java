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
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;

/**
 * Supports quirks in V44057 firmware.
 *
 * @author Stewart Cossey - Initial contribution
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
            // Little Endian len(codeBytes)+4, unsigned short (2 bytes) + command, unsigned int (4 bytes)
            int length = codeBytes.length + 4;
            length = length & 0xffff; // truncate

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write((byte) (length & 0xFF));
            outputStream.write((byte) ((length >> 8) & 0xFF));

            outputStream.write(commandByte);
            outputStream.write(0x00);
            outputStream.write(0x00);
            outputStream.write(0x00);
            byte[] message = buildMessage((byte) 0x6a, outputStream.toByteArray());
            logger.debug("Sending byte[]: {}", message);
            return sendAndReceiveDatagram(message, purpose);
        } catch (IOException e) {
            updateState(BroadlinkBindingConstants.LEARNING_CONTROL_CHANNEL,
                    new StringType("Error found during when entering IR learning mode"));
            logger.warn("Exception while sending command", e);
        }

        return null;
    }
}
