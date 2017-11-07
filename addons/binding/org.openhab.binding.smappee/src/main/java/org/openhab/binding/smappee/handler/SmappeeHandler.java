/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.handler;

import static org.openhab.binding.smappee.SmappeeBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smappee.service.InvalidConfigurationException;
import org.openhab.binding.smappee.service.ReadingsUpdate;
import org.openhab.binding.smappee.service.SmappeeDeviceReading;
import org.openhab.binding.smappee.service.SmappeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmappeeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeHandler extends BaseThingHandler implements ReadingsUpdate {

    private Logger logger = LoggerFactory.getLogger(SmappeeHandler.class);
    private SmappeeService smappeeService;

    public SmappeeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (smappeeService == null || smappeeService.isInitialized() == false) {
            return;
        }

        if (command instanceof RefreshType) {
            try {
                SmappeeDeviceReading readings = smappeeService.getDeviceReadings();
                newState(readings);
            } catch (InvalidConfigurationException invalidConfigurationException) {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void newState(SmappeeDeviceReading readings) {
        updateState(CHANNEL_CONSUMPTION, new DecimalType(readings.getLatestConsumption()));
        updateState(CHANNEL_SOLAR, new DecimalType(readings.getLatestSolar()));
        updateState(CHANNEL_ALWAYSON, new DecimalType(readings.getLatestAlwaysOn()));
    }

    @Override
    public void invalidConfig() {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void initialize() {
        Configuration conf = this.getConfig();

        String param_client_id = String.valueOf(conf.get(PARAMETER_CLIENT_ID));
        String param_client_secret = String.valueOf(conf.get(PARAMETER_CLIENT_SECRET));
        String param_username = String.valueOf(conf.get(PARAMETER_USERNAME));
        String param_password = String.valueOf(conf.get(PARAMETER_PASSWORD));
        String param_serviceLocationName = String.valueOf(conf.get(PARAMETER_SERVICE_LOCATION_NAME));
        String param_polltime = String.valueOf(conf.get(PARAMETER_POLLTIME));

        if (param_client_id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Client Id must be provided");
            return;
        }
        if (param_client_secret.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Client secret must be provided");
            return;
        }
        if (param_username.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Username must be provided");
            return;
        }
        if (param_password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Password must be provided");
            return;
        }
        if (param_serviceLocationName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Service location name must be provided");
            return;
        }
        if (param_polltime.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, polling time must be provided");
            return;
        }

        int polltime = Integer.parseInt(param_polltime) * 60000;

        logger.debug("Initialize Network handler.");
        smappeeService = new SmappeeService(param_client_id, param_client_secret, param_username, param_password,
                param_serviceLocationName, polltime);

        super.initialize();

        if (smappeeService.initialize()) {

            smappeeService.startAutomaticRefresh(scheduler, this);

            updateStatus(ThingStatus.ONLINE);

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find a smappee with configured service location name");
        }
    }

    @Override
    public void dispose() {
        smappeeService.stopAutomaticRefresh();
    }

}
