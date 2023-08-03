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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.KEY_CODE_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTvBindingConstants.TV_OFFLINE_MSG;

import java.io.IOException;

import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTvService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.keycode.KeyCodeDto;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KeyCodeService} is responsible for handling key code commands, which emulate a button
 * press on a remote control.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class KeyCodeService implements PhilipsTvService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    public KeyCodeService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        KeyCode keyCode = null;
        if (isSupportedCommand(command)) {
            // Three approaches to resolve the KEY_CODE
            try {
                keyCode = KeyCode.valueOf(command.toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                try {
                    keyCode = KeyCode.valueOf("KEY_" + command.toString().toUpperCase());
                } catch (IllegalArgumentException e2) {
                    try {
                        keyCode = KeyCode.getKeyCodeForValue(command.toString());
                    } catch (IllegalArgumentException e3) {
                        // do nothing, error message is logged later
                    }
                }
            }

            if (keyCode != null) {
                try {
                    sendKeyCode(keyCode);
                } catch (Exception e) {
                    if (isTvOfflineException(e)) {
                        logger.warn("Could not execute command for key code, the TV is offline.");
                        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
                    } else if (isTvNotListeningException(e)) {
                        handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                TV_NOT_LISTENING_MSG);
                    } else {
                        logger.warn("Unknown error occurred while sending keyCode code {}: {}", keyCode, e.getMessage(),
                                e);
                    }
                }
            } else {
                logger.warn("Command '{}' not a supported keyCode code.", command);
            }
        } else {
            if (!(command instanceof RefreshType)) { // RefreshType is valid but ignored
                logger.warn("Not a supported command: {}", command);
            }
        }
    }

    private static boolean isSupportedCommand(Command command) {
        return (command instanceof StringType) || (command instanceof NextPreviousType)
                || (command instanceof PlayPauseType) || (command instanceof RewindFastforwardType);
    }

    private void sendKeyCode(KeyCode key) throws IOException {
        KeyCodeDto keyCodeDto = new KeyCodeDto();
        keyCodeDto.setKey(key);
        String keyCodeJson = OBJECT_MAPPER.writeValueAsString(keyCodeDto);
        logger.debug("KeyCode Json sent: {}", keyCodeJson);
        connectionManager.doHttpsPost(KEY_CODE_PATH, keyCodeJson);
    }
}
