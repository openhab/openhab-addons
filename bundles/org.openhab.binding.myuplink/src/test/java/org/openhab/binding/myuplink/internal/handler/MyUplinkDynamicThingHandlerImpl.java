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
package org.openhab.binding.myuplink.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.command.MyUplinkCommand;
import org.openhab.binding.myuplink.internal.config.MyUplinkConfiguration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;

/**
 * test implementtion
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class MyUplinkDynamicThingHandlerImpl implements MyUplinkDynamicThingHandler {

    final Thing thing;

    MyUplinkDynamicThingHandlerImpl(Thing thing) {
        this.thing = thing;
    }

    @Override
    public Logger getLogger() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLogger'");
    }

    @Override
    public MyUplinkConfiguration getBridgeConfiguration() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBridgeConfiguration'");
    }

    @Override
    public void enqueueCommand(MyUplinkCommand command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'enqueueCommand'");
    }

    @Override
    public Thing getThing() {
        return thing;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dispose'");
    }

    @Override
    public void setCallback(@Nullable ThingHandlerCallback thingHandlerCallback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCallback'");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleConfigurationUpdate'");
    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'thingUpdated'");
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'channelLinked'");
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'channelUnlinked'");
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bridgeStatusChanged'");
    }

    @Override
    public void handleRemoval() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleRemoval'");
    }

    @Override
    public void addDynamicChannel(Channel channel) {
        // do nothing;
    }
}
