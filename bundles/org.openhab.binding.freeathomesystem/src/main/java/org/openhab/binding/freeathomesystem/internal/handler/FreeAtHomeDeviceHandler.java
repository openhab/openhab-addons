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
package org.openhab.binding.freeathomesystem.internal.handler;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.freeathomesystem.internal.datamodel.FreeAtHomeDatapointGroup;
import org.openhab.binding.freeathomesystem.internal.datamodel.FreeAtHomeDeviceChannel;
import org.openhab.binding.freeathomesystem.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathomesystem.internal.type.FreeAtHomeChannelGroupTypeProvider;
import org.openhab.binding.freeathomesystem.internal.type.FreeAtHomeChannelTypeProvider;
import org.openhab.binding.freeathomesystem.internal.util.UidUtils;
import org.openhab.binding.freeathomesystem.internal.valuestateconverter.ValueStateConverter;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeDeviceHandler} is responsible for handling the generic free@home device main communication
 * and thing updates
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
public class FreeAtHomeDeviceHandler extends FreeAtHomeSystemBaseHandler {

    private static final String CHANNEL_URI = "channel-type:freeathomesystem:config";

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDeviceHandler.class);
    private FreeAtHomeDeviceDescription device = new FreeAtHomeDeviceDescription();
    private FreeAtHomeChannelTypeProvider channelTypeProvider;

    private String deviceID;

    private static URI configDescriptionUriChannel;

    private Map<ChannelUID, FreeAtHomeDatapointGroup> mapChannelUID = new HashMap<ChannelUID, FreeAtHomeDatapointGroup>();
    private Map<ChannelUID, String> mapChannelUIDVal = new HashMap<ChannelUID, String>();

    public FreeAtHomeDeviceHandler(Thing thing, FreeAtHomeChannelTypeProvider channelTypeProvider,
            FreeAtHomeChannelGroupTypeProvider channelGroupTypeProvider) {
        super(thing);

        this.channelTypeProvider = channelTypeProvider;
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        deviceID = properties.get("deviceId");

        logger.debug("Start creating device - device id: {}", deviceID);

        updateChannels();

        updateStatus(ThingStatus.ONLINE);

        logger.info("Device created - device id: {}", deviceID);
    }

    @Override
    public void dispose() {
        // Unregister device and specific channel for event based state updated
        removeChannels();

        logger.info("Device removed - device id: {}", deviceID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String valueString = "0";

        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;
            }
        }

        if (freeAtHomeBridge != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        FreeAtHomeDatapointGroup dpg = mapChannelUID.get(channelUID);

        // is the dataponitgroup invalid
        if (dpg == null) {
            logger.debug("Handle command for device (but invalid datapointgroup) {} - at channel {} - full command {}",
                    deviceID, channelUID.getAsString(), command.toFullString());

            String errInfo = "Datapointgroup is not available in RefreshCommand - channel: " + channelUID.getAsString();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, errInfo);
        } else {
            if (command instanceof RefreshType) {
                String valueStr = "0";

                // Check whether it is a INPUT only datapoint group
                if (dpg.getDirection() == FreeAtHomeDatapointGroup.DATAPOINTGROUP_DIRECTION_INPUT) {
                    valueStr = freeAtHomeBridge.getDatapoint(deviceID, dpg.getInputDatapoint().channelId,
                            dpg.getInputDatapoint().getDatapointId());
                } else {
                    valueStr = freeAtHomeBridge.getDatapoint(deviceID, dpg.getOutputDatapoint().channelId,
                            dpg.getOutputDatapoint().getDatapointId());
                }

                ValueStateConverter vsc = dpg.getValueStateConverter();

                updateState(channelUID, vsc.convertToState(valueStr));

                // in case of virtual channels store the current value string
                if (device.isVirtual()) {
                    mapChannelUIDVal.put(channelUID, valueStr);
                }
            } else {
                ValueStateConverter vsc = dpg.getValueStateConverter();

                State state = null;

                if (command instanceof StopMoveType) {
                    valueString = "0";
                } else {
                    state = ((State) command);
                    valueString = vsc.convertToValueString(state);
                }

                freeAtHomeBridge.setDatapoint(deviceID, dpg.getInputDatapoint().channelId,
                        dpg.getInputDatapoint().getDatapointId(), valueString);

                if (state != null) {
                    updateState(channelUID, state);
                } else {
                    updateState(channelUID, new StringType("STOP"));
                }

                // in case of virtual channels store the current value string
                if (device.isVirtual()) {
                    logger.info("refresh virtual device state device: {} ch: {} val: {}", deviceID,
                            channelUID.getAsString(), valueString);

                    mapChannelUIDVal.put(channelUID, valueString);
                }
            }

            if (device.isScene()) {
                // the scene can be triggered only therefore reset after 5 seconds
                scheduler.execute(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        logger.debug("Handle wait for scene {} - at channel {} - full command {}", deviceID,
                                channelUID.getAsString(), command.toFullString());
                    }

                    updateState(channelUID, OnOffType.OFF);
                });
            }

            logger.debug("Handle command for device {} - at channel {} - full command {}", deviceID,
                    channelUID.getAsString(), command.toFullString());
        }
    }

    public void handleEventBasedUpdate(ChannelUID channelUID, State state) {
        this.updateState(channelUID, state);
    }

    public void feedbackForVirtualDevice(ChannelUID channelUID, String valueString) {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        FreeAtHomeDatapointGroup dpg = mapChannelUID.get(channelUID);

        Bridge bridge = this.getBridge();

        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;
            }
        }

        if (freeAtHomeBridge != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        if ((dpg.getDirection() == FreeAtHomeDatapointGroup.DATAPOINTGROUP_DIRECTION_INPUT)
                || (dpg.getDirection() == FreeAtHomeDatapointGroup.DATAPOINTGROUP_DIRECTION_INPUTOUTPUT)) {
            freeAtHomeBridge.setDatapoint(deviceID, dpg.getInputDatapoint().channelId,
                    dpg.getInputDatapoint().getDatapointId(), valueString);

            logger.debug("Handle feedback for virtual device {} - at channel {} - value {}", deviceID,
                    channelUID.getAsString(), valueString);
        } else {
            logger.debug("Handle feedback for virtual device {} - at channel {} - but only ubout DPG", deviceID,
                    channelUID.getAsString());
        }
    }

    public ChannelTypeUID createChannelTypeForDatapointgroup(FreeAtHomeDatapointGroup dpg,
            ChannelTypeUID channelTypeUID) {
        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        stateFragment.withReadOnly(dpg.isReadOnly());
        stateFragment.withPattern(dpg.getTypePattern());

        if (dpg.isDecimal() || dpg.isInteger()) {
            BigDecimal min = new BigDecimal(dpg.getMin());
            BigDecimal max = new BigDecimal(dpg.getMax());
            stateFragment.withMinimum(min).withMaximum(max);
        }

        try {
            configDescriptionUriChannel = new URI(CHANNEL_URI);
        } catch (URISyntaxException e) {
            logger.debug("Channel config URI cannot create for datapoint - datapoint group: {}", dpg.getLabel());

            return null;
        }

        ChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                .state(channelTypeUID,
                        String.format("%s-%s-%s-%s", dpg.getLabel(), dpg.getOpenHabItemType(), dpg.getOpenHabCategory(),
                                "type"),
                        dpg.getOpenHabItemType())
                .withCategory(dpg.getOpenHabCategory()).withStateDescriptionFragment(stateFragment.build());

        ChannelType channelType = channelTypeBuilder.isAdvanced(false)
                .withConfigDescriptionURI(configDescriptionUriChannel)
                .withDescription(String.format("Type for channel - %s ", dpg.getLabel())).build();

        channelTypeProvider.addChannelType(channelType);

        logger.debug("Channel type created {} - label: {} - caegory: {}", channelTypeUID.getAsString(), dpg.getLabel(),
                dpg.getOpenHabCategory());

        return channelTypeUID;
    }

    public void updateChannels() {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                device = freeAtHomeBridge.getFreeatHomeDeviceDescription(deviceID);
            }
        }

        // Initialize channels
        List<Channel> thingChannels = new ArrayList<>(this.getThing().getChannels());

        if (thingChannels.isEmpty()) {
            ThingBuilder thingBuilder = editThing();

            ThingUID thingUID = thing.getUID();

            for (int i = 0; i < device.getNumberOfChannels(); i++) {
                FreeAtHomeDeviceChannel channel = device.getChannel(i);

                for (int j = 0; j < channel.getNumberOfDatapointGroup(); j++) {
                    FreeAtHomeDatapointGroup dpg = channel.getDatapointGroup(j);
                    Map<String, String> channelProps = new HashMap<>();

                    if (dpg.getInputDatapoint() != null) {
                        channelProps.put("input", dpg.getInputDatapoint().getDatapointId());
                    }

                    if (dpg.getOutputDatapoint() != null) {
                        channelProps.put("output", dpg.getOutputDatapoint().getDatapointId());
                    }

                    ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dpg.getValueType(),
                            dpg.isReadOnly());

                    if (channelTypeProvider.getChannelType(channelTypeUID, null) == null) {
                        channelTypeUID = createChannelTypeForDatapointgroup(dpg, channelTypeUID);
                    }

                    ChannelUID channelUID = UidUtils.generateChannelUID(thingUID, device.getDeviceId(),
                            channel.getChannelId(), dpg.getLabel());

                    String channelLabel = String.format("%s - %s - %s", channel.getChannelLabel(),
                            channel.getFunctionIdText(), dpg.getLabel());

                    Channel thingChannel = ChannelBuilder.create(channelUID)
                            .withAcceptedItemType(dpg.getOpenHabItemType()).withKind(ChannelKind.STATE)
                            .withProperties(channelProps).withLabel(channelLabel).withDescription(dpg.getDescription())
                            .withType(channelTypeUID).build();
                    thingChannels.add(thingChannel);

                    logger.debug(
                            "Thing channel created - device: {} - channelUID: {} - channel label: {} - category: {}",
                            device.getDeviceId() + device.getDeviceLabel(), channelUID.getAsString(), dpg.getLabel(),
                            dpg.getOpenHabCategory());

                    // in case of output channel, register it for updates
                    if (dpg.getOutputDatapoint() != null) {
                        // Register channel for event based state updated
                        ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                        updateHandler.registerChannel(device.getDeviceId(), channel.getChannelId(),
                                dpg.getOutputDatapoint().getDatapointId(), this, channelUID,
                                dpg.getValueStateConverter());
                    }

                    // add the datapoint group to the maping channel
                    mapChannelUID.put(channelUID, dpg);

                    if (dpg.getInputDatapoint() == null) {
                        logger.debug(
                                "Thing channel registered - device:  {} - channelUID: {} - channel label: {} - category: {}",
                                device.getDeviceId() + device.getDeviceLabel(), channelUID.getAsString(),
                                dpg.getLabel(), dpg.getOpenHabCategory());
                    } else {
                        logger.debug(
                                "Thing channel registered - device: {} - channelUID: {} - channel label: {} - category: {}",
                                device.getDeviceId() + device.getDeviceLabel(), channelUID.getAsString(),
                                dpg.getLabel(), dpg.getOpenHabCategory());
                    }
                }

                thingBuilder.withChannels(thingChannels);

                updateThing(thingBuilder.build());
            }
        } else {
            reloadChannelTypes();
        }

        thingChannels.forEach(channel -> {
            if (isLinked(channel.getUID())) {
                channelLinked(channel.getUID());
            }
        });
    }

    public void reloadChannelTypes() {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        ThingUID thingUID = thing.getUID();

        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                device = freeAtHomeBridge.getFreeatHomeDeviceDescription(deviceID);
            }
        }

        for (int i = 0; i < device.getNumberOfChannels(); i++) {
            FreeAtHomeDeviceChannel channel = device.getChannel(i);

            for (int j = 0; j < channel.getNumberOfDatapointGroup(); j++) {
                FreeAtHomeDatapointGroup dpg = channel.getDatapointGroup(j);

                ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dpg.getValueType(), dpg.isReadOnly());

                if (channelTypeProvider.getChannelType(channelTypeUID, null) == null) {
                    channelTypeUID = createChannelTypeForDatapointgroup(dpg, channelTypeUID);
                }

                ChannelUID channelUID = UidUtils.generateChannelUID(thingUID, device.getDeviceId(),
                        channel.getChannelId(), dpg.getLabel());

                // in case of output channel, register it for updates
                if (dpg.getOutputDatapoint() != null) {
                    // Register channel for event based state updated
                    ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                    updateHandler.registerChannel(device.getDeviceId(), channel.getChannelId(),
                            dpg.getOutputDatapoint().getDatapointId(), this, channelUID, dpg.getValueStateConverter());
                }

                // add the datapoint group to the maping channel
                mapChannelUID.put(channelUID, dpg);

                logger.debug("Thing channelType reloaded - Device: {} - channelTypeUID: {}",
                        device.getDeviceId() + device.getDeviceLabel(), channelTypeUID.getAsString());
            }
        }
    }

    public void removeChannels() {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                device = freeAtHomeBridge.getFreeatHomeDeviceDescription(deviceID);
            }
        }

        // Initialize channels
        List<Channel> thingChannels = new ArrayList<>(this.getThing().getChannels());

        if (thingChannels.isEmpty()) {
            for (int i = 0; i < device.getNumberOfChannels(); i++) {
                FreeAtHomeDeviceChannel channel = device.getChannel(i);

                for (int j = 0; j < channel.getNumberOfDatapointGroup(); j++) {
                    FreeAtHomeDatapointGroup dpg = channel.getDatapointGroup(j);

                    // in case of output channel, unregister it for updates
                    if (dpg.getOutputDatapoint() != null) {
                        // Register channel for event based state updated
                        ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                        updateHandler.unregisterChannel(device.getDeviceId(), channel.getChannelId(),
                                dpg.getOutputDatapoint().getDatapointId());
                    }
                }
            }
        }

        mapChannelUID.clear();
    }

    public boolean isThingHandlesVirtualDevice() {
        return device.isVirtual();
    }
}
