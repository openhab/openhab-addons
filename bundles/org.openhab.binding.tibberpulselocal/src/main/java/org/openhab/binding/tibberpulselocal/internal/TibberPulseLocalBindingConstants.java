/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tibberpulselocal.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TibberPulseLocalBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class TibberPulseLocalBindingConstants {

    public static final String BINDING_ID = "tibberpulselocal";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter");

    // Channel ids. The value channel ids are also the keys of SmlReading.values().
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_ENERGY_IMPORT = "energy-import";
    public static final String CHANNEL_ENERGY_EXPORT = "energy-export";
    public static final String CHANNEL_POWER_L1 = "power-l1";
    public static final String CHANNEL_POWER_L2 = "power-l2";
    public static final String CHANNEL_POWER_L3 = "power-l3";
    public static final String CHANNEL_VOLTAGE_L1 = "voltage-l1";
    public static final String CHANNEL_VOLTAGE_L2 = "voltage-l2";
    public static final String CHANNEL_VOLTAGE_L3 = "voltage-l3";
    public static final String CHANNEL_CURRENT_L1 = "current-l1";
    public static final String CHANNEL_CURRENT_L2 = "current-l2";
    public static final String CHANNEL_CURRENT_L3 = "current-l3";
    public static final String CHANNEL_FREQUENCY = "frequency";
    public static final String CHANNEL_LAST_UPDATE = "last-update";

    /** The energy counters keep their last value when the meter goes stale or the connection is lost. */
    public static final Set<String> COUNTER_CHANNELS = Set.of(CHANNEL_ENERGY_IMPORT, CHANNEL_ENERGY_EXPORT);

    /** The live value channels: every value channel except the energy counters. They go UNDEF when stale. */
    public static final Set<String> LIVE_CHANNELS = Set.of(CHANNEL_POWER, CHANNEL_POWER_L1, CHANNEL_POWER_L2,
            CHANNEL_POWER_L3, CHANNEL_VOLTAGE_L1, CHANNEL_VOLTAGE_L2, CHANNEL_VOLTAGE_L3, CHANNEL_CURRENT_L1,
            CHANNEL_CURRENT_L2, CHANNEL_CURRENT_L3, CHANNEL_FREQUENCY);

    // Thing property names
    public static final String PROPERTY_DEVICE_ID = "deviceId";
    public static final String PROPERTY_METER_ID = "meterId";
    /** The host label of the bridge, for example {@code bridge01234567}; set by bridge discovery. */
    public static final String PROPERTY_HOST_LABEL = "hostLabel";

    // Configuration parameter names
    public static final String CONFIG_DEVICE_ID = "deviceId";
    public static final String CONFIG_HOSTNAME = "hostname";

    // Status descriptions. The keys are resolved against the default translation file of the binding.
    // Descriptions of configuration errors name the parameter in the key and need no argument.
    public static final String TEXT_INVALID_HOSTNAME = "@text/offline.conf-error.invalid-hostname";
    public static final String TEXT_EMPTY_PASSWORD = "@text/offline.conf-error.empty-password";
    public static final String TEXT_INVALID_DEVICE_ID = "@text/offline.conf-error.invalid-device-id";
    public static final String TEXT_INVALID_STALE_TIMEOUT = "@text/offline.conf-error.invalid-stale-timeout";
    public static final String TEXT_NO_BRIDGE = "@text/offline.conf-error.no-bridge";
    public static final String TEXT_PASSWORD_REJECTED = "@text/offline.conf-error.password-rejected";
    public static final String TEXT_NO_STREAM = "@text/offline.comm-error.no-stream";
    /** Takes the HTTP status as its only argument. */
    public static final String TEXT_HTTP_STATUS = "@text/offline.comm-error.http-status";
    public static final String TEXT_UNREACHABLE = "@text/offline.comm-error.unreachable";
    public static final String TEXT_CLOSED_BY_BRIDGE = "@text/offline.comm-error.closed-by-bridge";
    public static final String TEXT_SILENT = "@text/offline.comm-error.silent";
    public static final String TEXT_INVALID_DATA = "@text/offline.comm-error.invalid-data";
    /** Takes the stale timeout in seconds as its only argument. */
    public static final String TEXT_NO_READING = "@text/offline.comm-error.no-reading";

    private TibberPulseLocalBindingConstants() {
    }
}
