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

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.seneye.internal.CommunicationException;
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
public final class SeneyeHandler extends BaseThingHandler implements ReadingsUpdate {

    private final Logger logger = LoggerFactory.getLogger(SeneyeHandler.class);
    private SeneyeService seneyeService;
    private ExpiringCache<SeneyeDeviceReading> cachedSeneyeDeviceReading;

    public SeneyeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (seneyeService == null || seneyeService.isInitialized() == false) {
            return;
        }

        if (command instanceof RefreshType) {
            SeneyeDeviceReading readings = cachedSeneyeDeviceReading.getValue();
            newState(readings);
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void newState(SeneyeDeviceReading readings) {
        if (readings != null) {
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
    }

    @Override
    public void invalidConfig() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Override
    public void initialize() {
        SeneyeConfigurationParameters config = getConfigAs(SeneyeConfigurationParameters.class);

        if (config.aquarium_name == null || config.aquarium_name.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Aquarium name must be provided");
            return;
        }
        if (config.username == null || config.username.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Seneye username must be provided");
            return;
        }
        if (config.password == null || config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check configuration, Seneye password must be provided");
            return;
        }

        logger.debug("Initializing Seneye API service.");
        try {
            this.seneyeService = new SeneyeService(config);
        } catch (CommunicationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            return; // critical error
        }

        super.initialize();

        // contact Seneye API
        scheduler.submit(() -> {
            initializeSeneyeService();
        });
    }

    private void initializeSeneyeService() {
        try {
            seneyeService.initialize();
        } catch (CommunicationException ex) {
            // try again in 30 secs
            scheduler.schedule(() -> {
                initializeSeneyeService();
            }, 30, TimeUnit.SECONDS);

            return;
        } catch (InvalidConfigurationException ex) {
            // bad configuration, stay offline until user corrects the configuration
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());

            return;
        }

        // ok, initialization succeeded
        cachedSeneyeDeviceReading = new ExpiringCache<SeneyeDeviceReading>(TimeUnit.SECONDS.toMillis(10), () -> {
            return seneyeService.getDeviceReadings();
        });

        seneyeService.startAutomaticRefresh(scheduler, this);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        seneyeService.stopAutomaticRefresh();
        seneyeService = null;
    }
}
