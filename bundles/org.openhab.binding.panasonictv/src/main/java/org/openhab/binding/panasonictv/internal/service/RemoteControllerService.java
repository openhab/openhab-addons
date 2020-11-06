/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal.service;

import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.openhab.binding.panasonictv.internal.event.PanasonicEventListener;
import org.openhab.binding.panasonictv.internal.protocol.UpnpRemoteController;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteControllerService} is responsible for handling remote
 * controller commands.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class RemoteControllerService implements UpnpIOParticipant, PanasonicTvService {
    private Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    static final String SERVICE_NAME = "p00RemoteController";

    private Map<String, String> stateMap = new ConcurrentHashMap<>();
    private final List<String> supportedCommandsUpnp = List.of(KEY_CODE);

    private String udn;
    private UpnpRemoteController remoteController;

    private List<PanasonicEventListener> listeners = new CopyOnWriteArrayList<>();

    private RemoteControllerService(UpnpService upnpService, String udn, boolean upnp) {
        logger.debug("Create a Panasonic TV RemoteController service");
        this.udn = udn;
        this.remoteController = new UpnpRemoteController(upnpService);
    }

    static RemoteControllerService createUpnpService(UpnpService upnpService, String udn) {
        return new RemoteControllerService(upnpService, udn, true);
    }

    @Override
    public void addEventListener(PanasonicEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(PanasonicEventListener listener) {
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
    public String getServiceName() {
        return SERVICE_NAME;
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
    private static final List<String> tvInputKeyCodes = List.of("NRC_HDMI1-ONOFF", "NRC_HDMI2-ONOFF", "NRC_HDMI3-ONOFF",
            "NRC_HDMI4-ONOFF", "NRC_TV-ONOFF", "NRC_VIDEO1-ONOFF", "NRC_VIDEO2-ONOFF");

    private void sendKeyCode(final String key) {
        remoteController.invokeAction(this, "p00NetworkControl", "X_SendKey", Map.of("X_KeyEvent", key));

        if (tvInputKeyCodes.contains(key)) {
            onValueReceived("sourceName", key.substring(4, key.length() - 6), "p00NetworkControl");
        }
    }

    @Override
    public String getUDN() {
        return udn;
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("PanasonicTV RemoteControl status changed to {}", status);
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (variable == null) {
            return;
        }
        String oldValue = stateMap.get(variable);
        if ((value == null && oldValue == null) || (value != null && value.equals(oldValue))) {
            logger.trace("Value '{}' for {} hasn't changed, ignoring update", value, variable);
            return;
        }

        stateMap.compute(variable, (k, v) -> value);

        State newState = (value != null) ? StringType.valueOf(value) : UnDefType.UNDEF;
        for (PanasonicEventListener listener : listeners) {
            switch (variable) {
                case SOURCE_NAME:
                case SOURCE_ID:
                    listener.valueReceived(SOURCE_NAME, newState);
                    listener.valueReceived(SOURCE_ID, newState);
                    break;
            }
        }
    }
}
