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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wled.internal.WLedSegmentConfiguration;
import org.openhab.binding.wled.internal.api.ApiException;
import org.openhab.binding.wled.internal.api.WledApi;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedSegmentHandler} is responsible for handling only a single segment from a WLED device.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class WLedSegmentHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private WLedSegmentConfiguration config = new WLedSegmentConfiguration();
    private BigDecimal masterBrightness255 = BigDecimal.ZERO;
    private HSBType primaryColor = new HSBType();
    private HSBType secondaryColor = new HSBType();
    private HSBType thirdColor = new HSBType();

    public WLedSegmentHandler(Thing thing) {
        super(thing);
    }

    public void update(String channelID, State state) {
        updateState(channelID, state);
    }

    private void removeWhiteChannels() {
        List<Channel> removeChannels = new ArrayList<>();
        Channel channel = getThing().getChannel(CHANNEL_PRIMARY_WHITE);
        if (channel != null) {
            removeChannels.add(channel);
        }
        channel = getThing().getChannel(CHANNEL_SECONDARY_WHITE);
        if (channel != null) {
            removeChannels.add(channel);
        }
        channel = getThing().getChannel(CHANNEL_THIRD_WHITE);
        if (channel != null) {
            removeChannels.add(channel);
        }
        removeChannels(removeChannels);
    }

    private void removeChannels(List<Channel> removeChannels) {
        if (!removeChannels.isEmpty()) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withoutChannels(removeChannels);
            updateThing(thingBuilder.build());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        WLedBridgeHandler bridgeHandler = (WLedBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        WledApi localApi = bridgeHandler.api;
        if (localApi == null) {
            return;
        }
        if (command instanceof RefreshType) {
            return;// no need to check for refresh below
        }
        logger.debug("command {} sent to {}", command, channelUID.getId());
        try {
            switch (channelUID.getId()) {
                case CHANNEL_SEGMENT_BRIGHTNESS:
                    if (command instanceof OnOffType) {
                        localApi.setMasterOn(OnOffType.ON.equals(command), config.segmentIndex);
                    } else if (command instanceof PercentType percentCommand) {
                        if (PercentType.ZERO.equals(command)) {
                            localApi.setMasterOn(false, config.segmentIndex);
                            return;
                        }
                        localApi.setMasterBrightness(percentCommand, config.segmentIndex);
                    }
                    break;
                case CHANNEL_MIRROR:
                    localApi.setMirror(OnOffType.ON.equals(command), config.segmentIndex);
                    break;
                case CHANNEL_SPACING:
                    if (command instanceof DecimalType decimalCommand) {
                        localApi.setSpacing(decimalCommand.intValue(), config.segmentIndex);
                    }
                    break;
                case CHANNEL_GROUPING:
                    if (command instanceof DecimalType decimalCommand) {
                        localApi.setGrouping(decimalCommand.intValue(), config.segmentIndex);
                    }
                    break;
                case CHANNEL_REVERSE:
                    localApi.setReverse(OnOffType.ON.equals(command), config.segmentIndex);
                    break;
                case CHANNEL_PRIMARY_WHITE:
                    if (command instanceof PercentType percentCommand) {
                        localApi.sendGetRequest("/win&W=" + percentCommand.toBigDecimal().multiply(BIG_DECIMAL_2_55));
                    }
                    break;
                case CHANNEL_SECONDARY_WHITE:
                    if (command instanceof PercentType percentCommand) {
                        localApi.sendGetRequest("/win&W2=" + percentCommand.toBigDecimal().multiply(BIG_DECIMAL_2_55));
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
                    } else if (command instanceof HSBType hsbCommand) {
                        if ((hsbCommand.getBrightness()).equals(PercentType.ZERO)) {
                            localApi.setMasterOn(false, config.segmentIndex);
                            return;
                        }
                        localApi.setGlobalOn(true);
                        primaryColor = hsbCommand;
                        if (primaryColor.getSaturation().intValue() < bridgeHandler.config.saturationThreshold
                                && bridgeHandler.hasWhite) {
                            localApi.setWhiteOnly(hsbCommand, config.segmentIndex);
                        } else if (primaryColor.getSaturation().intValue() == 32
                                && primaryColor.getHue().intValue() == 36 && bridgeHandler.hasWhite) {
                            localApi.setWhiteOnly(hsbCommand, config.segmentIndex);
                        } else {
                            localApi.setMasterHSB(hsbCommand, config.segmentIndex);
                        }
                    } else if (command instanceof PercentType percentCommand) {
                        localApi.setMasterBrightness(percentCommand, config.segmentIndex);
                    }
                    return;
                case CHANNEL_PRIMARY_COLOR:
                    if (command instanceof HSBType hsbCommand) {
                        primaryColor = hsbCommand;
                    } else if (command instanceof PercentType percentCommand) {
                        primaryColor = new HSBType(primaryColor.getHue(), primaryColor.getSaturation(), percentCommand);
                    }
                    localApi.setPrimaryColor(primaryColor, config.segmentIndex);
                    return;
                case CHANNEL_SECONDARY_COLOR:
                    if (command instanceof HSBType hsbCommand) {
                        secondaryColor = hsbCommand;
                    } else if (command instanceof PercentType percentCommand) {
                        secondaryColor = new HSBType(secondaryColor.getHue(), secondaryColor.getSaturation(),
                                percentCommand);
                    }
                    localApi.setSecondaryColor(secondaryColor, config.segmentIndex);
                    return;
                case CHANNEL_THIRD_COLOR:
                    if (command instanceof HSBType hsbCommand) {
                        thirdColor = hsbCommand;
                    } else if (command instanceof PercentType percentCommand) {
                        thirdColor = new HSBType(thirdColor.getHue(), thirdColor.getSaturation(), percentCommand);
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
            }
        } catch (ApiException e) {
            logger.debug("Exception occured:{}", e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(WLedSegmentConfiguration.class);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge is selected.");
        } else {
            WLedBridgeHandler localBridgeHandler = (WLedBridgeHandler) bridge.getHandler();
            if (localBridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                return;
            }
            WledApi localAPI = localBridgeHandler.api;
            if (localAPI != null) {
                updateStatus(ThingStatus.ONLINE);
                localBridgeHandler.stateDescriptionProvider
                        .setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_FX), localAPI.getUpdatedFxList());
                localBridgeHandler.stateDescriptionProvider.setStateOptions(
                        new ChannelUID(getThing().getUID(), CHANNEL_PALETTES), localAPI.getUpdatedPaletteList());
                if (!localBridgeHandler.hasWhite) {
                    logger.debug("WLED is not setup to use RGBW, so removing un-needed white channels");
                    removeWhiteChannels();
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        }
    }
}
