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
    // Property Channels
    protected ChannelUID brandChannel;
    protected ChannelUID modelChannel;
    protected ChannelUID drivetrainChannel;
    protected ChannelUID bodyChannel;
    protected ChannelUID colorChannel;
    protected ChannelUID constructionYearChannel;
    protected ChannelUID communityStatisticsChannel;
    protected ChannelUID alarmChannel;
    protected ChannelUID dealerNameChannel;
    protected ChannelUID dealerAddressChannel;
    protected ChannelUID dealerPhoneChannel;
    protected ChannelUID breakdownPhoneChannel;
    protected ChannelUID activatedServicesChannel;
    protected ChannelUID deactivatedServicesChannel;
    protected ChannelUID supportedServicesChannel;
    protected ChannelUID notSupportedServicesChannel;
    protected ChannelUID chargingModesChannel;

    // Vahicle Status Channels
    protected ChannelUID doors;
    protected ChannelUID windows;
    protected ChannelUID lock;
    protected ChannelUID service;
    protected ChannelUID checkControl;
    protected ChannelUID chargingStatus;

    // Range channels
    protected ChannelUID mileage;
    protected ChannelUID remainingRangeHybrid;
    protected ChannelUID remainingRangeElectric;
    protected ChannelUID remainingSoc;
    protected ChannelUID remainingRangeFuel;
    protected ChannelUID remainingFuel;
    protected ChannelUID lastUpdate;

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
    protected ChannelUID latlong;
    protected ChannelUID heading;
    protected ChannelUID rangeRadius;

    // Image
    protected ChannelUID imageChannel;

    // Remote Services
    protected ChannelUID remoteLightChannel;
    protected ChannelUID remoteFinderChannel;
    protected ChannelUID remoteLockChannel;
    protected ChannelUID remoteUnlockChannel;
    protected ChannelUID remoteHornChannel;
    protected ChannelUID remoteClimateChannel;
    protected ChannelUID remoteStateChannel;

    public ConnectedCarChannelHandler(Thing thing) {
        super(thing);

        // create properties channels
        brandChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_BRAND);
        modelChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_MODEL);
        drivetrainChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_DRIVETRAIN);
        bodyChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_BODYTYPE);
        colorChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_COLOR);
        constructionYearChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES,
                PROPERTIES_CONSTRUCTION_YEAR);
        communityStatisticsChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_COMMUNITY);
        alarmChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_ALARM);
        dealerNameChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_DEALER_NAME);
        dealerAddressChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_DEALER_ADDRESS);
        dealerPhoneChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_DEALER_PHONE);
        breakdownPhoneChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_BREAKDOWN_PHONE);
        activatedServicesChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES,
                PROPERTIES_ACTIVATED_SERVICES);
        deactivatedServicesChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES,
                PROPERTIES_DEACTIVATED_SERVICES);
        supportedServicesChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES,
                PROPERTIES_SUPPORTED_SERVICES);
        notSupportedServicesChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES,
                PROPERTIES_NOT_SUPPORTED_SERVICES);
        chargingModesChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PROPERTIES, PROPERTIES_CHARGING_MODES);

        // Vehicle Status channels
        doors = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, DOORS);
        windows = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, WINDOWS);
        lock = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, LOCK);
        service = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, SERVICE);
        checkControl = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHECK_CONTROL);
        chargingStatus = new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATUS, CHARGING_STATUS);

        // range Channels
        mileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, MILEAGE);
        remainingRangeHybrid = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_RANGE_HYBRID);
        remainingRangeElectric = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_RANGE_ELECTRIC);
        remainingSoc = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_SOC);
        remainingRangeFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_RANGE_FUEL);
        remainingFuel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_FUEL);
        lastUpdate = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, LAST_UPDATE);

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
        latlong = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, LATLONG);
        heading = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, HEADING);
        rangeRadius = new ChannelUID(thing.getUID(), CHANNEL_GROUP_LOCATION, RANGE_RADIUS);

        imageChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CAR_IMAGE, IMAGE);

        remoteLightChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_LIGHT_FLASH);
        remoteFinderChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_VEHICLE_FINDER);
        remoteLockChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_DOOR_LOCK);
        remoteUnlockChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_DOOR_UNLOCK);
        remoteHornChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_HORN);
        remoteClimateChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_SERVICE_AIR_CONDITIONING);
        remoteStateChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_REMOTE, REMOTE_STATE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
