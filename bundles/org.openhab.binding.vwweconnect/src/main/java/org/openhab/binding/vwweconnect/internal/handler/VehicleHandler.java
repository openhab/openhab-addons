/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vwweconnect.internal.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.KILO;
import static org.openhab.binding.vwweconnect.internal.VWWeConnectBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.vwweconnect.internal.VWWeConnectSession;
import org.openhab.binding.vwweconnect.internal.action.VWWeConnectActions;
import org.openhab.binding.vwweconnect.internal.model.ActionNotification;
import org.openhab.binding.vwweconnect.internal.model.ActionNotification.ActionNotificationList;
import org.openhab.binding.vwweconnect.internal.model.BaseVehicle;
import org.openhab.binding.vwweconnect.internal.model.Details.VehicleDetails;
import org.openhab.binding.vwweconnect.internal.model.EManager;
import org.openhab.binding.vwweconnect.internal.model.HeaterStatus;
import org.openhab.binding.vwweconnect.internal.model.Location;
import org.openhab.binding.vwweconnect.internal.model.Status.VehicleStatusData;
import org.openhab.binding.vwweconnect.internal.model.Trips;
import org.openhab.binding.vwweconnect.internal.model.Trips.TripStatistic;
import org.openhab.binding.vwweconnect.internal.model.Trips.TripStatisticDetail;
import org.openhab.binding.vwweconnect.internal.model.Vehicle;
import org.openhab.binding.vwweconnect.internal.model.Vehicle.CompleteVehicleJson;
import org.openhab.binding.vwweconnect.internal.wrapper.VehiclePositionWrapper;

import com.jayway.jsonpath.JsonPath;

/**
 * Handler for the Vehicle thing type that VWWeConnect provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VehicleHandler extends VWWeConnectHandler {
    private static int count = 0;

    public VehicleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (command instanceof OnOffType) {
            OnOffType onOffCommand = (OnOffType) command;
            if (REMOTE_HEATER.equals(channelID)) {
                actionHeater(onOffCommand == OnOffType.ON);
            } else if (REMOTE_VENTILATION.equals(channelID)) {
                actionVentilation(onOffCommand == OnOffType.ON);
            } else if (DOORS_LOCKED.equals(channelID)) {
                if (onOffCommand == OnOffType.ON) {
                    actionLock();
                } else {
                    actionUnlock();
                }
            } else if (EMANAGER_CHARGE.equals(channelID)) {
                actionCharge(onOffCommand == OnOffType.ON);
            } else if (EMANAGER_CLIMATE.equals(channelID)) {
                actionClimate(onOffCommand == OnOffType.ON);
            } else if (EMANAGER_WINDOW_HEAT.equals(channelID)) {
                actionWindowHeat(onOffCommand == OnOffType.ON);
            }
        }
    }

    @Override
    public synchronized void update(BaseVehicle vehicle) {
        logger.debug("update on vehicle: {}", vehicle);

        if (getThing().getThingTypeUID().equals(VEHICLE_THING_TYPE)) {
            Vehicle obj = (Vehicle) vehicle;
            updateVehicleStatus(obj);
            logger.debug("update status to ONLINE for vehicle: {}", vehicle);
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateVehicleStatus(Vehicle vehicleJSON) {
        logger.debug("update vehicle status");
        CompleteVehicleJson vehicle = vehicleJSON.getCompleteVehicleJson();
        VehicleDetails vehicleDetails = vehicleJSON.getVehicleDetails().getVehicleDetails();
        VehicleStatusData vehicleStatus = vehicleJSON.getVehicleStatus().getVehicleStatusData();
        Trips trips = vehicleJSON.getTrips();
        Location vehicleLocation = vehicleJSON.getVehicleLocation();
        HeaterStatus vehicleHeaterStatus = vehicleJSON.getHeaterStatus();
        EManager eManager = vehicleJSON.getEManager();

        getThing().getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> isLinked(channelUID) && !LAST_TRIP_GROUP.equals(channelUID.getGroupId()))
                .forEach(channelUID -> {
                    State state = getValue(channelUID.getIdWithoutGroup(), vehicle, vehicleDetails, vehicleStatus,
                            trips, vehicleLocation, vehicleHeaterStatus, eManager);
                    updateState(channelUID, state);
                });
        updateLastTrip(trips);
    }

    public State getValue(String channelId, CompleteVehicleJson vehicle, VehicleDetails vehicleDetails,
            VehicleStatusData vehicleStatus, Trips trips, Location vehicleLocation, HeaterStatus vehicleHeaterStatus,
            EManager eManager) {
        switch (channelId) {
            case MODEL:
                return new StringType(vehicle.getModel());
            case NAME:
                return new StringType(vehicle.getName());
            case MODEL_CODE:
                return new StringType(vehicle.getModelCode());
            case MODEL_YEAR:
                return new StringType(vehicle.getModelYear());
            case ENROLLMENT_DATE:
                ZonedDateTime enrollmentStartDate = vehicle.getEnrollmentStartDate();
                return enrollmentStartDate != null ? new DateTimeType(enrollmentStartDate) : UnDefType.UNDEF;
            case DASHBOARD_URL:
                return new StringType(vehicle.getDashboardUrl());
            case IMAGE_URL:
                String imageUrl = vehicle.getImageUrl();
                return imageUrl != null ? HttpUtil.downloadImage(imageUrl) : UnDefType.UNDEF;
            case ENGINE_TYPE_COMBUSTIAN:
                return OnOffType.from(vehicle.getEngineTypeCombustian());
            case ENGINE_TYPE_ELECTRIC:
                return OnOffType.from(vehicle.getEngineTypeElectric());
            case ENGINE_TYPE_HYBRID_OCU1:
                return OnOffType.from(vehicle.getEngineTypeHybridOCU1());
            case ENGINE_TYPE_HYBRID_OCU2:
                return OnOffType.from(vehicle.getEngineTypeHybridOCU2());
            case ENGINE_TYPE_CNG:
                return OnOffType.from(vehicle.getEngineTypeCNG());
            case FUEL_LEVEL:
                return vehicleStatus.getFuelLevel() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(vehicleStatus.getFuelLevel(), SmartHomeUnits.PERCENT)
                        : UnDefType.UNDEF;
            case FUEL_CONSUMPTION:
                return trips.getRtsViewModel().getLongTermData().getAverageFuelConsumption() != BaseVehicle.UNDEFINED
                        ? new DecimalType(trips.getRtsViewModel().getLongTermData().getAverageFuelConsumption() / 10)
                        : UnDefType.UNDEF;
            case FUEL_RANGE:
                return vehicleStatus.getFuelRange() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(vehicleStatus.getFuelRange(), KILO(SIUnits.METRE))
                        : UnDefType.UNDEF;
            case FUEL_ALERT:
                return vehicleStatus.getFuelRange() != BaseVehicle.UNDEFINED
                        ? OnOffType.from(vehicleStatus.getFuelRange() < 100)
                        : UnDefType.UNDEF;
            case CNG_LEVEL:
                return vehicleStatus.getCngFuelLevel() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(vehicleStatus.getCngFuelLevel(), SmartHomeUnits.PERCENT)
                        : UnDefType.UNDEF;
            case CNG_CONSUMPTION:
                return trips.getRtsViewModel().getLongTermData().getAverageCngConsumption() != BaseVehicle.UNDEFINED
                        ? new DecimalType(trips.getRtsViewModel().getLongTermData().getAverageCngConsumption() / 10)
                        : UnDefType.UNDEF;
            case CNG_RANGE:
                return vehicleStatus.getCngRange() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(vehicleStatus.getCngRange(), KILO(SIUnits.METRE))
                        : UnDefType.UNDEF;
            case CNG_ALERT:
                return vehicleStatus.getCngRange() != BaseVehicle.UNDEFINED
                        ? OnOffType.from(vehicleStatus.getCngRange() < 100)
                        : UnDefType.UNDEF;
            case BATTERY_LEVEL:
                return vehicleStatus.getBatteryLevel() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(vehicleStatus.getBatteryLevel(), SmartHomeUnits.PERCENT)
                        : UnDefType.UNDEF;
            case ELECTRIC_CONSUMPTION:
                return trips.getRtsViewModel().getLongTermData()
                        .getAverageElectricConsumption() != BaseVehicle.UNDEFINED
                                ? new DecimalType(
                                        trips.getRtsViewModel().getLongTermData().getAverageElectricConsumption() / 10)
                                : UnDefType.UNDEF;
            case BATTERY_RANGE:
                return vehicleStatus.getBatteryRange() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(vehicleStatus.getBatteryRange(), KILO(SIUnits.METRE))
                        : UnDefType.UNDEF;
            case BATTERY_ALERT:
                return vehicleStatus.getBatteryRange() != BaseVehicle.UNDEFINED
                        ? OnOffType.from(vehicleStatus.getBatteryRange() < 100)
                        : UnDefType.UNDEF;
            case CHARGING_STATE:
            case EMANAGER_CHARGE:
                return OnOffType.from(eManager.getEManager().getRbc().getStatus().getChargingState());
            case CHARGING_REMAINING_HOUR:
                return eManager.getEManager().getRbc().getStatus().getChargingRemainingHour() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(eManager.getEManager().getRbc().getStatus().getChargingRemainingHour(),
                                SmartHomeUnits.HOUR)
                        : UnDefType.UNDEF;
            case CHARGING_REMAINING_MINUTE:
                return eManager.getEManager().getRbc().getStatus().getChargingRemainingMinute() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(eManager.getEManager().getRbc().getStatus().getChargingRemainingMinute(),
                                SmartHomeUnits.MINUTE)
                        : UnDefType.UNDEF;
            case CHARGING_REASON:
                String chargingReason = eManager.getEManager().getRbc().getStatus().getChargingReason();
                return chargingReason != null ? new StringType(chargingReason) : UnDefType.UNDEF;
            case PLUGIN_STATE:
                return OnOffType.from(eManager.getEManager().getRbc().getStatus().getPluginState());
            case LOCK_STATE:
                return OnOffType.from(eManager.getEManager().getRbc().getStatus().getLockState());
            case EXTERNAL_POWER_SUPPLY_STATE:
                return OnOffType.from(eManager.getEManager().getRbc().getStatus().getExtPowerSupplyState());
            case CHARGER_MAX_CURRENT:
                return eManager.getEManager().getRbc().getSettings().getChargerMaxCurrent() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(eManager.getEManager().getRbc().getSettings().getChargerMaxCurrent(),
                                SmartHomeUnits.AMPERE)
                        : UnDefType.UNDEF;
            case MAX_AMPERE:
                return eManager.getEManager().getRbc().getSettings().getMaxAmpere() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(eManager.getEManager().getRbc().getSettings().getMaxAmpere(),
                                SmartHomeUnits.AMPERE)
                        : UnDefType.UNDEF;
            case MAX_CURRENT_REDUCED:
                return OnOffType.from(eManager.getEManager().getRbc().getSettings().isMaxCurrentReduced());
            case CLIMATISATION_STATE:
            case EMANAGER_CLIMATE:
                return OnOffType.from(eManager.getEManager().getRpc().getStatus().getClimatisationState());
            case CLIMATISATION_REMAINING_TIME:
                return eManager.getEManager().getRpc().getStatus()
                        .getClimatisationRemaningTime() != BaseVehicle.UNDEFINED
                                ? new QuantityType<>(
                                        eManager.getEManager().getRpc().getStatus().getClimatisationRemaningTime(),
                                        SmartHomeUnits.MINUTE)
                                : UnDefType.UNDEF;
            case CLIMATISATION_REASON:
                return new StringType(eManager.getEManager().getRpc().getStatus().getClimatisationReason());
            case WINDOW_HEATING_STATE_FRONT:
                return OnOffType.from(eManager.getEManager().getRpc().getStatus().getWindowHeatingStateFront());
            case WINDOW_HEATING_STATE_REAR:
                return OnOffType.from(eManager.getEManager().getRpc().getStatus().getWindowHeatingStateRear());
            case EMANAGER_WINDOW_HEAT:
                return OnOffType.from(eManager.getEManager().getRpc().getStatus().getWindowHeatingState());
            case TOTAL_DISTANCE:
                return vehicleDetails.getDistanceCovered() != BaseVehicle.UNDEFINED
                        ? new QuantityType<Length>(vehicleDetails.getDistanceCovered() * 1000, KILO(SIUnits.METRE))
                        : UnDefType.UNDEF;
            case TOTAL_TRIP_DISTANCE:
                return trips.getRtsViewModel().getLongTermData().getTripLength() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(trips.getRtsViewModel().getLongTermData().getTripLength(),
                                KILO(SIUnits.METRE))
                        : UnDefType.UNDEF;
            case TOTAL_TRIP_DURATION:
                return trips.getRtsViewModel().getLongTermData().getTripDuration() != BaseVehicle.UNDEFINED
                        ? new QuantityType<Time>(trips.getRtsViewModel().getLongTermData().getTripDuration(),
                                SmartHomeUnits.MINUTE)
                        : UnDefType.UNDEF;
            case TOTAL_AVERAGE_SPEED:
                return trips.getRtsViewModel().getLongTermData().getAverageSpeed() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(trips.getRtsViewModel().getLongTermData().getAverageSpeed(),
                                SIUnits.KILOMETRE_PER_HOUR)
                        : UnDefType.UNDEF;
            case SERVICE_INSPECTION:
                return new StringType(vehicleDetails.getServiceInspectionData());
            case OIL_INSPECTION:
                return new StringType(vehicleDetails.getOilInspectionData());
            case TRUNK:
                return vehicleStatus.getCarRenderData().getDoors().getTrunk();
            case RIGHT_BACK:
                return vehicleStatus.getCarRenderData().getDoors().getRightBack();
            case LEFT_BACK:
                return vehicleStatus.getCarRenderData().getDoors().getLeftBack();
            case RIGHT_FRONT:
                return vehicleStatus.getCarRenderData().getDoors().getRightFront();
            case LEFT_FRONT:
                return vehicleStatus.getCarRenderData().getDoors().getLeftFront();
            case HOOD:
                return vehicleStatus.getCarRenderData().getHood();
            case ROOF:
                return vehicleStatus.getCarRenderData().getRoof();
            case SUN_ROOF:
                return vehicleStatus.getCarRenderData().getSunroof();
            case DOORS_LOCKED:
                return vehicleStatus.getLockData().getDoorsLocked();
            case TRUNK_LOCKED:
                return vehicleStatus.getLockData().getTrunk();
            case RIGHT_BACK_WND:
                return vehicleStatus.getCarRenderData().getWindows().getRightBack();
            case LEFT_BACK_WND:
                return vehicleStatus.getCarRenderData().getWindows().getLeftBack();
            case RIGHT_FRONT_WND:
                return vehicleStatus.getCarRenderData().getWindows().getRightFront();
            case LEFT_FRONT_WND:
                return vehicleStatus.getCarRenderData().getWindows().getLeftFront();
            case ACTUAL_LOCATION:
                return vehicleLocation.getPosition().getLat() != BaseVehicle.UNDEFINED
                        ? new VehiclePositionWrapper(vehicleLocation.getPosition()).getPosition()
                        : UnDefType.UNDEF;
            case REMOTE_HEATER:
                return OnOffType.from(vehicleHeaterStatus.getRemoteAuxiliaryHeating().getStatus().isActive());
            case REMOTE_VENTILATION:
                return OnOffType.from(vehicleHeaterStatus.getRemoteAuxiliaryHeating().getStatus().isActive());
            case TEMPERATURE:
                return vehicleHeaterStatus.getRemoteAuxiliaryHeating().getStatus()
                        .getTemperature() != BaseVehicle.UNDEFINED
                                ? new QuantityType<>(
                                        vehicleHeaterStatus.getRemoteAuxiliaryHeating().getStatus().getTemperature(),
                                        SIUnits.CELSIUS)
                                : UnDefType.NULL;
            case REMAINING_TIME:
                return vehicleHeaterStatus.getRemoteAuxiliaryHeating().getStatus()
                        .getRemainingTime() != BaseVehicle.UNDEFINED
                                ? new QuantityType<>(
                                        vehicleHeaterStatus.getRemoteAuxiliaryHeating().getStatus().getRemainingTime(),
                                        SmartHomeUnits.MINUTE)
                                : UnDefType.UNDEF;
        }
        return UnDefType.UNDEF;
    }

    private boolean filterLastTrip(@Nullable TripStatistic tripStat, int tripId) {
        if (tripStat != null) {
            return (tripStat.getAggregatedStatistics().getTripId() == tripId);
        }
        return false;
    }

    public void updateLastTrip(@Nullable Trips trips) {
        logger.debug("update last trip");

        if (trips != null) {
            // Find latest trip ID
            int tripId;
            List<TripStatistic> tripsStat = trips.getRtsViewModel().getTripStatistics();
            if (tripsStat != null) {
                // Do a reverse of the trips
                Collections.reverse(tripsStat);
                logger.trace("Last trip stats reversed: {}", tripsStat);
                if (tripsStat.size() != 0) {
                    Optional<TripStatistic> lastTrip = tripsStat.stream()
                            .filter(tripStatistics -> tripStatistics != null).findFirst();
                    int tripId1 = lastTrip.get().getAggregatedStatistics().getTripId();
                    logger.trace("Last trip ID1: {}", tripId1);
                    // Do another reverse of the trips
                    Collections.reverse(tripsStat);
                    logger.trace("Last trip stats reversed: {}", tripsStat);
                    lastTrip = tripsStat.stream().filter(tripStatistics -> tripStatistics != null).findFirst();
                    int tripId2 = lastTrip.get().getAggregatedStatistics().getTripId();
                    logger.trace("Last trip ID2: {}", tripId1);

                    // Find latest trip ID
                    if (tripId2 >= tripId1) {
                        tripId = tripId2;
                    } else {
                        tripId = tripId1;
                    }
                    logger.trace("Last trip ID: {}", tripId);

                    lastTrip = tripsStat.stream().filter(tripStat -> filterLastTrip(tripStat, tripId)).findFirst();

                    Optional<TripStatisticDetail> lastTripStats = lastTrip.get().getTripStatistics().stream()
                            .filter(t -> t.getTripId() == tripId).findFirst();
                    logger.debug("Last trip: {}", lastTrip);
                    logger.trace("Last trip stats: {}", lastTripStats);

                    getThing().getChannels().stream().map(Channel::getUID).filter(
                            channelUID -> isLinked(channelUID) && LAST_TRIP_GROUP.equals(channelUID.getGroupId()))
                            .forEach(channelUID -> {
                                State state = getTripValue(channelUID.getIdWithoutGroup(), lastTripStats.get());
                                updateState(channelUID, state);
                            });
                } else {
                    logger.debug("Cannot update last trip, tripsStat length is 0!");
                }
            } else {
                logger.debug("Cannot update last trip, tripsStat is null!");
            }
        } else {
            logger.warn("Cannot update last trip, trips is null!");
        }
    }

    public State getTripValue(String channelId, TripStatisticDetail trip) {
        switch (channelId) {
            case AVERAGE_FUEL_CONSUMPTION:
                return trip.getAverageFuelConsumption() != BaseVehicle.UNDEFINED
                        ? new DecimalType(trip.getAverageFuelConsumption() / 10)
                        : UnDefType.UNDEF;
            case AVERAGE_CNG_CONSUMPTION:
                return trip.getAverageCngConsumption() != BaseVehicle.UNDEFINED
                        ? new DecimalType(trip.getAverageCngConsumption() / 10)
                        : UnDefType.UNDEF;
            case AVERAGE_ELECTRIC_CONSUMPTION:
                return trip.getAverageElectricConsumption() != BaseVehicle.UNDEFINED
                        ? new DecimalType(trip.getAverageElectricConsumption())
                        : UnDefType.UNDEF;
            case AVERAGE_AUXILIARY_CONSUMPTION:
                return trip.getAverageAuxiliaryConsumption() != BaseVehicle.UNDEFINED
                        ? new DecimalType(trip.getAverageAuxiliaryConsumption())
                        : UnDefType.UNDEF;
            case TRIP_AVERAGE_SPEED:
                return trip.getAverageSpeed() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(trip.getAverageSpeed(), SIUnits.KILOMETRE_PER_HOUR)
                        : UnDefType.UNDEF;
            case TRIP_DISTANCE:
                return trip.getTripLength() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(trip.getTripLength(), KILO(SIUnits.METRE))
                        : UnDefType.UNDEF;
            case TRIP_START_TIME:
                ZonedDateTime localStartTimestamp = trip.getStartTimestamp();
                return localStartTimestamp != null ? new DateTimeType(localStartTimestamp) : UnDefType.UNDEF;
            case TRIP_END_TIME:
                ZonedDateTime localEndTimestamp = trip.getEndTimestamp();
                return localEndTimestamp != null ? new DateTimeType(localEndTimestamp) : UnDefType.UNDEF;
            case TRIP_DURATION:
                return trip.getTripDuration() != BaseVehicle.UNDEFINED
                        ? new QuantityType<>(trip.getTripDuration(), SmartHomeUnits.MINUTE)
                        : UnDefType.UNDEF;
        }

        return UnDefType.NULL;
    }

    public void actionHonkBlink(Boolean honk, Boolean blink) {
        VWWeConnectBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            logger.debug("Not implemented, unknown endpoint!");
        }
    }

    public class ActionResultController implements Runnable {
        private String vin;
        private String requestStatusUrl;
        private VWWeConnectSession session;

        public ActionResultController(String vin, String requestStatusUrl, VWWeConnectSession session) {
            this.vin = vin;
            this.requestStatusUrl = requestStatusUrl;
            this.session = session;

        }

        @Override
        public void run() {

            /*
             * String content = "{\"errorCode\":\"0\"}";
             * if (count == 0) {
             * content =
             * "{\"errorCode\":\"0\",\"actionNotificationList\":[{\"actionState\":\"FETCHED\",\"actionType\":\"START\",\"serviceType\":\"RBC\",\"errorTitle\":null,\"errorMessage\":null}]}";
             * count++;
             * } else {
             * content =
             * "{\"errorCode\":\"0\",\"actionNotificationList\":[{\"actionState\":\"SUCCEEDED\",\"actionType\":\"START\",\"serviceType\":\"RBC\",\"errorTitle\":null,\"errorMessage\":null}]}";
             * count++;
             * }
             * logger.debug("Content: {}", content);
             * if (!session.isErrorCode(content)) {
             * String requestStatus = null;
             * ActionNotification notification = null;
             * if (requestStatusUrl.contains(REQUEST_STATUS)) {
             * requestStatus = JsonPath.read(content, PARSE_REQUEST_STATUS);
             * } else if (requestStatusUrl.contains(EMANAGER_GET_NOTIFICATIONS)) {
             * notification = session.convertFromJSON(content, ActionNotification.class);
             * }
             *
             * if (requestStatus != null && requestStatus.equals("REQUEST_SUCCESSFUL")) {
             * logger.debug("Command has status {} ", requestStatus);
             * scheduleImmediateRefresh(0);
             * } else if (notification != null) {
             * List<ActionNotificationList> list = notification.getActionNotificationList().stream()
             * .filter(a -> a.getActionState() != null && a.getActionState().equals("SUCCEEDED"))
             * .collect(Collectors.toList());
             * if (list.size() > 0) {
             * logger.debug("Command has status: {} notification: {}", list.get(0).getActionState(),
             * notification);
             * scheduleImmediateRefresh(0);
             * } else {
             * logger.debug("No command status yet: {}", notification);
             * scheduler.schedule(new ActionResultController(vin, requestStatusUrl, session), 15000,
             * TimeUnit.MILLISECONDS);
             * }
             * } else {
             * logger.warn("Failed to request status for vehicle {}! Request status: {}", vin,
             * requestStatus != null ? requestStatus : notification);
             *
             * }
             * } else {
             * logger.warn("Failed to request status for vehicle {}! HTTP response: {} Response: {}", vin,
             * HttpStatus.BAD_REQUEST_400, content);
             * }
             * }
             */

            Fields fields = null;
            ContentResponse httpResponse = session.sendCommand(requestStatusUrl, fields);
            if (httpResponse != null) {
                String content = httpResponse.getContentAsString();
                logger.debug("Content: {}", content);
                if (!session.isErrorCode(content)) {
                    String requestStatus = null;
                    ActionNotification notification = null;
                    if (requestStatusUrl.contains(REQUEST_STATUS_LOCK_ACTION)) {
                        requestStatus = JsonPath.read(content, PARSE_REQUEST_STATUS);
                        if (requestStatus != null) {
                            if (requestStatus.equals("REQUEST_SUCCESSFUL")) {
                                logger.debug("Command has status {} ", requestStatus);
                                scheduleImmediateRefresh(0);
                            } else {
                                logger.debug("Command has status {} ", requestStatus);
                                scheduler.schedule(new ActionResultController(vin, requestStatusUrl, session), 15000,
                                        TimeUnit.MILLISECONDS);
                            }
                        } else {
                            logger.warn("Failed to request status for vehicle {}! Request status is null", vin);
                        }
                    } else if (requestStatusUrl.contains(EMANAGER_GET_NOTIFICATIONS)) {
                        notification = session.convertFromJSON(content, ActionNotification.class);
                        if (notification != null) {
                            List<ActionNotificationList> list = notification.getActionNotificationList().stream()
                                    .filter(a -> a.getActionState() != null && (a.getActionState().equals("SUCCEEDED")))
                                    .collect(Collectors.toList());
                            if (list.size() > 0) {
                                logger.debug("Command has status: {} ", list.get(0).getActionState());
                                scheduleImmediateRefresh(0);
                            } else {
                                logger.debug("No command status yet: {}", notification);
                                scheduler.schedule(new ActionResultController(vin, requestStatusUrl, session), 15000,
                                        TimeUnit.MILLISECONDS);
                            }
                        } else {
                            logger.warn("Failed to request status for vehicle {}! Notification status is null", vin);
                        }
                    }
                } else {
                    logger.warn("Failed to request status for vehicle {}! HTTP response: {} Response: {}", vin,
                            httpResponse.getStatus(), content);
                }
            }
        }
    }

    private boolean sendCommand(String vin, String url, String requestStatusUrl, String data,
            VWWeConnectBridgeHandler bridgeHandler, VWWeConnectSession session) {

        /*
         * String content = "{\"errorCode\":\"0\"}";
         * content =
         * "{\"errorCode\":\"0\",\"actionNotificationList\":[{\"actionState\":\"FETCHED\",\"actionType\":\"START\",\"serviceType\":\"RBC\",\"errorTitle\":null,\"errorMessage\":null}]}";
         * logger.debug("Content: {}", content);
         * if (!session.isErrorCode(content)) {
         * String requestStatus = null;
         * ActionNotification notification = null;
         * if (requestStatusUrl.contains(REQUEST_STATUS)) {
         * requestStatus = JsonPath.read(content, PARSE_REQUEST_STATUS);
         * } else if (requestStatusUrl.contains(EMANAGER_GET_NOTIFICATIONS)) {
         * notification = session.convertFromJSON(content, ActionNotification.class);
         * }
         *
         * if (requestStatus != null && (requestStatus.equals("REQUEST_IN_PROGRESS")
         * || requestStatus.equals("REQUEST_SUCCESSFUL"))) {
         * logger.debug("Command has status {} ", requestStatus);
         * } else if (notification != null) {
         * List<ActionNotificationList> list = notification.getActionNotificationList().stream()
         * .filter(a -> a.getActionState() != null && (a.getActionState().equals("QUEUED")
         * || a.getActionState().equals("FETCHED") || a.getActionState().equals("SUCCEEDED")))
         * .collect(Collectors.toList());
         * if (list.size() > 0) {
         * logger.warn("Command has status: {} notification: {}", list.get(0).getActionState(),
         * notification);
         * } else {
         * logger.debug("No command status yet: {}", notification);
         * }
         * } else {
         * logger.warn("Failed to request status for vehicle {}! Request status: {}", vin,
         * requestStatus != null ? requestStatus : notification);
         * return false;
         * }
         * } else {
         * logger.warn("Failed to request status for vehicle {}! HTTP response: {} Response: {}", vin,
         * HttpStatus.BAD_REQUEST_400, content);
         * return false;
         * }
         * }
         */

        ContentResponse httpResponse = session.sendCommand(url, data);
        if (httpResponse != null && httpResponse.getStatus() == HttpStatus.OK_200) {
            logger.debug(" VIN: {} JSON response: {}", vin, httpResponse.getContentAsString());
            if (!session.isErrorCode(httpResponse.getContentAsString())) {
                logger.debug("Command {} successfully sent to vehicle!", url);
            } else {
                logger.warn("Failed to send {} to the vehicle {} JSON response: {}", url, vin,
                        httpResponse.getContentAsString());
                return false;
            }
        } else {
            logger.warn("Failed to send {} to the vehicle {} HTTP response: {}", url, vin,
                    httpResponse != null ? httpResponse.getStatus() : -1);
            return false;
        }

        bridgeHandler.addPendingAction(scheduler.schedule(new ActionResultController(vin, requestStatusUrl, session),
                15000, TimeUnit.MILLISECONDS));

        bridgeHandler.removeFinishedJobs();

        /*
         * try {
         * Thread.sleep(30 * SLEEP_TIME_MILLIS);
         * } catch (InterruptedException e) {
         * logger.warn("InterruptedException caught: {}", e.getMessage(), e);
         * }
         *
         * Fields fields = null;
         * httpResponse = session.sendCommand(requestStatusUrl, fields);
         * if (httpResponse != null) {
         * String content = httpResponse.getContentAsString();
         * logger.debug("Content: {}", content);
         * if (!session.isErrorCode(content)) {
         * String requestStatus = null;
         * ActionNotification notification = null;
         * if (requestStatusUrl.contains(REQUEST_STATUS)) {
         * requestStatus = JsonPath.read(content, PARSE_REQUEST_STATUS);
         * } else if (requestStatusUrl.contains(EMANAGER_GET_NOTIFICATIONS)) {
         * notification = session.convertFromJSON(content, ActionNotification.class);
         * }
         * if (requestStatus != null && (requestStatus.equals("REQUEST_IN_PROGRESS")
         * || requestStatus.equals("REQUEST_SUCCESSFUL"))) {
         * logger.debug("Command has status {} ", requestStatus);
         * } else if (notification != null) {
         * List<ActionNotificationList> list = notification.getActionNotificationList().stream()
         * .filter(a -> a.getActionState() != null && (a.getActionState().equals("QUEUED")
         * || a.getActionState().equals("FETCHED") || a.getActionState().equals("SUCCEEDED")))
         * .collect(Collectors.toList());
         * if (list.size() > 0) {
         * logger.debug("Command has status: {} ", list.get(0).getActionState());
         * } else {
         * logger.debug("No command status yet: {}", notification);
         * }
         * } else {
         * logger.warn("Failed to request status for vehicle {}! Request status: {}", vin,
         * requestStatus != null ? requestStatus : notification);
         * return false;
         * }
         * } else {
         * logger.warn("Failed to request status for vehicle {}! HTTP response: {} Response: {}", vin,
         * httpResponse.getStatus(), content);
         * return false;
         * }
         * }
         */
        return true;
    }

    private void actionUnlockLock(String action, OnOffType controlState) {
        VWWeConnectBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            if (bridgeHandler.getSecurePIN() == null) {
                logger.warn("S-PIN has to be configured to handle action {}", action);
                return;
            }
            String vin = config.vin;
            VWWeConnectSession session = getSession();
            if (session != null && vin != null) {
                Vehicle vehicle = (Vehicle) session.getVWWeConnectThing(vin);
                if (vehicle != null && vehicle.getVehicleStatus().getVehicleStatusData().getLockData()
                        .getDoorsLocked() != controlState) {
                    String data = "{\"spin\":\"" + bridgeHandler.getSecurePIN() + "\"}";
                    String url = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl() + LOCKUNLOCK
                            + action;
                    String requestStatusUrl = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl()
                            + REQUEST_STATUS_LOCK_ACTION;
                    if (sendCommand(vin, url, requestStatusUrl, data, bridgeHandler, session)) {
                        // scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
                    } else {
                        logger.warn("The vehicle {} failed to handle action {}", vin, action);
                    }
                } else {
                    logger.info("The vehicle {} is already {}ed", config.vin, action);
                }
            } else {
                logger.warn("Session or vin is null vin: {} action: {}", vin, action);
            }
        } else {
            logger.warn("Bridgehandler is null, vin: {}, action: {}", config.vin, action);
        }
    }

    public void actionUnlock() {
        actionUnlockLock(UNLOCK, OnOffType.OFF);
    }

    public void actionLock() {
        actionUnlockLock(LOCK, OnOffType.ON);
    }

    private void actionHeaterVentilation(String action, Boolean start) {
        VWWeConnectBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            if (bridgeHandler.getSecurePIN() == null) {
                logger.warn("S-PIN has to be configured to handle action {}", action);
                return;
            }
            String vin = config.vin;
            VWWeConnectSession session = getSession();
            if (session != null && vin != null) {
                Vehicle vehicle = (Vehicle) session.getVWWeConnectThing(vin);
                if (vehicle != null && (action.contains(REMOTE_HEATER) || action.contains(REMOTE_VENTILATION))) {
                    String command = start ? START_HEATER : STOP_HEATER;
                    String data;
                    if (command.equals(START_HEATER)) {
                        if (action.contains(REMOTE_HEATER)) {
                            data = "{\"startMode\":\"" + HEATING + "\", \"spin\":\"" + bridgeHandler.getSecurePIN()
                                    + "\"}";
                        } else {
                            data = "{\"startMode\":\"" + VENTILATION + "\", \"spin\":\"" + bridgeHandler.getSecurePIN()
                                    + "\"}";
                        }

                    } else {
                        data = "empty";
                    }
                    String url = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl() + STARTSTOP_HEATER
                            + command;
                    String requestStatusUrl = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl()
                            + REQUEST_STATUS_HEATER_ACTION;
                    if (sendCommand(vin, url, requestStatusUrl, data, bridgeHandler, session)) {
                        // scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
                    } else {
                        logger.warn("The vehicle {} failed to handle action {} {}", config.vin, action, start);
                    }
                }
            } else {
                logger.warn("Session or vin is null vin: {} action: {}", vin, action);
            }
        } else {
            logger.warn("Bridgehandler is null, vin: {}, action: {} {}", config.vin, action, start);
        }
    }

    public void actionHeater(Boolean start) {
        actionHeaterVentilation(REMOTE_HEATER, start);
    }

    public void actionVentilation(Boolean start) {
        actionHeaterVentilation(REMOTE_VENTILATION, start);
    }

    private void actionChargeOnOff(Boolean start) {
        VWWeConnectBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            String vin = config.vin;
            VWWeConnectSession session = getSession();
            if (session != null && vin != null) {
                Vehicle vehicle = (Vehicle) session.getVWWeConnectThing(vin);
                if (vehicle != null) {
                    String data;
                    if (start) {
                        data = "{\"triggerAction\":\"True\", \"batteryPercent\":\"100\"}";
                    } else {
                        data = "{\"triggerAction\":\"False\", \"batteryPercent\":\"99\"}";
                    }
                    String url = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl() + CHARGE_BATTERY;
                    String requestStatusUrl = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl()
                            + EMANAGER_GET_NOTIFICATIONS;
                    if (sendCommand(vin, url, requestStatusUrl, data, bridgeHandler, session)) {
                        // scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
                    } else {
                        logger.warn("The vehicle {} failed to handle command {} {}", config.vin, url, start);
                    }
                }
            }
        }
    }

    public void actionCharge(Boolean start) {
        actionChargeOnOff(start);
    }

    private void actionClimateOnOff(Boolean start) {
        VWWeConnectBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            String vin = config.vin;
            VWWeConnectSession session = getSession();
            if (session != null && vin != null) {
                Vehicle vehicle = (Vehicle) session.getVWWeConnectThing(vin);
                if (vehicle != null) {
                    String data;
                    if (start) {
                        data = "{\"triggerAction\":\"True\", \"electricClima\":\"True\"}";
                    } else {
                        data = "{\"triggerAction\":\"False\", \"electricClima\":\"True\"}";
                    }
                    String url = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl()
                            + TRIGGER_CLIMATISATION;
                    String requestStatusUrl = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl()
                            + EMANAGER_GET_NOTIFICATIONS;
                    ;
                    if (sendCommand(vin, url, requestStatusUrl, data, bridgeHandler, session)) {
                        // scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
                    } else {
                        logger.warn("The vehicle {} failed to handle command {} {}", config.vin, url, start);
                    }
                }
            }
        }
    }

    public void actionClimate(Boolean start) {
        actionClimateOnOff(start);
    }

    private void actionWindowHeatOnOff(Boolean start) {
        VWWeConnectBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            String vin = config.vin;
            VWWeConnectSession session = getSession();
            if (session != null && vin != null) {
                Vehicle vehicle = (Vehicle) session.getVWWeConnectThing(vin);
                if (vehicle != null) {
                    String data;
                    if (start) {
                        data = "{\"triggerAction\":\"True\"}";
                    } else {
                        data = "{\"triggerAction\":\"False\"}";
                    }
                    String url = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl()
                            + TRIGGER_WINDOW_HEAT;
                    String requestStatusUrl = SESSION_BASE + vehicle.getCompleteVehicleJson().getDashboardUrl()
                            + EMANAGER_GET_NOTIFICATIONS;
                    ;
                    if (sendCommand(vin, url, requestStatusUrl, data, bridgeHandler, session)) {
                        // scheduleImmediateRefresh(REFRESH_DELAY_SECONDS);
                    } else {
                        logger.warn("The vehicle {} failed to handle command {} {}", config.vin, url, start);
                    }
                }
            }
        }
    }

    public void actionWindowHeat(Boolean start) {
        actionWindowHeatOnOff(start);
    }

    private @Nullable VWWeConnectBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                return (VWWeConnectBridgeHandler) handler;
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        return null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(VWWeConnectActions.class);
    }

}
