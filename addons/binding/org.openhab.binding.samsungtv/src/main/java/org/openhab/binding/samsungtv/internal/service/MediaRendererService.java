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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.samsungtv.internal.service.api.EventListener;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MediaRendererService} is responsible for handling MediaRenderer
 * commands.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class MediaRendererService implements UpnpIOParticipant, SamsungTvService {

    public static final String SERVICE_NAME = "MediaRenderer";
    private final List<String> supportedCommands = Arrays.asList(VOLUME, MUTE, BRIGHTNESS, CONTRAST, SHARPNESS,
            COLOR_TEMPERATURE);

    private Logger logger = LoggerFactory.getLogger(MediaRendererService.class);

    private UpnpIOService service;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pollingJob;

    private String udn;
    private int pollingInterval;

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    private List<EventListener> listeners = new CopyOnWriteArrayList<>();

    public MediaRendererService(UpnpIOService upnpIOService, String udn, int pollingInterval) {
        logger.debug("Create a Samsung TV MediaRenderer service");

        if (upnpIOService != null) {
            service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

        this.udn = udn;
        this.pollingInterval = pollingInterval;

        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public List<String> getSupportedChannelNames() {
        return supportedCommands;
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
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("Start refresh task, interval={}", pollingInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, pollingInterval, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stop() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void clearCache() {
        stateMap.clear();
    }

    @Override
    public boolean isUpnp() {
        return true;
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            if (isRegistered()) {

                try {
                    updateResourceState("RenderingControl", "GetVolume",
                            SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
                    updateResourceState("RenderingControl", "GetMute",
                            SamsungTvUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
                    updateResourceState("RenderingControl", "GetBrightness",
                            SamsungTvUtils.buildHashMap("InstanceID", "0"));
                    updateResourceState("RenderingControl", "GetContrast",
                            SamsungTvUtils.buildHashMap("InstanceID", "0"));
                    updateResourceState("RenderingControl", "GetSharpness",
                            SamsungTvUtils.buildHashMap("InstanceID", "0"));
                    updateResourceState("RenderingControl", "GetColorTemperature",
                            SamsungTvUtils.buildHashMap("InstanceID", "0"));

                } catch (Exception e) {
                    reportError("Error occurred during poll", e);
                }
            }
        }
    };

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

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
                    Integer.valueOf(stateMap.get("CurrentVolume")));
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
                    Integer.valueOf(stateMap.get("CurrentBrightness")));
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
                    Integer.valueOf(stateMap.get("CurrentContrast")));
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
                    Integer.valueOf(stateMap.get("CurrentSharpness")));
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
                    Integer.valueOf(stateMap.get("CurrentColorTemperature")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetColorTemperature",
                SamsungTvUtils.buildHashMap("InstanceID", "0", "DesiredColorTemperature", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetColorTemperature", SamsungTvUtils.buildHashMap("InstanceID", "0"));
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("onStatusChanged");
    }

    private void reportError(String message, Throwable e) {
        for (EventListener listener : listeners) {
            listener.reportError(ThingStatusDetail.COMMUNICATION_ERROR, message, e);
        }
    }
}
