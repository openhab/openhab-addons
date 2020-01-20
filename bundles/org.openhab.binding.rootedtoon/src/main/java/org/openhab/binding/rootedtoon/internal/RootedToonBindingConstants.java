/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.rootedtoon.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RootedToonBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daan Meijer - Initial contribution
 */
@NonNullByDefault
public class RootedToonBindingConstants {

    private static final String BINDING_ID = "rootedtoon";
    public static final ThingTypeUID THING_TYPE_TOON = new ThingTypeUID("rootedtoon", "toon");
    public static final String CHANNEL_TEMPERATURE = "Temperature";
    public static final String CHANNEL_SETPOINT = "Setpoint";
    public static final String CHANNEL_SETPOINT_MODE = "SetpointMode";
    public static final String CHANNEL_MODULATION_LEVEL = "ModulationLevel";
    public static final String CHANNEL_PROGRAM_ENABLED = "ProgramEnabled";
    public static final String CHANNEL_NEXT_SETPOINT = "NextSetpoint";
    public static final String CHANNEL_NEXT_SETPOINT_TIME = "NextSetpointTime";
    public static final String CHANNEL_BOILER_SETPOINT = "BoilerSetpoint";
    public static final String CHANNEL_HEATING_SWITCH = "Heating";
    public static final String CHANNEL_TAPWATER_SWITCH = "Tapwater";
    public static final String CHANNEL_PREHEAT_SWITCH = "Preheat";
    public static final String CHANNEL_GAS_METER_READING = "GasMeterReading";
    public static final String CHANNEL_GAS_CONSUMPTION = "GasConsumption";
    public static final String CHANNEL_POWER_METER_READING = "PowerMeterReading";
    public static final String CHANNEL_POWER_METER_READING_LOW = "PowerMeterReadingLow";
    public static final String CHANNEL_POWER_CONSUMPTION = "PowerConsumption";
    public static final String CHANNEL_SWITCH_BINARY = "SwitchBinary";
    public static final String PROPERTY_AGREEMENT_ID = "agreementId";
    public static final String PROPERTY_COMMON_NAME = "toon_displayCommonName";
    public static final String PROPERTY_ADDRESS = "toon_address";
    public static final String PROPERTY_DEV_UUID = "devUUID";
    public static final String PROPERTY_DEV_TYPE = "devType";

}
