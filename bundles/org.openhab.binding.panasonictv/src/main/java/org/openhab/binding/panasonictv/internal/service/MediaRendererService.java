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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panasonictv.internal.event.PanasonicEventListener;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MediaRendererService} is responsible for handling MediaRenderer
 * commands.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class MediaRendererService implements UpnpIOParticipant, PanasonicTvService {

    public static final String SERVICE_NAME = "MediaRenderer";
    private final List<String> supportedCommands = Arrays.asList(VOLUME, MUTE);

    private Logger logger = LoggerFactory.getLogger(MediaRendererService.class);

    private UpnpIOService service;

    private @Nullable ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> pollingJob;

    private String udn;
    private int pollingInterval;

    private Map<String, String> stateMap = new ConcurrentHashMap<>();
    private List<PanasonicEventListener> listeners = new CopyOnWriteArrayList<>();

    public MediaRendererService(UpnpIOService upnpIOService, String udn, int pollingInterval) {
        logger.debug("Create a Panasonic TV MediaRenderer service");

        service = upnpIOService;
        this.udn = udn;
        this.pollingInterval = pollingInterval;
    }

    @Override
    public List<String> getSupportedChannelNames() {
        return supportedCommands;
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
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            stop();
        }
        logger.debug("Start refresh task, interval={}", pollingInterval);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        this.scheduler = scheduler;
        this.pollingJob = scheduler.scheduleWithFixedDelay(this::polling, 0, pollingInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
        ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler != null) {
            scheduler.shutdown();
            this.scheduler = null;
        }
    }

    @Override
    public void clearCache() {
        stateMap.clear();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    private void polling() {
        if (isRegistered()) {
            try {
                updateResourceState("RenderingControl", "GetVolume", Map.of("InstanceID", "0", "Channel", "Master"));
                updateResourceState("RenderingControl", "GetMute", Map.of("InstanceID", "0", "Channel", "Master"));
            } catch (Exception e) {
                reportError("Error occurred during poll", e);
            }
        }
    }

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
            default:
                logger.warn("Panasonic TV doesn't support transmitting for channel '{}'", channel);
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

        stateMap.compute(variable, (k, v) -> value);

        for (PanasonicEventListener listener : listeners) {
            switch (variable) {
                case "CurrentVolume":
                    listener.valueReceived(VOLUME, (value != null) ? new PercentType(value) : UnDefType.UNDEF);
                    break;
                case "CurrentMute":
                    State newState = value != null ? OnOffType.from(value.equals("true")) : UnDefType.UNDEF;
                    listener.valueReceived(MUTE, newState);
                    break;
            }
        }
    }

    protected void updateResourceState(String serviceId, String actionId, Map<String, String> inputs) {
        service.invokeAction(this, serviceId, actionId, inputs).forEach((k, v) -> onValueReceived(k, v, serviceId));
    }

    private void setVolume(Command command) {
        int newValue;

        try {
            newValue = DataConverters.convertCommandToIntValue(command, 0, 100,
                    Integer.parseInt(stateMap.getOrDefault("CurrentVolume", "")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetVolume",
                Map.of("InstanceID", "0", "Channel", "Master", "DesiredVolume", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetVolume", Map.of("InstanceID", "0", "Channel", "Master"));
    }

    private void setMute(Command command) {
        boolean newValue;

        try {
            newValue = DataConverters.convertCommandToBooleanValue(command);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetMute",
                Map.of("InstanceID", "0", "Channel", "Master", "DesiredMute", Boolean.toString(newValue)));

        updateResourceState("RenderingControl", "GetMute", Map.of("InstanceID", "0", "Channel", "Master"));
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("onStatusChanged");
    }

    private void reportError(String message, Throwable e) {
        for (PanasonicEventListener listener : listeners) {
            listener.reportError(ThingStatusDetail.COMMUNICATION_ERROR, message, e);
        }
    }
}
