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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.keypress.KeyPressDTO;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KeyPressService} is responsible for handling key code commands, which emulate a button
 * press on a remote control.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class KeyPressService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    public KeyPressService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        KeyPress keyPress = null;
        if (isSupportedCommand(command)) {
            // Three approaches to resolve the KEY_CODE
            try {
                keyPress = KeyPress.valueOf(command.toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                try {
                    keyPress = KeyPress.valueOf("KEY_" + command.toString().toUpperCase());
                } catch (IllegalArgumentException e2) {
                    try {
                        keyPress = KeyPress.getKeyPressForValue(command.toString());
                    } catch (IllegalArgumentException e3) {
                        logger.trace("KeyPress threw IllegalArgumentException", e3);
                    }
                }
            }

            if (keyPress != null) {
                try {
                    sendKeyPress(keyPress);
                } catch (Exception e) {
                    if (isTvOfflineException(e)) {
                        logger.debug("Could not execute command for key code, the TV is offline.");
                        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
                    } else if (isTvNotListeningException(e)) {
                        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                TV_NOT_LISTENING_MSG);
                    } else {
                        logger.warn("Unknown error occurred while sending keyPress code {}: {}", keyPress,
                                e.getMessage(), e);
                    }
                }
            } else {
                logger.warn("Command '{}' not a supported keyPress code.", command);
            }
        }
    }

    private static boolean isSupportedCommand(Command command) {
        return (command instanceof StringType) || (command instanceof NextPreviousType)
                || (command instanceof PlayPauseType) || (command instanceof RewindFastforwardType);
    }

    private void sendKeyPress(KeyPress key) throws IOException {
        KeyPressDTO keyPressDTO = new KeyPressDTO(key);
        String keyPressJson = OBJECT_MAPPER.writeValueAsString(keyPressDTO);
        logger.debug("KeyPress Json sent: {}", keyPressJson);
        connectionManager.doHttpsPost(KEY_CODE_PATH, keyPressJson);
    }
}
