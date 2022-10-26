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

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_CONFIG_TIMEFRAME;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_NAME;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_PRESSURE;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_TEMPERATURE_GUARD;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_VALVE_OPEN;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_WATERCONSUMPTION;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_WATERCONSUMPTION_SINCE_MIDNIGHT;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
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
import io.github.floriansw.ondus.api.model.BaseApplianceCommand;
import io.github.floriansw.ondus.api.model.BaseApplianceData;
import io.github.floriansw.ondus.api.model.guard.Appliance;
import io.github.floriansw.ondus.api.model.guard.ApplianceCommand;
import io.github.floriansw.ondus.api.model.guard.ApplianceData;
import io.github.floriansw.ondus.api.model.guard.ApplianceData.Data;
import io.github.floriansw.ondus.api.model.guard.ApplianceData.Measurement;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusSenseGuardHandler<T, M> extends GroheOndusBaseHandler<Appliance, Data> {
    private static final int MIN_API_TIMEFRAME_DAYS = 1;
    private static final int MAX_API_TIMEFRAME_DAYS = 90;
    private static final int DEFAULT_TIMEFRAME_DAYS = 1;

    private final Logger logger = LoggerFactory.getLogger(GroheOndusSenseGuardHandler.class);

    public GroheOndusSenseGuardHandler(Thing thing, int thingCounter) {
        super(thing, Appliance.TYPE, thingCounter);
    }

    @Override
    protected int getPollingInterval(Appliance appliance) {
        if (config.pollingInterval > 0) {
            return config.pollingInterval;
        }
        return appliance.getConfig().getMeasurementTransmissionIntervall();
    }

    @Override
    protected void updateChannel(ChannelUID channelUID, Appliance appliance, Data dataPoint) {
        String channelId = channelUID.getIdWithoutGroup();
        State newState = UnDefType.UNDEF;
        Measurement lastMeasurement = getLastMeasurement(dataPoint);
        switch (channelId) {
            case CHANNEL_NAME:
                newState = new StringType(appliance.getName());
                break;
            case CHANNEL_PRESSURE:
                newState = new QuantityType<>(lastMeasurement.getPressure(), Units.BAR);
                break;
            case CHANNEL_TEMPERATURE_GUARD:
                newState = new QuantityType<>(lastMeasurement.getTemperatureGuard(), SIUnits.CELSIUS);
                break;
            case CHANNEL_VALVE_OPEN:

                OnOffType valveOpenType = getValveOpenType(appliance);
                if (valveOpenType != null) {
                    newState = valveOpenType;
                }
                break;
            case CHANNEL_WATERCONSUMPTION:
                newState = sumWaterConsumption(dataPoint);
                break;
            case CHANNEL_WATERCONSUMPTION_SINCE_MIDNIGHT:
                newState = sumWaterConsumptionSinceMidnight(dataPoint);
                break;
            default:
                throw new IllegalArgumentException("Channel " + channelUID + " not supported.");
        }
        updateState(channelUID, newState);
    }

    private QuantityType<Volume> sumWaterConsumptionSinceMidnight(Data dataPoint) {
        ZonedDateTime earliestWithdrawal = ZonedDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime latestWithdrawal = earliestWithdrawal.plus(1, ChronoUnit.DAYS);

        Double waterConsumption = dataPoint.getWithdrawals().stream()
                .filter(e -> earliestWithdrawal.isBefore(e.starttime.toInstant().atZone(ZoneId.systemDefault()))
                        && latestWithdrawal.isAfter(e.starttime.toInstant().atZone(ZoneId.systemDefault())))
                .mapToDouble(withdrawal -> withdrawal.getWaterconsumption()).sum();
        return new QuantityType<>(waterConsumption, Units.LITRE);
    }

    private QuantityType<Volume> sumWaterConsumption(Data dataPoint) {
        Double waterConsumption = dataPoint.getWithdrawals().stream()
                .mapToDouble(withdrawal -> withdrawal.getWaterconsumption()).sum();
        return new QuantityType<Volume>(waterConsumption, Units.LITRE);
    }

    private Measurement getLastMeasurement(Data dataPoint) {
        List<Measurement> measurementList = dataPoint.getMeasurement();
        return measurementList.isEmpty() ? new Measurement() : measurementList.get(measurementList.size() - 1);
    }

    @Nullable
    private OnOffType getValveOpenType(Appliance appliance) {
        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        Optional<BaseApplianceCommand> commandOptional;
        try {
            commandOptional = service.applianceCommand(appliance);
        } catch (IOException e) {
            logger.debug("Could not get appliance command", e);
            return null;
        }
        if (!commandOptional.isPresent()) {
            return null;
        }
        if (commandOptional.get().getType() != Appliance.TYPE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.notsenseguard");
            return null;
        }
        return ((ApplianceCommand) commandOptional.get()).getCommand().getValveOpen() ? OnOffType.ON : OnOffType.OFF;
    }

    @Override
    protected Data getLastDataPoint(Appliance appliance) {
        if (getOndusService() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            return new Data();
        }

        ApplianceData applianceData = getApplianceData(appliance);
        if (applianceData == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.empty.response");
            return new Data();
        }
        Data data = applianceData.getData();
        Collections.sort(data.measurement, Comparator.comparing(e -> ZonedDateTime.parse(e.timestamp)));
        Collections.sort(data.withdrawals, Comparator.comparing(e -> e.starttime));
        return data;
    }

    private @Nullable ApplianceData getApplianceData(Appliance appliance) {
        Instant from = fromTime();
        // Truncated to date only inside api package
        Instant to = Instant.now().plus(1, ChronoUnit.DAYS);

        OndusService service = getOndusService();
        if (service == null) {
            return null;
        }
        try {
            logger.debug("Fetching data for {} from {} to {}", thing.getUID(), from, to);
            BaseApplianceData applianceData = service.applianceData(appliance, from, to).orElse(null);
            if (applianceData != null) {
                if (applianceData.getType() != Appliance.TYPE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/error.notsenseguard");
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

    private Instant fromTime() {
        Instant from = Instant.now().minus(DEFAULT_TIMEFRAME_DAYS, ChronoUnit.DAYS);
        Channel waterconsumptionChannel = this.thing.getChannel(CHANNEL_WATERCONSUMPTION);
        if (waterconsumptionChannel == null) {
            return from;
        }

        Object timeframeConfig = waterconsumptionChannel.getConfiguration().get(CHANNEL_CONFIG_TIMEFRAME);
        if (!(timeframeConfig instanceof BigDecimal)) {
            return from;
        }

        int timeframe = ((BigDecimal) timeframeConfig).intValue();
        if (timeframe < MIN_API_TIMEFRAME_DAYS && timeframe > MAX_API_TIMEFRAME_DAYS) {
            logger.info(
                    "timeframe configuration of waterconsumption channel needs to be a number between 1 to 90, got {}",
                    timeframe);
            return from;
        }

        return Instant.now().minus(timeframe, ChronoUnit.DAYS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
            return;
        }

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
