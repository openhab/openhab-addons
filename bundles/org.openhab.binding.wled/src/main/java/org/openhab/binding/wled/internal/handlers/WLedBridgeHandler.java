/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wled.internal.handlers;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wled.internal.WLedActions;
import org.openhab.binding.wled.internal.WLedConfiguration;
import org.openhab.binding.wled.internal.WLedSegmentDiscoveryService;
import org.openhab.binding.wled.internal.WledDynamicStateDescriptionProvider;
import org.openhab.binding.wled.internal.api.ApiException;
import org.openhab.binding.wled.internal.api.WledApi;
import org.openhab.binding.wled.internal.api.WledApiFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedBridgeHandler} is responsible for talking and parsing data to/from the WLED device.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class WLedBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public final WledDynamicStateDescriptionProvider stateDescriptionProvider;
    private Map<Integer, WLedSegmentHandler> segmentHandlers = new HashMap<>();
    private WledApiFactory apiFactory;
    public boolean hasWhite = false;
    public @Nullable WledApi api;
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    public WLedConfiguration config = new WLedConfiguration();

    public WLedBridgeHandler(Bridge bridge, WledApiFactory apiFactory,
            WledDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(bridge);
        this.apiFactory = apiFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    /**
     * If no thing is setup for specified segmentIndex this will return FALSE.
     */
    public boolean handlerMissing(int segmentIndex) {
        return (segmentHandlers.get(segmentIndex) == null);
    }

    public void savePreset(int position, String presetName) {
        WledApi localAPI = api;
        try {
            if (localAPI != null) {
                localAPI.savePreset(position, presetName);
            }
        } catch (ApiException e) {
            logger.debug("Error occured when trying to save a preset:{}", e.getMessage());
        }
    }

    public void removeBridgeChannels(ArrayList<Channel> removeChannels) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withoutChannels(removeChannels);
        updateThing(thingBuilder.build());
    }

    /**
     * Updates a channel with a new state for a child of this bridge using the segmentIndex
     *
     * @param segmentIndex
     * @param channelID
     * @param state
     */
    public void update(int segmentIndex, String channelID, State state) {
        WLedSegmentHandler segmentHandler = segmentHandlers.get(segmentIndex);
        if (segmentHandler != null) {
            segmentHandler.update(channelID, state);
        }
    }

    /**
     * Updates the bridges channels with a new state.
     *
     * @param channelID
     * @param state
     */
    public void update(String channelID, State state) {
        updateState(channelID, state);
    }

    @Override
    public void childHandlerInitialized(final ThingHandler childHandler, final Thing childThing) {
        BigDecimal segmentIndex = (BigDecimal) childThing.getConfiguration().get(CONFIG_SEGMENT_INDEX);
        segmentHandlers.put(segmentIndex.intValue(), (WLedSegmentHandler) childHandler);
    }

    @Override
    public void childHandlerDisposed(final ThingHandler childHandler, final Thing childThing) {
        BigDecimal segmentIndex = (BigDecimal) childThing.getConfiguration().get(CONFIG_SEGMENT_INDEX);
        segmentHandlers.remove(segmentIndex.intValue());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        WledApi localApi = api;
        if (localApi == null) {
            return;
        }
        try {
            switch (channelUID.getId()) {
                case CHANNEL_GLOBAL_BRIGHTNESS:
                    if (command instanceof OnOffType) {
                        localApi.setGlobalOn(OnOffType.ON.equals(command));
                    } else if (command instanceof PercentType percentCommand) {
                        if (PercentType.ZERO.equals(command)) {
                            localApi.setGlobalOn(false);
                            return;
                        }
                        localApi.setGlobalBrightness(percentCommand);
                    }
                    break;
                case CHANNEL_SLEEP:
                    localApi.setSleep(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_SLEEP_MODE:
                    localApi.setSleepMode(command.toString());
                    break;
                case CHANNEL_SLEEP_BRIGHTNESS:
                    if (command instanceof PercentType percentCommand) {
                        localApi.setSleepTargetBrightness(percentCommand);
                    }
                    break;
                case CHANNEL_SLEEP_DURATION:
                    if (command instanceof QuantityType quantityCommand) {
                        QuantityType<?> minutes = quantityCommand.toUnit(Units.MINUTE);
                        if (minutes != null) {
                            localApi.setSleepDuration(new BigDecimal(minutes.intValue()));
                        }
                    } else if (command instanceof DecimalType decimalCommand) {
                        localApi.setSleepDuration(new BigDecimal(decimalCommand.intValue()));
                    }
                    break;
                case CHANNEL_PLAYLISTS:
                    localApi.setPreset(command.toString());
                    break;
                case CHANNEL_SYNC_SEND:
                    localApi.setUdpSend(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_SYNC_RECEIVE:
                    localApi.setUdpRecieve(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_LIVE_OVERRIDE:
                    localApi.setLiveOverride(command.toString());
                    break;
                case CHANNEL_PRESETS:
                    localApi.setPreset(command.toString());
                    break;
                case CHANNEL_TRANS_TIME:
                    if (command instanceof QuantityType quantityCommand) {
                        QuantityType<?> seconds = quantityCommand.toUnit(Units.SECOND);
                        if (seconds != null) {
                            localApi.setTransitionTime(new BigDecimal(seconds.multiply(BigDecimal.TEN).intValue()));
                        }
                    } else if (command instanceof DecimalType decimalCommand) {
                        localApi.setTransitionTime(new BigDecimal(decimalCommand.intValue()).multiply(BigDecimal.TEN));
                    }
                    break;
                case CHANNEL_PRESET_DURATION:// ch removed in firmware 0.13.0 and newer
                    if (command instanceof QuantityType quantityCommand) {
                        QuantityType<?> seconds = quantityCommand.toUnit(Units.SECOND);
                        if (seconds != null) {
                            BigDecimal bigTemp = new BigDecimal(seconds.intValue()).multiply(new BigDecimal(1000));
                            localApi.sendGetRequest("/win&PT=" + bigTemp.intValue());
                        }
                    } else if (command instanceof DecimalType decimalCommand) {
                        BigDecimal bigTemp = new BigDecimal(decimalCommand.intValue()).multiply(new BigDecimal(1000));
                        localApi.sendGetRequest("/win&PT=" + bigTemp.intValue());
                    }
                    break;
                case CHANNEL_PRESET_CYCLE: // ch removed in firmware 0.13.0 and newer
                    if (command instanceof OnOffType) {
                        localApi.setPresetCycle(OnOffType.ON.equals(command));
                    }
                    break;
            }
        } catch (ApiException e) {
            logger.debug("Exception occured when Channel:{}, Command:{}, Error:{}", channelUID.getId(), command,
                    e.getMessage());
        }
    }

    private void pollState() {
        WledApi localApi = api;
        try {
            if (localApi == null) {
                api = localApi = apiFactory.getApi(this);
                localApi.initialize();
            }
            localApi.update();
            updateStatus(ThingStatus.ONLINE);
        } catch (ApiException e) {
            api = null;// Firmware may be updated so need to check next connect
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(WLedConfiguration.class);
        if (!config.address.contains("://")) {
            logger.debug("Address was not entered in correct format, it may be the raw IP so adding http:// to start");
            config.address = "http://" + config.address;
        }
        pollingFuture = scheduler.scheduleWithFixedDelay(this::pollState, 0, config.pollTime, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        Future<?> future = pollingFuture;
        if (future != null) {
            future.cancel(true);
            pollingFuture = null;
        }
        api = null; // re-initialize api after configuration change
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(WLedActions.class, WLedSegmentDiscoveryService.class);
    }
}
