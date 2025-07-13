/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
import org.openhab.core.types.TimeSeries.Policy;

/**
 * The {@link CallbackMock} is a helper for unit tests to receive callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CallbackMock implements ThingHandlerCallback {

    @Nullable
    Bridge bridge;
    Map<String, TimeSeries> seriesMap = new HashMap<>();
    Map<String, List<State>> stateMap = new HashMap<>();
    ThingStatusInfo currentInfo = new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);

    @Override
    public void stateUpdated(ChannelUID channelUID, State state) {
        String key = channelUID.getAsString();
        List<State> stateList = stateMap.get(key);
        if (stateList == null) {
            stateList = new ArrayList<>();
        }
        stateList.add(state);
        stateMap.put(key, stateList);
    }

    public List<State> getStateList(String cuid) {
        List<State> stateList = stateMap.get(cuid);
        if (stateList == null) {
            stateList = new ArrayList<State>();
        }
        return stateList;
    }

    @Override
    public void postCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void sendTimeSeries(ChannelUID channelUID, TimeSeries timeSeries) {
        seriesMap.put(channelUID.getAsString(), timeSeries);
    }

    public TimeSeries getTimeSeries(String cuid) {
        TimeSeries ts = seriesMap.get(cuid);
        if (ts == null) {
            ts = new TimeSeries(Policy.REPLACE);
        }
        return ts;
    }

    @Override
    public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
        currentInfo = thingStatus;
    }

    public ThingStatusInfo getStatus() {
        return currentInfo;
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
        return ChannelBuilder.create(channelUID);
    }

    @Override
    public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
        return ChannelBuilder.create(channelUID);
    }

    @Override
    public List<ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID,
            ChannelGroupTypeUID channelGroupTypeUID) {
        return List.of();
    }

    @Override
    public boolean isChannelLinked(ChannelUID channelUID) {
        return false;
    }

    @Override
    public @Nullable Bridge getBridge(ThingUID bridgeUID) {
        return bridge;
    }

    public void setBridge(Bridge b) {
        bridge = b;
    }
}
