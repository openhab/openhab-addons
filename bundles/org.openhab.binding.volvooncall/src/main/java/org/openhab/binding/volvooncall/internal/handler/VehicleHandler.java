/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.handler;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException;
import org.openhab.binding.volvooncall.internal.action.VolvoOnCallActions;
import org.openhab.binding.volvooncall.internal.api.ActionResultController;
import org.openhab.binding.volvooncall.internal.api.VocHttpApi;
import org.openhab.binding.volvooncall.internal.config.VehicleConfiguration;
import org.openhab.binding.volvooncall.internal.dto.Attributes;
import org.openhab.binding.volvooncall.internal.dto.DoorsStatus;
import org.openhab.binding.volvooncall.internal.dto.Heater;
import org.openhab.binding.volvooncall.internal.dto.HvBattery;
import org.openhab.binding.volvooncall.internal.dto.Position;
import org.openhab.binding.volvooncall.internal.dto.PostResponse;
import org.openhab.binding.volvooncall.internal.dto.Status;
import org.openhab.binding.volvooncall.internal.dto.Status.FluidLevel;
import org.openhab.binding.volvooncall.internal.dto.Trip;
import org.openhab.binding.volvooncall.internal.dto.TripDetail;
import org.openhab.binding.volvooncall.internal.dto.Trips;
import org.openhab.binding.volvooncall.internal.dto.TyrePressure;
import org.openhab.binding.volvooncall.internal.dto.TyrePressure.PressureLevel;
import org.openhab.binding.volvooncall.internal.dto.Vehicles;
import org.openhab.binding.volvooncall.internal.dto.WindowsStatus;
import org.openhab.binding.volvooncall.internal.wrapper.VehiclePositionWrapper;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);
    private final Map<String, String> activeOptions = new HashMap<>();
    private @Nullable ScheduledFuture<?> refreshJob;
    private final List<ScheduledFuture<?>> pendingActions = new Stack<>();

    private Vehicles vehicle = new Vehicles();
    private VehiclePositionWrapper vehiclePosition = new VehiclePositionWrapper(new Position());
    private Status vehicleStatus = new Status();
    private @NonNullByDefault({}) VehicleConfiguration configuration;
    private @NonNullByDefault({}) VolvoOnCallBridgeHandler bridgeHandler;
    private long lastTripId;

    public VehicleHandler(Thing thing, VehicleStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Volvo On Call handler for {}", getThing().getUID());

        Bridge bridge = getBridge();
        initializeBridge(bridge == null ? null : bridge.getHandler(), bridge == null ? null : bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());

        Bridge bridge = getBridge();
        initializeBridge(bridge == null ? null : bridge.getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeBridge(@Nullable ThingHandler thingHandler, @Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (thingHandler != null && bridgeStatus != null) {
            bridgeHandler = (VolvoOnCallBridgeHandler) thingHandler;
            if (bridgeStatus == ThingStatus.ONLINE) {
                configuration = getConfigAs(VehicleConfiguration.class);
                VocHttpApi api = bridgeHandler.getApi();
                if (api != null) {
                    try {
                        vehicle = api.getURL("vehicles/" + configuration.vin, Vehicles.class);
                        if (thing.getProperties().isEmpty()) {
                            Map<String, String> properties = discoverAttributes(api);
                            updateProperties(properties);
                        }

                        activeOptions.putAll(
                                thing.getProperties().entrySet().stream().filter(p -> "true".equals(p.getValue()))
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                        String lastTripIdString = thing.getProperties().get(LAST_TRIP_ID);
                        if (lastTripIdString != null) {
                            lastTripId = Long.parseLong(lastTripIdString);
                        }

                        updateStatus(ThingStatus.ONLINE);
                        startAutomaticRefresh(configuration.refresh, api);
                    } catch (VolvoOnCallException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
                    }

                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private Map<String, String> discoverAttributes(VocHttpApi service) throws VolvoOnCallException {
        Attributes attributes = service.getURL(vehicle.attributesURL, Attributes.class);

        Map<String, String> properties = new HashMap<>();
        properties.put(CAR_LOCATOR, attributes.carLocatorSupported.toString());
        properties.put(HONK_AND_OR_BLINK, Boolean.toString(attributes.honkAndBlinkSupported
                && attributes.honkAndBlinkVersionsSupported.contains(HONK_AND_OR_BLINK)));
        properties.put(HONK_BLINK, Boolean.toString(
                attributes.honkAndBlinkSupported && attributes.honkAndBlinkVersionsSupported.contains(HONK_BLINK)));
        properties.put(REMOTE_HEATER, attributes.remoteHeaterSupported.toString());
        properties.put(UNLOCK, attributes.unlockSupported.toString());
        properties.put(LOCK, attributes.lockSupported.toString());
        properties.put(JOURNAL_LOG, Boolean.toString(attributes.journalLogSupported && attributes.journalLogEnabled));
        properties.put(PRECLIMATIZATION, attributes.preclimatizationSupported.toString());
        properties.put(ENGINE_START, attributes.engineStartSupported.toString());
        properties.put(UNLOCK_TIME, attributes.unlockTimeFrame.toString());

        return properties;
    }

    /**
     * Start the job refreshing the vehicle data
     *
     * @param refresh : refresh frequency in minutes
     * @param service
     */
    private void startAutomaticRefresh(int refresh, VocHttpApi service) {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            this.refreshJob = scheduler.scheduleWithFixedDelay(() -> queryApiAndUpdateChannels(service), 1, refresh,
                    TimeUnit.MINUTES);
        }
    }

    private void queryApiAndUpdateChannels(VocHttpApi service) {
        try {
            Status newVehicleStatus = service.getURL(vehicle.statusURL, Status.class);
            vehiclePosition = new VehiclePositionWrapper(service.getURL(Position.class, configuration.vin));
            // Update all channels from the updated data
            getThing().getChannels().stream().map(Channel::getUID)
                    .filter(channelUID -> isLinked(channelUID) && !LAST_TRIP_GROUP.equals(channelUID.getGroupId()))
                    .forEach(channelUID -> {
                        String groupID = channelUID.getGroupId();
                        if (groupID != null) {
                            State state = getValue(groupID, channelUID.getIdWithoutGroup(), newVehicleStatus,
                                    vehiclePosition);
                            updateState(channelUID, state);
                        }
                    });
            if (newVehicleStatus.odometer != vehicleStatus.odometer) {
                triggerChannel(GROUP_OTHER + "#" + CAR_EVENT, EVENT_CAR_MOVED);
                // We will update trips only if car position has changed to save server queries
                updateTrips(service);
            }
            if (!vehicleStatus.getEngineRunning().equals(newVehicleStatus.getEngineRunning())
                    && newVehicleStatus.getEngineRunning().get() == OnOffType.ON) {
                triggerChannel(GROUP_OTHER + "#" + CAR_EVENT, EVENT_CAR_STARTED);
            }
            vehicleStatus = newVehicleStatus;
        } catch (VolvoOnCallException e) {
            logger.warn("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            freeRefreshJob();
            startAutomaticRefresh(configuration.refresh, service);
        }
    }

    private void freeRefreshJob() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        pendingActions.stream().filter(f -> !f.isCancelled()).forEach(f -> f.cancel(true));
    }

    @Override
    public void dispose() {
        freeRefreshJob();
        super.dispose();
    }

    private void updateTrips(VocHttpApi service) throws VolvoOnCallException {
        // This seems to rewind 100 days by default, did not find any way to filter it
        Trips carTrips = service.getURL(Trips.class, configuration.vin);
        List<Trip> newTrips = carTrips.trips.stream().filter(trip -> trip.id >= lastTripId)
                .collect(Collectors.toList());
        Collections.reverse(newTrips);

        logger.debug("Trips discovered : {}", newTrips.size());

        if (!newTrips.isEmpty()) {
            Long newTripId = newTrips.get(newTrips.size() - 1).id;
            if (newTripId > lastTripId) {
                updateProperty(LAST_TRIP_ID, newTripId.toString());
                triggerChannel(GROUP_OTHER + "#" + CAR_EVENT, EVENT_CAR_STOPPED);
                lastTripId = newTripId;
            }

            newTrips.stream().map(t -> t.tripDetails.get(0)).forEach(catchUpTrip -> {
                logger.debug("Trip found {}", catchUpTrip.getStartTime());
                getThing().getChannels().stream().map(Channel::getUID)
                        .filter(channelUID -> isLinked(channelUID) && LAST_TRIP_GROUP.equals(channelUID.getGroupId()))
                        .forEach(channelUID -> {
                            State state = getTripValue(channelUID.getIdWithoutGroup(), catchUpTrip);
                            updateState(channelUID, state);
                        });
            });
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            VocHttpApi api = bridgeHandler.getApi();
            if (api != null) {
                queryApiAndUpdateChannels(api);
            }
        } else if (command instanceof OnOffType onOffCommand) {
            if (ENGINE_START.equals(channelID) && onOffCommand == OnOffType.ON) {
                actionStart(5);
            } else if (REMOTE_HEATER.equals(channelID) || PRECLIMATIZATION.equals(channelID)) {
                actionHeater(channelID, onOffCommand == OnOffType.ON);
            } else if (CAR_LOCKED.equals(channelID)) {
                actionOpenClose((onOffCommand == OnOffType.ON) ? LOCK : UNLOCK, onOffCommand);
            }
        }
    }

    private State getTripValue(String channelId, TripDetail tripDetails) {
        switch (channelId) {
            case TRIP_CONSUMPTION:
                return tripDetails.getFuelConsumption()
                        .map(value -> (State) new QuantityType<>(value.floatValue() / 100, LITRE))
                        .orElse(UnDefType.UNDEF);
            case TRIP_DISTANCE:
                return new QuantityType<>((double) tripDetails.distance / 1000, KILO(METRE));
            case TRIP_START_TIME:
                return tripDetails.getStartTime();
            case TRIP_END_TIME:
                return tripDetails.getEndTime();
            case TRIP_DURATION:
                return tripDetails.getDurationInMinutes().map(value -> (State) new QuantityType<>(value, MINUTE))
                        .orElse(UnDefType.UNDEF);
            case TRIP_START_ODOMETER:
                return new QuantityType<>((double) tripDetails.startOdometer / 1000, KILO(METRE));
            case TRIP_STOP_ODOMETER:
                return new QuantityType<>((double) tripDetails.endOdometer / 1000, KILO(METRE));
            case TRIP_START_POSITION:
                return tripDetails.getStartPosition();
            case TRIP_END_POSITION:
                return tripDetails.getEndPosition();
        }
        return UnDefType.NULL;
    }

    private State getDoorsValue(String channelId, DoorsStatus doors) {
        switch (channelId) {
            case TAILGATE:
                return doors.tailgateOpen;
            case REAR_RIGHT:
                return doors.rearRightDoorOpen;
            case REAR_LEFT:
                return doors.rearLeftDoorOpen;
            case FRONT_RIGHT:
                return doors.frontRightDoorOpen;
            case FRONT_LEFT:
                return doors.frontLeftDoorOpen;
            case HOOD:
                return doors.hoodOpen;
        }
        return UnDefType.NULL;
    }

    private State getWindowsValue(String channelId, WindowsStatus windows) {
        switch (channelId) {
            case REAR_RIGHT_WND:
                return windows.rearRightWindowOpen;
            case REAR_LEFT_WND:
                return windows.rearLeftWindowOpen;
            case FRONT_RIGHT_WND:
                return windows.frontRightWindowOpen;
            case FRONT_LEFT_WND:
                return windows.frontLeftWindowOpen;
        }
        return UnDefType.NULL;
    }

    private State pressureLevelToState(PressureLevel level) {
        return level != PressureLevel.UNKNOWN ? new DecimalType(level.ordinal()) : UnDefType.UNDEF;
    }

    private State getTyresValue(String channelId, TyrePressure tyrePressure) {
        switch (channelId) {
            case REAR_RIGHT_TYRE:
                return pressureLevelToState(tyrePressure.rearRightTyrePressure);
            case REAR_LEFT_TYRE:
                return pressureLevelToState(tyrePressure.rearLeftTyrePressure);
            case FRONT_RIGHT_TYRE:
                return pressureLevelToState(tyrePressure.frontRightTyrePressure);
            case FRONT_LEFT_TYRE:
                return pressureLevelToState(tyrePressure.frontLeftTyrePressure);
        }
        return UnDefType.NULL;
    }

    private State getHeaterValue(String channelId, Heater heater) {
        switch (channelId) {
            case REMOTE_HEATER:
            case PRECLIMATIZATION:
                return heater.getStatus();
        }
        return UnDefType.NULL;
    }

    private State getBatteryValue(String channelId, HvBattery hvBattery) {
        switch (channelId) {
            case BATTERY_LEVEL:
                /*
                 * If the car is charging the battery level can be reported as 100% by the API regardless of actual
                 * charge level, but isn't always. So, if we see that the car is Charging, ChargingPaused, or
                 * ChargingInterrupted and the reported battery level is 100%, then instead produce UNDEF.
                 *
                 * If we see FullyCharged, then we can rely on the value being 100% anyway.
                 */
                if (hvBattery.hvBatteryChargeStatusDerived != null
                        && hvBattery.hvBatteryChargeStatusDerived.toString().startsWith("CablePluggedInCar_Charging")
                        && hvBattery.hvBatteryLevel != UNDEFINED && hvBattery.hvBatteryLevel == 100) {
                    return UnDefType.UNDEF;
                } else {
                    return hvBattery.hvBatteryLevel != UNDEFINED ? new QuantityType<>(hvBattery.hvBatteryLevel, PERCENT)
                            : UnDefType.UNDEF;
                }
            case BATTERY_LEVEL_RAW:
                return hvBattery.hvBatteryLevel != UNDEFINED ? new QuantityType<>(hvBattery.hvBatteryLevel, PERCENT)
                        : UnDefType.UNDEF;
            case BATTERY_DISTANCE_TO_EMPTY:
                return hvBattery.distanceToHVBatteryEmpty != UNDEFINED
                        ? new QuantityType<>(hvBattery.distanceToHVBatteryEmpty, KILO(METRE))
                        : UnDefType.UNDEF;
            case CHARGE_STATUS:
                return hvBattery.hvBatteryChargeStatusDerived != null ? hvBattery.hvBatteryChargeStatusDerived
                        : UnDefType.UNDEF;
            case CHARGE_STATUS_CABLE:
                return hvBattery.hvBatteryChargeStatusDerived != null
                        ? OnOffType.from(
                                hvBattery.hvBatteryChargeStatusDerived.toString().startsWith("CablePluggedInCar_"))
                        : UnDefType.UNDEF;
            case CHARGE_STATUS_CHARGING:
                return hvBattery.hvBatteryChargeStatusDerived != null
                        ? OnOffType.from(hvBattery.hvBatteryChargeStatusDerived.toString().endsWith("_Charging"))
                        : UnDefType.UNDEF;
            case CHARGE_STATUS_FULLY_CHARGED:
                /*
                 * If the car is charging the battery level can be reported incorrectly by the API, so use the charging
                 * status instead of checking the level when the car is plugged in.
                 */
                if (hvBattery.hvBatteryChargeStatusDerived != null
                        && hvBattery.hvBatteryChargeStatusDerived.toString().startsWith("CablePluggedInCar_")) {
                    return OnOffType.from(hvBattery.hvBatteryChargeStatusDerived.toString().endsWith("_FullyCharged"));
                } else {
                    return hvBattery.hvBatteryLevel != UNDEFINED ? OnOffType.from(hvBattery.hvBatteryLevel == 100)
                            : UnDefType.UNDEF;
                }
            case TIME_TO_BATTERY_FULLY_CHARGED:
                return hvBattery.timeToHVBatteryFullyCharged != UNDEFINED
                        ? new QuantityType<>(hvBattery.timeToHVBatteryFullyCharged, MINUTE)
                        : UnDefType.UNDEF;
            case CHARGING_END:
                return hvBattery.timeToHVBatteryFullyCharged != UNDEFINED && hvBattery.timeToHVBatteryFullyCharged > 0
                        ? new DateTimeType(ZonedDateTime.now().plusMinutes(hvBattery.timeToHVBatteryFullyCharged))
                        : UnDefType.UNDEF;
        }
        return UnDefType.NULL;
    }

    private State getValue(String groupId, String channelId, Status status, VehiclePositionWrapper position) {
        switch (channelId) {
            case CAR_LOCKED:
                // Warning : carLocked is in the Doors group but is part of general status informations.
                // Did not change it to avoid breaking change for users
                return status.getCarLocked().map(State.class::cast).orElse(UnDefType.UNDEF);
            case ENGINE_RUNNING:
                return status.getEngineRunning().map(State.class::cast).orElse(UnDefType.UNDEF);
            case BRAKE_FLUID_LEVEL:
                return fluidLevelToState(status.brakeFluidLevel);
            case WASHER_FLUID_LEVEL:
                return fluidLevelToState(status.washerFluidLevel);
            case AVERAGE_SPEED:
                return status.averageSpeed != UNDEFINED ? new QuantityType<>(status.averageSpeed, KILOMETRE_PER_HOUR)
                        : UnDefType.UNDEF;
            case SERVICE_WARNING:
                return new StringType(status.serviceWarningStatus);
            case BULB_FAILURE:
                return OnOffType.from(status.aFailedBulb());
            case REMOTE_HEATER:
            case PRECLIMATIZATION:
                return status.getHeater().map(heater -> getHeaterValue(channelId, heater)).orElse(UnDefType.NULL);
        }
        switch (groupId) {
            case GROUP_TANK:
                return getTankValue(channelId, status);
            case GROUP_ODOMETER:
                return getOdometerValue(channelId, status);
            case GROUP_POSITION:
                return getPositionValue(channelId, position);
            case GROUP_DOORS:
                return status.getDoors().map(doors -> getDoorsValue(channelId, doors)).orElse(UnDefType.NULL);
            case GROUP_WINDOWS:
                return status.getWindows().map(windows -> getWindowsValue(channelId, windows)).orElse(UnDefType.NULL);
            case GROUP_TYRES:
                return status.getTyrePressure().map(tyres -> getTyresValue(channelId, tyres)).orElse(UnDefType.NULL);
            case GROUP_BATTERY:
                return status.getHvBattery().map(batteries -> getBatteryValue(channelId, batteries))
                        .orElse(UnDefType.NULL);
        }
        return UnDefType.NULL;
    }

    private State fluidLevelToState(FluidLevel level) {
        return level != FluidLevel.UNKNOWN ? new DecimalType(level.ordinal()) : UnDefType.UNDEF;
    }

    private State getTankValue(String channelId, Status status) {
        switch (channelId) {
            case DISTANCE_TO_EMPTY:
                return status.distanceToEmpty != UNDEFINED ? new QuantityType<>(status.distanceToEmpty, KILO(METRE))
                        : UnDefType.UNDEF;
            case FUEL_AMOUNT:
                return status.fuelAmount != UNDEFINED ? new QuantityType<>(status.fuelAmount, LITRE) : UnDefType.UNDEF;
            case FUEL_LEVEL:
                return status.fuelAmountLevel != UNDEFINED ? new QuantityType<>(status.fuelAmountLevel, PERCENT)
                        : UnDefType.UNDEF;
            case FUEL_CONSUMPTION:
                return status.averageFuelConsumption != UNDEFINED ? new DecimalType(status.averageFuelConsumption / 10)
                        : UnDefType.UNDEF;
            case FUEL_ALERT:
                return OnOffType.from(status.distanceToEmpty < 100);
        }
        return UnDefType.UNDEF;
    }

    private State getOdometerValue(String channelId, Status status) {
        switch (channelId) {
            case ODOMETER:
                return status.odometer != UNDEFINED ? new QuantityType<>((double) status.odometer / 1000, KILO(METRE))
                        : UnDefType.UNDEF;
            case TRIPMETER1:
                return status.tripMeter1 != UNDEFINED
                        ? new QuantityType<>((double) status.tripMeter1 / 1000, KILO(METRE))
                        : UnDefType.UNDEF;
            case TRIPMETER2:
                return status.tripMeter2 != UNDEFINED
                        ? new QuantityType<>((double) status.tripMeter2 / 1000, KILO(METRE))
                        : UnDefType.UNDEF;
        }
        return UnDefType.UNDEF;
    }

    private State getPositionValue(String channelId, VehiclePositionWrapper position) {
        switch (channelId) {
            case ACTUAL_LOCATION:
                return position.getPosition();
            case CALCULATED_LOCATION:
                return position.isCalculated();
            case HEADING:
                return position.isHeading();
            case LOCATION_TIMESTAMP:
                return position.getTimestamp();
        }
        return UnDefType.UNDEF;
    }

    public void actionHonkBlink(Boolean honk, Boolean blink) {
        StringBuilder url = new StringBuilder("vehicles/" + vehicle.vehicleId + "/honk_blink/");

        if (honk && blink && activeOptions.containsKey(HONK_BLINK)) {
            url.append("both");
        } else if (honk && activeOptions.containsKey(HONK_AND_OR_BLINK)) {
            url.append("horn");
        } else if (blink && activeOptions.containsKey(HONK_AND_OR_BLINK)) {
            url.append("lights");
        } else {
            logger.warn("The vehicle is not capable of this action");
            return;
        }

        post(url.toString(), vehiclePosition.getPositionAsJSon());
    }

    private void post(String url, @Nullable String param) {
        VocHttpApi api = bridgeHandler.getApi();
        if (api != null) {
            try {
                PostResponse postResponse = api.postURL(url, param);
                if (postResponse != null) {
                    pendingActions
                            .add(scheduler.schedule(new ActionResultController(api, postResponse, scheduler, this),
                                    1000, TimeUnit.MILLISECONDS));
                }
            } catch (VolvoOnCallException e) {
                logger.warn("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        pendingActions.removeIf(ScheduledFuture::isDone);
    }

    public void actionOpenClose(String action, OnOffType controlState) {
        if (activeOptions.containsKey(action)) {
            if (vehicleStatus.getCarLocked().isEmpty() || vehicleStatus.getCarLocked().get() != controlState) {
                post(String.format("vehicles/%s/%s", configuration.vin, action), "{}");
            } else {
                logger.info("The car {} is already {}ed", configuration.vin, action);
            }
        } else {
            logger.warn("The car {} does not support remote {}ing", configuration.vin, action);
        }
    }

    public void actionHeater(String action, Boolean start) {
        if (activeOptions.containsKey(action)) {
            String address = String.format("vehicles/%s/%s/%s", configuration.vin,
                    action.contains(REMOTE_HEATER) ? "heater" : "preclimatization", start ? "start" : "stop");
            post(address, start ? "{}" : null);
        } else {
            logger.warn("The car {} does not support {}", configuration.vin, action);
        }
    }

    public void actionStart(Integer runtime) {
        if (activeOptions.containsKey(ENGINE_START)) {
            String address = String.format("vehicles/%s/engine/start", vehicle.vehicleId);
            String json = "{\"runtime\":" + runtime.toString() + "}";

            post(address, json);
        } else {
            logger.warn("The car {} does not support remote engine starting", vehicle.vehicleId);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(VolvoOnCallActions.class);
    }
}
