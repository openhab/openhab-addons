/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.seneye.internal.handler;

import static org.openhab.binding.seneye.internal.SeneyeBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.openhab.binding.seneye.internal.CommunicationException;
import org.openhab.binding.seneye.internal.InvalidConfigurationException;
import org.openhab.binding.seneye.internal.ReadingsUpdate;
import org.openhab.binding.seneye.internal.SeneyeConfigurationParameters;
import org.openhab.binding.seneye.internal.SeneyeDeviceReading;
import org.openhab.binding.seneye.internal.SeneyeService;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
        if (seneyeService == null || !seneyeService.isInitialized()) {
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
            logger.debug("Updating readings for sensor type {}", seneyeService.seneyeType);
            switch (seneyeService.seneyeType) {
                case 3:
                    updateState(CHANNEL_NH4, new DecimalType(readings.nh4.curr));
                    updateState(CHANNEL_PAR, new DecimalType(readings.par.curr));
                    updateState(CHANNEL_LUX, new DecimalType(readings.lux.curr));
                    updateState(CHANNEL_KELVIN, new DecimalType(readings.kelvin.curr));
                case 2:
                    updateState(CHANNEL_O2, new DecimalType(readings.o2.curr));
                case 1:
                    updateState(CHANNEL_TEMPERATURE, new DecimalType(readings.temperature.curr));
                    updateState(CHANNEL_NH3, new DecimalType(readings.nh3.curr));
                    updateState(CHANNEL_PH, new DecimalType(readings.ph.curr));
                    updateState(CHANNEL_LASTREADING, new DateTimeType(readings.status.getLast_experimentDate()));
                    updateState(CHANNEL_SLIDEEXPIRES, new DateTimeType(readings.status.getSlide_expiresDate()));
                    updateState(CHANNEL_WRONGSLIDE, new StringType(readings.status.getWrong_slideString()));
                    updateState(CHANNEL_SLIDESERIAL, new StringType(readings.status.getSlide_serialString()));
                    updateState(CHANNEL_OUTOFWATER, new StringType(readings.status.getOut_of_waterString()));
                    updateState(CHANNEL_DISCONNECTED, new StringType(readings.status.getDisconnectedString()));
            }
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

        updateStatus(ThingStatus.ONLINE);

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
        cachedSeneyeDeviceReading = new ExpiringCache<>(TimeUnit.SECONDS.toMillis(10), () -> {
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
