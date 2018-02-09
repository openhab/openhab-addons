/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link GardenaBinding} class defines common constants, which are used across the whole binding.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaBindingConstants {

    public static final String BINDING_ID = "gardena";

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    public static final String PROPERTY_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_PRODUCT = "product";
    public static final String PROPERTY_SERIALNUMBER = "serial_number";
    public static final String PROPERTY_SGTIN = "sgtin";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_CATEGORY = "category";

    public static final String PROPERTY_CONNECTION_STATUS = "connection_status";
    public static final String PROPERTY_CONNECTION_STATUS_UNREACH_VALUE = "status_device_unreachable";
    public static final String PROPERTY_STATE = "state";

    public static final String ABILITY_DEVICE_INFO = "device_info";
    public static final String ABILITY_RADIO = "radio";

    public static final String SETTING_LEAKAGE_DETECTION = "leakage_detection";
    public static final String SETTING_OPERATION_MODE = "operating_mode";
    public static final String SETTING_TURN_ON_PRESSURE = "turn_on_pressure";
}
