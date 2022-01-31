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
package org.openhab.binding.gardena.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GardenaBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class GardenaBindingConstants {
    public static final String BINDING_ID = "gardena";

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    public static final String PROPERTY_SERIALNUMBER = "serial";
    public static final String PROPERTY_MODELTYPE = "modelType";

    public static final String CONNECTION_STATUS_ONLINE = "ONLINE";

    public static final String DEVICE_TYPE_MOWER = "mower";
    public static final String DEVICE_TYPE_PUMP = "pump";
    public static final String DEVICE_TYPE_IRRIGATION_CONTROL = "irrigation_control";
    public static final String DEVICE_TYPE_WATER_CONTROL = "water_control";
    public static final String DEVICE_TYPE_SENSOR = "sensor";
    public static final String DEVICE_TYPE_POWER = "power";
}
