<<<<<<< HEAD:bundles/org.openhab.binding.samsungtv/src/main/java/org/openhab/binding/samsungtv/internal/service/RemoteControllerService.java
/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.service;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.samsungtv.internal.protocol.KeyCode;
import org.openhab.binding.samsungtv.internal.protocol.RemoteController;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerException;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerLegacy;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerWebSocket;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerWebsocketCallback;
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
 * @author Arjan Mels - Implemented websocket interface for recent TVs
 */
public class RemoteControllerService implements SamsungTvService, RemoteControllerWebsocketCallback {

    private Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    public static final String SERVICE_NAME = "RemoteControlReceiver";

    private final List<String> supportedCommandsUpnp = Arrays.asList(KEY_CODE, POWER, CHANNEL);
    private final List<String> supportedCommandsNonUpnp = Arrays.asList(KEY_CODE, VOLUME, MUTE, POWER, CHANNEL);
    private final List<String> extraSupportedCommandsWebSocket = Arrays.asList(BROWSER_URL, SOURCE_APP, ART_MODE);

    private String host;
    private int port;
    private boolean upnp;

    private Set<EventListener> listeners = new CopyOnWriteArraySet<>();

    private RemoteControllerService(String host, int port, boolean upnp) {
        logger.debug("Create a Samsung TV RemoteController service: " + upnp);
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
        List<String> supported = upnp ? supportedCommandsUpnp : supportedCommandsNonUpnp;
        if (remoteController instanceof RemoteControllerWebSocket) {
            supported = new ArrayList<>(supported);
            supported.addAll(extraSupportedCommandsWebSocket);
        }
        logger.debug("getSupportedChannelNames: {}", supported);
        return supported;
    }

    @Override
    public void addEventListener(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        listeners.remove(listener);
    }

    public boolean checkConnection() {
        return remoteController != null;
    }

    RemoteController remoteController = null;

    @Override
    public void start() {
        if (remoteController != null) {
            try {
                remoteController.openConnection();
            } catch (RemoteControllerException e) {
                logger.warn("Cannot open remote interface ({})" + e.getMessage());
            }
            return;
        }

        // first try legacy interface
        try {
            RemoteController remoteLegacy = new RemoteControllerLegacy(host, port, "openHAB", "openHAB");
            remoteLegacy.openConnection();
            // will not reach this if exception thrown in openConnection
            remoteController = remoteLegacy;
            logger.info("Using legacy remote interface");
        } catch (RemoteControllerException ignore) {
        }

        // then try websocket: may give timeout
        try {
            RemoteController remoteWebSocket = new RemoteControllerWebSocket(host, 8001, "openHAB", "openHAB", this);
            remoteWebSocket.openConnection();
            // will not reach this if exception thrown in openConnection
            remoteController = remoteWebSocket;
            logger.info("Using websocket remote interface");
            return;
        } catch (RemoteControllerException ignore) {
        }

        logger.info("No remote interface detected");
        remoteController = null;
    }

    @Override
    public void stop() {
        if (remoteController != null) {
            try {
                remoteController.close();
            } catch (Exception ignore) {
            }
        }
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

        if (remoteController == null) {
            return;
        }

        KeyCode key = null;

        if (remoteController instanceof RemoteControllerWebSocket) {
            RemoteControllerWebSocket remoteControllerWebSocket = (RemoteControllerWebSocket) remoteController;
            switch (channel) {
                case BROWSER_URL:
                    if (command instanceof StringType) {
                        remoteControllerWebSocket.sendUrl(command.toString());
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
                case SOURCE_APP:
                    if (command instanceof StringType) {
                        remoteControllerWebSocket.sendSourceApp(command.toString());
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
                case POWER:
                    if (command instanceof OnOffType) {
                        // websocket uses KEY_POWER
                        // send key only to toggle state
                        if (OnOffType.ON.equals(command) != power) {
                            sendKeyCode(KeyCode.KEY_POWER);
                        }
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
                case ART_MODE:
                    if (command instanceof OnOffType) {
                        // websocket uses KEY_POWER
                        // send key only to toggle state when power = off
                        if (!power) {
                            if (OnOffType.ON.equals(command)) {
                                if (!artmode) {
                                    sendKeyCode(KeyCode.KEY_POWER);
                                }
                            } else {
                                sendKeyCodePress(KeyCode.KEY_POWER);
                                // really switch off
                            }
                        } else {
                            // switch TV off
                            sendKeyCode(KeyCode.KEY_POWER);
                            // switch TV to art mode
                            sendKeyCode(KeyCode.KEY_POWER);
                        }
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
            }
        }

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
                        logger.warn("Remote control: Command '{}' not supported for channel '{}'", command, channel);
                    }
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;

            case POWER:
                if (command instanceof OnOffType) {
                    // legacy controller uses KEY_POWERON/OFF
                    if (command.equals(OnOffType.ON)) {
                        sendKeyCode(KeyCode.KEY_POWERON);
                    } else {
                        sendKeyCode(KeyCode.KEY_POWEROFF);
                    }
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;

            case MUTE:
                sendKeyCode(KeyCode.KEY_MUTE);
                return;

            case VOLUME:
                if (command instanceof UpDownType) {
                    if (command.equals(UpDownType.UP)) {
                        sendKeyCode(KeyCode.KEY_VOLUP);
                    } else {
                        sendKeyCode(KeyCode.KEY_VOLDOWN);
                    }
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;

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
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;
            default:
                logger.warn("Remote control: unsupported channel: {}", channel);
        }
    }

    /**
     * Sends a command to Samsung TV device.
     *
     * @param key Button code to send
     */
    private void sendKeyCode(KeyCode key) {
        try {
            if (remoteController != null) {
                remoteController.sendKey(key);
            }
        } catch (RemoteControllerException e) {
            reportError(String.format("Could not send command to device on %s:%d", host, port), e);
        }

    }

    private void sendKeyCodePress(KeyCode key) {
        try {
            if (remoteController != null && remoteController instanceof RemoteControllerWebSocket) {
                ((RemoteControllerWebSocket) remoteController).sendKeyPress(key);
            }
        } catch (RemoteControllerException e) {
            reportError(String.format("Could not send command to device on %s:%d", host, port), e);
        }

    }

    /**
     * Sends a sequence of command to Samsung TV device.
     *
     * @param keys List of button codes to send
     */
    private void sendKeyCodes(final List<KeyCode> keys) {
        try {
            if (remoteController != null) {
                remoteController.sendKeys(keys);
            }
        } catch (RemoteControllerException e) {
            reportError(String.format("Could not send command to device on %s:%d", host, port), e);
        }
    }

    private void reportError(String message, RemoteControllerException e) {
        reportError(ThingStatusDetail.COMMUNICATION_ERROR, message, e);
    }

    private void reportError(ThingStatusDetail statusDetail, String message, RemoteControllerException e) {
        for (EventListener listener : listeners) {
            listener.reportError(statusDetail, message, e);
        }
    }

    @Override
    public void appsUpdated(List<String> apps) {
        // do nothing
    }

    @Override
    public void currentAppUpdated(String app) {
        for (EventListener listener : listeners) {
            listener.valueReceived(SOURCE_APP, new StringType(app));
        }
    }

    boolean power = true;
    boolean artmode = false;

    @Override
    public void powerUpdated(boolean on, boolean artmode) {
        power = on;
        this.artmode = artmode;
        for (EventListener listener : listeners) {
            listener.valueReceived(POWER, on ? OnOffType.ON : OnOffType.OFF);
            listener.valueReceived(ART_MODE, artmode ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    public void connectionError(Throwable error) {
        try {
            if (remoteController != null) {
                remoteController.close();
            }
        } catch (Exception e) {
            logger.warn("Error in connection close: {}", e.getMessage());
        }
        remoteController = null;
    }

}
=======
/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.service;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.samsungtv.internal.protocol.KeyCode;
import org.openhab.binding.samsungtv.internal.protocol.RemoteController;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerException;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerLegacy;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerWebSocket;
import org.openhab.binding.samsungtv.internal.protocol.RemoteControllerWebsocketCallback;
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
 * @author Arjan Mels - Implemented websocket interface for recent TVs
 */
public class RemoteControllerService implements SamsungTvService, RemoteControllerWebsocketCallback {

    private Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    public static final String SERVICE_NAME = "RemoteControlReceiver";

    private final List<String> supportedCommandsUpnp = Arrays.asList(KEY_CODE, POWER, CHANNEL);
    private final List<String> supportedCommandsNonUpnp = Arrays.asList(KEY_CODE, VOLUME, MUTE, POWER, CHANNEL);
    private final List<String> extraSupportedCommandsWebSocket = Arrays.asList(BROWSER_URL, SOURCE_APP, ART_MODE);

    private String host;
    private int port;
    private boolean upnp;

    boolean power = true;
    boolean artMode = false;

    private boolean artModeSupported = false;

    private Set<EventListener> listeners = new CopyOnWriteArraySet<>();

    private RemoteControllerService(String host, int port, boolean upnp) {
        logger.debug("Create a Samsung TV RemoteController service: " + upnp);
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
        List<String> supported = upnp ? supportedCommandsUpnp : supportedCommandsNonUpnp;
        if (remoteController instanceof RemoteControllerWebSocket) {
            supported = new ArrayList<>(supported);
            supported.addAll(extraSupportedCommandsWebSocket);
        }
        logger.debug("getSupportedChannelNames: {}", supported);
        return supported;
    }

    @Override
    public void addEventListener(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        listeners.remove(listener);
    }

    public boolean checkConnection() {
        return remoteController != null;
    }

    RemoteController remoteController = null;

    @Override
    public void start() {
        if (remoteController != null) {
            try {
                remoteController.openConnection();
            } catch (RemoteControllerException e) {
                logger.warn("Cannot open remote interface ({})" + e.getMessage());
            }
            return;
        }

        // first try legacy interface
        try {
            RemoteController remoteLegacy = new RemoteControllerLegacy(host, port, "openHAB", "openHAB");
            remoteLegacy.openConnection();
            // will not reach this if exception thrown in openConnection
            remoteController = remoteLegacy;
            logger.info("Using legacy remote interface");
            artModeSupported = false;
        } catch (RemoteControllerException ignore) {
        }

        // then try websocket: may give timeout
        try {
            RemoteController remoteWebSocket = new RemoteControllerWebSocket(host, 8001, "openHAB", "openHAB", this);
            remoteWebSocket.openConnection();
            // will not reach this if exception thrown in openConnection
            remoteController = remoteWebSocket;
            logger.info("Using websocket remote interface");
            return;
        } catch (RemoteControllerException ignore) {
        }

        logger.info("No remote interface detected");
        remoteController = null;
    }

    @Override
    public void stop() {
        if (remoteController != null) {
            try {
                remoteController.close();
            } catch (Exception ignore) {
            }
        }
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

        if (remoteController == null) {
            return;
        }

        KeyCode key = null;

        if (remoteController instanceof RemoteControllerWebSocket) {
            RemoteControllerWebSocket remoteControllerWebSocket = (RemoteControllerWebSocket) remoteController;
            switch (channel) {
                case BROWSER_URL:
                    if (command instanceof StringType) {
                        remoteControllerWebSocket.sendUrl(command.toString());
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
                case SOURCE_APP:
                    if (command instanceof StringType) {
                        remoteControllerWebSocket.sendSourceApp(command.toString());
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
                case POWER:
                    if (command instanceof OnOffType) {
                        // websocket uses KEY_POWER
                        // send key only to toggle state
                        if (OnOffType.ON.equals(command) != power) {
                            sendKeyCode(KeyCode.KEY_POWER);
                        }
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
                case ART_MODE:
                    if (command instanceof OnOffType) {
                        // websocket uses KEY_POWER
                        // send key only to toggle state when power = off
                        if (!power) {
                            if (OnOffType.ON.equals(command)) {
                                if (!artMode) {
                                    sendKeyCode(KeyCode.KEY_POWER);
                                }
                            } else {
                                sendKeyCodePress(KeyCode.KEY_POWER);
                                // really switch off
                            }
                        } else {
                            // switch TV off
                            sendKeyCode(KeyCode.KEY_POWER);
                            // switch TV to art mode
                            sendKeyCode(KeyCode.KEY_POWER);
                        }
                    } else {
                        logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                    }
                    return;
            }
        }

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
                        logger.warn("Remote control: Command '{}' not supported for channel '{}'", command, channel);
                    }
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;

            case POWER:
                if (command instanceof OnOffType) {
                    // legacy controller uses KEY_POWERON/OFF
                    if (command.equals(OnOffType.ON)) {
                        sendKeyCode(KeyCode.KEY_POWERON);
                    } else {
                        sendKeyCode(KeyCode.KEY_POWEROFF);
                    }
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;

            case MUTE:
                sendKeyCode(KeyCode.KEY_MUTE);
                return;

            case VOLUME:
                if (command instanceof UpDownType) {
                    if (command.equals(UpDownType.UP)) {
                        sendKeyCode(KeyCode.KEY_VOLUP);
                    } else {
                        sendKeyCode(KeyCode.KEY_VOLDOWN);
                    }
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;

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
                } else {
                    logger.warn("Remote control: unsupported command type {} for channel", command, channel);
                }
                return;
            default:
                logger.warn("Remote control: unsupported channel: {}", channel);
        }
    }

    /**
     * Sends a command to Samsung TV device.
     *
     * @param key Button code to send
     */
    private void sendKeyCode(KeyCode key) {
        try {
            if (remoteController != null) {
                remoteController.sendKey(key);
            }
        } catch (RemoteControllerException e) {
            reportError(String.format("Could not send command to device on %s:%d", host, port), e);
        }

    }

    private void sendKeyCodePress(KeyCode key) {
        try {
            if (remoteController != null && remoteController instanceof RemoteControllerWebSocket) {
                ((RemoteControllerWebSocket) remoteController).sendKeyPress(key);
            }
        } catch (RemoteControllerException e) {
            reportError(String.format("Could not send command to device on %s:%d", host, port), e);
        }

    }

    /**
     * Sends a sequence of command to Samsung TV device.
     *
     * @param keys List of button codes to send
     */
    private void sendKeyCodes(final List<KeyCode> keys) {
        try {
            if (remoteController != null) {
                remoteController.sendKeys(keys);
            }
        } catch (RemoteControllerException e) {
            reportError(String.format("Could not send command to device on %s:%d", host, port), e);
        }
    }

    private void reportError(String message, RemoteControllerException e) {
        reportError(ThingStatusDetail.COMMUNICATION_ERROR, message, e);
    }

    private void reportError(ThingStatusDetail statusDetail, String message, RemoteControllerException e) {
        for (EventListener listener : listeners) {
            listener.reportError(statusDetail, message, e);
        }
    }

    @Override
    public void appsUpdated(List<String> apps) {
        // do nothing
    }

    @Override
    public void currentAppUpdated(String app) {
        for (EventListener listener : listeners) {
            listener.valueReceived(SOURCE_APP, new StringType(app));
        }
    }

    @Override
    public void powerUpdated(boolean on, boolean artmode) {
        artModeSupported = true;
        power = on;
        this.artMode = artmode;

        for (EventListener listener : listeners) {
            // order of state updates is important to prevent extraneous transitions in overall state
            if (on) {
                listener.valueReceived(POWER, on ? OnOffType.ON : OnOffType.OFF);
                listener.valueReceived(ART_MODE, artmode ? OnOffType.ON : OnOffType.OFF);
            } else {
                listener.valueReceived(ART_MODE, artmode ? OnOffType.ON : OnOffType.OFF);
                listener.valueReceived(POWER, on ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    @Override
    public void connectionError(Throwable error) {
        try {
            if (remoteController != null) {
                remoteController.close();
            }
        } catch (Exception e) {
            logger.warn("Error in connection close: {}", e.getMessage());
        }
        remoteController = null;
    }

    public boolean isArtModeSupported() {
        return artModeSupported;
    }

}
>>>>>>> Prevent extraneous ON-OFF and OFF-ON transitions when TV supports Art:addons/binding/org.openhab.binding.samsungtv/src/main/java/org/openhab/binding/samsungtv/internal/service/RemoteControllerService.java
