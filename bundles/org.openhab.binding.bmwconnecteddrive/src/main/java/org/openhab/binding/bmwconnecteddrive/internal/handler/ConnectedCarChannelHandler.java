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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link ConnectedCarChannelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedCarChannelHandler extends BaseThingHandler {

    // Vahicle Status Channels
    protected ChannelUID doors;
    protected ChannelUID windows;
    protected ChannelUID lock;
    protected ChannelUID service;
    protected ChannelUID checkControl;
    protected ChannelUID lastUpdate;

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
    protected ChannelUID lifeTimeAverageRecuperation;
    protected ChannelUID lifeTimeCumulatedDrivenDistance;
    protected ChannelUID lifeTimeSingleLongestDistance;

    // Last Trip Channels
    protected ChannelUID tripDistance;
    protected ChannelUID tripDistanceSinceCharging;
    protected ChannelUID tripAvgConsumption;
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
    protected ChannelUID destinationName1;
    protected ChannelUID destinationLat1;
    protected ChannelUID destinationLon1;
    protected ChannelUID destinationName2;
    protected ChannelUID destinationLat2;
    protected ChannelUID destinationLon2;
    protected ChannelUID destinationName3;
    protected ChannelUID destinationLat3;
    protected ChannelUID destinationLon3;

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

    public ConnectedCarChannelHandler(Thing thing) {
        super(thing);

        // Vehicle Status channels
        doors = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, DOORS);
        windows = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, WINDOWS);
        lock = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, LOCK);
        service = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, SERVICE);
        checkControl = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHECK_CONTROL);
        lastUpdate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, LAST_UPDATE);

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
        tripDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DISTANCE);
        tripDistanceSinceCharging = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, DISTANCE_SINCE_CHARGING);
        tripAvgConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_CONSUMPTION);
        tripAvgRecuperation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LAST_TRIP, AVG_RECUPERATION);

        // Lifetime Channels
        lifeTimeAverageConsumption = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, AVG_CONSUMPTION);
        lifeTimeAverageRecuperation = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, AVG_RECUPERATION);
        lifeTimeCumulatedDrivenDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME,
                CUMULATED_DRIVEN_DISTANCE);
        lifeTimeSingleLongestDistance = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LIFETIME, SINGLE_LONGEST_DISTANCE);

        // Location Channels
        longitude = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, LONGITUDE);
        latitude = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, LATITUDE);
        heading = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, HEADING);

        // Charge Channels
        chargingStatus = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CHARGE, CHARGE_STATUS);
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

        destinationName1 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_NAME_1);
        destinationLat1 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_LAT_1);
        destinationLon1 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_LON_1);
        destinationName2 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_NAME_2);
        destinationLat2 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_LAT_2);
        destinationLon2 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_LON_2);
        destinationName3 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_NAME_3);
        destinationLat3 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_LAT_3);
        destinationLon3 = new ChannelUID(thing.getUID(), CHANNEL_GROUP_DESTINATION, DESTINATION_LON_3);

        imageChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CAR_IMAGE, IMAGE);

        vehicleFingerPrint = new ChannelUID(thing.getUID(), CHANNEL_GROUP_TROUBLESHOOT, VEHICLE_FINGERPRINT);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
