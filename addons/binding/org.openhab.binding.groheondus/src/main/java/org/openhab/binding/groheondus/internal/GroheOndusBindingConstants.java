/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.groheondus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusBindingConstants {

    private static final String BINDING_ID = "groheondus";

    public static final ThingTypeUID THING_TYPE_BRIDGE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_SENSEGUARD = new ThingTypeUID(BINDING_ID, "senseguard");
    public static final ThingTypeUID THING_TYPE_SENSE = new ThingTypeUID(BINDING_ID, "sense");

    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_TEMPERATURE_GUARD = "temperature_guard";
    public static final String CHANNEL_VALVE_OPEN = "valve_open";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_BATTERY = "battery";
}
