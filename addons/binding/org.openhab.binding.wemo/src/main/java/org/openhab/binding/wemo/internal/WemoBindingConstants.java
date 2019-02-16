/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WemoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Mihir Patil - Added standby switch
 */
public class WemoBindingConstants {

    public static final String BINDING_ID = "wemo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SOCKET = new ThingTypeUID(BINDING_ID, "socket");
    public static final ThingTypeUID THING_TYPE_INSIGHT = new ThingTypeUID(BINDING_ID, "insight");
    public static final ThingTypeUID THING_TYPE_LIGHTSWITCH = new ThingTypeUID(BINDING_ID, "lightswitch");
    public static final ThingTypeUID THING_TYPE_MOTION = new ThingTypeUID(BINDING_ID, "motion");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_MZ100 = new ThingTypeUID(BINDING_ID, "MZ100");
    public static final ThingTypeUID THING_TYPE_MAKER = new ThingTypeUID(BINDING_ID, "Maker");
    public static final ThingTypeUID THING_TYPE_COFFEE = new ThingTypeUID(BINDING_ID, "CoffeeMaker");

    // List of all Channel ids
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_MOTIONDETECTION = "motionDetection";
    public static final String CHANNEL_LASTMOTIONDETECTED = "lastMotionDetected";
    public static final String CHANNEL_LASTCHANGEDAT = "lastChangedAt";
    public static final String CHANNEL_LASTONFOR = "lastOnFor";
    public static final String CHANNEL_ONTODAY = "onToday";
    public static final String CHANNEL_ONTOTAL = "onTotal";
    public static final String CHANNEL_TIMESPAN = "timespan";
    public static final String CHANNEL_AVERAGEPOWER = "averagePower";
    public static final String CHANNEL_CURRENTPOWER = "currentPower";
    public static final String CHANNEL_ENERGYTODAY = "energyToday";
    public static final String CHANNEL_ENERGYTOTAL = "energyTotal";
    public static final String CHANNEL_STANDBYLIMIT = "standByLimit";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_RELAY = "relay";
    public static final String CHANNEL_SENSOR = "sensor";
    public static final String CHANNEL_ONSTANDBY = "onStandBy";


    public static final String CHANNEL_COFFEEMODE = "coffeeMode";
    public static final String CHANNEL_MODETIME = "modeTime";
    public static final String CHANNEL_TIMEREMAINING = "timeRemaining";
    public static final String CHANNEL_WATERLEVELREACHED = "waterLevelReached";
    public static final String CHANNEL_CLEANADVISE = "cleanAdvise";
    public static final String CHANNEL_FILTERADVISE = "filterAdvise";
    public static final String CHANNEL_BREWED = "brewed";
    public static final String CHANNEL_LASTCLEANED = "lastCleaned";

    // List of thing configuration properties
    public static final String UDN = "udn";
    public static final String DEVICE_ID = "deviceID";
    public static final String POLLINGINTERVALL = "pollingInterval";

    public static final int SUBSCRIPTION_DURATION = 600;
    public static final int LINK_DISCOVERY_SERVICE_INITIAL_DELAY = 5;
    public static final String HTTP_CALL_CONTENT_HEADER = "text/xml; charset=utf-8";

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    public static final Set<ThingTypeUID> SUPPORTED_LIGHT_THING_TYPES = Collections.singleton(THING_TYPE_MZ100);

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_SOCKET, THING_TYPE_INSIGHT, THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION)
                    .collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream
                    .of(THING_TYPE_SOCKET, THING_TYPE_INSIGHT, THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION,
                            THING_TYPE_BRIDGE, THING_TYPE_MZ100, THING_TYPE_MAKER, THING_TYPE_COFFEE)
                    .collect(Collectors.toSet()));

}
