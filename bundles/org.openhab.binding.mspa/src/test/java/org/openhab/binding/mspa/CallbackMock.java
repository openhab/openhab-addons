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
package org.openhab.binding.mspa;

import static org.mockito.Mockito.mock;
import static org.openhab.binding.mspa.internal.MSpaConstants.THING_TYPE_POOL;

import java.util.Hashtable;
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

/**
 * {@link CallbackMock} listen to state updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class CallbackMock implements ThingHandlerCallback {
    Hashtable<String, State> states = new Hashtable<>();

    public int numberOfUpdates() {
        return states.size();
    }

    public Hashtable<String, State> getStates() {
        return states;
    }

    public @Nullable State getState(String channel) {
        return states.get(channel);
    }

    @Override
    public void stateUpdated(ChannelUID channelUID, State state) {
        states.put(channelUID.getId(), state);
    }

    @Override
    public void postCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void sendTimeSeries(ChannelUID channelUID, TimeSeries timeSeries) {
    }

    @Override
    public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
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
        return ChannelBuilder.create(new ChannelUID(new ThingUID(THING_TYPE_POOL, "test"), "test-channel"));
    }

    @Override
    public ChannelBuilder editChannel(Thing thing, ChannelUID channelUID) {
        return ChannelBuilder.create(new ChannelUID(new ThingUID(THING_TYPE_POOL, "test"), "test-channel"));
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
        return mock();
    }
}
