/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv.internal.service;

import static org.openhab.binding.panasonictv.PanasonicTvBindingConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jupnp.UpnpService;
import org.openhab.binding.panasonictv.internal.protocol.RemoteControllerException;
import org.openhab.binding.panasonictv.internal.protocol.UpnpRemoteController;
import org.openhab.binding.panasonictv.internal.service.api.EventListener;
import org.openhab.binding.panasonictv.internal.service.api.PanasonicTvService;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteControllerService} is responsible for handling remote
 * controller commands.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
public class RemoteControllerService implements UpnpIOParticipant, PanasonicTvService {

    private Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    private UpnpService service;

    static final String SERVICE_NAME = "p00RemoteController";

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());
    private final List<String> supportedCommandsUpnp = Arrays.asList(KEY_CODE);

    private String udn;
    private String host;
    private int port;
    private UpnpRemoteController remoteController;

    private List<EventListener> listeners = new CopyOnWriteArrayList<>();

    private RemoteControllerService(UpnpService upnpService, String udn, String host, int port, boolean upnp) {
        logger.debug("Create a Panasonic TV RemoteController service");
        this.host = host;
        this.port = port;
        this.udn = udn;
        this.service = upnpService;
        this.remoteController = new UpnpRemoteController(upnpService);
    }

    static RemoteControllerService createUpnpService(UpnpService upnpService, String udn, String host, int port) {
        return new RemoteControllerService(upnpService, udn, host, port, true);
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
        return true;
    }

    @Override
    public List<String> getSupportedChannelNames() {
        return supportedCommandsUpnp;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

        switch (channel) {
            case KEY_CODE:
                if (command instanceof StringType) {
                    sendKeyCode(command.toString().toUpperCase());
                } else {
                    logger.warn("Command '{}' not supported for channel '{}'", command, channel);
                }
                break;
        }
    }

    /**
     * Sends a key code to Panasonic TV device.
     *
     * @param key Button code to send
     */
    private static final List<String> tvInputKeyCodes = Arrays.asList("NRC_HDMI1-ONOFF", "NRC_HDMI2-ONOFF",
            "NRC_HDMI3-ONOFF", "NRC_HDMI4-ONOFF", "NRC_TV-ONOFF", "NRC_VIDEO1-ONOFF", "NRC_VIDEO2-ONOFF");

    private void sendKeyCode(final String key) {
        Map<String, String> result = remoteController.invokeAction(this, "p00NetworkControl", "X_SendKey",
                PanasonicTvUtils.buildHashMap("X_KeyEvent", key));

        if (tvInputKeyCodes.contains(key)) {
            onValueReceived("sourceName", key.substring(4, key.length() - 6), "p00NetworkControl");
        }
    }

    protected Map<String, String> updateResourceState(String serviceId, String actionId, Map<String, String> inputs) {
        Map<String, String> result = remoteController.invokeAction(this, serviceId, actionId, inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), serviceId);
        }

        return result;
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

    @Override
    public String getUDN() {
        return udn;
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("PanasonicTV RemoteControl status changed " + status);
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {

        String oldValue = stateMap.get(variable);
        if ((value == null && oldValue == null) || (value != null && value.equals(oldValue))) {
            logger.trace("Value '{}' for {} hasn't changed, ignoring update", value, variable);
            return;
        }

        stateMap.put(variable, value);

        for (EventListener listener : listeners) {
            switch (variable) {
                case SOURCE_NAME:
                case SOURCE_ID:
                    listener.valueReceived(SOURCE_NAME, (value != null) ? StringType.valueOf(value) : UnDefType.UNDEF);
                    listener.valueReceived(SOURCE_ID, (value != null) ? StringType.valueOf(value) : UnDefType.UNDEF);
                    break;
            }
        }
    }
}
