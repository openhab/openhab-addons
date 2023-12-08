/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.salus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

import java.util.Set;

/**
 * The {@link SalusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class SalusBindingConstants {

    public static final String BINDING_ID = "salus";

    // List of all Thing Type UIDs

    public static final ThingTypeUID SALUS_DEVICE_TYPE = new ThingTypeUID(BINDING_ID, "salus-device");
    public static final ThingTypeUID SALUS_SERVER_TYPE = new ThingTypeUID(BINDING_ID, "salus-cloud-bridge");


    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(SALUS_DEVICE_TYPE, SALUS_SERVER_TYPE);


    public static class SalusCloud {
    }

    public static class SalusDevice {
        public static final String DSN = "dsn";
        public static final String PROPERTY_CACHE = "propertyCache";
    }

    public static class Channels {
        public static final String GENERIC_OUTPUT_CHANNEL = "generic-output-channel";
        public static final String GENERIC_INPUT_CHANNEL = "generic-input-channel";
        public static final String GENERIC_OUTPUT_BOOL_CHANNEL = "generic-output-bool-channel";
        public static final String GENERIC_INPUT_BOOL_CHANNEL = "generic-input-bool-channel";
        public static final String GENERIC_OUTPUT_NUMBER_CHANNEL = "generic-output-number-channel";
        public static final String GENERIC_INPUT_NUMBER_CHANNEL = "generic-input-number-channel";
        public static final String TEMPERATURE_OUTPUT_NUMBER_CHANNEL = "temperature-output-channel";
        public static final String TEMPERATURE_INPUT_NUMBER_CHANNEL = "temperature-input-channel";
        public static final Set<String> TEMPERATURE_CHANNELS = Set.of(
                "ep_9:sIT600TH:AutoCoolingSetpoint_x100",
                "ep_9:sIT600TH:AutoCoolingSetpoint_x100_a",
                "ep_9:sIT600TH:AutoHeatingSetpoint_x100",
                "ep_9:sIT600TH:AutoHeatingSetpoint_x100_a",
                "ep_9:sIT600TH:CoolingSetpoint_x100",
                "ep_9:sIT600TH:CoolingSetpoint_x100_a",
                "ep_9:sIT600TH:FloorCoolingMax_x100",
                "ep_9:sIT600TH:FloorCoolingMin_x100",
                "ep_9:sIT600TH:FloorHeatingMax_x100",
                "ep_9:sIT600TH:FloorHeatingMin_x100",
                "ep_9:sIT600TH:FrostSetpoint_x100",
                "ep_9:sIT600TH:HeatingSetpoint_x100",
                "ep_9:sIT600TH:HeatingSetpoint_x100_a",
                "ep_9:sIT600TH:LocalTemperature_x100",
                "ep_9:sIT600TH:MaxCoolSetpoint_x100",
                "ep_9:sIT600TH:MaxHeatSetpoint_x100",
                "ep_9:sIT600TH:MaxHeatSetpoint_x100_a",
                "ep_9:sIT600TH:MinCoolSetpoint_x100",
                "ep_9:sIT600TH:MinCoolSetpoint_x100_a",
                "ep_9:sIT600TH:MinHeatSetpoint_x100",
                "ep_9:sIT600TH:PipeTemperature_x100",
                "ep_9:sIT600TH:SetAutoCoolingSetpoint_x100",
                "ep_9:sIT600TH:SetAutoHeatingSetpoint_x100",
                "ep_9:sIT600TH:SetCoolingSetpoint_x100",
                "ep_9:sIT600TH:SetFloorCoolingMin_x100",
                "ep_9:sIT600TH:SetFloorHeatingMax_x100",
                "ep_9:sIT600TH:SetFloorHeatingMin_x100",
                "ep_9:sIT600TH:SetFrostSetpoint_x100",
                "ep_9:sIT600TH:SetHeatingSetpoint_x100",
                "ep_9:sIT600TH:SetMaxHeatSetpoint_x100",
                "ep_9:sIT600TH:SetMinCoolSetpoint_x100"
        );
    }
}
