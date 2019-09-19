/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.grohe.ondus.api.OndusService;
import org.grohe.ondus.api.model.ApplianceStatus;
import org.grohe.ondus.api.model.BaseApplianceData;
import org.grohe.ondus.api.model.SenseAppliance;
import org.grohe.ondus.api.model.SenseApplianceData;
import org.grohe.ondus.api.model.SenseApplianceData.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public class GroheOndusSenseHandler<T, M> extends GroheOndusBaseHandler<SenseAppliance, Measurement> {

    private static final int DEFAULT_POLLING_INTERVAL = 900;

    private final Logger logger = LoggerFactory.getLogger(GroheOndusSenseHandler.class);

    public GroheOndusSenseHandler(Thing thing) {
        super(thing, SenseAppliance.TYPE);
    }

    @Override
    protected int getPollingInterval(SenseAppliance appliance) {
        if (config.pollingInterval > 0) {
            return config.pollingInterval;
        }
        return DEFAULT_POLLING_INTERVAL;
    }

    @Override
    protected void updateChannel(ChannelUID channelUID, SenseAppliance appliance, Measurement measurement) {
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
                newState = new QuantityType<>(measurement.getHumidity(), SmartHomeUnits.PERCENT);
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
    protected Measurement getLastDataPoint(SenseAppliance appliance) {
        if (getOndusService() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return new Measurement();
        }

        SenseApplianceData applianceData = getApplianceData(appliance);
        if (applianceData == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not load data from API.");
            return new Measurement();
        }
        List<Measurement> measurementList = applianceData.getData().getMeasurement();

        return measurementList.isEmpty() ? new Measurement() : measurementList.get(measurementList.size() - 1);
    }

    private int getBatteryStatus(SenseAppliance appliance) {
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return -1;
        }

        Optional<ApplianceStatus> applianceStatusOptional;
        try {
            applianceStatusOptional = ondusService.getApplianceStatus(appliance);
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

    private @Nullable SenseApplianceData getApplianceData(SenseAppliance appliance) {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant today = Instant.now();
        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        try {
            BaseApplianceData applianceData = service.getApplianceData(appliance, yesterday, today).orElse(null);
            if (applianceData.getType() != SenseAppliance.TYPE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Thing is not a GROHE SENSE device.");
                return null;
            }
            return (SenseApplianceData) applianceData;
        } catch (IOException e) {
            logger.debug("Could not load appliance data", e);
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
