/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolaxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxBindingConstants {

    private static final String BINDING_ID = "solax";
    private static final String THING_LOCAL_CONNECT_INVERTER_ID = "local-connect-inverter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LOCAL_CONNECT_INVERTER = new ThingTypeUID(BINDING_ID,
            THING_LOCAL_CONNECT_INVERTER_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_LOCAL_CONNECT_INVERTER);

    // List of properties
    public static final String PROPERTY_INVERTER_TYPE = "inverterType";

    // List of all Channel ids
    public static final String INVERTER_OUTPUT_POWER = "inverter-output-power";
    public static final String INVERTER_OUTPUT_CURRENT = "inverter-current";
    public static final String INVERTER_OUTPUT_VOLTAGE = "inverter-voltage";
    public static final String INVERTER_OUTPUT_FREQUENCY = "inverter-frequency";

    public static final String INVERTER_PV1_POWER = "pv1-power";
    public static final String INVERTER_PV1_VOLTAGE = "pv1-voltage";
    public static final String INVERTER_PV1_CURRENT = "pv1-current";

    public static final String INVERTER_PV2_POWER = "pv2-power";
    public static final String INVERTER_PV2_VOLTAGE = "pv2-voltage";
    public static final String INVERTER_PV2_CURRENT = "pv2-current";

    public static final String INVERTER_PV_TOTAL_POWER = "pv-total-power";
    public static final String INVERTER_PV_TOTAL_CURRENT = "pv-total-current";

    public static final String BATTERY_POWER = "battery-power";
    public static final String BATTERY_VOLTAGE = "battery-voltage";
    public static final String BATTERY_CURRENT = "battery-current";
    public static final String BATTERY_TEMPERATURE = "battery-temperature";
    public static final String BATTERY_STATE_OF_CHARGE = "battery-level";

    public static final String FEED_IN_POWER = "feed-in-power";

    public static final String TIMESTAMP = "last-update-time";
    public static final String RAW_DATA = "raw-data";

    // I18N Keys
    protected static final String I18N_KEY_OFFLINE_COMMUNICATION_ERROR_JSON_CANNOT_BE_RETRIEVED = "@text/offline.communication-error.json-cannot-be-retrieved";
}
