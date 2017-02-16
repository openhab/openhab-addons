/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.service;

import static org.openhab.binding.samsungtv.SamsungTvBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.samsungtv.internal.protocol.KeyCode;
import org.openhab.binding.samsungtv.internal.protocol.RemoteController;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerException;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.binding.samsungtv.internal.service.api.ValueReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteControllerService} is responsible for handling remote
 * controller commands.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RemoteControllerService implements SamsungTvService {

    public static final String SERVICE_NAME = "RemoteControlReceiver";
    private final List<String> supportedCommands = Arrays.asList(KEY_CODE, POWER, CHANNEL);

    private Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    private String host;
    private int port;

    public RemoteControllerService(String host, int port) {
        logger.debug("Create a Samsung TV RemoteController service");
        this.host = host;
        this.port = port;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<String> getSupportedChannelNames() {
        return supportedCommands;
    }

    @Override
    public void addEventListener(ValueReceiver listener) {
        // This service does not send any value updates
    }

    @Override
    public void removeEventListener(ValueReceiver listener) {
    }

    @Override
    public void start() {
        // nothing to start
    }

    @Override
    public void stop() {
    }

    @Override
    public void clearCache() {
    }

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

        KeyCode key = null;

        switch (channel) {
            case KEY_CODE:
                if (command instanceof StringType) {

                    try {
                        key = KeyCode.valueOf(command.toString().toUpperCase());
                    } catch (Exception e) {

                        try {
                            key = KeyCode.valueOf("KEY_" + command.toString().toUpperCase());
                        } catch (Exception e2) {
                            // do nothing, error message is logged later
                        }
                    }

                    if (key != null) {
                        sendKeyCode(key);
                    } else {
                        logger.warn("Command '{}' not supported for channel '{}'", command, channel);
                    }
                }
                break;

            case POWER:
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        sendKeyCode(KeyCode.KEY_POWERON);
                    } else {
                        sendKeyCode(KeyCode.KEY_POWEROFF);
                    }
                }
                break;

            case CHANNEL:
                if (command instanceof DecimalType) {
                    int val = ((DecimalType) command).intValue();
                    int num4 = val / 1000 % 10;
                    int num3 = val / 100 % 10;
                    int num2 = val / 10 % 10;
                    int num1 = val % 10;

                    List<KeyCode> commands = new ArrayList<KeyCode>();

                    if (num4 > 0) {
                        commands.add(KeyCode.valueOf("KEY_" + num4));
                    }
                    if (num4 > 0 || num3 > 0) {
                        commands.add(KeyCode.valueOf("KEY_" + num3));
                    }
                    if (num4 > 0 || num3 > 0 || num2 > 0) {
                        commands.add(KeyCode.valueOf("KEY_" + num2));
                    }
                    commands.add(KeyCode.valueOf("KEY_" + num1));
                    commands.add(KeyCode.KEY_ENTER);
                    sendKeyCodes(commands);
                }
                break;
        }
    }

    /**
     * Sends a command to Samsung TV device.
     * 
     * @param key
     *            Button code to send
     */
    private void sendKeyCode(final KeyCode key) {

        if (host != null) {

            RemoteController remoteController = new RemoteController(host, port, "openHAB2", "openHAB2");

            if (remoteController != null) {
                try {
                    remoteController.sendKey(key);

                } catch (RemoteControllerException e) {
                    logger.error("Could not send command to device on {}: {}", host + ":" + port, e);
                }
            }
        } else {
            logger.error("TV network address not defined");
        }
    }

    /**
     * Sends a sequence of command to Samsung TV device.
     * 
     * @param key
     *            Button code to send
     */
    private void sendKeyCodes(final List<KeyCode> keys) {

        if (host != null) {

            RemoteController remoteController = new RemoteController(host, port, "openHAB2", "openHAB2");

            if (remoteController != null) {
                try {
                    remoteController.sendKeys(keys);

                } catch (RemoteControllerException e) {
                    logger.error("Could not send command(s) to device on {}: {}", host + ":" + port, e);
                }
            }
        } else {
            logger.error("TV network address not defined");
        }
    }

}
