/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;

import com.google.gson.JsonObject;

public class RemoteHandler extends WinkHandler {
    public RemoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (!this.deviceConfig.validateConfig()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid config.");
            return;
        }
        // TODO: Update status.
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected String getDeviceRequestPath() {
        return "remotes/" + this.deviceConfig.getDeviceId();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // TODO
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO
    }

    @Override
    public void sendCommandCallback(JsonObject jsonResult) {
    }

    @Override
    protected void updateDeviceStateCallback(JsonObject jsonDataBlob) {
    }

    @Override
    protected void pubNubMessageCallback(JsonObject jsonDataBlob) {
    }
}
