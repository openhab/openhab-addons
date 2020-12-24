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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.VehicleType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.Destination;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargeProfile;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargingWindow;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.Timer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.WeeklyPlanner;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTrips;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CBSMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CCMMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Doors;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Position;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Windows;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.binding.bmwconnecteddrive.internal.utils.VehicleStatusUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * The {@link VehicleChannelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleChannelHandler extends BaseThingHandler {
    protected boolean imperial = false;
    protected boolean hasFuel = false;
    protected boolean isElectric = false;
    protected boolean isHybrid = false;

    // List Interfaces
    private List<CBSMessage> serviceList = new ArrayList<CBSMessage>();
    private int serviceListIndex = -1;
    private List<CCMMessage> checkControlList = new ArrayList<CCMMessage>();
    private int checkControlListIndex = -1;
    private List<Destination> destinationList = new ArrayList<Destination>();
    private int destinationListIndex = -1;

    // Vahicle Status Channels
    protected ChannelUID doors;
    protected ChannelUID windows;
    protected ChannelUID lock;
    protected ChannelUID serviceNextDate;
    protected ChannelUID serviceNextMileage;
    protected ChannelUID checkControl;
    protected ChannelUID lastUpdate;

    protected ChannelUID serviceDate;
    protected ChannelUID serviceMileage;
    protected ChannelUID serviceName;
    protected ChannelUID serviceSize;
    protected ChannelUID serviceIndex;
    protected ChannelUID serviceNext;

    protected ChannelUID checkControlMileage;
    protected ChannelUID checkControlName;
    protected ChannelUID checkControlSize;
    protected ChannelUID checkControlIndex;
    protected ChannelUID checkControlNext;

    protected ChannelUID doorDriverFront;
    protected ChannelUID doorDriverRear;
    protected ChannelUID doorPassengerFront;
    protected ChannelUID doorPassengerRear;
    protected ChannelUID doorHood;
    protected ChannelUID doorTrunk;

    protected ChannelUID windowDriverFront;
    protected ChannelUID windowDriverRear;
    protected ChannelUID windowPassengerFront;
    protected ChannelUID windowPassengerRear;
    protected ChannelUID windowRear;
    protected ChannelUID windowSunroof;

    // Range channels
    protected ChannelUID mileage;
    protected ChannelUID remainingRangeHybrid;
    protected ChannelUID remainingRangeElectric;
    protected ChannelUID remainingSoc;
    protected ChannelUID remainingRangeFuel;
    protected ChannelUID remainingFuel;
    protected ChannelUID rangeRadiusElectric;
    protected ChannelUID rangeRadiusFuel;
    protected ChannelUID rangeRadiusHybrid;

    // Lifetime Efficiency Channels
    protected ChannelUID lifeTimeAverageConsumption;
    protected ChannelUID lifetimeAvgCombinedConsumption;
    protected ChannelUID lifeTimeAverageRecuperation;
    protected ChannelUID lifeTimeCumulatedDrivenDistance;
    protected ChannelUID lifeTimeSingleLongestDistance;

    // Last Trip Channels
    protected ChannelUID tripDateTime;
    protected ChannelUID tripDuration;
    protected ChannelUID tripDistance;
    protected ChannelUID tripDistanceSinceCharging;
    protected ChannelUID tripAvgConsumption;
    protected ChannelUID tripAvgCombinedConsumption;
    protected ChannelUID tripAvgRecuperation;

    // Location Channels
    protected ChannelUID gpsLocation;
    protected ChannelUID heading;

    // Remote Services
    protected ChannelUID remoteLightChannel;
    protected ChannelUID remoteFinderChannel;
    protected ChannelUID remoteLockChannel;
    protected ChannelUID remoteUnlockChannel;
    protected ChannelUID remoteHornChannel;
    protected ChannelUID remoteClimateChannel;
    protected ChannelUID remoteStateChannel;

    // Remote Services
    protected ChannelUID destinationName;
    protected ChannelUID destinationLocation;
    protected ChannelUID destinationSize;
    protected ChannelUID destinationIndex;
    protected ChannelUID destinationNext;

    // Charging
    protected ChannelUID chargingStatus;
    protected ChannelUID chargeProfileClimate;
    protected ChannelUID chargeProfileChargeMode;
    protected ChannelUID chargeWindowStart;
    protected ChannelUID chargeWindowEnd;
    protected ChannelUID timer1Departure;
    protected ChannelUID timer1Enabled;
    protected ChannelUID timer1Days;
    protected ChannelUID timer2Departure;
    protected ChannelUID timer2Enabled;
    protected ChannelUID timer2Days;
    protected ChannelUID timer3Departure;
    protected ChannelUID timer3Enabled;
    protected ChannelUID timer3Days;

    // Troubleshooting
    protected ChannelUID vehicleFingerPrint;

    // Image
    protected ChannelUID imageChannel;
    protected ChannelUID imageViewportChannel;
    protected ChannelUID imageSizeChannel;

    // Data Caches
    protected Optional<String> vehicleStatusCache = Optional.empty();
    protected Optional<String> lastTripCache = Optional.empty();
    protected Optional<String> allTripsCache = Optional.empty();
    protected Optional<String> chargeProfileCache = Optional.empty();
    protected Optional<String> rangeMapCache = Optional.empty();
    protected Optional<String> destinationCache = Optional.empty();
    protected Optional<byte[]> imageCache = Optional.empty();

    public VehicleChannelHandler(Thing thing, String type, boolean imperial) {
        super(thing);

        this.imperial = imperial;
        hasFuel = type.equals(VehicleType.CONVENTIONAL.toString()) || type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString());
        isElectric = type.equals(VehicleType.PLUGIN_HYBRID.toString())
                || type.equals(VehicleType.ELECTRIC_REX.toString()) || type.equals(VehicleType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;

        // Vehicle Status channels
        doors = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, DOORS);
        windows = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, WINDOWS);
        lock = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, LOCK);
        serviceNextDate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, SERVICE_DATE);
        serviceNextMileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, SERVICE_MILEAGE);
        checkControl = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHECK_CONTROL);
        chargingStatus = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHARGE_STATUS);
        lastUpdate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, LAST_UPDATE);

        serviceDate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, DATE);
        serviceMileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, MILEAGE);
        serviceName = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, NAME);
        serviceSize = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, SIZE);
        serviceIndex = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, INDEX);
        serviceNext = new ChannelUID(thing.getUID(), CHANNEL_GROUP_SERVICE, NEXT);

        checkControlMileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHECK_CONTROL, MILEAGE);
        checkControlName = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHECK_CONTROL, NAME);
        checkControlSize = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHECK_CONTROL, SIZE);
        checkControlIndex = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHECK_CONTROL, INDEX);
        checkControlNext = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHECK_CONTROL, NEXT);

        doorDriverFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_DRIVER_FRONT);
        doorDriverRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_DRIVER_REAR);
        doorPassengerFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_PASSENGER_FRONT);
        doorPassengerRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, DOOR_PASSENGER_REAR);
        doorHood = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, HOOD);
        doorTrunk = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, TRUNK);

        windowDriverFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_FORNT);
        windowDriverRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_DRIVER_REAR);
        windowPassengerFront = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_FRONT);
        windowPassengerRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_DOOR_PASSENGER_REAR);
        windowRear = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, WINDOW_REAR);
        windowSunroof = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DOORS, SUNROOF);

        // range Channels
        mileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, MILEAGE);
        remainingRangeHybrid = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_HYBRID);
        remainingRangeElectric = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_ELECTRIC);
        remainingRangeFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_FUEL);
        remainingSoc = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, SOC);
        remainingFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_FUEL);
        rangeRadiusElectric = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_RADIUS_ELECTRIC);
        rangeRadiusFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_RADIUS_FUEL);
        rangeRadiusHybrid = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, RANGE_RADIUS_HYBRID);

        // Last Trip Channels
        tripDateTime = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DATE);
        tripDuration = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DURATION);
        tripDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DISTANCE);
        tripDistanceSinceCharging = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DISTANCE_SINCE_CHARGING);
        tripAvgConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_CONSUMPTION);
        tripAvgCombinedConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_COMBINED_CONSUMPTION);
        tripAvgRecuperation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_RECUPERATION);

        // Lifetime Channels
        lifeTimeAverageConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, AVG_CONSUMPTION);
        lifetimeAvgCombinedConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME,
                AVG_COMBINED_CONSUMPTION);
        lifeTimeAverageRecuperation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, AVG_RECUPERATION);
        lifeTimeCumulatedDrivenDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME,
                CUMULATED_DRIVEN_DISTANCE);
        lifeTimeSingleLongestDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, SINGLE_LONGEST_DISTANCE);

        // Location Channels
        gpsLocation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, GPS);
        heading = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, HEADING);

        // Charge Channels
        chargeProfileClimate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_CLIMATE);
        chargeProfileChargeMode = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_MODE);
        chargeWindowStart = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_START);
        chargeWindowEnd = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_WINDOW_END);
        timer1Departure = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DEPARTURE);
        timer1Enabled = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_ENABLED);
        timer1Days = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER1_DAYS);
        timer2Departure = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DEPARTURE);
        timer2Enabled = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_ENABLED);
        timer2Days = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER2_DAYS);
        timer3Departure = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DEPARTURE);
        timer3Enabled = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_DAYS);
        timer3Days = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_TIMER3_ENABLED);

        remoteLightChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_LIGHT_FLASH);
        remoteFinderChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_VEHICLE_FINDER);
        remoteLockChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_DOOR_LOCK);
        remoteUnlockChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_DOOR_UNLOCK);
        remoteHornChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_HORN);
        remoteClimateChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_AIR_CONDITIONING);
        remoteStateChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_STATE);

        destinationName = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, NAME);
        destinationLocation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, GPS);
        destinationSize = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, SIZE);
        destinationIndex = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, INDEX);
        destinationNext = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, NEXT);

        imageChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_FORMAT);
        imageViewportChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT);
        imageSizeChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_SIZE);

        vehicleFingerPrint = new ChannelUID(thing.getUID(), CHANNEL_GROUP_TROUBLESHOOT, VEHICLE_FINGERPRINT);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
    }

    /**
     * Update List Interfaces
     */

    public synchronized void nextCheckControl() {
        checkControlListIndex++;
        updateService();
    }

    public synchronized void setCheckControlList(List<CCMMessage> l) {
        checkControlList = l;
        updateCheckControls();
    }

    private void updateCheckControls() {
        if (!checkControlList.isEmpty()) {
            if (checkControlListIndex < 0 || checkControlListIndex >= checkControlList.size()) {
                // select first item
                checkControlListIndex = 0;
            }
            CCMMessage entry = checkControlList.get(checkControlListIndex);
            updateState(checkControlName, StringType.valueOf(entry.ccmDescriptionShort));
            if (imperial) {
                updateState(checkControlMileage,
                        QuantityType.valueOf(Converter.round(entry.ccmMileage), ImperialUnits.MILE));
            } else {
                updateState(checkControlMileage,
                        QuantityType.valueOf(Converter.round(entry.ccmMileage), MetricPrefix.KILO(SIUnits.METRE)));
            }
            updateState(checkControlSize, new DecimalType(checkControlList.size()));
            updateState(checkControlIndex, new DecimalType(checkControlListIndex));
        } else {
            // list is empty - set all fields to INVALID. If this isn't done the old values remain
            updateState(checkControlName, StringType.valueOf(Constants.INVALID));
            if (imperial) {
                updateState(checkControlMileage, QuantityType.valueOf(Converter.round(-1), ImperialUnits.MILE));
            } else {
                updateState(checkControlMileage,
                        QuantityType.valueOf(Converter.round(-1), MetricPrefix.KILO(SIUnits.METRE)));
            }
            updateState(checkControlSize, new DecimalType(checkControlList.size()));
            updateState(checkControlIndex, new DecimalType(-1));
        }
    }

    public synchronized void nextService() {
        serviceListIndex++;
        updateService();
    }

    public synchronized void setServiceList(List<CBSMessage> l) {
        serviceList = l;
        updateService();
    }

    private void updateService() {
        if (!serviceList.isEmpty()) {
            if (serviceListIndex < 0 || serviceListIndex >= serviceList.size()) {
                // select first item
                serviceListIndex = 0;
            }
            CBSMessage entry = serviceList.get(serviceListIndex);
            updateState(serviceName, StringType.valueOf(Converter.toTitleCase(entry.getType())));
            updateState(serviceDate, DateTimeType.valueOf(Converter.getLocalDateTime(entry.getDueDate())));
            if (imperial) {
                updateState(serviceMileage,
                        QuantityType.valueOf(Converter.round(entry.cbsRemainingMileage), ImperialUnits.MILE));
            } else {
                updateState(serviceMileage, QuantityType.valueOf(Converter.round(entry.cbsRemainingMileage),
                        MetricPrefix.KILO(SIUnits.METRE)));
            }
            updateState(serviceSize, new DecimalType(serviceList.size()));
            updateState(serviceIndex, new DecimalType(serviceListIndex));
        } else {
            // list is empty - set all fields to INVALID. If this isn't done the old values remain
            updateState(serviceName, StringType.valueOf(Constants.INVALID));
            updateState(serviceDate, DateTimeType.valueOf(Converter.getLocalDateTime(Constants.NULL_DATE)));
            if (imperial) {
                updateState(serviceMileage, QuantityType.valueOf(-1, ImperialUnits.MILE));
            } else {
                updateState(serviceMileage, QuantityType.valueOf(-1, MetricPrefix.KILO(SIUnits.METRE)));
            }
            updateState(serviceSize, new DecimalType(serviceList.size()));
            updateState(serviceIndex, new DecimalType(-1));
        }
    }

    public synchronized void setDestinationList(List<Destination> l) {
        destinationList = l;
        updateDestination();
    }

    public synchronized void nextDestination() {
        destinationListIndex++;
        updateDestination();
    }

    /**
     * needs to be synchronized with onResponse update
     */
    private void updateDestination() {
        if (!destinationList.isEmpty()) {
            if (destinationListIndex < 0 || destinationListIndex >= destinationList.size()) {
                // select first item
                destinationListIndex = 0;
            }
            Destination entry = destinationList.get(destinationListIndex);
            updateState(destinationName, StringType.valueOf(entry.getAddress()));
            updateState(destinationLocation, PointType.valueOf(entry.getCoordinates()));
            updateState(destinationSize, new DecimalType(destinationList.size()));
            updateState(destinationIndex, new DecimalType(destinationListIndex));
        } else {
            // list is empty - set all fields to INVALID. If this isn't done the old values remain
            updateState(destinationName, StringType.valueOf(Constants.INVALID));
            updateState(destinationLocation, PointType.valueOf("-1,-1"));
            updateState(destinationSize, new DecimalType(destinationList.size()));
            updateState(destinationIndex, new DecimalType(-1));
        }
    }

    /**
     * Channel Groups
     */

    protected void updateAllTrips(AllTrips allTrips) {
        updateState(lifeTimeCumulatedDrivenDistance, QuantityType
                .valueOf(Converter.round(allTrips.totalElectricDistance.userTotal), MetricPrefix.KILO(SIUnits.METRE)));
        updateState(lifeTimeSingleLongestDistance, QuantityType
                .valueOf(Converter.round(allTrips.chargecycleRange.userHigh), MetricPrefix.KILO(SIUnits.METRE)));
        updateState(lifeTimeAverageConsumption, QuantityType
                .valueOf(Converter.round(allTrips.avgElectricConsumption.userAverage), Units.KILOWATT_HOUR));
        updateState(lifetimeAvgCombinedConsumption,
                QuantityType.valueOf(allTrips.avgCombinedConsumption.userAverage, Units.LITRE));
        updateState(lifeTimeAverageRecuperation,
                QuantityType.valueOf(Converter.round(allTrips.avgRecuperation.userAverage), Units.KILOWATT_HOUR));
        updateState(tripDistanceSinceCharging, QuantityType.valueOf(
                Converter.round(allTrips.chargecycleRange.userCurrentChargeCycle), MetricPrefix.KILO(SIUnits.METRE)));
    }

    protected void updateLastTrip(LastTrip trip) {
        // Whyever the Last Trip DateTime is delivered without offest - so LocalTime
        updateState(tripDateTime, DateTimeType.valueOf(Converter.getLocalDateTimeWithoutOffest(trip.date)));
        updateState(tripDuration, QuantityType.valueOf(trip.duration, Units.MINUTE));
        updateState(tripDistance,
                QuantityType.valueOf(Converter.round(trip.totalDistance), MetricPrefix.KILO(SIUnits.METRE)));
        updateState(tripAvgConsumption,
                QuantityType.valueOf(Converter.round(trip.avgElectricConsumption), Units.KILOWATT_HOUR));
        updateState(tripAvgCombinedConsumption, QuantityType.valueOf(trip.avgCombinedConsumption, Units.LITRE));
        updateState(tripAvgRecuperation,
                QuantityType.valueOf(Converter.round(trip.avgRecuperation), Units.KILOWATT_HOUR));
    }

    protected void updateChargeProfile(ChargeProfile cp) {
        WeeklyPlanner planner = cp.weeklyPlanner;
        updateState(chargeProfileClimate, OnOffType.from(planner.climatizationEnabled));
        updateState(chargeProfileChargeMode, StringType.valueOf(Converter.toTitleCase(planner.chargingMode)));

        ChargingWindow cw = planner.preferredChargingWindow;
        updateState(chargeWindowStart, StringType.valueOf(cw.startTime));
        updateState(chargeWindowEnd, StringType.valueOf(cw.endTime));

        Timer t1 = planner.timer1;
        updateState(timer1Departure, StringType.valueOf(t1.departureTime));
        updateState(timer1Enabled, OnOffType.from(t1.timerEnabled));
        updateState(timer1Days, StringType.valueOf(t1.getDays()));

        Timer t2 = planner.timer2;
        updateState(timer2Departure, StringType.valueOf(t2.departureTime));
        updateState(timer2Enabled, OnOffType.from(t2.timerEnabled));
        updateState(timer2Days, StringType.valueOf(t2.getDays()));

        Timer t3 = planner.timer3;
        updateState(timer3Departure, StringType.valueOf(t3.departureTime));
        updateState(timer3Enabled, OnOffType.from(t3.timerEnabled));
        updateState(timer3Days, StringType.valueOf(t3.getDays()));
    }

    protected void updateDoors(Doors doorState) {
        updateState(doorDriverFront, StringType.valueOf(doorState.doorDriverFront));
        updateState(doorDriverRear, StringType.valueOf(doorState.doorDriverRear));
        updateState(doorPassengerFront, StringType.valueOf(doorState.doorPassengerFront));
        updateState(doorPassengerRear, StringType.valueOf(doorState.doorPassengerRear));
        updateState(doorTrunk, StringType.valueOf(doorState.trunk));
        updateState(doorHood, StringType.valueOf(doorState.hood));
    }

    protected void updateWindows(Windows windowState) {
        updateState(windowDriverFront, StringType.valueOf(windowState.windowDriverFront));
        updateState(windowDriverRear, StringType.valueOf(windowState.windowDriverRear));
        updateState(windowPassengerFront, StringType.valueOf(windowState.windowPassengerFront));
        updateState(windowPassengerRear, StringType.valueOf(windowState.windowPassengerRear));
        updateState(windowRear, StringType.valueOf(windowState.rearWindow));
        updateState(windowSunroof, StringType.valueOf(windowState.sunroof));
    }

    protected void updatePosition(Position pos) {
        updateState(gpsLocation, PointType.valueOf(pos.getCoordinates()));
        updateState(heading, QuantityType.valueOf(pos.heading, Units.DEGREE_ANGLE));
    }

    protected void updateVehicleStatus(VehicleStatus vStatus) {
        // Vehicle Status
        updateState(lock, StringType.valueOf(Converter.toTitleCase(vStatus.doorLockState)));

        // Service Updates
        String nextServiceDate = VehicleStatusUtils.getNextServiceDate(vStatus);
        updateState(serviceNextDate, DateTimeType.valueOf(Converter.getLocalDateTime(nextServiceDate)));
        double nextServiceMileage = VehicleStatusUtils.getNextServiceMileage(vStatus);
        if (imperial) {
            updateState(serviceNextMileage,
                    QuantityType.valueOf(Converter.round(nextServiceMileage), ImperialUnits.MILE));
        } else {
            updateState(serviceNextMileage,
                    QuantityType.valueOf(Converter.round(nextServiceMileage), MetricPrefix.KILO(SIUnits.METRE)));
        }
        // CheckControl Active?
        updateState(checkControl,
                StringType.valueOf(Converter.toTitleCase(VehicleStatusUtils.checkControlActive(vStatus))));
        // last update Time
        updateState(lastUpdate,
                DateTimeType.valueOf(Converter.getLocalDateTime(VehicleStatusUtils.getUpdateTime(vStatus))));

        Doors doorState = Converter.getGson().fromJson(Converter.getGson().toJson(vStatus), Doors.class);
        Windows windowState = Converter.getGson().fromJson(Converter.getGson().toJson(vStatus), Windows.class);
        if (doorState != null) {
            updateState(doors, StringType.valueOf(VehicleStatusUtils.checkClosed(doorState)));
            updateDoors(doorState);
        }
        if (windowState != null) {
            updateState(windows, StringType.valueOf(VehicleStatusUtils.checkClosed(windowState)));
            updateWindows(windowState);
        }

        // Range values
        // based on unit of length decide if range shall be reported in km or miles
        if (!imperial) {
            updateState(mileage, QuantityType.valueOf(vStatus.mileage, MetricPrefix.KILO(SIUnits.METRE)));
            float totalRange = 0;
            if (isElectric) {
                totalRange += vStatus.remainingRangeElectric;
                updateState(remainingRangeElectric,
                        QuantityType.valueOf(vStatus.remainingRangeElectric, MetricPrefix.KILO(SIUnits.METRE)));
                updateState(rangeRadiusElectric, QuantityType.valueOf(
                        Converter.guessRangeRadius(vStatus.remainingRangeElectric), MetricPrefix.KILO(SIUnits.METRE)));
            }
            if (hasFuel) {
                totalRange += vStatus.remainingRangeFuel;
                updateState(remainingRangeFuel,
                        QuantityType.valueOf(vStatus.remainingRangeFuel, MetricPrefix.KILO(SIUnits.METRE)));
                updateState(rangeRadiusFuel, QuantityType.valueOf(
                        Converter.guessRangeRadius(vStatus.remainingRangeFuel), MetricPrefix.KILO(SIUnits.METRE)));
            }
            if (isHybrid) {
                updateState(remainingRangeHybrid,
                        QuantityType.valueOf(Converter.round(totalRange), MetricPrefix.KILO(SIUnits.METRE)));
                updateState(rangeRadiusHybrid,
                        QuantityType.valueOf(Converter.guessRangeRadius(totalRange), MetricPrefix.KILO(SIUnits.METRE)));
            }
        } else {
            updateState(mileage, QuantityType.valueOf(vStatus.mileage, ImperialUnits.MILE));
            float totalRange = 0;
            if (isElectric) {
                totalRange += vStatus.remainingRangeElectricMls;
                updateState(remainingRangeElectric,
                        QuantityType.valueOf(vStatus.remainingRangeElectricMls, ImperialUnits.MILE));
                updateState(rangeRadiusElectric, QuantityType
                        .valueOf(Converter.guessRangeRadius(vStatus.remainingRangeElectricMls), ImperialUnits.MILE));
            }
            if (hasFuel) {
                totalRange += vStatus.remainingRangeFuelMls;
                updateState(remainingRangeFuel,
                        QuantityType.valueOf(vStatus.remainingRangeFuelMls, ImperialUnits.MILE));
                updateState(rangeRadiusFuel, QuantityType
                        .valueOf(Converter.guessRangeRadius(vStatus.remainingRangeFuelMls), ImperialUnits.MILE));
            }
            if (isHybrid) {
                updateState(remainingRangeHybrid,
                        QuantityType.valueOf(Converter.round(totalRange), ImperialUnits.MILE));
                updateState(rangeRadiusHybrid,
                        QuantityType.valueOf(Converter.guessRangeRadius(totalRange), ImperialUnits.MILE));
            }
        }
        if (isElectric) {
            updateState(remainingSoc, QuantityType.valueOf(vStatus.chargingLevelHv, Units.PERCENT));
        }
        if (hasFuel) {
            updateState(remainingFuel, QuantityType.valueOf(vStatus.remainingFuel, Units.LITRE));
        }

        // Charge Values
        if (isElectric) {
            if (vStatus.chargingStatus != null) {
                if (Constants.INVALID.equals(vStatus.chargingStatus)) {
                    updateState(chargingStatus,
                            StringType.valueOf(Converter.toTitleCase(vStatus.lastChargingEndReason)));
                } else {
                    // State INVALID is somehow misleading. Instead show the Last Charging End Reason
                    updateState(chargingStatus, StringType.valueOf(Converter.toTitleCase(vStatus.chargingStatus)));
                }
            }
        }
    }
}
