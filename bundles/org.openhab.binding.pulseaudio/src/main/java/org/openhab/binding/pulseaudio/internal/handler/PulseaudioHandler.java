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
package org.openhab.binding.pulseaudio.internal.handler;

import static org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.binding.pulseaudio.internal.items.Sink;
import org.openhab.binding.pulseaudio.internal.items.SinkInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PulseaudioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public class PulseaudioHandler extends BaseThingHandler implements DeviceStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(SINK_THING_TYPE, COMBINED_SINK_THING_TYPE, SINK_INPUT_THING_TYPE,
                    SOURCE_THING_TYPE, SOURCE_OUTPUT_THING_TYPE).collect(Collectors.toSet()));

    private int refresh = 60; // refresh every minute as default
    private ScheduledFuture<?> refreshJob;

    private PulseaudioBridgeHandler bridgeHandler;

    private final Logger logger = LoggerFactory.getLogger(PulseaudioHandler.class);

    private String name;

    public PulseaudioHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        name = (String) config.get(DEVICE_PARAMETER_NAME);

        // until we get an update put the Thing offline
        updateStatus(ThingStatus.OFFLINE);
        deviceOnlineWatchdog();
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        updateStatus(ThingStatus.OFFLINE);
        bridgeHandler = null;
        logger.trace("Thing {} {} disposed.", getThing().getUID(), name);
        super.dispose();
    }

    private void deviceOnlineWatchdog() {
        Runnable runnable = () -> {
            try {
                PulseaudioBridgeHandler bridgeHandler = getPulseaudioBridgeHandler();
                if (bridgeHandler != null) {
                    if (bridgeHandler.getDevice(name) == null) {
                        updateStatus(ThingStatus.OFFLINE);
                        bridgeHandler = null;
                    } else {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    logger.debug("Bridge for pulseaudio device {} not found.", name);
                    updateStatus(ThingStatus.OFFLINE);
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                bridgeHandler = null;
            }
        };

        refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, refresh, TimeUnit.SECONDS);
    }

    private synchronized PulseaudioBridgeHandler getPulseaudioBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Required bridge not defined for device {}.", name);
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof PulseaudioBridgeHandler) {
                this.bridgeHandler = (PulseaudioBridgeHandler) handler;
                this.bridgeHandler.registerDeviceStatusListener(this);
            } else {
                logger.debug("No available bridge handler found for device {} bridge {} .", name, bridge.getUID());
                return null;
            }
        }
        return this.bridgeHandler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        PulseaudioBridgeHandler bridge = getPulseaudioBridgeHandler();
        if (bridge == null) {
            logger.warn("pulseaudio server bridge handler not found. Cannot handle command without bridge.");
            return;
        }
        if (command instanceof RefreshType) {
            bridge.handleCommand(channelUID, command);
            return;
        }

        AbstractAudioDeviceConfig device = bridge.getDevice(name);
        if (device == null) {
            logger.warn("device {} not found", name);
            updateStatus(ThingStatus.OFFLINE);
            bridgeHandler = null;
            return;
        } else {
            State updateState = UnDefType.UNDEF;
            if (channelUID.getId().equals(VOLUME_CHANNEL)) {
                if (command instanceof IncreaseDecreaseType) {
                    // refresh to get the current volume level
                    bridge.getClient().update();
                    device = bridge.getDevice(name);
                    int volume = device.getVolume();
                    if (command.equals(IncreaseDecreaseType.INCREASE)) {
                        volume = Math.min(100, volume + 5);
                    }
                    if (command.equals(IncreaseDecreaseType.DECREASE)) {
                        volume = Math.max(0, volume - 5);
                    }
                    bridge.getClient().setVolumePercent(device, volume);
                    updateState = new PercentType(volume);
                } else if (command instanceof PercentType) {
                    DecimalType volume = (DecimalType) command;
                    bridge.getClient().setVolumePercent(device, volume.intValue());
                    updateState = (PercentType) command;
                } else if (command instanceof DecimalType) {
                    // set volume
                    DecimalType volume = (DecimalType) command;
                    bridge.getClient().setVolume(device, volume.intValue());
                    updateState = (DecimalType) command;
                }
            } else if (channelUID.getId().equals(MUTE_CHANNEL)) {
                if (command instanceof OnOffType) {
                    bridge.getClient().setMute(device, OnOffType.ON.equals(command));
                    updateState = (OnOffType) command;
                }
            } else if (channelUID.getId().equals(SLAVES_CHANNEL)) {
                if (device instanceof Sink && ((Sink) device).isCombinedSink()) {
                    if (command instanceof StringType) {
                        List<Sink> slaves = new ArrayList<>();
                        for (String slaveName : command.toString().split(",")) {
                            Sink slave = bridge.getClient().getSink(slaveName.trim());
                            if (slave != null) {
                                slaves.add(slave);
                            }
                        }
                        if (!slaves.isEmpty()) {
                            bridge.getClient().setCombinedSinkSlaves(((Sink) device), slaves);
                        }
                    }
                } else {
                    logger.error("{} is no combined sink", device);
                }
            } else if (channelUID.getId().equals(ROUTE_TO_SINK_CHANNEL)) {
                if (device instanceof SinkInput) {
                    Sink newSink = null;
                    if (command instanceof DecimalType) {
                        newSink = bridge.getClient().getSink(((DecimalType) command).intValue());
                    } else {
                        newSink = bridge.getClient().getSink(command.toString());
                    }
                    if (newSink != null) {
                        logger.debug("rerouting {} to {}", device, newSink);
                        bridge.getClient().moveSinkInput(((SinkInput) device), newSink);
                        updateState = new StringType(newSink.getPaName());
                    } else {
                        logger.error("no sink {} found", command.toString());
                    }
                }
            }
            logger.trace("updating {} to {}", channelUID, updateState);
            if (!updateState.equals(UnDefType.UNDEF)) {
                updateState(channelUID, updateState);
            }
        }
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, AbstractAudioDeviceConfig device) {
        if (device.getPaName().equals(name)) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Updating states of {} id: {}", device, VOLUME_CHANNEL);
            updateState(VOLUME_CHANNEL, new PercentType(device.getVolume()));
            updateState(MUTE_CHANNEL, device.isMuted() ? OnOffType.ON : OnOffType.OFF);
            updateState(STATE_CHANNEL,
                    device.getState() != null ? new StringType(device.getState().toString()) : new StringType("-"));
            if (device instanceof SinkInput) {
                updateState(ROUTE_TO_SINK_CHANNEL,
                        ((SinkInput) device).getSink() != null
                                ? new StringType(((SinkInput) device).getSink().getPaName())
                                : new StringType("-"));
            }
            if (device instanceof Sink && ((Sink) device).isCombinedSink()) {
                updateState(SLAVES_CHANNEL,
                        new StringType(StringUtils.join(((Sink) device).getCombinedSinkNames(), ",")));
            }
        }
    }

    @Override
    public void onDeviceRemoved(PulseaudioBridgeHandler bridge, AbstractAudioDeviceConfig device) {
        if (device.getPaName().equals(name)) {
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void onDeviceAdded(Bridge bridge, AbstractAudioDeviceConfig device) {
        logger.trace("new device discovered {} by {}", device, bridge);
    }
}
