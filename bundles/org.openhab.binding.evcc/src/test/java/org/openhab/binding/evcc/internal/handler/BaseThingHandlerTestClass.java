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
package org.openhab.binding.evcc.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link BaseThingHandlerTestClass} is responsible for creating a subclass for testing the BaseThingHandler
 * implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class BaseThingHandlerTestClass extends EvccBaseThingHandler {
    public boolean createChannelCalled = false;
    public boolean updateThingCalled = false;
    public boolean updateStatusCalled = false;
    public boolean prepareApiResponseForChannelStateUpdateCalled = true;
    public boolean logUnknownChannelXmlCalled = false;
    public ThingStatus lastUpdatedStatus = ThingStatus.UNKNOWN;
    public boolean updateStateCalled = false;
    public State lastState = UnDefType.UNDEF;
    public ChannelUID lastChannelUID = new ChannelUID("dummy:dummy:dummy:dummy");

    public BaseThingHandlerTestClass(Thing thing, ChannelTypeRegistry registry) {
        super(thing, registry);
    }

    @Override
    protected void updateThing(Thing thing) {
        updateThingCalled = true;
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        lastUpdatedStatus = status;
        updateStatusCalled = true;
    }

    @Override
    @Nullable
    protected Channel createChannel(String thingKey, JsonElement value) {
        createChannelCalled = true;
        return super.createChannel(thingKey, value);
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        prepareApiResponseForChannelStateUpdateCalled = true;
        super.updateStatesFromApiResponse(state);
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return new JsonObject();
    }

    @Override
    public void updateState(ChannelUID uid, State state) {
        updateStateCalled = true;
        lastState = state;
        lastChannelUID = uid;
    }

    // Make sure no files are getting created
    @Override
    protected void logUnknownChannelXml(String key, String itemType) {
    }

    @Override
    public void logUnknownChannelXmlAsync(String key, String itemType) {
        logUnknownChannelXmlCalled = true;
        super.logUnknownChannelXmlAsync(key, itemType);
    }
}
