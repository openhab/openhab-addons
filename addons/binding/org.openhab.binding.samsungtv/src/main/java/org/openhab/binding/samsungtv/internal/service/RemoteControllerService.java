/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.samsungtv.internal.protocol.KeyCode;
import org.openhab.binding.samsungtv.internal.protocol.RemoteController;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerException;
import org.openhab.binding.samsungtv.internal.service.api.EventListener;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteControllerService} is responsible for handling remote
 * controller commands.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Martin van Wingerden - Some changes for manually configured devices
 */
public class RemoteControllerService implements SamsungTvService {

    private Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    static final String SERVICE_NAME = "RemoteControlReceiver";

    private final List<String> supportedCommandsUpnp = Arrays.asList(KEY_CODE, POWER, CHANNEL);
    private final List<String> supportedCommandsNonUpnp = Arrays.asList(KEY_CODE, VOLUME, MUTE, POWER, CHANNEL);

    private String host;
    private int port;
    private boolean upnp;

    private List<EventListener> listeners = new CopyOnWriteArrayList<>();

    private RemoteControllerService(String host, int port, boolean upnp) {
        logger.debug("Create a Samsung TV RemoteController service");
        this.upnp = upnp;
        this.host = host;
        this.port = port;
    }

    static RemoteControllerService createUpnpService(String host, int port) {
        return new RemoteControllerService(host, port, true);
    }

    public static RemoteControllerService createNonUpnpService(String host, int port) {
        return new RemoteControllerService(host, port, false);
    }

    @Override
    public List<String> getSupportedChannelNames() {
        return upnp ? supportedCommandsUpnp : supportedCommandsNonUpnp;
    }

    @Override
    public void addEventListener(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        listeners.remove(listener);
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
    public boolean isUpnp() {
        return upnp;
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
                    } catch (IllegalArgumentException e) {
                        try {
                            key = KeyCode.valueOf("KEY_" + command.toString().toUpperCase());
                        } catch (IllegalArgumentException e2) {
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

            case MUTE:
                sendKeyCode(KeyCode.KEY_MUTE);
                break;

            case VOLUME:
                if (command instanceof UpDownType) {
                    if (command.equals(UpDownType.UP)) {
                        sendKeyCode(KeyCode.KEY_VOLUP);
                    } else {
                        sendKeyCode(KeyCode.KEY_VOLDOWN);
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
     * @param key Button code to send
     */
    private void sendKeyCode(final KeyCode key) {
        if (host != null) {
            try {
                getRemoteController().sendKey(key);
            } catch (RemoteControllerException e) {
                reportError(String.format("Could not send command to device on %s:%d", host, port), e);
            }
        } else {
            reportError(ThingStatusDetail.CONFIGURATION_ERROR, "TV network address not defined");
        }
    }

    /**
     * Sends a sequence of command to Samsung TV device.
     *
     * @param keys List of button codes to send
     */
    private void sendKeyCodes(final List<KeyCode> keys) {
        if (host != null) {
            try {
                getRemoteController().sendKeys(keys);
            } catch (RemoteControllerException e) {
                reportError(String.format("Could not send command to device on %s:%d", host, port), e);
            }
        } else {
            reportError(ThingStatusDetail.CONFIGURATION_ERROR, "TV network address not defined");
        }
    }

    private void reportError(ThingStatusDetail statusDetail, String message) {
        reportError(statusDetail, message, null);
    }

    private void reportError(String message, RemoteControllerException e) {
        reportError(ThingStatusDetail.COMMUNICATION_ERROR, message, e);
    }

    private void reportError(ThingStatusDetail statusDetail, String message, RemoteControllerException e) {
        for (EventListener listener : listeners) {
            listener.reportError(statusDetail, message, e);
        }
    }

    public boolean checkConnection() {
        try {
            getRemoteController().openConnection();
            return true;
        } catch (RemoteControllerException e) {
            logger.trace("Failed opening connection, check failed", e);
            return false;
        }
    }

    private RemoteController getRemoteController() {
        return new RemoteController(host, port, "openHAB", "openHAB");
    }
}
