/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link NibeUplinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - initial contribution
 *
 */
public final class NibeUplinkBindingConstants {

    private static final @NonNull String BINDING_ID = "nibeuplink";

    // List of main device types
    public static final @NonNull String DEVICE_VVM320 = "vvm320";
    public static final @NonNull String DEVICE_VVM320_SENSORS = "vvm320-sensors";
    public static final @NonNull String DEVICE_VVM320_SETTINGS = "vvm320-settings";
    public static final @NonNull String DEVICE_VVM320_SPECIAL = "vvm320-special";
    public static final @NonNull String DEVICE_VVM310 = "vvm310";
    public static final @NonNull String DEVICE_VVM310_SENSORS = "vvm310-sensors";
    public static final @NonNull String DEVICE_VVM310_SETTINGS = "vvm310-settings";
    public static final @NonNull String DEVICE_VVM310_SPECIAL = "vvm310-special";
    public static final @NonNull String DEVICE_F750 = "f750";
    public static final @NonNull String DEVICE_F750_SENSORS = "f750-sensors";
    public static final @NonNull String DEVICE_F750_SETTINGS = "f750-settings";
    public static final @NonNull String DEVICE_F750_SPECIAL = "f750-special";
    public static final @NonNull String DEVICE_F1145 = "f1145";
    public static final @NonNull String DEVICE_F1145_SENSORS = "f1145-sensors";
    public static final @NonNull String DEVICE_F1145_SETTINGS = "f1145-settings";
    public static final @NonNull String DEVICE_F1145_SPECIAL = "f1145-special";
    public static final @NonNull String DEVICE_F1155 = "f1155";
    public static final @NonNull String DEVICE_F1155_SENSORS = "f1155-sensors";
    public static final @NonNull String DEVICE_F1155_SETTINGS = "f1155-settings";
    public static final @NonNull String DEVICE_F1155_SPECIAL = "f1155-special";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VVM320 = new ThingTypeUID(BINDING_ID, DEVICE_VVM320);
    public static final ThingTypeUID THING_TYPE_VVM320_SENSORS = new ThingTypeUID(BINDING_ID, DEVICE_VVM320_SENSORS);
    public static final ThingTypeUID THING_TYPE_VVM320_SETTINGS = new ThingTypeUID(BINDING_ID, DEVICE_VVM320_SETTINGS);
    public static final ThingTypeUID THING_TYPE_VVM320_SPECIAL = new ThingTypeUID(BINDING_ID, DEVICE_VVM320_SPECIAL);
    public static final ThingTypeUID THING_TYPE_VVM310 = new ThingTypeUID(BINDING_ID, DEVICE_VVM310);
    public static final ThingTypeUID THING_TYPE_VVM310_SENSORS = new ThingTypeUID(BINDING_ID, DEVICE_VVM310_SENSORS);
    public static final ThingTypeUID THING_TYPE_VVM310_SETTINGS = new ThingTypeUID(BINDING_ID, DEVICE_VVM310_SETTINGS);
    public static final ThingTypeUID THING_TYPE_VVM310_SPECIAL = new ThingTypeUID(BINDING_ID, DEVICE_VVM310_SPECIAL);
    public static final ThingTypeUID THING_TYPE_F750 = new ThingTypeUID(BINDING_ID, DEVICE_F750);
    public static final ThingTypeUID THING_TYPE_F750_SENSORS = new ThingTypeUID(BINDING_ID, DEVICE_F750_SENSORS);
    public static final ThingTypeUID THING_TYPE_F750_SETTINGS = new ThingTypeUID(BINDING_ID, DEVICE_F750_SETTINGS);
    public static final ThingTypeUID THING_TYPE_F750_SPECIAL = new ThingTypeUID(BINDING_ID, DEVICE_F750_SPECIAL);
    public static final ThingTypeUID THING_TYPE_F1145 = new ThingTypeUID(BINDING_ID, DEVICE_F1145);
    public static final ThingTypeUID THING_TYPE_F1145_SENSORS = new ThingTypeUID(BINDING_ID, DEVICE_F1145_SENSORS);
    public static final ThingTypeUID THING_TYPE_F1145_SETTINGS = new ThingTypeUID(BINDING_ID, DEVICE_F1145_SETTINGS);
    public static final ThingTypeUID THING_TYPE_F1145_SPECIAL = new ThingTypeUID(BINDING_ID, DEVICE_F1145_SPECIAL);
    public static final ThingTypeUID THING_TYPE_F1155 = new ThingTypeUID(BINDING_ID, DEVICE_F1155);
    public static final ThingTypeUID THING_TYPE_F1155_SENSORS = new ThingTypeUID(BINDING_ID, DEVICE_F1155_SENSORS);
    public static final ThingTypeUID THING_TYPE_F1155_SETTINGS = new ThingTypeUID(BINDING_ID, DEVICE_F1155_SETTINGS);
    public static final ThingTypeUID THING_TYPE_F1155_SPECIAL = new ThingTypeUID(BINDING_ID, DEVICE_F1155_SPECIAL);

    // List of all Channel ids ==> see UplinkDataChannels

    // URLs
    public static final String LOGIN_URL = "https://www.nibeuplink.com/LogIn";
    public static final String DATA_API_URL = "https://www.nibeuplink.com/PrivateAPI/Values";

    // login field names
    public static final String LOGIN_FIELD_PASSWORD = "Password";
    public static final String LOGIN_FIELD_EMAIL = "Email";
    public static final String LOGIN_FIELD_RETURN_URL = "returnUrl";

    // other field names
    public static final String DATA_API_FIELD_LAST_DATE = "currentWebDate";
    public static final String DATA_API_FIELD_LAST_DATE_DEFAULT_VALUE = "01.01.2017 13:37:42";
    public static final String DATA_API_FIELD_ID = "hpid";
    public static final String DATA_API_FIELD_DATA = "variables";
    public static final String DATA_API_FIELD_DATA_DEFAULT_VALUE = "0";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_VVM320,
            THING_TYPE_VVM320_SENSORS, THING_TYPE_VVM320_SETTINGS, THING_TYPE_VVM320_SPECIAL, THING_TYPE_VVM310,
            THING_TYPE_VVM310_SENSORS, THING_TYPE_VVM310_SETTINGS, THING_TYPE_VVM310_SPECIAL, THING_TYPE_F750,
            THING_TYPE_F750_SENSORS, THING_TYPE_F750_SETTINGS, THING_TYPE_F750_SPECIAL, THING_TYPE_F1145,
            THING_TYPE_F1145_SENSORS, THING_TYPE_F1145_SETTINGS, THING_TYPE_F1145_SPECIAL, THING_TYPE_F1155,
            THING_TYPE_F1155_SENSORS, THING_TYPE_F1155_SETTINGS, THING_TYPE_F1155_SPECIAL);

}
