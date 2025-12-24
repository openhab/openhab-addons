/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_BATTERY;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_HUMIDITY;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_NAME;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_TEMPERATURE;

import java.io.IOException;
import java.time.Instant;
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
import org.openhab.core.types.UnDefType;
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
        State newState = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_NAME:
                newState = new StringType(appliance.getName());
                break;
            case CHANNEL_TEMPERATURE:
                if (measurement.getTemperature() != null) {
                    newState = new QuantityType<>(measurement.getTemperature(), SIUnits.CELSIUS);
                }
                break;
            case CHANNEL_HUMIDITY:
                if (measurement.getHumidity() != null) {
                    newState = new QuantityType<>(measurement.getHumidity(), Units.PERCENT);
                }
                break;
            case CHANNEL_BATTERY:
                Integer batteryStatus = getBatteryStatus(appliance);
                if (batteryStatus != null) {
                    newState = new DecimalType(batteryStatus);
                }
                break;
            default:
                throw new IllegalArgumentException("Channel " + channelUID + " not supported.");
        }
        updateState(channelUID, newState);
    }

    @Override
    protected Measurement getLastDataPoint(Appliance appliance) {
        if (getOndusService() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            return new Measurement();
        }

        ApplianceData applianceData = getApplianceData(appliance);
        if (applianceData == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.empty.response");
            return new Measurement();
        }
        List<Measurement> measurementList = applianceData.getData().getMeasurement();
        Collections.sort(measurementList, Comparator.comparing(e -> e.date));
        return measurementList.isEmpty() ? new Measurement() : measurementList.get(measurementList.size() - 1);
    }

    private @Nullable Integer getBatteryStatus(Appliance appliance) {
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            return null;
        }

        Optional<ApplianceStatus> applianceStatusOptional;
        try {
            applianceStatusOptional = ondusService.applianceStatus(appliance);
            if (applianceStatusOptional.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.empty.response");
                return null;
            }

            return applianceStatusOptional.get().getBatteryStatus();
        } catch (IOException e) {
            logger.debug("Could not load appliance status", e);
        }
        return null;
    }

    private @Nullable ApplianceData getApplianceData(Appliance appliance) {
        // Dates are stripped of time part inside library
        Instant now = Instant.now();
        Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS); // Devices only report once a day - at best
        Instant tomorrow = now.plus(1, ChronoUnit.DAYS);
        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        try {
            logger.debug("Fetching data for {} from {} to {}", thing.getUID(), twoDaysAgo, tomorrow);
            BaseApplianceData applianceData = service.applianceData(appliance, twoDaysAgo, tomorrow).orElse(null);
            if (applianceData != null) {
                if (applianceData.getType() != Appliance.TYPE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.notsense");
                    return null;
                }
                return (ApplianceData) applianceData;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.failedtoloaddata");
            }
        } catch (IOException e) {
            logger.debug("Could not load appliance data for {}", thing.getUID(), e);
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
        }
    }
}
