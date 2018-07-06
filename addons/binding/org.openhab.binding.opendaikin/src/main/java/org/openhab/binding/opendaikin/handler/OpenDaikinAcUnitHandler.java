/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.opendaikin.OpenDaikinBindingConstants;
import org.openhab.binding.opendaikin.internal.OpenDaikinWebTargets;
import org.openhab.binding.opendaikin.internal.api.ControlInfo;
import org.openhab.binding.opendaikin.internal.api.SensorInfo;
import org.openhab.binding.opendaikin.internal.config.OpenDaikinConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communicating with a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class OpenDaikinAcUnitHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(OpenDaikinAcUnitHandler.class);

    private long refreshInterval;

    private final Client client = ClientBuilder.newClient();
    private OpenDaikinWebTargets webTargets;
    private ScheduledFuture<?> pollFuture;

    public OpenDaikinAcUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case OpenDaikinBindingConstants.CHANNEL_AC_POWER:
                if (command instanceof OnOffType) {
                    changePower(((OnOffType) command).equals(OnOffType.ON));
                } else {
                    logger.warn("Received command of wrong type");
                }
                break;
            case OpenDaikinBindingConstants.CHANNEL_AC_TEMPC:
                if (command instanceof DecimalType) {
                    changeSetPointC(((DecimalType) command).doubleValue());
                } else {
                    logger.warn("Received command of wrong type");
                }
                break;
            case OpenDaikinBindingConstants.CHANNEL_AC_TEMPF:
                if (command instanceof DecimalType) {
                    changeSetPointF(((DecimalType) command).doubleValue());
                } else {
                    logger.warn("Received command of wrong type");
                }
                break;
            case OpenDaikinBindingConstants.CHANNEL_AC_FAN_SPEED:
                if (command instanceof StringType) {
                    changeFanSpeed(ControlInfo.FanSpeed.valueOf(((StringType) command).toString()));
                } else {
                    logger.warn("Received command of wrong type");
                }
                break;
            case OpenDaikinBindingConstants.CHANNEL_AC_MODE:
                if (command instanceof StringType) {
                    changeMode(ControlInfo.Mode.valueOf(((StringType) command).toString()));
                } else {
                    logger.warn("Received command of wrong type");
                }
                break;
        }

        poll();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing HDPowerView HUB");
        OpenDaikinConfiguration config = getConfigAs(OpenDaikinConfiguration.class);
        if (config.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host address must be set");
        }
        webTargets = new OpenDaikinWebTargets(client, config.host);
        refreshInterval = config.refresh;

        schedulePoll();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    void pollNow() {
        if (isInitialized()) {
            schedulePoll();
        }
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 500ms out, then every {} ms", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(pollingRunnable, 500, refreshInterval, TimeUnit.MILLISECONDS);
    }

    private synchronized void stopPoll() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

    private synchronized void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to bridge", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        } catch (Exception e) {
            logger.warn("Unexpected error connecting to bridge", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void pollStatus() throws IOException {
        ControlInfo controlInfo = webTargets.getControlInfo();
        updateStatus(ThingStatus.ONLINE);
        if (controlInfo != null) {
            updateState(OpenDaikinBindingConstants.CHANNEL_AC_POWER, controlInfo.power ? OnOffType.ON : OnOffType.OFF);
            updateState(OpenDaikinBindingConstants.CHANNEL_AC_TEMPC, new DecimalType(controlInfo.temp));
            updateState(OpenDaikinBindingConstants.CHANNEL_AC_TEMPF, new DecimalType(cToF(controlInfo.temp)));

            updateState(OpenDaikinBindingConstants.CHANNEL_AC_MODE, new StringType(controlInfo.mode.name()));
            updateState(OpenDaikinBindingConstants.CHANNEL_AC_FAN_SPEED, new StringType(controlInfo.fanSpeed.name()));
        }

        SensorInfo sensorInfo = webTargets.getSensorInfo();
        if (sensorInfo != null) {
            if (sensorInfo.indoortemp.isPresent()) {
                updateState(OpenDaikinBindingConstants.CHANNEL_INDOOR_TEMPC,
                        new DecimalType(sensorInfo.indoortemp.get()));
                updateState(OpenDaikinBindingConstants.CHANNEL_INDOOR_TEMPF,
                        new DecimalType(cToF(sensorInfo.indoortemp.get())));
            } else {
                updateState(OpenDaikinBindingConstants.CHANNEL_INDOOR_TEMPC, UnDefType.UNDEF);
                updateState(OpenDaikinBindingConstants.CHANNEL_INDOOR_TEMPF, UnDefType.UNDEF);
            }

            if (sensorInfo.outdoortemp.isPresent()) {
                updateState(OpenDaikinBindingConstants.CHANNEL_OUTDOOR_TEMPC,
                        new DecimalType(sensorInfo.outdoortemp.get()));
                updateState(OpenDaikinBindingConstants.CHANNEL_OUTDOOR_TEMPF,
                        new DecimalType(cToF(sensorInfo.outdoortemp.get())));
            } else {
                updateState(OpenDaikinBindingConstants.CHANNEL_OUTDOOR_TEMPC, UnDefType.UNDEF);
                updateState(OpenDaikinBindingConstants.CHANNEL_OUTDOOR_TEMPF, UnDefType.UNDEF);
            }

            if (sensorInfo.indoorhumidity.isPresent()) {
                updateState(OpenDaikinBindingConstants.CHANNEL_HUMIDITY,
                        new DecimalType(sensorInfo.indoorhumidity.get()));
            } else {
                updateState(OpenDaikinBindingConstants.CHANNEL_HUMIDITY, UnDefType.UNDEF);
            }
        }
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            poll();
        }

    };

    private void changePower(boolean power) {
        ControlInfo info = webTargets.getControlInfo();
        info.power = power;
        webTargets.setControlInfo(info);
    }

    private void changeSetPointC(double tempc) {
        ControlInfo info = webTargets.getControlInfo();
        info.temp = tempc;
        webTargets.setControlInfo(info);
    }

    private void changeSetPointF(double tempf) {
        changeSetPointC(fToC(tempf));
    }

    private void changeMode(ControlInfo.Mode mode) {
        ControlInfo info = webTargets.getControlInfo();
        info.mode = mode;
        webTargets.setControlInfo(info);
    }

    private void changeFanSpeed(ControlInfo.FanSpeed fanSpeed) {
        ControlInfo info = webTargets.getControlInfo();
        info.fanSpeed = fanSpeed;
        webTargets.setControlInfo(info);
    }

    private double cToF(double tempc) {
        return Math.round((tempc * 1.8) + 32);
    }

    private double fToC(double tempf) {
        return roundToNearestHalf((tempf - 32.0) / 1.8);
    }

    private double roundToNearestHalf(double temp) {
        return Math.round(temp * 2.0) / 2.0;
    }
}
