/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import static org.openhab.binding.wink.WinkBindingConstants.CHANNEL_SWITCHSTATE;

import java.text.DecimalFormat;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * TODO: The {@link BinarySwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Hanson - Initial contribution
 */
public class BinarySwitchHandler extends WinkHandler {
    public BinarySwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (!this.deviceConfig.validateConfig()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid config.");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected String getDeviceRequestPath() {
        return "binary_switches/" + this.deviceConfig.getDeviceId();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_SWITCHSTATE)) {
            ReadDeviceState();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SWITCHSTATE)) {
            if (command.equals(OnOffType.ON)) {
                setSwitchState(true);
            } else if (command.equals(OnOffType.OFF)) {
                setSwitchState(false);
            } else if (command instanceof RefreshType) {
                logger.debug("Refreshing state");
                ReadDeviceState();
            }
        }
    }

    private void setSwitchState(boolean state) {
        if (state) {
            sendCommand("{\"desired_state\":{\"powered\": true}}");
        } else {
            sendCommand("{\"desired_state\": {\"powered\": false}}");
        }
    }

    private void updateState(JsonObject jsonDataBlob) {
        boolean poweredLastReading = false;
        JsonElement lastReadingBlob = jsonDataBlob.get("last_reading");
        if (lastReadingBlob != null) {
            JsonElement poweredBlob = lastReadingBlob.getAsJsonObject().get("powered");
            if (poweredBlob != null && poweredBlob.getAsBoolean() == true) {
                poweredLastReading = true;
            }
        }
        boolean poweredDesiredState = false;
        JsonElement desiredStateBlob = jsonDataBlob.get("desired_state");
        if (desiredStateBlob != null) {
            JsonElement poweredBlob = desiredStateBlob.getAsJsonObject().get("powered");
            if (poweredBlob != null && poweredBlob.getAsBoolean() == true) {
                poweredDesiredState = true;
            }
        }
        // Don't update the state during a transition.
        if (poweredDesiredState == poweredLastReading) {
            updateState(CHANNEL_SWITCHSTATE, (poweredLastReading ? OnOffType.ON : OnOffType.OFF));
        }
    }

    @Override
    public void sendCommandCallback(JsonObject jsonResult) {
        // TODO: Is there something to do here? Maybe verify that the request succeed (e.g. that the device is online
        // etc...)
    }

    @Override
    protected void updateDeviceStateCallback(JsonObject jsonDataBlob) {
        updateState(jsonDataBlob);
    }

    @Override
    protected void pubNubMessageCallback(JsonObject jsonDataBlob) {
        updateState(jsonDataBlob);
    }
}
