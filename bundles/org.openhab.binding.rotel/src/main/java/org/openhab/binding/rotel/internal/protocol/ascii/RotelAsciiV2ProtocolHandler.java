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
package org.openhab.binding.rotel.internal.protocol.ascii;

import static org.openhab.binding.rotel.internal.RotelBindingConstants.*;

import java.nio.charset.StandardCharsets;
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
 * Class for handling the Rotel ASCII V2 protocol (build of command messages, decoding of incoming data)
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelAsciiV2ProtocolHandler extends RotelAbstractAsciiProtocolHandler {

    private static final char CHAR_END_RESPONSE = '$';

    private static final Set<String> KEYSET = Set.of(KEY_DISC_NAME, KEY_DISC_TYPE, KEY_TRACK_NAME, KEY_TIME, KEY_FM_RDS,
            KEY_DAB_STATION);

    private final Logger logger = LoggerFactory.getLogger(RotelAsciiV2ProtocolHandler.class);

    private boolean searchKey = true;
    private boolean variableLength;
    private boolean prevIsEndCharacter;

    /**
     * Constructor
     *
     * @param model the Rotel model in use
     */
    public RotelAsciiV2ProtocolHandler(RotelModel model) {
        super(model);
    }

    @Override
    public RotelProtocol getProtocol() {
        return RotelProtocol.ASCII_V2;
    }

    @Override
    public byte[] buildCommandMessage(RotelCommand cmd, @Nullable Integer value) throws RotelException {
        String messageStr = cmd.getAsciiCommandV2();
        if (messageStr == null) {
            throw new RotelException("Command \"" + cmd.getLabel() + "\" ignored: not available for ASCII V2 protocol");
        }
        if (value != null) {
            switch (cmd) {
                case VOLUME_SET:
                case ZONE1_VOLUME_SET:
                case ZONE2_VOLUME_SET:
                case ZONE3_VOLUME_SET:
                case ZONE4_VOLUME_SET:
                    messageStr += String.format("%02d", value);
                    break;
                case BASS_SET:
                case ZONE1_BASS_SET:
                case ZONE2_BASS_SET:
                case ZONE3_BASS_SET:
                case ZONE4_BASS_SET:
                case TREBLE_SET:
                case ZONE1_TREBLE_SET:
                case ZONE2_TREBLE_SET:
                case ZONE3_TREBLE_SET:
                case ZONE4_TREBLE_SET:
                    if (value == 0) {
                        messageStr += "000";
                    } else if (value > 0) {
                        messageStr += String.format("+%02d", value);
                    } else {
                        messageStr += String.format("-%02d", -value);
                    }
                    break;
                case BALANCE_SET:
                case ZONE1_BALANCE_SET:
                case ZONE2_BALANCE_SET:
                case ZONE3_BALANCE_SET:
                case ZONE4_BALANCE_SET:
                    if (value == 0) {
                        messageStr += "000";
                    } else if (value > 0) {
                        messageStr += String.format("r%02d", value);
                    } else {
                        messageStr += String.format("l%02d", -value);
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
                variableLength = KEYSET.contains(key);
                logger.trace("handleIncomingData: key = *{}* {}", key, variableLength ? "variable" : "fixed");
                fillDataBuffer(inDataBuffer[i]);
            } else if (searchKey) {
                // Reading key
                fillDataBuffer(inDataBuffer[i]);
            } else if (inDataBuffer[i] == CHAR_END_RESPONSE) {
                end = !variableLength || prevIsEndCharacter;
            } else {
                if (prevIsEndCharacter) {
                    // End character inside a variable length value
                    fillDataBuffer((byte) CHAR_END_RESPONSE);
                }
                // Reading value
                fillDataBuffer(inDataBuffer[i]);
            }
            if (end) {
                // End of value reading
                handleIncomingMessage(getDataBuffer());
                resetDataBuffer();
                searchKey = true;
                variableLength = false;
                prevIsEndCharacter = false;
            } else {
                prevIsEndCharacter = inDataBuffer[i] == CHAR_END_RESPONSE;
            }
        }
    }

    @Override
    protected void dispatchKeyValue(String key, String value) {
        // For distribution amplifiers, we need to split certain values to get the value for each zone
        if (model == RotelModel.C8 && value.contains(",")) {
            switch (key) {
                case KEY_INPUT:
                case KEY_VOLUME:
                case KEY_MUTE:
                case KEY_BASS:
                case KEY_TREBLE:
                case KEY_BALANCE:
                case KEY_FREQ:
                    String[] splitValues = value.split(",");
                    int nb = splitValues.length;
                    if (nb > MAX_NUMBER_OF_ZONES) {
                        nb = MAX_NUMBER_OF_ZONES;
                    }
                    for (int i = 1; i <= nb; i++) {
                        String val = KEY_INPUT.equals(key) ? String.format("z%d:input_%s", i, splitValues[i - 1])
                                : splitValues[i - 1];
                        dispatchKeyValue(String.format("%s_zone%d", key, i), val);
                    }
                    break;
                default:
                    super.dispatchKeyValue(key, value);
                    break;
            }
        } else {
            super.dispatchKeyValue(key, value);
        }
    }
}
