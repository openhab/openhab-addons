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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.util.Arrays;

import org.openhab.binding.paradoxalarm.internal.communication.crypto.EncryptionHandler;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Response}. The response which is returned after receiving data from socket.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class Response implements IResponse {

    private final Logger logger = LoggerFactory.getLogger(Response.class);

    private IRequest request;
    private byte[] packetBytes;
    private byte[] header;
    private byte[] payload;

    public Response(IRequest request, byte[] content, boolean useEncryption) {
        this.request = request;
        this.packetBytes = content;
        ParadoxUtil.printPacket("Rx packet", packetBytes);

        if (useEncryption) {
            decrypt();
        }
        parsePacket();
    }

    @Override
    public RequestType getType() {
        return request.getType();
    }

    @Override
    public byte[] getPacketBytes() {
        return packetBytes;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public byte[] getHeader() {
        return header;
    }

    @Override
    public IRequest getRequest() {
        return request;
    }

    public void updatePayload(byte[] payload) {
        this.packetBytes = payload;
    }

    private void decrypt() {
        byte[] payloadBytes = Arrays.copyOfRange(packetBytes, 16, packetBytes.length);
        logger.trace("DECRYPTING. Full packet length={}", packetBytes.length);
        EncryptionHandler handler = EncryptionHandler.getInstance();
        byte[] decrypted = handler.decrypt(payloadBytes);

        header = Arrays.copyOfRange(packetBytes, 0, 16);
        payload = Arrays.copyOfRange(decrypted, 0, header[1]);
        packetBytes = ParadoxUtil.mergeByteArrays(header, payload);
        ParadoxUtil.printByteArray("Decrypted package=", packetBytes, packetBytes.length);
    }

    /**
     * This method parses data from the IP150 module.
     * A panel command, e.g. 0x5
     * A logon sequence part is starting with 0x0, 0x1 or 0x7
     * We ignore invalid packets which do not match our pattern.
     *
     */
    private void parsePacket() {
        // Message too short
        if (packetBytes.length < 17) {
            logger.debug("Message length is too short. Length={}", packetBytes.length);
            return;
        }

        byte receivedCommand = packetBytes[16];
        byte highNibble = ParadoxUtil.getHighNibble(receivedCommand);
        RequestType requestType = request.getType();

        switch (requestType) {
            // For EPROM and RAM messages received command must be 0x5x
            case EPROM:
            case RAM:
                if (highNibble == 0x5) {
                    header = Arrays.copyOfRange(packetBytes, 0, 22);
                    payload = Arrays.copyOfRange(packetBytes, 22, packetBytes.length - 1);
                    return;
                }
                break;

            // For logon sequence packets there are various commands but their high nibbles should be either 0x0, 0x1 or
            // 0x7
            case LOGON_SEQUENCE:
                switch (highNibble) {
                    case 0x0:
                    case 0x1:
                    case 0x7:
                        header = Arrays.copyOfRange(packetBytes, 0, 16);
                        payload = Arrays.copyOfRange(packetBytes, 16, packetBytes.length);
                        return;
                }
                break;

            case PARTITION_COMMAND:
                if (highNibble == 0x4) {
                    header = Arrays.copyOfRange(packetBytes, 0, 16);
                    payload = Arrays.copyOfRange(packetBytes, 16, 16 + packetBytes[1]);
                    logger.debug("Received a valid response for partition command");
                    return;
                }
                break;

            case ZONE_COMMAND:
                if (highNibble == 0xD) {
                    header = Arrays.copyOfRange(packetBytes, 0, 16);
                    payload = Arrays.copyOfRange(packetBytes, 16, 16 + packetBytes[1]);
                    logger.debug("Received a valid response for zone command");
                    return;
                }
                break;
        }

        // All other cases are considered wrong results for the parser and are probably live events which cannot be
        // parsed currently
        logger.debug("Message command not expected. Received command={}", String.format("0x%08X", receivedCommand));
        header = null;
        payload = null;
    }

    @Override
    public String toString() {
        return "Response [request=" + request + ", packetBytes=" + ParadoxUtil.byteArrayToString(packetBytes) + "]";
    }
}
