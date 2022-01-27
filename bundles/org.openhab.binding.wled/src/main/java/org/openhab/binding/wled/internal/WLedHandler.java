/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wled.internal;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wled.internal.api.ApiException;
import org.openhab.binding.wled.internal.api.WledApi;
import org.openhab.binding.wled.internal.api.WledApiFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedHandler} is responsible for handling commands and states, which are
 * sent to one of the channels or http replies back.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class WLedHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public final WledDynamicStateDescriptionProvider stateDescriptionProvider;
    private WledApiFactory apiFactory;
    private @Nullable WledApi api;
    private @Nullable ScheduledFuture<?> pollingFuture = null;
    private BigDecimal masterBrightness255 = BigDecimal.ZERO;
    public boolean hasWhite = false;
    private HSBType primaryColor = new HSBType();
    private HSBType secondaryColor = new HSBType();
    private HSBType thirdColor = new HSBType();
    public WLedConfiguration config = new WLedConfiguration();

    public WLedHandler(Thing thing, WledApiFactory apiFactory,
            WledDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.apiFactory = apiFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        WledApi localApi = api;
        if (localApi == null) {
            return;
        }
        BigDecimal bigTemp;
        if (command instanceof RefreshType) {
            return;// no need to check for refresh below
        }
        logger.debug("command {} sent to {}", command, channelUID.getId());
        try {
            switch (channelUID.getId()) {
                case CHANNEL_SEGMENT_BRIGHTNESS:
                    if (command instanceof OnOffType) {
                        localApi.setMasterOn(OnOffType.ON.equals(command), config.segmentIndex);
                    } else if (command instanceof PercentType) {
                        if (PercentType.ZERO.equals(command)) {
                            localApi.setMasterOn(false, config.segmentIndex);
                            return;
                        }
                        localApi.setMasterBrightness((PercentType) command, config.segmentIndex);
                    }
                    break;
                case CHANNEL_MIRROR:
                    localApi.setMirror(OnOffType.ON.equals(command), config.segmentIndex);
                    break;
                case CHANNEL_LIVE_OVERRIDE:
                    localApi.setLiveOverride(command.toString());
                    break;
                case CHANNEL_SPACING:
                    if (command instanceof DecimalType) {
                        localApi.setSpacing(((DecimalType) command).intValue(), config.segmentIndex);
                    }
                    break;
                case CHANNEL_GROUPING:
                    if (command instanceof DecimalType) {
                        localApi.setGrouping(((DecimalType) command).intValue(), config.segmentIndex);
                    }
                    break;
                case CHANNEL_REVERSE:
                    localApi.setReverse(OnOffType.ON.equals(command), config.segmentIndex);
                    break;
                case CHANNEL_SYNC_SEND:
                    localApi.setUdpSend(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_SYNC_RECEIVE:
                    localApi.setUdpRecieve(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_PRIMARY_WHITE:
                    if (command instanceof PercentType) {
                        localApi.sendGetRequest(
                                "/win&W=" + ((PercentType) command).toBigDecimal().multiply(BIG_DECIMAL_2_55));
                    }
                    break;
                case CHANNEL_SECONDARY_WHITE:
                    if (command instanceof PercentType) {
                        localApi.sendGetRequest(
                                "/win&W2=" + ((PercentType) command).toBigDecimal().multiply(BIG_DECIMAL_2_55));
                    }
                    break;
                case CHANNEL_MASTER_CONTROLS:
                    if (command instanceof OnOffType) {
                        if (OnOffType.ON.equals(command)) {
                            // global may be off, but we don't want to switch global off and affect other segments
                            localApi.setGlobalOn(true);
                        }
                        localApi.setMasterOn(OnOffType.ON.equals(command), config.segmentIndex);
                    } else if (command instanceof IncreaseDecreaseType) {
                        if (IncreaseDecreaseType.INCREASE.equals(command)) {
                            if (masterBrightness255.intValue() < 240) {
                                localApi.sendGetRequest("/win&TT=1000&A=~15"); // 255 divided by 15 = 17 levels
                            } else {
                                localApi.sendGetRequest("/win&TT=1000&A=255");
                            }
                        } else {
                            if (masterBrightness255.intValue() > 15) {
                                localApi.sendGetRequest("/win&TT=1000&A=~-15");
                            } else {
                                localApi.sendGetRequest("/win&TT=1000&A=0");
                            }
                        }
                    } else if (command instanceof HSBType) {
                        if ((((HSBType) command).getBrightness()).equals(PercentType.ZERO)) {
                            localApi.setMasterOn(false, config.segmentIndex);
                            return;
                        }
                        localApi.setGlobalOn(true);
                        primaryColor = (HSBType) command;
                        if (primaryColor.getSaturation().intValue() < config.saturationThreshold && hasWhite) {
                            localApi.setWhiteOnly((PercentType) command, config.segmentIndex);
                        } else if (primaryColor.getSaturation().intValue() == 32
                                && primaryColor.getHue().intValue() == 36 && hasWhite) {
                            localApi.setWhiteOnly((PercentType) command, config.segmentIndex);
                        } else {
                            localApi.setMasterHSB((HSBType) command, config.segmentIndex);
                        }
                    } else if (command instanceof PercentType) {
                        localApi.setMasterBrightness((PercentType) command, config.segmentIndex);
                    }
                    return;
                case CHANNEL_PRIMARY_COLOR:
                    if (command instanceof HSBType) {
                        primaryColor = (HSBType) command;
                    } else if (command instanceof PercentType) {
                        primaryColor = new HSBType(primaryColor.getHue(), primaryColor.getSaturation(),
                                ((PercentType) command));
                    }
                    localApi.setPrimaryColor(primaryColor, config.segmentIndex);
                    return;
                case CHANNEL_SECONDARY_COLOR:
                    if (command instanceof HSBType) {
                        secondaryColor = (HSBType) command;
                    } else if (command instanceof PercentType) {
                        secondaryColor = new HSBType(secondaryColor.getHue(), secondaryColor.getSaturation(),
                                ((PercentType) command));
                    }
                    localApi.setSecondaryColor(secondaryColor, config.segmentIndex);
                    return;
                case CHANNEL_THIRD_COLOR:
                    if (command instanceof HSBType) {
                        thirdColor = (HSBType) command;
                    } else if (command instanceof PercentType) {
                        thirdColor = new HSBType(thirdColor.getHue(), thirdColor.getSaturation(),
                                ((PercentType) command));
                    }
                    localApi.setTertiaryColor(thirdColor, config.segmentIndex);
                    return;
                case CHANNEL_PALETTES:
                    localApi.setPalette(command.toString(), config.segmentIndex);
                    break;
                case CHANNEL_FX:
                    localApi.setEffect(command.toString(), config.segmentIndex);
                    break;
                case CHANNEL_SPEED:
                    localApi.setFxSpeed((PercentType) command, config.segmentIndex);
                    break;
                case CHANNEL_INTENSITY:
                    localApi.setFxIntencity((PercentType) command, config.segmentIndex);
                    break;
                case CHANNEL_SLEEP:
                    localApi.setSleep(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_PLAYLISTS:
                case CHANNEL_PRESETS:
                    localApi.setPreset(command.toString());
                    break;
                case CHANNEL_PRESET_DURATION:// ch removed in firmware 0.13.0 and newer
                    if (command instanceof QuantityType) {
                        QuantityType<?> seconds = ((QuantityType<?>) command).toUnit(Units.SECOND);
                        if (seconds != null) {
                            bigTemp = new BigDecimal(seconds.intValue()).multiply(new BigDecimal(1000));
                            localApi.sendGetRequest("/win&PT=" + bigTemp.intValue());
                        }
                    }
                    break;
                case CHANNEL_TRANS_TIME:
                    if (command instanceof QuantityType) {
                        QuantityType<?> seconds = ((QuantityType<?>) command).toUnit(Units.SECOND);
                        if (seconds != null) {
                            localApi.setTransitionTime(new BigDecimal(seconds.multiply(BigDecimal.TEN).intValue()));
                        }
                    }
                    break;
                case CHANNEL_PRESET_CYCLE: // ch removed in firmware 0.13.0 and newer
                    if (command instanceof OnOffType) {
                        localApi.setPresetCycle(OnOffType.ON.equals(command));
                    }
                    break;
            }
        } catch (ApiException e) {
            logger.debug("Exception occured:{}", e.getMessage());
        }
    }

    public void savePreset(int position, String presetName) {
        try {
            if (api != null) {
                api.savePreset(position, presetName);
            }
        } catch (ApiException e) {
        }
    }

    public void removeChannels(ArrayList<Channel> removeChannels) {
        if (!removeChannels.isEmpty()) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withoutChannels(removeChannels);
            updateThing(thingBuilder.build());
        }
    }

    public void update(String channelID, State state) {
        updateState(channelID, state);
    }

    private void pollState() {
        WledApi localApi = api;
        try {
            if (localApi == null) {
                api = apiFactory.getApi(this);
                api.initialize();
            }
            if (localApi == null) {
                return;
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
        if (config.segmentIndex < 0) {
            config.segmentIndex = 0;
        }
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
        return Collections.singleton(WLedActions.class);
    }
}
