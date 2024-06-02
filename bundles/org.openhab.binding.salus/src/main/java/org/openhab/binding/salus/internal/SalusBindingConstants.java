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
package org.openhab.binding.salus.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Martin Grześlowski - Initial contribution
 */
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
    public static final ThingTypeUID SALUS_IT600_DEVICE_TYPE = new ThingTypeUID(BINDING_ID, "salus-it600-device");
    public static final ThingTypeUID SALUS_SERVER_TYPE = new ThingTypeUID(BINDING_ID, "salus-cloud-bridge");
    public static final ThingTypeUID SALUS_AWS_TYPE = new ThingTypeUID(BINDING_ID, "salus-aws-bridge");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(SALUS_DEVICE_TYPE,
            SALUS_IT600_DEVICE_TYPE, SALUS_SERVER_TYPE, SALUS_AWS_TYPE);

    public static class SalusCloud {
        public static final String DEFAULT_URL = "https://eu.salusconnect.io";
    }

    public static class SalusDevice {
        public static final String DSN = "dsn";
        public static final String OEM_MODEL = "oem_model";
        public static final String IT_600 = "it600";
    }

    public static class It600Device {
        public static class HoldType {
            public static final int AUTO = 0;
            public static final int MANUAL = 2;
            public static final int TEMPORARY_MANUAL = 1;
            public static final int OFF = 7;
        }
    }

    public static class Channels {
        public static class It600 {
            public static final String TEMPERATURE = "temperature";
            public static final String EXPECTED_TEMPERATURE = "expected-temperature";
            public static final String WORK_TYPE = "work-type";
        }

        public static final String GENERIC_OUTPUT_CHANNEL = "generic-output-channel";
        public static final String GENERIC_INPUT_CHANNEL = "generic-input-channel";
        public static final String GENERIC_OUTPUT_BOOL_CHANNEL = "generic-output-bool-channel";
        public static final String GENERIC_INPUT_BOOL_CHANNEL = "generic-input-bool-channel";
        public static final String GENERIC_OUTPUT_NUMBER_CHANNEL = "generic-output-number-channel";
        public static final String GENERIC_INPUT_NUMBER_CHANNEL = "generic-input-number-channel";
        public static final String TEMPERATURE_OUTPUT_NUMBER_CHANNEL = "temperature-output-channel";
        public static final String TEMPERATURE_INPUT_NUMBER_CHANNEL = "temperature-input-channel";
        public static final Set<String> TEMPERATURE_CHANNELS = Set.of("ep_9:sIT600TH:AutoCoolingSetpoint_x100",
                "ep_9:sIT600TH:AutoCoolingSetpoint_x100_a", "ep_9:sIT600TH:AutoHeatingSetpoint_x100",
                "ep_9:sIT600TH:AutoHeatingSetpoint_x100_a", "ep_9:sIT600TH:CoolingSetpoint_x100",
                "ep_9:sIT600TH:CoolingSetpoint_x100_a", "ep_9:sIT600TH:FloorCoolingMax_x100",
                "ep_9:sIT600TH:FloorCoolingMin_x100", "ep_9:sIT600TH:FloorHeatingMax_x100",
                "ep_9:sIT600TH:FloorHeatingMin_x100", "ep_9:sIT600TH:FrostSetpoint_x100",
                "ep_9:sIT600TH:HeatingSetpoint_x100", "ep_9:sIT600TH:HeatingSetpoint_x100_a",
                "ep_9:sIT600TH:LocalTemperature_x100", "ep_9:sIT600TH:MaxCoolSetpoint_x100",
                "ep_9:sIT600TH:MaxHeatSetpoint_x100", "ep_9:sIT600TH:MaxHeatSetpoint_x100_a",
                "ep_9:sIT600TH:MinCoolSetpoint_x100", "ep_9:sIT600TH:MinCoolSetpoint_x100_a",
                "ep_9:sIT600TH:MinHeatSetpoint_x100", "ep_9:sIT600TH:PipeTemperature_x100",
                "ep_9:sIT600TH:SetAutoCoolingSetpoint_x100", "ep_9:sIT600TH:SetAutoHeatingSetpoint_x100",
                "ep_9:sIT600TH:SetCoolingSetpoint_x100", "ep_9:sIT600TH:SetFloorCoolingMin_x100",
                "ep_9:sIT600TH:SetFloorHeatingMax_x100", "ep_9:sIT600TH:SetFloorHeatingMin_x100",
                "ep_9:sIT600TH:SetFrostSetpoint_x100", "ep_9:sIT600TH:SetHeatingSetpoint_x100",
                "ep_9:sIT600TH:SetMaxHeatSetpoint_x100", "ep_9:sIT600TH:SetMinCoolSetpoint_x100");
    }
}
