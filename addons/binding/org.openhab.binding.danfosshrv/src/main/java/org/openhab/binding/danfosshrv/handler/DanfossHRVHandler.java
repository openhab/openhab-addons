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
            // Placeholder for later refinement
            update();
        } else if (channelUID.getId().equals(CHANNEL_MODE)) {
            try {
                updateState(channelUID, hrv.setMode(command));
            } catch (IOException ioe) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
            }

        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
            try {
                updateState(channelUID, hrv.setFanSpeed(command));
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
            updateStatus(ThingStatus.ONLINE);

        } catch (UnknownHostException uhe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, uhe.getMessage());
            return;
        } catch (IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
            return;
        }

        pollingJob = scheduler.scheduleWithFixedDelay(() -> update(), 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Danfoss HRV handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        hrv.disconnect();
        hrv = null;
    }

    private void update() {
        logger.debug("Updating DanfossHRV data '{}'", getThing().getUID());

        try {
            updateState(CHANNEL_MODE, hrv.getMode());
            updateState(CHANNEL_FAN_SPEED, hrv.getFanSpeed());
            updateState(CHANNEL_HUMIDITY, hrv.getHumidity());
            updateState(CHANNEL_BATTERY_LIFE, hrv.getBatteryLife());
            updateState(CHANNEL_CURRENT_TIME, hrv.getCurrentTime());

        } catch (IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, ioe.getMessage());
        }

        if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
