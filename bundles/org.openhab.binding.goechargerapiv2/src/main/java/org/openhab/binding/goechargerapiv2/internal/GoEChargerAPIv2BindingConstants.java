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
package org.openhab.binding.goechargerapiv2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GoEChargerAPIv2BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Reinhard Plaim - Initial contribution
 */
@NonNullByDefault
public class GoEChargerAPIv2BindingConstants {

    private static final String BINDING_ID = "goechargerapiv2";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GOEAPIV2 = new ThingTypeUID(BINDING_ID, "goeapiv2");

    // List of all Channel ids
    // writeable
    public static final String MAX_CURRENT = "maxCurrent"; // amp
    public static final String SESSION_CHARGE_CONSUMPTION_LIMIT = "sessionChargeEnergyLimit"; // dwo
    public static final String CHARGING_PHASES = "chargingPhases"; // psm
    // read only
    public static final String SESSION_CHARGE_CONSUMPTION = "sessionChargedEnergy"; // wh
    public static final String ALLOW_CHARGING = "allowCharging"; // alw
    public static final String PWM_SIGNAL = "pwmSignal"; // car
    public static final String ERROR = "error"; // err
    public static final String CABLE_ENCODING = "cableCurrent"; // cbl
    public static final String PHASES = "phases"; // pha
    public static final String TEMPERATURE1 = "temperature1"; // tma
    public static final String TEMPERATURE2 = "temperature2"; // tma
    public static final String TOTAL_CONSUMPTION = "totalChargedEnergy"; // eto
    public static final String FIRMWARE = "firmware"; // fwv
    public static final String VOLTAGE_L1 = "voltageL1";
    public static final String VOLTAGE_L2 = "voltageL2";
    public static final String VOLTAGE_L3 = "voltageL3";
    public static final String CURRENT_L1 = "currentL1";
    public static final String CURRENT_L2 = "currentL2";
    public static final String CURRENT_L3 = "currentL3";
    public static final String POWER_L1 = "powerL1";
    public static final String POWER_L2 = "powerL2";
    public static final String POWER_L3 = "powerL3";
    // api URIs
    public static final String API_URL = "http://%IP%/api/status";
    public static final String SET_URL = "http://%IP%/api/set?%KEY%=%VALUE%";
}
