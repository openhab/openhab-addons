/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.seneye.handler;

import static org.openhab.binding.seneye.SeneyeBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.seneye.internal.InvalidConfigurationException;
import org.openhab.binding.seneye.internal.ReadingsUpdate;
import org.openhab.binding.seneye.internal.SeneyeConfigurationParameters;
import org.openhab.binding.seneye.internal.SeneyeDeviceReading;
import org.openhab.binding.seneye.internal.SeneyeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SeneyeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SeneyeHandler extends BaseThingHandler implements ReadingsUpdate {

    private Logger logger = LoggerFactory.getLogger(SeneyeHandler.class);
    private SeneyeService seneyeService;

    public SeneyeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (seneyeService == null || seneyeService.isInitialized() == false) {
            return;
        }

        if (command instanceof RefreshType) {
            try {
                SeneyeDeviceReading readings = seneyeService.getDeviceReadings();
                newState(readings);
            } catch (InvalidConfigurationException invalidConfigurationException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void newState(SeneyeDeviceReading readings) {
        updateState(CHANNEL_TEMPERATURE, new DecimalType(readings.temperature.curr));
        updateState(CHANNEL_NH3, new DecimalType(readings.nh3.curr));
        updateState(CHANNEL_NH4, new DecimalType(readings.nh4.curr));
        updateState(CHANNEL_O2, new DecimalType(readings.o2.curr));
        updateState(CHANNEL_PAR, new DecimalType(readings.par.curr));
        updateState(CHANNEL_PH, new DecimalType(readings.ph.curr));
        updateState(CHANNEL_LUX, new DecimalType(readings.lux.curr));
        updateState(CHANNEL_KELVIN, new DecimalType(readings.kelvin.curr));
        updateState(CHANNEL_LASTREADING, new DateTimeType(readings.status.getLast_experimentDate()));
        updateState(CHANNEL_SLIDEEXPIRES, new DateTimeType(readings.status.getSlide_expiresDate()));
    }

    @Override
    public void invalidConfig() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Override
    public void initialize() {
        SeneyeConfigurationParameters config = getConfigAs(SeneyeConfigurationParameters.class);

        if (config.aquarium_name != null && config.aquarium_name.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, aquarium name must be provided");
            return;
        }
        if (config.username != null && config.username.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Seneye username must be provided");
            return;
        }
        if (config.password != null && config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Seneye password must be provided");
            return;
        }

        logger.debug("Initialize Network handler.");
        this.seneyeService = new SeneyeService(config);

        super.initialize();

        if (seneyeService.initialize()) {

            seneyeService.startAutomaticRefresh(scheduler, this);

            updateStatus(ThingStatus.ONLINE);

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find a seneye with configured aquarium name");
        }
    }

    @Override
    public void dispose() {
        seneyeService.stopAutomaticRefresh();
    }
}
