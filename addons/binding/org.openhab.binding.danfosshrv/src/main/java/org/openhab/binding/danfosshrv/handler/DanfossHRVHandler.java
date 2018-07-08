/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.danfosshrv.handler;

import static org.openhab.binding.danfosshrv.DanfossHRVBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.danfosshrv.internal.DanfossHRV;
import org.openhab.binding.danfosshrv.internal.DanfossHRVConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DanfossHRVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ralf Duckstein - Initial contribution
 */
public class DanfossHRVHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DanfossHRVHandler.class);
    private ScheduledFuture<?> pollingJob;
    private DanfossHRV hrv;
    private DanfossHRVConfiguration config;

    public DanfossHRVHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            update();

        } else {
            try {
                if (hrv == null) {
                    return;
                }
                hrv.connect();
                if (channelUID.getIdWithoutGroup().equals(CHANNEL_MODE)) {
                    updateState(channelUID, hrv.setMode(command));
                } else if (channelUID.getIdWithoutGroup().equals(CHANNEL_FAN_SPEED)) {
                    updateState(channelUID, hrv.setFanSpeed(command));
                } else if (channelUID.getIdWithoutGroup().equals(CHANNEL_BOOST)) {
                    updateState(channelUID, hrv.setBoost(command));
                } else if (channelUID.getIdWithoutGroup().equals(CHANNEL_BYPASS)) {
                    updateState(channelUID, hrv.setBypass(command));
                }
                hrv.disconnect();
            } catch (IOException ioe) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(DanfossHRVConfiguration.class);

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        try {
            hrv = new DanfossHRV(InetAddress.getByName(config.host), 30046);

            pollingJob = scheduler.scheduleAtFixedRate(() -> update(), 5, config.polling, TimeUnit.SECONDS);

            hrv.connect();
            thing.setProperty(PROPERTY_UNIT_NAME, hrv.getUnitName());
            thing.setProperty(PROPERTY_SERIAL, hrv.getUnitSerialNumber());
            hrv.disconnect();
            updateStatus(ThingStatus.ONLINE);

        } catch (UnknownHostException uhe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, uhe.getMessage());
            return;
        } catch (IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
            return;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Danfoss HRV handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (hrv != null) {
            hrv.disconnect();
            hrv = null;
        }
    }

    private void update() {
        if (hrv == null) {
            return;
        }

        logger.debug("Updating DanfossHRV data '{}'", getThing().getUID());

        try {
            hrv.connect();
            updateState(GROUP_MAIN, CHANNEL_CURRENT_TIME, hrv.getCurrentTime());
            updateState(GROUP_MAIN, CHANNEL_MODE, hrv.getMode());
            updateState(GROUP_MAIN, CHANNEL_FAN_SPEED, hrv.getFanSpeed());
            updateState(GROUP_MAIN, CHANNEL_BOOST, hrv.getBoost());

            updateState(GROUP_TEMPS, CHANNEL_ROOM_TEMP, hrv.getRoomTemperature());
            updateState(GROUP_TEMPS, CHANNEL_OUTDOOR_TEMP, hrv.getOutdoorTemperature());

            updateState(GROUP_HUMIDITY, CHANNEL_HUMIDITY, hrv.getHumidity());

            updateState(GROUP_RECUPERATOR, CHANNEL_BYPASS, hrv.getBypass());
            updateState(GROUP_RECUPERATOR, CHANNEL_SUPPLY_TEMP, hrv.getSupplyTemperature());
            updateState(GROUP_RECUPERATOR, CHANNEL_EXTRACT_TEMP, hrv.getExtractTemperature());
            updateState(GROUP_RECUPERATOR, CHANNEL_EXHAUST_TEMP, hrv.getExhaustTemperature());

            updateState(GROUP_SERVICE, CHANNEL_BATTERY_LIFE, hrv.getBatteryLife());
            updateState(GROUP_SERVICE, CHANNEL_FILTER_LIFE, hrv.getFilterLife());
            hrv.disconnect();
            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
        }
    }

    private void updateState(String groupId, String channelId, State state) {
        updateState(new ChannelUID(thing.getUID(), groupId, channelId), state);
    }
}
