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
package org.openhab.binding.nikobus.internal.handler;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.CHANNEL_OUTPUT_PREFIX;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nikobus.internal.protocol.NikobusCommand;
import org.openhab.binding.nikobus.internal.protocol.SwitchModuleCommandFactory;
import org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusSwitchModuleHandler} is responsible for communication between Nikobus modules and binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
abstract class NikobusModuleHandler extends NikobusBaseThingHandler {
    private final EnumSet<SwitchModuleGroup> pendingRefresh = EnumSet.noneOf(SwitchModuleGroup.class);
    private final Logger logger = LoggerFactory.getLogger(NikobusModuleHandler.class);
    private final Map<String, Integer> cachedStates = new HashMap<>();

    protected NikobusModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (thing.getStatus() != ThingStatus.OFFLINE) {
            // Fetch all linked channels to get initial values.
            thing.getChannels().forEach(channel -> refreshChannel(channel.getUID()));
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        synchronized (cachedStates) {
            cachedStates.clear();
        }

        synchronized (pendingRefresh) {
            pendingRefresh.clear();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand '{}' for channel '{}'", command, channelUID.getId());

        if (command instanceof RefreshType) {
            refreshChannel(channelUID);
        } else {
            processWrite(channelUID, command);
        }
    }

    private void refreshChannel(ChannelUID channelUID) {
        logger.debug("Refreshing channel '{}'", channelUID.getId());

        if (!isLinked(channelUID)) {
            logger.debug("Refreshing channel '{}' skipped since it is not linked", channelUID.getId());
            return;
        }

        updateGroup(SwitchModuleGroup.mapFromChannel(channelUID));
    }

    public void refreshModule() {
        Set<SwitchModuleGroup> groups = new HashSet<>();
        for (Channel channel : thing.getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (isLinked(channelUID)) {
                groups.add(SwitchModuleGroup.mapFromChannel(channelUID));
            }
        }

        if (groups.isEmpty()) {
            logger.debug("Nothing to refresh for '{}'", thing.getUID());
            return;
        }

        logger.debug("Refreshing {} - {}", thing.getUID(), groups);

        for (SwitchModuleGroup group : groups) {
            updateGroup(group);
        }
    }

    public void requestStatus(SwitchModuleGroup group) {
        updateGroup(group);
    }

    private void updateGroup(SwitchModuleGroup group) {
        synchronized (pendingRefresh) {
            if (pendingRefresh.contains(group)) {
                logger.debug("Refresh already scheduled for group {} of module '{}'", group, getAddress());
                return;
            }

            pendingRefresh.add(group);
        }

        logger.debug("Refreshing group {} of switch module '{}'", group, getAddress());

        NikobusPcLinkHandler pcLink = getPcLink();
        if (pcLink != null) {
            NikobusCommand command = SwitchModuleCommandFactory.createReadCommand(getAddress(), group,
                    result -> processStatusUpdate(result, group));
            pcLink.sendCommand(command);
        }
    }

    private void processStatusUpdate(NikobusCommand.Result result, SwitchModuleGroup group) {
        try {
            String responsePayload = result.get();

            logger.debug("processStatusUpdate '{}' for group {} in module '{}'", responsePayload, group, getAddress());

            if (thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            // Update channel's statuses based on response.
            for (int i = 0; i < group.getCount(); i++) {
                String channelId = CHANNEL_OUTPUT_PREFIX + (i + group.getOffset());
                String responseDigits = responsePayload.substring(9 + (i * 2), 11 + (i * 2));

                int value = Integer.parseInt(responseDigits, 16);

                updateStateAndCacheValue(channelId, value);
            }
        } catch (Exception e) {
            logger.warn("Processing response for '{}'-{} failed with {}", getAddress(), group, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            synchronized (pendingRefresh) {
                pendingRefresh.remove(group);
            }
        }
    }

    private void updateStateAndCacheValue(String channelId, int value) {
        if (value < 0x00 || value > 0xff) {
            throw new IllegalArgumentException("Invalid range. 0x00 - 0xff expected but got value " + value);
        }

        logger.debug("setting channel '{}' to {}", channelId, value);

        Integer previousValue;
        synchronized (cachedStates) {
            previousValue = cachedStates.put(channelId, value);
        }

        if (previousValue == null || previousValue.intValue() != value) {
            updateState(channelId, stateFromValue(channelId, value));
        }
    }

    private void processWrite(ChannelUID channelUID, Command command) {
        StringBuilder commandPayload = new StringBuilder();
        SwitchModuleGroup group = SwitchModuleGroup.mapFromChannel(channelUID);

        for (int i = group.getOffset(); i < group.getOffset() + group.getCount(); i++) {
            String channelId = CHANNEL_OUTPUT_PREFIX + i;
            Integer digits;

            if (channelId.equals(channelUID.getId())) {
                digits = valueFromCommand(channelId, command);
                updateStateAndCacheValue(channelId, digits.intValue());
            } else {
                synchronized (cachedStates) {
                    digits = cachedStates.get(channelId);
                }
            }

            if (digits == null) {
                commandPayload.append("00");
                logger.warn("no cached value found for '{}' in module '{}'", channelId, getAddress());
            } else {
                commandPayload.append(String.format("%02X", digits.intValue()));
            }
        }

        NikobusPcLinkHandler pcLink = getPcLink();
        if (pcLink != null) {
            pcLink.sendCommand(SwitchModuleCommandFactory.createWriteCommand(getAddress(), group,
                    commandPayload.toString(), this::processWriteCommandResponse));
        }
    }

    private void processWriteCommandResponse(NikobusCommand.Result result) {
        try {
            String responsePayload = result.get();

            logger.debug("processWriteCommandResponse '{}'", responsePayload);

            if (thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            logger.warn("Processing write confirmation failed with {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected abstract int valueFromCommand(String channelId, Command command);

    protected abstract State stateFromValue(String channelId, int value);
}
