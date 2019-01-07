/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.smarthome.core.library.types.OnOffType;
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
import org.grohe.ondus.api.model.ApplianceCommand;
import org.grohe.ondus.api.model.BaseApplianceData;
import org.grohe.ondus.api.model.SenseGuardAppliance;
import org.grohe.ondus.api.model.SenseGuardApplianceData;
import org.grohe.ondus.api.model.SenseGuardApplianceData.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusSenseGuardHandler<T, M> extends GroheOndusBaseHandler<SenseGuardAppliance, Measurement> {

    private final Logger logger = LoggerFactory.getLogger(GroheOndusSenseGuardHandler.class);

    public GroheOndusSenseGuardHandler(Thing thing) {
        super(thing, SenseGuardAppliance.TYPE);
    }

    @Override
    protected int getPollingInterval(SenseGuardAppliance appliance) {
        if (config.pollingInterval > 0) {
            return config.pollingInterval;
        }
        return appliance.getConfig().getMeasurementTransmissionIntervall();
    }

    @Override
    protected void updateChannel(ChannelUID channelUID, SenseGuardAppliance appliance, Measurement measurement) {
        String channelId = channelUID.getIdWithoutGroup();
        State newState;
        switch (channelId) {
            case CHANNEL_NAME:
                newState = new StringType(appliance.getName());
                break;
            case CHANNEL_PRESSURE:
                newState = new QuantityType<>(measurement.getPressure(), SmartHomeUnits.BAR);
                break;
            case CHANNEL_TEMPERATURE_GUARD:
                newState = new QuantityType<>(measurement.getTemperatureGuard(), SIUnits.CELSIUS);
                break;
            case CHANNEL_VALVE_OPEN:
                newState = getValveOpenType(appliance);
                break;
            default:
                throw new IllegalArgumentException("Channel " + channelUID + " not supported.");
        }
        if (newState != null) {
            updateState(channelUID, newState);
        }
    }

    @Nullable
    private OnOffType getValveOpenType(SenseGuardAppliance appliance) {
        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        Optional<ApplianceCommand> commandOptional;
        try {
            commandOptional = service.getApplianceCommand(appliance);
        } catch (IOException e) {
            logger.debug("Could not get appliance command", e);
            return null;
        }
        if (!commandOptional.isPresent()) {
            return null;
        }
        return commandOptional.get().getCommand().getValveOpen() ? OnOffType.ON : OnOffType.OFF;
    }

    @Override
    protected Measurement getLastMeasurement(SenseGuardAppliance appliance) {
        if (getOndusService() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return new Measurement();
        }

        SenseGuardApplianceData applianceData = getApplianceData(appliance);
        if (applianceData == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not load data from API.");
            return new Measurement();
        }
        List<Measurement> measurementList = applianceData.getData().getMeasurement();

        return measurementList.isEmpty() ? new Measurement() : measurementList.get(measurementList.size() - 1);
    }

    private @Nullable SenseGuardApplianceData getApplianceData(SenseGuardAppliance appliance) {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant today = Instant.now();
        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        try {
            BaseApplianceData applianceData = service.getApplianceData(appliance, yesterday, today).orElse(null);
            if (applianceData.getType() != SenseGuardAppliance.TYPE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Thing is not a GROHE SENSE Guard device.");
                return null;
            }
            return (SenseGuardApplianceData) applianceData;
        } catch (IOException e) {
            logger.debug("Could not load appliance data", e);
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!CHANNEL_VALVE_OPEN.equals(channelUID.getIdWithoutGroup())) {
            return;
        }
        if (!(command instanceof OnOffType)) {
            logger.debug("Invalid command received for channel. Expected OnOffType, received {}.",
                    command.getClass().getName());
            return;
        }
        OnOffType openClosedCommand = (OnOffType) command;
        boolean openState = openClosedCommand == OnOffType.ON;

        OndusService service = getOndusService();
        if (service == null) {
            return;
        }
        SenseGuardAppliance appliance = getAppliance(service);
        if (appliance == null) {
            return;
        }
        try {
            service.setValveOpen(appliance, openState);
            updateChannels();
        } catch (IOException e) {
            logger.debug("Could not update valve open state", e);
        }
    }
}
