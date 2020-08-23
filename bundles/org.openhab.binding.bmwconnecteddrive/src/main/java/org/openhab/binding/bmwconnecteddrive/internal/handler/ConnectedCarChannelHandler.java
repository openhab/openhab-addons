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

    // Vehcile channels
    protected ChannelUID mileage;
    protected ChannelUID remainingRange;
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

    protected ChannelUID imageChannel;
    protected ChannelUID imageSizeChannel;
    protected ChannelUID imageViewDirectionChannel;

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

        // range Channels
        mileage = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, MILEAGE);
        remainingRange = new ChannelUID(thing.getUID(), CHANNEL_GROUP_RANGE, REMAINING_RANGE);
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

        imageChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CAR_IMAGE, IMAGE);
        imageSizeChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CAR_IMAGE, IMAGE_VIEW_DIRECTION);
        imageViewDirectionChannel = new ChannelUID(thing.getUID(), CHANNEL_GROUP_CAR_IMAGE, IMAGE_SIZE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
