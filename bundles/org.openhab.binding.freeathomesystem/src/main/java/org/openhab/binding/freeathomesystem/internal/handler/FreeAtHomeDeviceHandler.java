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
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.EventDescription;
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

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDeviceHandler.class);
    private FreeAtHomeDeviceDescription device = new FreeAtHomeDeviceDescription();
    private FreeAtHomeChannelTypeProvider channelTypeProvider;

    private String deviceID;

    private static URI configDescriptionUriChannel;

    private Map<ChannelUID, FreeAtHomeDatapointGroup> mapChannelUID = new HashMap<ChannelUID, FreeAtHomeDatapointGroup>();

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

        if (command instanceof RefreshType) {
            FreeAtHomeDatapointGroup dpg = mapChannelUID.get(channelUID);

            String valueStr = freeAtHomeBridge.getDatapoint(deviceID, dpg.getOutputDatapoint().channelId,
                    dpg.getOutputDatapoint().getDatapointId());

            ValueStateConverter vsc = dpg.getValueStateConverter();

            updateState(channelUID, vsc.convertToState(valueStr));
        } else {
            FreeAtHomeDatapointGroup dpg = mapChannelUID.get(channelUID);

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

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
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

        EventDescription eventDescription = null;
        ChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                .state(channelTypeUID,
                        String.format("%s-%s-%s-%s", dpg.getLabel(), dpg.getOpenHabItemType(), dpg.getOpenHabCategory(),
                                "type"),
                        dpg.getOpenHabItemType())
                .withCategory(dpg.getOpenHabCategory()).withStateDescriptionFragment(stateFragment.build());

        try {
            configDescriptionUriChannel = new URI("channel-type:freeathomesystem:config");
        } catch (URISyntaxException e) {
            logger.debug("Channel config URI cannot create for datapoint - datapoint group: {}", dpg.getLabel());

            return null;
        }

        ChannelType channelType = channelTypeBuilder.isAdvanced(false)
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

                    String channelLabel = String.format("%s - %s", channel.getFunctionIdText(), dpg.getLabel());

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
            ThingUID thingUID = thing.getUID();

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
}
