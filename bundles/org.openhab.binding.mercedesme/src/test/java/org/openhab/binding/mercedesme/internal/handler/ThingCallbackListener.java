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
package org.openhab.binding.mercedesme.internal.handler;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;

/**
 * {@link ThingCallbackListener} Listener mock to store vehicle state updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ThingCallbackListener implements ThingHandlerCallback {

    public Map<String, State> updatesReceived = new HashMap<>();
    public Map<String, Map<String, State>> updatesPerGroupMap = new HashMap<>();
    public boolean linked = false;
    public Optional<ThingStatusInfo> status = Optional.empty();

    public ThingStatusInfo getThingStatus() {
        return status.get();
    }

    public int getUpdatesForGroup(String group) {
        Map<String, State> groupMap = updatesPerGroupMap.get(group);
        if (groupMap != null) {
            return groupMap.size();
        }
        return 0;
    }

    @Override
    public void stateUpdated(ChannelUID channelUID, State state) {
        updatesReceived.put(channelUID.toString(), state);
        Map<String, State> groupMap = updatesPerGroupMap.get(channelUID.getGroupId());
        if (groupMap == null) {
            groupMap = new HashMap<>();
            String groupId = channelUID.getGroupId();
            if (groupId != null) {
                updatesPerGroupMap.put(groupId, groupMap);
            }
        }
        groupMap.put(channelUID.toString(), state);
    }

    @Override
    public void postCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
        status = Optional.of(thingStatus);
    }

    @Override
    public void thingUpdated(Thing thing) {
    }

    @Override
    public void validateConfigurationParameters(Thing thing, Map<String, Object> configurationParameters) {
    }

    @Override
    public void validateConfigurationParameters(Channel channel, Map<String, Object> configurationParameters) {
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(ChannelTypeUID channelTypeUID) {
        return null;
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(ThingTypeUID thingTypeUID) {
        return null;
    }

    @Override
    public void configurationUpdated(Thing thing) {
    }

    @Override
    public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration) {
    }

    @Override
    public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
    }

    @Override
    public ChannelBuilder createChannelBuilder(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
        return mock(ChannelBuilder.class);
    }

    @Override
    public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
        return mock(ChannelBuilder.class);
    }

    @Override
    public List<ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID,
            ChannelGroupTypeUID channelGroupTypeUID) {
        return new ArrayList<ChannelBuilder>();
    }

    @Override
    public boolean isChannelLinked(ChannelUID channelUID) {
        return linked;
    }

    @Override
    public @Nullable Bridge getBridge(ThingUID bridgeUID) {
        return null;
    }

    @Override
    public void sendTimeSeries(ChannelUID channelUID, TimeSeries timeSeries) {
    }

    public State getResponse(String channel) {
        State response = updatesReceived.get(channel);
        if (response != null) {
            return response;
        }
        return UnDefType.UNDEF;
    }
}
