/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.handler;

import static org.openhab.binding.roku.RokuBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
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
        if (command.toFullString().equals("REFRESH")) {
            updateData();
        } else {
            RokuCommands rokuDevice = new RokuCommands(ipAddress, port);
            try {
                rokuDevice.generateAction(channelUID);
            } catch (IOException e) {
                logger.error("IOException occurred when attempting action", e);
            }
            updateState(channelUID, OnOffType.OFF);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RokuDevice handler '{}'", getThing().getUID());
        updateStatus(ThingStatus.INITIALIZING);
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
        try {
            updateState(CHANNEL_STATUS, state.getPowerMode());
            updateState(CHANNEL_ACTIVE, state.getActive());
            updateState(CHANNEL_UDN, state.getUdn());
            updateState(CHANNEL_SERIAL, state.getSerialNumber());
            updateState(CHANNEL_DEVICEID, state.getDeviceId());
            updateState(CHANNEL_ADID, state.getAdvertisingId());
            updateState(CHANNEL_VENDOR, state.getVendorName());
            updateState(CHANNEL_MODELNAME, state.getModelName());
            updateState(CHANNEL_MODELNUMBER, state.getModelNumber());
            updateState(CHANNEL_MODELREGION, state.getModelRegion());
            updateState(CHANNEL_WIFI, state.getWifiMac());
            updateState(CHANNEL_ETHERNET, state.getEthernetMac());
            updateState(CHANNEL_NETWORK, state.getNetworkType());
            updateState(CHANNEL_DEVICENAME, state.getUserDeviceName());
            updateState(CHANNEL_SOFTWAREV, state.getSoftwareVersion());
            updateState(CHANNEL_SOFTWAREB, state.getSoftwareBuild());
            updateState(CHANNEL_SECUREDEVICE, state.getSecureDevice());
            updateState(CHANNEL_LANGUAGE, state.getLanguage());
            updateState(CHANNEL_COUNTRY, state.getCountry());
            updateState(CHANNEL_LOCALE, state.getLocale());
            updateState(CHANNEL_TIMEZONE, state.getTimeZone());
            updateState(CHANNEL_TIMEZONEOFF, state.getTimeZoneOffSet());
            updateState(CHANNEL_SUSPENDED, state.getSupportSuspend());
            updateState(CHANNEL_DEVELOPERENABLED, state.getDeveloperEnabled());
            updateState(CHANNEL_SEARCHENABLED, state.getSearchEnabled());
            updateState(CHANNEL_VOICESEARCHENABLED, state.getVoiceSearchEnabled());
            updateState(CHANNEL_NOTIFICATIONSENABLED, state.getNotificationsEnabled());
            updateState(CHANNEL_HEADPHONESCONNECTED, state.getHeadphonesConnected());
            updateState(CHANNEL_ICON, state.getActiveImage());
            /**
             * Future Functionality
             * updateState(CHANNEL_APPBROWSER, state.getApplicationMenu());
             */
            if (!state.getPowerMode().toFullString().matches("PowerOn")) {
                updateStatus(ThingStatus.OFFLINE);
            } else {
                if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        } catch (NullPointerException e) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
        }
    }
}
