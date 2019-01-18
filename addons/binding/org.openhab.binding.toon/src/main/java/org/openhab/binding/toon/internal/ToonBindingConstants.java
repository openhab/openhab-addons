/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.toon.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ToonBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jorg de Jong - Initial contribution
 */
@NonNullByDefault
public class ToonBindingConstants {

    public static final String BINDING_ID = "toon";

    // List of all Thing Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "toonapi");
    public static final ThingTypeUID MAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, "main");
    public static final ThingTypeUID PLUG_THING_TYPE = new ThingTypeUID(BINDING_ID, "plug");
    public static final ThingTypeUID SMOKE_THING_TYPE = new ThingTypeUID(BINDING_ID, "smoke");

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "Temperature";
    public static final String CHANNEL_SETPOINT = "Setpoint";
    public static final String CHANNEL_SETPOINT_MODE = "SetpointMode";
    public static final String CHANNEL_MODULATION_LEVEL = "ModulationLevel";

    public static final String CHANNEL_HEATING_SWITCH = "Heating";
    public static final String CHANNEL_TAPWATER_SWITCH = "Tapwater";
    public static final String CHANNEL_PREHEAT_SWITCH = "Preheat";

    // gasUsage
    public static final String CHANNEL_GAS_METER_READING = "GasMeterReading";

    // powerUsage
    public static final String CHANNEL_POWER_METER_READING = "PowerMeterReading";
    public static final String CHANNEL_POWER_METER_READING_LOW = "PowerMeterReadingLow";
    public static final String CHANNEL_POWER_CONSUMPTION = "PowerConsumption";

    // plug channels
    public static final String CHANNEL_SWITCH_BINARY = "SwitchBinary";

    // main unit property names
    public static final String PROPERTY_AGREEMENT_ID = "agreementId";
    public static final String PROPERTY_COMMON_NAME = "toon_displayCommonName";
    public static final String PROPERTY_ADDRESS = "toon_address";

    // plug property names
    public static final String PROPERTY_DEV_UUID = "devUUID";
    public static final String PROPERTY_DEV_TYPE = "devType";

    // List of all supported physical devices and modules
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(MAIN_THING_TYPE, SMOKE_THING_TYPE, PLUG_THING_TYPE).collect(Collectors.toSet()));

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(MAIN_THING_TYPE, SMOKE_THING_TYPE, PLUG_THING_TYPE, APIBRIDGE_THING_TYPE).collect(Collectors.toSet()));
}
