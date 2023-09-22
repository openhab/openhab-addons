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
package org.openhab.binding.mercedesme.internal.handler;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
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

/**
 * The {@link ThingCallbackListener} Helper Util to read test resource files
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ThingCallbackListener implements ThingHandlerCallback {

    public Map<String, State> updatesReceived = new HashMap<String, State>();
    public Map<String, Map<String, State>> updatesPerGroupMap = new HashMap<String, Map<String, State>>();
    public boolean linked = false;

    public int getUpdatesForGroup(String group) {
        Map<String, State> groupMap = updatesPerGroupMap.get(group);
        if (groupMap != null) {
            return groupMap.size();
        }
        return 0;
    }

    @Override
    public void stateUpdated(ChannelUID channelUID, State state) {
        // if (Constants.GROUP_LOCK.equals(channelUID.getGroupId())) {
        System.out.println(channelUID.toString() + " received " + state.toFullString());
        // }
        updatesReceived.put(channelUID.toString(), state);
        Map<String, State> groupMap = updatesPerGroupMap.get(channelUID.getGroupId());
        if (groupMap == null) {
            groupMap = new HashMap<String, State>();
            updatesPerGroupMap.put(channelUID.getGroupId(), groupMap);
        }
        groupMap.put(channelUID.toString(), state);
    }

    @Override
    public void postCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    @Override
    public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
        // TODO Auto-generated method stub
    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO Auto-generated method stub
    }

    @Override
    public void validateConfigurationParameters(Thing thing,
            Map<@NonNull String, @NonNull Object> configurationParameters) {
        // TODO Auto-generated method stub
    }

    @Override
    public void validateConfigurationParameters(Channel channel,
            Map<@NonNull String, @NonNull Object> configurationParameters) {
        // TODO Auto-generated method stub
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(ChannelTypeUID channelTypeUID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable ConfigDescription getConfigDescription(ThingTypeUID thingTypeUID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void configurationUpdated(Thing thing) {
        // TODO Auto-generated method stub
    }

    @Override
    public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration) {
        // TODO Auto-generated method stub
    }

    @Override
    public void channelTriggered(Thing thing, ChannelUID channelUID, String event) {
        // TODO Auto-generated method stub
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
    public List<@NonNull ChannelBuilder> createChannelBuilders(ChannelGroupUID channelGroupUID,
            ChannelGroupTypeUID channelGroupTypeUID) {
        return new ArrayList<ChannelBuilder>();
    }

    @Override
    public boolean isChannelLinked(ChannelUID channelUID) {
        return linked;
    }

    @Override
    public @Nullable Bridge getBridge(ThingUID bridgeUID) {
        // TODO Auto-generated method stub
        return null;
    }
}
