/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal.handler;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.floriansw.ondus.api.OndusService;
import io.github.floriansw.ondus.api.model.ApplianceStatus;
import io.github.floriansw.ondus.api.model.BaseApplianceData;
import io.github.floriansw.ondus.api.model.sense.Appliance;
import io.github.floriansw.ondus.api.model.sense.ApplianceData;
import io.github.floriansw.ondus.api.model.sense.ApplianceData.Measurement;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public class GroheOndusSenseHandler<T, M> extends GroheOndusBaseHandler<Appliance, Measurement> {

    private static final int DEFAULT_POLLING_INTERVAL = 900;

    private final Logger logger = LoggerFactory.getLogger(GroheOndusSenseHandler.class);

    public GroheOndusSenseHandler(Thing thing, int thingCounter) {
        super(thing, Appliance.TYPE, thingCounter);
    }

    @Override
    protected int getPollingInterval(Appliance appliance) {
        if (config.pollingInterval > 0) {
            return config.pollingInterval;
        }
        return DEFAULT_POLLING_INTERVAL;
    }

    @Override
    protected void updateChannel(ChannelUID channelUID, Appliance appliance, Measurement measurement) {
        String channelId = channelUID.getIdWithoutGroup();
        State newState;
        switch (channelId) {
            case CHANNEL_NAME:
                newState = new StringType(appliance.getName());
                break;
            case CHANNEL_TEMPERATURE:
                newState = new QuantityType<>(measurement.getTemperature(), SIUnits.CELSIUS);
                break;
            case CHANNEL_HUMIDITY:
                newState = new QuantityType<>(measurement.getHumidity(), Units.PERCENT);
                break;
            case CHANNEL_BATTERY:
                newState = new DecimalType(getBatteryStatus(appliance));
                break;
            default:
                throw new IllegalArgumentException("Channel " + channelUID + " not supported.");
        }
        if (newState != null) {
            updateState(channelUID, newState);
        }
    }

    @Override
    protected Measurement getLastDataPoint(Appliance appliance) {
        if (getOndusService() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return new Measurement();
        }

        ApplianceData applianceData = getApplianceData(appliance);
        if (applianceData == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not load data from API.");
            return new Measurement();
        }
        List<Measurement> measurementList = applianceData.getData().getMeasurement();
        Collections.sort(measurementList, Comparator.comparing(e -> ZonedDateTime.parse(e.timestamp)));
        return measurementList.isEmpty() ? new Measurement() : measurementList.get(measurementList.size() - 1);
    }

    private int getBatteryStatus(Appliance appliance) {
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return -1;
        }

        Optional<ApplianceStatus> applianceStatusOptional;
        try {
            applianceStatusOptional = ondusService.applianceStatus(appliance);
            if (!applianceStatusOptional.isPresent()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not load data from API.");
                return -1;
            }

            return applianceStatusOptional.get().getBatteryStatus();
        } catch (IOException e) {
            logger.debug("Could not load appliance status", e);
        }
        return -1;
    }

    private @Nullable ApplianceData getApplianceData(Appliance appliance) {
        // Dates are stripped of time part inside library
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant today = Instant.now().plus(1, ChronoUnit.DAYS);
        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        try {
            BaseApplianceData applianceData = service.applianceData(appliance, yesterday, today).orElse(null);
            if (applianceData != null) {
                if (applianceData.getType() != Appliance.TYPE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Thing is not a GROHE SENSE device.");
                    return null;
                }
                return (ApplianceData) applianceData;
            } else {
                logger.debug("Could not load appliance data");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to find applicance data");
            }
        } catch (IOException e) {
            logger.debug("Could not load appliance data", e);
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
            return;
        }
    }
}
