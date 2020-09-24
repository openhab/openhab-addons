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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    protected ChannelUID longitude;
    protected ChannelUID latitude;
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
    protected ChannelUID destinationLat;
    protected ChannelUID destinationLon;
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

    public VehicleChannelHandler(Thing thing) {
        super(thing);

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
        longitude = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, LONGITUDE);
        latitude = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, LATITUDE);
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
        destinationLat = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, LATITUDE);
        destinationLon = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, LONGITUDE);
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
}
