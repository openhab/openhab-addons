/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.honeywellwifithermostat.handler;

import static org.openhab.binding.honeywellwifithermostat.honeywellWifiThermostatBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.honeywellwifithermostat.internal.data.HoneywellThermostatData;
import org.openhab.binding.honeywellwifithermostat.internal.data.HoneywellThermostatFanMode;
import org.openhab.binding.honeywellwifithermostat.internal.data.HoneywellThermostatSystemMode;
import org.openhab.binding.honeywellwifithermostat.internal.webapi.HoneywellWebsiteJetty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link honeywellWifiThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author JD Steffen - Initial contribution
 */
public class honeywellWifiThermostatHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(honeywellWifiThermostatHandler.class);

    HoneywellWebsiteJetty webapi;
    ScheduledFuture<?> refreshJob;

    private String deviceID = null;

    private HoneywellThermostatData thermodata;

    public honeywellWifiThermostatHandler(Thing thing) {
        super(thing);

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case COOL_SETPOINT:
                DecimalType coolSP_val = (DecimalType) command;
                thermodata.setCoolSetPoint(coolSP_val.intValue());
                break;
            case HEAT_SETPOINT:
                DecimalType heatSP_val = (DecimalType) command;
                thermodata.setHeatSetPoint(heatSP_val.intValue());
                break;
            case SYSTEM_MODE:
                StringType sysmode_val = (StringType) command;
                thermodata.setCurrentSystemMode(HoneywellThermostatSystemMode.valueOf(sysmode_val.toString()));
                break;
            case FAN_MODE:
                OnOffType fanmode_val = (OnOffType) command;
                if (command == OnOffType.OFF) {
                    thermodata.setCurrentFanMode(HoneywellThermostatFanMode.AUTO);
                } else if (command == OnOffType.ON) {
                    thermodata.setCurrentFanMode(HoneywellThermostatFanMode.ON);
                }

                break;
        }

        if (!webapi.submitThermostatChange(deviceID, thermodata)) {
            logger.error("Failed to submit changes to honeywell site.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to submit changes to Honeywell site.");
        }
    }

    @Override
    public void initialize() {

        super.initialize();

        Configuration conf = this.getConfig();
        webapi = HoneywellWebsiteJetty.getInstance();

        if (conf.get("userName") != null) {
            webapi.setUsername(String.valueOf(conf.get("userName")));
        }
        if (conf.get("passWord") != null) {
            webapi.setPassword(String.valueOf(conf.get("passWord")));
        }
        if (conf.get("deviceID") != null) {
            deviceID = String.valueOf(conf.get("deviceID"));
        }

        thermodata = new HoneywellThermostatData();

        logger.debug("Attempting to login to Honeywell site.");

        logger.debug("Logged into Honeywell website.");
        if (webapi.isLoginValid()) {
            logger.debug("Login valid.");

            thermodata = webapi.getTherostatData(deviceID);
            if (thermodata == null) {
                logger.error("Failed to get thermostat data from website.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to retrive data from Honeywell site.");
            }

            updateState(new ChannelUID(getThing().getUID(), CURRENT_TEMPERATURE),
                    new DecimalType(thermodata.getCurrentTemperature()));
            updateState(new ChannelUID(getThing().getUID(), SYSTEM_MODE),
                    new StringType(thermodata.getCurrentSystemMode().name()));
            updateState(new ChannelUID(getThing().getUID(), HEAT_SETPOINT),
                    new DecimalType(thermodata.getHeatSetPoint()));
            updateState(new ChannelUID(getThing().getUID(), COOL_SETPOINT),
                    new DecimalType(thermodata.getCoolSetPoint()));
            updateState(new ChannelUID(getThing().getUID(), FAN_MODE),
                    new StringType(thermodata.getCurrentFanMode().name()));

            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    logger.debug("Getting thermostat data.");
                    thermodata = webapi.getTherostatData(deviceID);
                    if (thermodata == null) {
                        logger.error("Failed to get thermostat data from website.");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Failed to retrive data from Honeywell site.");
                    }

                    updateState(new ChannelUID(getThing().getUID(), CURRENT_TEMPERATURE),
                            new DecimalType(thermodata.getCurrentTemperature()));
                    updateState(new ChannelUID(getThing().getUID(), SYSTEM_MODE),
                            new StringType(thermodata.getCurrentSystemMode().name()));
                    updateState(new ChannelUID(getThing().getUID(), HEAT_SETPOINT),
                            new DecimalType(thermodata.getHeatSetPoint()));
                    updateState(new ChannelUID(getThing().getUID(), COOL_SETPOINT),
                            new DecimalType(thermodata.getCoolSetPoint()));
                    updateState(new ChannelUID(getThing().getUID(), FAN_MODE),
                            new StringType(thermodata.getCurrentFanMode().name()));

                }
            };
            refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, 60, TimeUnit.SECONDS);

        } else {
            logger.error("Login invalid.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Login username/password invalid.");
        }
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        webapi.dispose();
        super.dispose();
    }

}
