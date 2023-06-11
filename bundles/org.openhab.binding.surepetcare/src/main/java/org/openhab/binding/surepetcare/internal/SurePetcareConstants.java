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
package org.openhab.binding.surepetcare.internal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.surepetcare.internal.utils.GsonLocalDateTypeAdapter;
import org.openhab.binding.surepetcare.internal.utils.GsonLocalTimeTypeAdapter;
import org.openhab.binding.surepetcare.internal.utils.GsonZonedDateTimeTypeAdapter;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link SurePetcareConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareConstants {

    public static final String BINDING_ID = "surepetcare";

    // List all Thing Type UIDs, related to the binding
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_HOUSEHOLD = new ThingTypeUID(BINDING_ID, "household");
    public static final ThingTypeUID THING_TYPE_PET = new ThingTypeUID(BINDING_ID, "pet");
    public static final ThingTypeUID THING_TYPE_HUB_DEVICE = new ThingTypeUID(BINDING_ID, "hubDevice");
    public static final ThingTypeUID THING_TYPE_FLAP_DEVICE = new ThingTypeUID(BINDING_ID, "flapDevice");
    public static final ThingTypeUID THING_TYPE_FEEDER_DEVICE = new ThingTypeUID(BINDING_ID, "feederDevice");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(THING_TYPE_HOUSEHOLD,
            THING_TYPE_PET, THING_TYPE_HUB_DEVICE, THING_TYPE_FLAP_DEVICE, THING_TYPE_FEEDER_DEVICE));

    public static final long DEFAULT_REFRESH_INTERVAL_TOPOLOGY = 36000; // 10 hours
    public static final long DEFAULT_REFRESH_INTERVAL_STATUS = 300; // 5 mins

    public static final String PROPERTY_NAME_ID = "id";

    public static final int FLAP_MAX_NUMBER_OF_CURFEWS = 4;
    public static final int BOWL_ID_ONE_BOWL_USED = 1;
    public static final int BOWL_ID_TWO_BOWLS_USED = 4;

    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(ZonedDateTime.class, new GsonZonedDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new GsonLocalDateTypeAdapter())
            .registerTypeAdapter(LocalTime.class, new GsonLocalTimeTypeAdapter()).create();

    // Bridge Channel Names
    public static final String BRIDGE_CHANNEL_REFRESH = "refresh";

    // Household Channel Names
    public static final String HOUSEHOLD_CHANNEL_ID = "id";
    public static final String HOUSEHOLD_CHANNEL_NAME = "name";
    public static final String HOUSEHOLD_CHANNEL_TIMEZONE_ID = "timezoneId";

    // Device Channel Names
    public static final String DEVICE_CHANNEL_ID = "id";
    public static final String DEVICE_CHANNEL_NAME = "name";
    public static final String DEVICE_CHANNEL_PRODUCT = "product";
    public static final String DEVICE_CHANNEL_LED_MODE = "ledMode";
    public static final String DEVICE_CHANNEL_PAIRING_MODE = "pairingMode";
    public static final String DEVICE_CHANNEL_ONLINE = "online";
    public static final String DEVICE_CHANNEL_CURFEW_BASE = "curfew";
    public static final String DEVICE_CHANNEL_CURFEW_ENABLED = DEVICE_CHANNEL_CURFEW_BASE + "Enabled";
    public static final String DEVICE_CHANNEL_CURFEW_LOCK_TIME = DEVICE_CHANNEL_CURFEW_BASE + "LockTime";
    public static final String DEVICE_CHANNEL_CURFEW_UNLOCK_TIME = DEVICE_CHANNEL_CURFEW_BASE + "UnlockTime";
    public static final String DEVICE_CHANNEL_LOCKING_MODE = "lockingMode";
    public static final String DEVICE_CHANNEL_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String DEVICE_CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String DEVICE_CHANNEL_LOW_BATTERY = "lowBattery";
    public static final String DEVICE_CHANNEL_DEVICE_RSSI = "deviceRSSI";
    public static final String DEVICE_CHANNEL_HUB_RSSI = "hubRSSI";
    public static final String DEVICE_CHANNEL_BOWLS_FOOD = "bowlsFood";
    public static final String DEVICE_CHANNEL_BOWLS_TARGET = "bowlsTarget";
    public static final String DEVICE_CHANNEL_BOWLS_FOOD_LEFT = "bowlsFoodLeft";
    public static final String DEVICE_CHANNEL_BOWLS_TARGET_LEFT = "bowlsTargetLeft";
    public static final String DEVICE_CHANNEL_BOWLS_FOOD_RIGHT = "bowlsFoodRight";
    public static final String DEVICE_CHANNEL_BOWLS_TARGET_RIGHT = "bowlsTargetRight";
    public static final String DEVICE_CHANNEL_BOWLS = "bowls";
    public static final String DEVICE_CHANNEL_BOWLS_CLOSE_DELAY = "bowlsCloseDelay";
    public static final String DEVICE_CHANNEL_BOWLS_TRAINING_MODE = "bowlsTrainingMode";

    // Pet Channel Names
    public static final String PET_CHANNEL_ID = "id";
    public static final String PET_CHANNEL_NAME = "name";
    public static final String PET_CHANNEL_COMMENT = "comment";
    public static final String PET_CHANNEL_GENDER = "gender";
    public static final String PET_CHANNEL_BREED = "breed";
    public static final String PET_CHANNEL_SPECIES = "species";
    public static final String PET_CHANNEL_PHOTO = "photo";
    public static final String PET_CHANNEL_LOCATION = "location";
    public static final String PET_CHANNEL_LOCATION_CHANGED = "locationChanged";
    public static final String PET_CHANNEL_LOCATION_TIMEOFFSET = "locationTimeoffset";
    public static final String PET_CHANNEL_LOCATION_CHANGED_THROUGH = "locationChangedThrough";
    public static final String PET_CHANNEL_DATE_OF_BIRTH = "dateOfBirth";
    public static final String PET_CHANNEL_WEIGHT = "weight";
    public static final String PET_CHANNEL_TAG_IDENTIFIER = "tagIdentifier";
    public static final String PET_CHANNEL_FEEDER_DEVICE = "feederDevice";
    public static final String PET_CHANNEL_FEEDER_LASTFEEDING = "feederLastFeeding";
    public static final String PET_CHANNEL_FEEDER_LAST_CHANGE = "feederLastChange";
    public static final String PET_CHANNEL_FEEDER_LAST_CHANGE_LEFT = "feederLastChangeLeft";
    public static final String PET_CHANNEL_FEEDER_LAST_CHANGE_RIGHT = "feederLastChangeRight";
}
