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
package org.openhab.binding.tapocontrol.internal.constants;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoComConstants} class defines communication constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 ***/
@NonNullByDefault
public class TapoComConstants {
    // DEVICE JSON STRINGS
    public static final String JSON_KEY_LIGHTNING_EFFECT_OFF = "off";
    public static final String DEVICE_REPRESENTATION_PROPERTY = "macAddress";

    // List of Cloud-Commands
    public static final String CLOUD_CMD_LOGIN = "login";
    public static final String CLOUD_CMD_GETDEVICES = "getDeviceList";

    // List of Device-Control-Commands
    public static final String DEVICE_CMD_GETINFO = "get_device_info";
    public static final String DEVICE_CMD_SETINFO = "set_device_info";
    public static final String DEVICE_CMD_GETENERGY = "get_energy_usage";
    public static final String DEVICE_CMD_GETCHILDDEVICELIST = "get_child_device_list";
    public static final String DEVICE_CMD_CONTROL_CHILD = "control_child";
    public static final String DEVICE_CMD_MULTIPLE_REQ = "multipleRequest";
    public static final String DEVICE_CMD_CUSTOM = "custom_command";
    public static final String DEVICE_CMD_SET_DYNAIMCLIGHT_FX = "set_dynamic_light_effect_rule_enable";
    public static final String DEVICE_CMD_SET_LIGHT_FX = "set_lighting_effect";

    // Sets
    public static final Set<String> DEVICE_CMDLIST_QUERY = Set.of(DEVICE_CMD_GETINFO, DEVICE_CMD_GETENERGY,
            DEVICE_CMD_GETCHILDDEVICELIST);
    public static final Set<String> DEVICE_CMDLIST_SET = Set.of(DEVICE_CMD_SETINFO, DEVICE_CMD_SET_DYNAIMCLIGHT_FX,
            DEVICE_CMD_CONTROL_CHILD);

    public static final int LOGIN_RETRIES = 1;
}
