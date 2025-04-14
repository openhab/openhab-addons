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
package org.openhab.binding.onecta.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.ThingHandler;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@NonNullByDefault
public class DummyThing implements Thing {

    private ThingTypeUID thingTypeUID;
    private ThingHandler thingHandler;
    private ThingStatus thingStatus;
    private Configuration configuration = new Configuration();
    private Map<String, String> properties = new HashMap<>();

    public DummyThing(ThingTypeUID thingTypeUID, ThingHandler thingHandler, ThingStatus thingStatus) {
        this.thingTypeUID = thingTypeUID;
        this.thingHandler = thingHandler;
        this.thingStatus = thingStatus;
    }

    @Override
    public @Nullable String getLabel() {
        return null;
    }

    @Override
    public void setLabel(@Nullable String label) {
    }

    @Override
    public List<@NonNull Channel> getChannels() {
        return new ArrayList<>();
    }

    @Override
    public List<@NonNull Channel> getChannelsOfGroup(String channelGroupId) {
        return new ArrayList<>();
    }

    @Override
    public @Nullable Channel getChannel(String channelId) {
        return null;
    }

    @Override
    public @Nullable Channel getChannel(ChannelUID channelUID) {
        return null;
    }

    @Override
    public ThingStatus getStatus() {
        return this.thingStatus;
    }

    @Override
    public ThingStatusInfo getStatusInfo() {
        return new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Dummy");
    }

    @Override
    public void setStatusInfo(ThingStatusInfo status) {
    }

    @Override
    public void setHandler(@Nullable ThingHandler thingHandler) {
    }

    @Override
    public @Nullable ThingHandler getHandler() {
        return this.thingHandler;
    }

    @Override
    public @Nullable ThingUID getBridgeUID() {
        return null;
    }

    @Override
    public void setBridgeUID(@Nullable ThingUID bridgeUID) {
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public ThingUID getUID() {
        return new ThingUID("dummy");
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    @Override
    public Map<@NonNull String, @NonNull String> getProperties() {
        return this.properties;
    }

    @Override
    public @Nullable String setProperty(String name, @Nullable String value) {
        return null;
    }

    @Override
    public void setProperties(Map<@NonNull String, @NonNull String> properties) {
        this.properties = properties;
    }

    @Override
    public @Nullable String getLocation() {
        return null;
    }

    @Override
    public void setLocation(@Nullable String location) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
