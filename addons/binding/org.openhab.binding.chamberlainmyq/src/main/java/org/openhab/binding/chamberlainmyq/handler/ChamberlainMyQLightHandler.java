/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.chamberlainmyq.handler;

import static org.openhab.binding.chamberlainmyq.ChamberlainMyQBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.chamberlainmyq.internal.InvalidLoginException;
import org.openhab.binding.chamberlainmyq.internal.json.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChamberlainMyQLightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Hanson - Initial contribution
 */
public class ChamberlainMyQLightHandler extends ChamberlainMyQHandler {

    private Logger logger = LoggerFactory.getLogger(ChamberlainMyQLightHandler.class);

    public ChamberlainMyQLightHandler(Thing thing) {
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
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_LIGHT_STATE) || channelUID.getId().equals(CHANNEL_SERIAL_NUMBER)
                || channelUID.getId().equals(CHANNEL_DESCRIPTION)) {
            readDeviceState();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHT_STATE)) {
            if (command.equals(OnOffType.ON)) {
                setLightState(true);
            } else if (command.equals(OnOffType.OFF)) {
                setLightState(false);
            } else if (command instanceof RefreshType) {
                readDeviceState();
            }
        }
    }

    private void setLightState(boolean state) {
        String deviceID = getThing().getProperties().get(MYQ_ID);
        try {
            if (state) {
                getGatewayHandler().executeMyQCommand(deviceID, "desiredlightstate", 1, false);
            } else {
                getGatewayHandler().executeMyQCommand(deviceID, "desiredlightstate", 0, false);
            }
        } catch (InvalidLoginException e) {
            logger.warn("Could Not Set Light State: {}", e.getMessage());
        } catch (IOException e) {
            logger.warn("Could Not Set Light State: {}", e.getMessage());
        }
    }

    @Override
    public void updateState(Device myQDevice) {
        deviceConfig.readConfig(myQDevice);
        updateState(CHANNEL_LIGHT_STATE, deviceConfig.getLightStatusOnOff());
        updateState(CHANNEL_ROLLER_STATE, deviceConfig.getDeviceStatusPercent());
        updateState(CHANNEL_DESCRIPTION, StringType.valueOf(deviceConfig.getDescription()));
        updateState(CHANNEL_SERIAL_NUMBER, StringType.valueOf(deviceConfig.getSerialNumber()));
        updateStatus(deviceConfig.getThingOnline());
    }
}
