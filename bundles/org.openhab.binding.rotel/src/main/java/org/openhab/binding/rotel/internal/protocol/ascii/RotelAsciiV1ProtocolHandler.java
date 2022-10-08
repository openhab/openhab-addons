/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.protocol.ascii;

import static org.openhab.binding.rotel.internal.RotelBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.communication.RotelCommand;
import org.openhab.binding.rotel.internal.protocol.RotelProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the Rotel ASCII V1 protocol (build of command messages, decoding of incoming data)
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelAsciiV1ProtocolHandler extends RotelAbstractAsciiProtocolHandler {

    private static final char CHAR_END_RESPONSE = '!';

    private static final Set<String> KEYSET1 = Set.of(KEY_DISPLAY, KEY_DISPLAY1, KEY_DISPLAY2, KEY_DISPLAY3,
            KEY_DISPLAY4, KEY_PRODUCT_TYPE, KEY_PRODUCT_VERSION, KEY_TC_VERSION, KEY_TRACK);
    private static final Set<String> KEYSET2 = Set.of(KEY_FM_PRESET, KEY_FM_ALL_PRESET, KEY_DAB_PRESET,
            KEY_DAB_ALL_PRESET, KEY_IRADIO_PRESET, KEY_IRADIO_ALL_PRESET);

    private final Logger logger = LoggerFactory.getLogger(RotelAsciiV1ProtocolHandler.class);

    private final byte[] lengthBuffer = new byte[8];
    private boolean searchKey = true;
    private boolean searchLength;
    private int valueLength;
    private int indexLengthBuffer;

    /**
     * Constructor
     *
     * @param model the Rotel model in use
     */
    public RotelAsciiV1ProtocolHandler(RotelModel model) {
        super(model);
    }

    @Override
    public RotelProtocol getProtocol() {
        return RotelProtocol.ASCII_V1;
    }

    @Override
    public byte[] buildCommandMessage(RotelCommand cmd, @Nullable Integer value) throws RotelException {
        String messageStr = cmd.getAsciiCommandV1();
        if (messageStr == null) {
            throw new RotelException("Command \"" + cmd.getLabel() + "\" ignored: not available for ASCII V1 protocol");
        }
        if (value != null) {
            switch (cmd) {
                case VOLUME_SET:
                    messageStr += String.format("%d", value);
                    break;
                case BASS_SET:
                case TREBLE_SET:
                    if (value == 0) {
                        messageStr += "000";
                    } else if (value > 0) {
                        messageStr += String.format("+%02d", value);
                    } else {
                        messageStr += String.format("-%02d", -value);
                    }
                    break;
                case BALANCE_SET:
                    if (value == 0) {
                        messageStr += "000";
                    } else if (value > 0) {
                        messageStr += String.format("R%02d", value);
                    } else {
                        messageStr += String.format("L%02d", -value);
                    }
                    break;
                case DIMMER_LEVEL_SET:
                    if (value > 0 && model.getDimmerLevelMin() < 0) {
                        messageStr += String.format("+%d", value);
                    } else {
                        messageStr += String.format("%d", value);
                    }
                    break;
                case CALL_FM_PRESET:
                case CALL_DAB_PRESET:
                case CALL_IRADIO_PRESET:
                    messageStr += String.format("%02d", value);
                    break;
                default:
                    break;
            }
        }
        if (!messageStr.endsWith("?")) {
            messageStr += "!";
        }
        byte[] message = messageStr.getBytes(StandardCharsets.US_ASCII);
        logger.debug("Command \"{}\" => {}", cmd, messageStr);
        return message;
    }

    @Override
    public void handleIncomingData(byte[] inDataBuffer, int length) {
        for (int i = 0; i < length; i++) {
            boolean end = false;
            if (searchKey && inDataBuffer[i] == '=') {
                // End of key reading, check if the value is a fixed or variable length
                searchKey = false;
                byte[] dataKey = getDataBuffer();
                String key = new String(dataKey, 0, dataKey.length, StandardCharsets.US_ASCII).trim();
                searchLength = isVariableLengthApplicable(key);
                indexLengthBuffer = 0;
                valueLength = 0;
                logger.trace("handleIncomingData: key = *{}* {}", key, searchLength ? "variable" : "fixed");
                fillDataBuffer(inDataBuffer[i]);
            } else if (searchKey) {
                // Reading key
                fillDataBuffer(inDataBuffer[i]);
            } else if (searchLength && inDataBuffer[i] == ',') {
                // End of value length reading
                searchLength = false;
                byte[] lengthData = Arrays.copyOf(lengthBuffer, indexLengthBuffer);
                String lengthStr = new String(lengthData, 0, lengthData.length, StandardCharsets.US_ASCII);
                valueLength = Integer.parseInt(lengthStr);
                logger.trace("handleIncomingData: valueLength = {}", valueLength);
                if (getRemainingSizeInDataBuffer() < valueLength) {
                    logger.warn(
                            "handleIncomingData: the size of the internal buffer is too small, reponse will be truncated");
                }
                end = valueLength == 0;
            } else if (searchLength) {
                // Reading value length
                lengthBuffer[indexLengthBuffer++] = inDataBuffer[i];
            } else if (valueLength > 0) {
                // Reading value (variable length)
                fillDataBuffer(inDataBuffer[i]);
                valueLength--;
                end = valueLength == 0;
            } else if (inDataBuffer[i] == CHAR_END_RESPONSE) {
                // End of value reading
                end = true;
            } else {
                // Reading value (fixed length)
                fillDataBuffer(inDataBuffer[i]);
            }
            if (end) {
                handleIncomingMessage(getDataBuffer());
                resetDataBuffer();
                searchKey = true;
                searchLength = false;
            }
        }
    }

    private boolean isVariableLengthApplicable(String key) {
        return KEYSET1.contains(key) || KEYSET2.stream().filter(k -> key.startsWith(k)).count() > 0;
    }
}
