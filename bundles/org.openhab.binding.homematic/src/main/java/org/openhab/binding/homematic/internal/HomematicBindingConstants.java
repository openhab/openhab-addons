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
package org.openhab.binding.homematic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the binding.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class HomematicBindingConstants {

    public static final String BINDING_ID = "homematic";
    public static final String GATEWAY_POOL_NAME = "homematicGateway";
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:homematic:config";

    /**
     * A thing's config-description-uri is generally composed as follows:<br>
     * {@link #CONFIG_DESCRIPTION_URI_THING_PREFIX}:{@link ThingTypeUID}
     */
    public static final String CONFIG_DESCRIPTION_URI_THING_PREFIX = "thing-type";

    public static final String PROPERTY_VENDOR_NAME = "eQ-3 AG";

    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_ROLLERSHUTTER = "Rollershutter";
    public static final String ITEM_TYPE_CONTACT = "Contact";
    public static final String ITEM_TYPE_STRING = "String";
    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_DIMMER = "Dimmer";
    public static final String ITEM_TYPE_DATETIME = "DateTime";

    public static final String CHANNEL_TYPE_DUTY_CYCLE_RATIO = "DUTY_CYCLE_RATIO";

    public static final String CATEGORY_BATTERY = "Battery";
    public static final String CATEGORY_ALARM = "Alarm";
    public static final String CATEGORY_HUMIDITY = "Humidity";
    public static final String CATEGORY_TEMPERATURE = "Temperature";
    public static final String CATEGORY_MOTION = "Motion";
    public static final String CATEGORY_PRESSURE = "Pressure";
    public static final String CATEGORY_SMOKE = "Smoke";
    public static final String CATEGORY_WATER = "Water";
    public static final String CATEGORY_WIND = "Wind";
    public static final String CATEGORY_RAIN = "Rain";
    public static final String CATEGORY_ENERGY = "Energy";
    public static final String CATEGORY_BLINDS = "Blinds";
    public static final String CATEGORY_CONTACT = "Contact";
    public static final String CATEGORY_SWITCH = "Switch";

    public static final String PROPERTY_BATTERY_TYPE = "batteryType";
    public static final String PROPERTY_AES_KEY = "aesKey";
    public static final String PROPERTY_DYNAMIC_FUNCTION_FORMAT = "dynamicFunction-%d";

    public static final int INSTALL_MODE_NORMAL = 1;

    public static final int CONFIGURATION_CHANNEL_NUMBER = -1;

    public static final String RX_BURST_MODE = "BURST";
    public static final String RX_WAKEUP_MODE = "WAKEUP";
}
