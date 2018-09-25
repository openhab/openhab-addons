/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.util.concurrent.TimeUnit;

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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.grohe.ondus.api.OndusService;
import org.grohe.ondus.api.model.Appliance;
import org.grohe.ondus.api.model.ApplianceCommand;
import org.grohe.ondus.api.model.ApplianceData;
import org.grohe.ondus.api.model.ApplianceData.Measurement;
import org.grohe.ondus.api.model.Location;
import org.grohe.ondus.api.model.Room;
import org.openhab.binding.groheondus.internal.GroheOndusApplianceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusApplianceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GroheOndusApplianceHandler.class);

    private @Nullable GroheOndusApplianceConfiguration config;

    public GroheOndusApplianceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(GroheOndusApplianceConfiguration.class);

        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return;
        }

        Appliance appliance = getAppliance(ondusService);
        if (appliance == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, "Could not load appliance");
            return;
        }
        int pollingInterval = getPollingInterval(appliance);
        scheduler.scheduleAtFixedRate(this::updateChannels, 0, pollingInterval, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);

        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return;
        }
        Appliance appliance = getAppliance(ondusService);
        if (appliance == null) {
            return;
        }
        updateChannel(channelUID, appliance, getLastMeasurement(appliance));
    }

    public void updateChannels() {
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "No initialized OndusService available from bridge.");
            return;
        }
        Appliance appliance = getAppliance(ondusService);
        if (appliance == null) {
            return;
        }

        Measurement measurement = getLastMeasurement(appliance);
        getThing().getChannels().forEach(channel -> updateChannel(channel.getUID(), appliance, measurement));

        updateStatus(ThingStatus.ONLINE);
    }

    private int getPollingInterval(Appliance appliance) {
        if (config.pollingInterval > 0) {
            return config.pollingInterval;
        }
        return appliance.getConfig().getMeasurementTransmissionIntervall();
    }

    private void updateChannel(ChannelUID channel, Appliance appliance, Measurement measurement) {
        String channelId = channel.getIdWithoutGroup();
        State newState;
        switch (channelId) {
            case CHANNEL_NAME:
                newState = new StringType(appliance.getName());
                break;
            case CHANNEL_PRESSURE:
                newState = new QuantityType<>(measurement.getPressure(), SmartHomeUnits.BAR);
                break;
            case CHANNEL_TEMPERATURE:
                newState = new QuantityType<>(measurement.getTemperatureGuard(), SIUnits.CELSIUS);
                break;
            case CHANNEL_VALVE_OPEN:
                newState = getValveOpenType(appliance);
                break;
            default:
                throw new IllegalArgumentException("Channel " + channel + " not supported.");
        }
        if (newState != null) {
            updateState(channel, newState);
        }
    }

    @Nullable
    private OnOffType getValveOpenType(Appliance appliance) {
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

    private Measurement getLastMeasurement(Appliance appliance) {
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

        return measurementList.isEmpty() ? new Measurement() : measurementList.get(measurementList.size() - 1);
    }

    private @Nullable ApplianceData getApplianceData(Appliance appliance) {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant today = Instant.now();
        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        try {
            return service.getApplianceData(appliance, yesterday, today).orElse(null);
        } catch (IOException e) {
            logger.debug("Could not load appliance data", e);
        }
        return null;
    }

    private @Nullable Appliance getAppliance(OndusService ondusService) {
        try {
            return ondusService.getAppliance(getRoom(), config.applianceId).orElse(null);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Could not load appliance", e);
        }
        return null;
    }

    private Room getRoom() {
        return new Room(config.roomId, getLocation());
    }

    private Location getLocation() {
        return new Location(config.locationId);
    }

    public @Nullable OndusService getOndusService() {
        if (getBridge() == null) {
            return null;
        }
        if (getBridge().getHandler() == null) {
            return null;
        }
        if (!(getBridge().getHandler() instanceof GroheOndusAccountHandler)) {
            return null;
        }
        try {
            return ((GroheOndusAccountHandler) getBridge().getHandler()).getService();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
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
        Appliance appliance = getAppliance(service);
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
