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
package org.openhab.binding.samsungtv.internal.service;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.samsungtv.internal.service.api.EventListener;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MediaRendererService} is responsible for handling MediaRenderer
 * commands.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class MediaRendererService implements UpnpIOParticipant, SamsungTvService {

    public static final String SERVICE_NAME = "MediaRenderer";
    private static final List<String> SUPPORTED_CHANNELS = Arrays.asList(VOLUME, MUTE, BRIGHTNESS, CONTRAST, SHARPNESS,
            COLOR_TEMPERATURE);

    private final Logger logger = LoggerFactory.getLogger(MediaRendererService.class);

    private final UpnpIOService service;

    private final String udn;

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());

    private Set<EventListener> listeners = new CopyOnWriteArraySet<>();

    private boolean started;

    public MediaRendererService(UpnpIOService upnpIOService, String udn) {
        logger.debug("Creating a Samsung TV MediaRenderer service");
        this.service = upnpIOService;
        this.udn = udn;
    }

    @Override
    public List<String> getSupportedChannelNames() {
        return SUPPORTED_CHANNELS;
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
        service.registerParticipant(this);
        started = true;
    }

    @Override
    public void stop() {
        service.unregisterParticipant(this);
        started = false;
    }

    @Override
    public void clearCache() {
        stateMap.clear();
    }

    @Override
    public boolean isUpnp() {
        return true;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

        if (!started) {
            return;
        }

        if (command == RefreshType.REFRESH) {
            if (isRegistered()) {
                switch (channel) {
                    case VOLUME:
                        updateResourceState("RenderingControl", "GetVolume",
                                SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
                        break;
                    case MUTE:
                        updateResourceState("RenderingControl", "GetMute",
                                SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
                        break;
                    case BRIGHTNESS:
                        updateResourceState("RenderingControl", "GetBrightness",
                                SamsungTvUtils.buildHashMap("InstanceID", "0"));
                        break;
                    case CONTRAST:
                        updateResourceState("RenderingControl", "GetContrast",
                                SamsungTvUtils.buildHashMap("InstanceID", "0"));
                        break;
                    case SHARPNESS:
                        updateResourceState("RenderingControl", "GetSharpness",
                                SamsungTvUtils.buildHashMap("InstanceID", "0"));
                        break;
                    case COLOR_TEMPERATURE:
                        updateResourceState("RenderingControl", "GetColorTemperature",
                                SamsungTvUtils.buildHashMap("InstanceID", "0"));
                        break;
                    default:
                        break;
                }
            }
            return;
        }

        switch (channel) {
            case VOLUME:
                setVolume(command);
                break;
            case MUTE:
                setMute(command);
                break;
            case BRIGHTNESS:
                setBrightness(command);
                break;
            case CONTRAST:
                setContrast(command);
                break;
            case SHARPNESS:
                setSharpness(command);
                break;
            case COLOR_TEMPERATURE:
                setColorTemperature(command);
                break;
            default:
                logger.warn("Samsung TV doesn't support transmitting for channel '{}'", channel);
        }
    }

    private boolean isRegistered() {
        return service.isRegistered(this);
    }

    @Override
    public String getUDN() {
        return udn;
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

        stateMap.put(variable, (value != null) ? value : "");

        for (EventListener listener : listeners) {
            switch (variable) {
                case "CurrentVolume":
                    listener.valueReceived(VOLUME, (value != null) ? new PercentType(value) : UnDefType.UNDEF);
                    break;

                case "CurrentMute":
                    State newState = UnDefType.UNDEF;
                    if (value != null) {
                        newState = value.equals("true") ? OnOffType.ON : OnOffType.OFF;
                    }
                    listener.valueReceived(MUTE, newState);
                    break;

                case "CurrentBrightness":
                    listener.valueReceived(BRIGHTNESS, (value != null) ? new PercentType(value) : UnDefType.UNDEF);
                    break;

                case "CurrentContrast":
                    listener.valueReceived(CONTRAST, (value != null) ? new PercentType(value) : UnDefType.UNDEF);
                    break;

                case "CurrentSharpness":
                    listener.valueReceived(SHARPNESS, (value != null) ? new PercentType(value) : UnDefType.UNDEF);
                    break;

                case "CurrentColorTemperature":
                    listener.valueReceived(COLOR_TEMPERATURE,
                            (value != null) ? new DecimalType(value) : UnDefType.UNDEF);
                    break;
            }
        }
    }

    protected Map<String, String> updateResourceState(String serviceId, String actionId, Map<String, String> inputs) {
        Map<String, String> result = service.invokeAction(this, serviceId, actionId, inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), serviceId);
        }

        return result;
    }

    private void setVolume(Command command) {
        int newValue;

        try {
            newValue = DataConverters.convertCommandToIntValue(command, 0, 100,
                    Integer.valueOf(stateMap.getOrDefault("CurrentVolume", "")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetVolume", SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel",
                "Master", "DesiredVolume", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetVolume",
                SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
    }

    private void setMute(Command command) {
        boolean newValue;

        try {
            newValue = DataConverters.convertCommandToBooleanValue(command);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetMute", SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel",
                "Master", "DesiredMute", Boolean.toString(newValue)));

        updateResourceState("RenderingControl", "GetMute",
                SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
    }

    private void setBrightness(Command command) {
        int newValue;

        try {
            newValue = DataConverters.convertCommandToIntValue(command, 0, 100,
                    Integer.valueOf(stateMap.getOrDefault("CurrentBrightness", "")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetBrightness",
                SamsungTvUtils.buildHashMap("InstanceID", "0", "DesiredBrightness", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetBrightness", SamsungTvUtils.buildHashMap("InstanceID", "0"));
    }

    private void setContrast(Command command) {
        int newValue;

        try {
            newValue = DataConverters.convertCommandToIntValue(command, 0, 100,
                    Integer.valueOf(stateMap.getOrDefault("CurrentContrast", "")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetContrast",
                SamsungTvUtils.buildHashMap("InstanceID", "0", "DesiredContrast", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetContrast", SamsungTvUtils.buildHashMap("InstanceID", "0"));
    }

    private void setSharpness(Command command) {
        int newValue;

        try {
            newValue = DataConverters.convertCommandToIntValue(command, 0, 100,
                    Integer.valueOf(stateMap.getOrDefault("CurrentSharpness", "")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetSharpness",
                SamsungTvUtils.buildHashMap("InstanceID", "0", "DesiredSharpness", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetSharpness", SamsungTvUtils.buildHashMap("InstanceID", "0"));
    }

    private void setColorTemperature(Command command) {
        int newValue;

        try {
            newValue = DataConverters.convertCommandToIntValue(command, 0, 4,
                    Integer.valueOf(stateMap.getOrDefault("CurrentColorTemperature", "")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetColorTemperature",
                SamsungTvUtils.buildHashMap("InstanceID", "0", "DesiredColorTemperature", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetColorTemperature", SamsungTvUtils.buildHashMap("InstanceID", "0"));
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("onStatusChanged: status={}", status);
    }
}
