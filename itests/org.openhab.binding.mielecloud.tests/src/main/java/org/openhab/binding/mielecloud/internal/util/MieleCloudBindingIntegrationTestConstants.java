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
package org.openhab.binding.mielecloud.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link MieleCloudBindingIntegrationTestConstants} class holds common constants used in integration tests.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class MieleCloudBindingIntegrationTestConstants {
    private MieleCloudBindingIntegrationTestConstants() {
    }

    public static final String SERIAL_NUMBER = "000124430017";

    public static final String BRIDGE_ID = "genesis";

    public static final ThingUID BRIDGE_THING_UID = new ThingUID(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
            BRIDGE_ID);

    public static final ThingUID WASHING_MACHINE_THING_UID = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_WASHING_MACHINE, BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID OVEN_DEVICE_THING_UID = new ThingUID(MieleCloudBindingConstants.THING_TYPE_OVEN,
            BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID HOB_DEVICE_THING_UID = new ThingUID(MieleCloudBindingConstants.THING_TYPE_HOB,
            BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID FRIDGE_FREEZER_DEVICE_THING_UID = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_FRIDGE_FREEZER, BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID HOOD_DEVICE_THING_UID = new ThingUID(MieleCloudBindingConstants.THING_TYPE_HOOD,
            BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID COFFEE_SYSTEM_THING_UID = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_COFFEE_SYSTEM, BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID WINE_STORAGE_DEVICE_THING_UID = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_WINE_STORAGE, BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID DRYER_DEVICE_THING_UID = new ThingUID(MieleCloudBindingConstants.THING_TYPE_DRYER,
            BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID DISHWASHER_DEVICE_THING_UID = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_DISHWASHER, BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID DISH_WARMER_DEVICE_THING_UID = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_DISH_WARMER, BRIDGE_THING_UID, SERIAL_NUMBER);
    public static final ThingUID ROBOTIC_VACUUM_CLEANER_THING_UID = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_ROBOTIC_VACUUM_CLEANER, BRIDGE_THING_UID, SERIAL_NUMBER);

    public static final String MIELE_CLOUD_ACCOUNT_LABEL = "Miele Cloud Account";
    public static final String CONFIG_PARAM_REFRESH_TOKEN = "refreshToken";

    public static final String ACCESS_TOKEN = "DE_ABCDE";
    public static final String ALTERNATIVE_ACCESS_TOKEN = "DE_01234";
    public static final String REFRESH_TOKEN = "AT_12345";

    public static final String CLIENT_ID = "01234567-890a-bcde-f012-34567890abcd";
    public static final String CLIENT_SECRET = "0123456789abcdefghijklmnopqrstiu";

    public static final String AUTHORIZATION_CODE = "0123456789";

    public static final String EMAIL = "openhab@openhab.org";
}
