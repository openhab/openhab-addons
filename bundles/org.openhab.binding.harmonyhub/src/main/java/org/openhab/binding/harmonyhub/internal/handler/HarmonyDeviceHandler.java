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
package org.openhab.binding.harmonyhub.internal.handler;

import static org.openhab.binding.harmonyhub.internal.HarmonyHubBindingConstants.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.harmonyhub.internal.HarmonyHubDynamicTypeProvider;
import org.openhab.binding.harmonyhub.internal.config.HarmonyDeviceConfig;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.harmony.config.ControlGroup;
import com.digitaldan.harmony.config.Device;
import com.digitaldan.harmony.config.Function;
import com.digitaldan.harmony.config.HarmonyConfig;

/**
 * The {@link HarmonyDeviceHandler} is responsible for handling commands for Harmony Devices, which are
 * sent to one of the channels. It also is responsible for dynamically creating the button press channel
 * based on the device's available button press functions.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public class HarmonyDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HarmonyDeviceHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(HARMONY_DEVICE_THING_TYPE);

    private final HarmonyHubDynamicTypeProvider typeProvider;

    private @NonNullByDefault({}) HarmonyDeviceConfig config;

    public HarmonyDeviceHandler(Thing thing, HarmonyHubDynamicTypeProvider typeProvider) {
        super(thing);
        this.typeProvider = typeProvider;
    }

    protected @Nullable HarmonyHubHandler getHarmonyHubHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (HarmonyHubHandler) bridge.getHandler() : null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command '{}' for {}", command, channelUID);

        if (command instanceof RefreshType) {
            // nothing to refresh
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Hub is offline, ignoring command {} for channel {}", command, channelUID);
            return;
        }

        if (!(command instanceof StringType)) {
            logger.warn("Command '{}' is not a String type for channel {}", command, channelUID);
            return;
        }

        HarmonyHubHandler hubHandler = getHarmonyHubHandler();
        if (hubHandler == null) {
            logger.warn("Command '{}' cannot be handled because {} has no bridge", command, getThing().getUID());
            return;
        }

        int id = config.id;
        String name = config.name;
        String message = "Pressing button '{}' on {}";
        if (id > 0) {
            logger.debug(message, command, id);
            hubHandler.pressButton(id, command.toString());
        } else if (name != null) {
            logger.debug(message, command, name);
            hubHandler.pressButton(name, command.toString());
        } else {
            logger.warn("Command '{}' cannot be handled because {} has no valid id or name configured", command,
                    getThing().getUID());
        }
        // may need to ask the list if this can be set here?
        updateState(channelUID, UnDefType.UNDEF);
    }

    @Override
    public void initialize() {
        config = getConfigAs(HarmonyDeviceConfig.class);
        boolean validConfiguration = config.name != null || config.id >= 0;
        if (validConfiguration) {
            updateStatus(ThingStatus.UNKNOWN);
            updateBridgeStatus();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "A harmony device thing must be configured with a device name OR a postive device id");
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateBridgeStatus();
    }

    @Override
    public void handleRemoval() {
        typeProvider.removeChannelTypesForThing(getThing().getUID());
        super.handleRemoval();
    }

    /**
     * Updates our state based on the bridge/hub
     */
    private void updateBridgeStatus() {
        Bridge bridge = getBridge();
        ThingStatus bridgeStatus = bridge != null ? bridge.getStatus() : null;
        HarmonyHubHandler hubHandler = getHarmonyHubHandler();

        boolean bridgeOnline = bridgeStatus == ThingStatus.ONLINE;
        boolean thingOnline = getThing().getStatus() == ThingStatus.ONLINE;

        if (bridgeOnline && hubHandler != null && !thingOnline) {
            updateStatus(ThingStatus.ONLINE);
            hubHandler.getConfigFuture().thenAcceptAsync(this::updateButtonPressChannel, scheduler).exceptionally(e -> {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Getting config failed: " + e.getMessage());
                return null;
            });
        } else if (!bridgeOnline || hubHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Updates the buttonPress channel with the available buttons as option states.
     */
    private void updateButtonPressChannel(@Nullable HarmonyConfig harmonyConfig) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(
                getThing().getUID().getAsString() + ":" + CHANNEL_BUTTON_PRESS);

        if (harmonyConfig == null) {
            logger.debug("Cannot update {} when HarmonyConfig is null", channelTypeUID);
            return;
        }

        logger.debug("Updating {}", channelTypeUID);

        List<StateOption> states = getButtonStateOptions(harmonyConfig);

        ChannelType channelType = ChannelTypeBuilder.state(channelTypeUID, "Send Button Press", "String")
                .withDescription("Send a button press to device " + getThing().getLabel())
                .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withOptions(states).build())
                .build();

        typeProvider.putChannelType(channelType);

        Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), CHANNEL_BUTTON_PRESS), "String")
                .withType(channelTypeUID).build();

        // replace existing buttonPress with updated one
        List<Channel> newChannels = new ArrayList<>();
        for (Channel c : getThing().getChannels()) {
            if (!c.getUID().equals(channel.getUID())) {
                newChannels.add(c);
            }
        }
        newChannels.add(channel);

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(newChannels);
        updateThing(thingBuilder.build());
    }

    private List<StateOption> getButtonStateOptions(HarmonyConfig harmonyConfig) {
        int id = config.id;
        String name = config.name;
        List<StateOption> states = new LinkedList<>();

        // Iterate through button function commands and add them to our state list
        for (Device device : harmonyConfig.getDevices()) {
            boolean sameId = name == null && device.getId() == id;
            boolean sameName = name != null && name.equals(device.getLabel());

            if (sameId || sameName) {
                for (ControlGroup controlGroup : device.getControlGroup()) {
                    for (Function function : controlGroup.getFunction()) {
                        states.add(new StateOption(function.getName(), function.getLabel()));
                    }
                }
                break;
            }
        }
        return states;
    }
}
