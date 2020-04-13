/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.daikinmadoka.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 *
 * @author blafois
 *
 */
@NonNullByDefault
public class DummyThing implements Thing {

    @Override
    public @Nullable String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLabel(@Nullable String label) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<@NonNull Channel> getChannels() {
        // TODO Auto-generated method stub
        return new ArrayList<Channel>();
    }

    @Override
    public List<@NonNull Channel> getChannelsOfGroup(String channelGroupId) {
        // TODO Auto-generated method stub
        return new ArrayList<Channel>();
    }

    @Override
    public @Nullable Channel getChannel(String channelId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable Channel getChannel(ChannelUID channelUID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThingStatus getStatus() {
        // TODO Auto-generated method stub
        return ThingStatus.ONLINE;
    }

    @Override
    public ThingStatusInfo getStatusInfo() {
        // TODO Auto-generated method stub
        return new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Dummy");
    }

    @Override
    public void setStatusInfo(ThingStatusInfo status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHandler(@Nullable ThingHandler thingHandler) {
        // TODO Auto-generated method stub

    }

    @Override
    public @Nullable ThingHandler getHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable ThingUID getBridgeUID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setBridgeUID(@Nullable ThingUID bridgeUID) {
        // TODO Auto-generated method stub

    }

    @Override
    public Configuration getConfiguration() {
        // TODO Auto-generated method stub
        return new Configuration();
    }

    @Override
    public ThingUID getUID() {
        // TODO Auto-generated method stub
        return new ThingUID("dummy");
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        // TODO Auto-generated method stub
        return new ThingTypeUID("dummy");
    }

    @Override
    public Map<@NonNull String, @NonNull String> getProperties() {
        // TODO Auto-generated method stub
        return new HashMap<String, String>();
    }

    @Override
    public @Nullable String setProperty(String name, @Nullable String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProperties(Map<@NonNull String, @NonNull String> properties) {
        // TODO Auto-generated method stub

    }

    @Override
    public @Nullable String getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLocation(@Nullable String location) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

}
