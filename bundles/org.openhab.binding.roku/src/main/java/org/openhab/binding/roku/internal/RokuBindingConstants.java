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
package org.openhab.binding.roku.internal;

import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RokuBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RokuBindingConstants {
    public static final String BINDING_ID = "roku";
    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_HOST_NAME = "hostName";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_MODEL_NAME = "Model Name";
    public static final String PROPERTY_MODEL_NUMBER = "Model Number";
    public static final String PROPERTY_DEVICE_LOCAITON = "Device Location";
    public static final String PROPERTY_SERIAL_NUMBER = "Serial Number";
    public static final String PROPERTY_DEVICE_ID = "Device Id";
    public static final String PROPERTY_SOFTWARE_VERSION = "Software Version";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ROKU_PLAYER = new ThingTypeUID(BINDING_ID, "roku_player");
    public static final ThingTypeUID THING_TYPE_ROKU_TV = new ThingTypeUID(BINDING_ID, "roku_tv");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ROKU_PLAYER,
            THING_TYPE_ROKU_TV);

    // List of all Channel id's
    public static final String ACTIVE_APP = "activeApp";
    public static final String ACTIVE_APPNAME = "activeAppName";
    public static final String BUTTON = "button";
    public static final String CONTROL = "control";
    public static final String PLAY_MODE = "playMode";
    public static final String TIME_ELAPSED = "timeElapsed";
    public static final String TIME_TOTAL = "timeTotal";
    public static final String ACTIVE_CHANNEL = "activeChannel";
    public static final String SIGNAL_MODE = "signalMode";
    public static final String SIGNAL_QUALITY = "signalQuality";
    public static final String CHANNEL_NAME = "channelName";
    public static final String PROGRAM_TITLE = "programTitle";
    public static final String PROGRAM_DESCRIPTION = "programDescription";
    public static final String PROGRAM_RATING = "programRating";
    public static final String POWER = "power";
    public static final String POWER_STATE = "powerState";

    // Units of measurement of the data delivered by the API
    public static final Unit<Time> API_SECONDS_UNIT = Units.SECOND;
    public static final Unit<Dimensionless> API_PERCENT_UNIT = Units.PERCENT;

    public static final String PLAY = "play";
    public static final String STOP = "stop";
    public static final String CLOSE = "close";
    public static final String EMPTY = "";
    public static final String ROKU_HOME = "Roku Home";
    public static final String ROKU_HOME_ID = "-1";
    public static final String ROKU_HOME_ID_562859 = "562859";
    public static final String ROKU_HOME_BUTTON = "Home";
    public static final String ROKU_PLAY_BUTTON = "Play";
    public static final String ROKU_NEXT_BUTTON = "Fwd";
    public static final String ROKU_PREV_BUTTON = "Rev";
    public static final String NON_DIGIT_PATTERN = "[^\\d]";
    public static final String TV_APP = "tvinput.dtv";
    public static final String TV_INPUT = "tvinput";
    public static final String POWER_ON = "POWERON";
}
