/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.honeywellwifithermostat;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link honeywellWifiThermostatBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author JD Steffen - Initial contribution
 */
public class honeywellWifiThermostatBindingConstants {

    public static final String BINDING_ID = "honeywellwifithermostat";

    // List of all Thing Type UIDs
    public final static ThingTypeUID HONEYWIFI_THING = new ThingTypeUID(BINDING_ID, "honeythermo");

    // List of all Channel ids
    public final static String SYSTEM_MODE = "sysMode";
    public final static String CURRENT_TEMPERATURE = "currentTemp";
    public final static String HEAT_SETPOINT = "heatSP";
    public final static String COOL_SETPOINT = "coolSP";
    public final static String FAN_MODE = "fanMode";

}
