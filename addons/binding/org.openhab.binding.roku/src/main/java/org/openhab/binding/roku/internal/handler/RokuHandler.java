/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.internal.handler;

import static org.openhab.binding.roku.RokuBindingConstants.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.roku.internal.RokuState;
import org.openhab.binding.roku.internal.protocol.RokuCommands;
import org.openhab.binding.roku.internal.protocol.RokuCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jarod Peters - Initial contribution
 * @auther Shawn Wilsher - Overhaul of channels and properties
 */
public class RokuHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(RokuHandler.class);

    public RokuHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String ipAddress = (String) this.getConfig().get(IP_ADDRESS);
        Number port = (Number) this.getConfig().get(PORT);
        if ("REFRESH".equals(command.toFullString())) {
            updateData();
        } else {
            RokuCommands rokuDevice = new RokuCommands(ipAddress, port);
            try {
                rokuDevice.generateAction(channelUID);
            } catch (IOException e) {
                logger.debug("IOException occurred when attempting action with device '{}'", ipAddress, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            updateState(channelUID, OnOffType.OFF);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RokuDevice handler '{}'", getThing().getUID());
        Number refreshInterval = (Number) this.getConfig().get(REFRESH_INTERVAL);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, refreshInterval.longValue(), TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", refreshInterval, getThing().getUID());
    }

    private synchronized void updateData() {
        logger.info("Update Roku data '{}'", getThing().getUID());
        String ipAddress = (String) this.getConfig().get(IP_ADDRESS);
        Number port = (Number) this.getConfig().get(PORT);
        RokuState state = new RokuState(new RokuCommunication(ipAddress, port));
        try {
            state.updateDeviceInformation();
        } catch (IOException e) {
            logger.debug("Roku device '{}' but is not communicating", ipAddress, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
        }

        updatePropertiesFromState(state);

        updateState(CHANNEL_ACTIVE, state.getActive());
        updateState(CHANNEL_HEADPHONES, new StringType(state.getHeadphonesConnected()));
        updateState(CHANNEL_ICON, state.getActiveImage());
        /**
         * Future Functionality
         * updateState(CHANNEL_APPBROWSER, state.getApplicationMenu());
         */
        if (!"PowerOn".equals(state.getPowerMode())) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updatePropertiesFromState(RokuState state) {
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_UDN, state.getUdn());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, state.getSerialNumber());
        properties.put(PROPERTY_DEVICE_ID, state.getDeviceId());
        properties.put(Thing.PROPERTY_VENDOR, state.getVendorName());
        properties.put(PROPERTY_MODEL_NAME, state.getModelName());
        properties.put(Thing.PROPERTY_MODEL_ID, state.getModelNumber());
        properties.put(PROPERTY_MODEL_REGION, state.getModelRegion());
        if ("ethernet".equals(state.getNetworkType())) {
            properties.put(PROPERTY_MAC, state.getEthernetMac());
        } else {
            properties.put(PROPERTY_MAC, state.getWifiMac());
        }
        if (StringUtils.isNotEmpty(state.getUserDeviceName())) {
            properties.put(PROPERTY_USER_DEVICE_NAME, state.getUserDeviceName());
        }
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, state.getSoftwareVersion());

        updateProperties(properties);
    }
}
