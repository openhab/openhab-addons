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
package org.openhab.binding.ocpp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OcppBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppBindingConstants {

    public static final String BINDING_ID = "ocpp";

    // Thing Type UIDs — three tiers mirroring the OCPP topology:
    // server (the JSON WebSocket endpoint) > chargepoint (one charger) > connector (one outlet).
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_CHARGEPOINT = new ThingTypeUID(BINDING_ID, "chargepoint");
    public static final ThingTypeUID THING_TYPE_CONNECTOR = new ThingTypeUID(BINDING_ID, "connector");

    // Chargepoint channels
    public static final String CHANNEL_CONNECTED = "connected";
    public static final String CHANNEL_LAST_SEEN = "last-seen";
    // Reset addresses the charge point, not a single outlet, so it lives on the chargepoint.
    public static final String CHANNEL_RESET = "reset";

    // Connector channels — control
    public static final String CHANNEL_STATUS = "charge-point-status";
    public static final String CHANNEL_CABLE_CONNECTED = "cable-connected";
    public static final String CHANNEL_CHARGING = "charging";
    public static final String CHANNEL_CHARGE_LIMIT = "charge-limit";
    public static final String CHANNEL_POWER_LIMIT = "power-limit";
    public static final String CHANNEL_NUMBER_PHASES = "number-phases";
    public static final String CHANNEL_PAUSE = "pause";
    public static final String CHANNEL_AVAILABILITY = "availability";
    public static final String CHANNEL_UNLOCK = "unlock";
    public static final String CHANNEL_HARDWARE_MAX_CURRENT = "hardware-max-current";

    // Connector channels — metering (fixed per-phase layout: explicit L1/L2/L3)
    public static final String CHANNEL_CURRENT_L1 = "current-import-l1";
    public static final String CHANNEL_CURRENT_L2 = "current-import-l2";
    public static final String CHANNEL_CURRENT_L3 = "current-import-l3";
    public static final String CHANNEL_VOLTAGE_L1 = "voltage-l1";
    public static final String CHANNEL_VOLTAGE_L2 = "voltage-l2";
    public static final String CHANNEL_VOLTAGE_L3 = "voltage-l3";
    public static final String CHANNEL_CURRENT_OFFERED = "current-offered";
    public static final String CHANNEL_POWER_ACTIVE_IMPORT = "power-active-import";
    public static final String CHANNEL_POWER_OFFERED = "power-offered";
    public static final String CHANNEL_ENERGY_ACTIVE_IMPORT = "energy-active-import";

    // Additional metering channels — full OCPP measurand set
    public static final String CHANNEL_CURRENT_IMPORT = "current-import";
    public static final String CHANNEL_CURRENT_EXPORT = "current-export";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_FREQUENCY = "frequency";
    public static final String CHANNEL_POWER_ACTIVE_EXPORT = "power-active-export";
    public static final String CHANNEL_POWER_REACTIVE_IMPORT = "power-reactive-import";
    public static final String CHANNEL_POWER_REACTIVE_EXPORT = "power-reactive-export";
    public static final String CHANNEL_POWER_FACTOR = "power-factor";
    public static final String CHANNEL_ENERGY_ACTIVE_EXPORT = "energy-active-export";
    public static final String CHANNEL_ENERGY_ACTIVE_IMPORT_INTERVAL = "energy-active-import-interval";
    public static final String CHANNEL_ENERGY_ACTIVE_EXPORT_INTERVAL = "energy-active-export-interval";
    public static final String CHANNEL_ENERGY_REACTIVE_IMPORT = "energy-reactive-import";
    public static final String CHANNEL_ENERGY_REACTIVE_EXPORT = "energy-reactive-export";
    public static final String CHANNEL_ENERGY_REACTIVE_IMPORT_INTERVAL = "energy-reactive-import-interval";
    public static final String CHANNEL_ENERGY_REACTIVE_EXPORT_INTERVAL = "energy-reactive-export-interval";
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_RPM = "rpm";
    public static final String CHANNEL_TEMPERATURE = "temperature";

    // Transaction metadata channels
    public static final String CHANNEL_ID_TAG = "id-tag";
    public static final String CHANNEL_TRANSACTION_ID = "transaction-id";
    public static final String CHANNEL_METER_START = "meter-start";
    public static final String CHANNEL_METER_STOP = "meter-stop";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_TIMESTAMP_START = "timestamp-start";
    public static final String CHANNEL_TIMESTAMP_STOP = "timestamp-stop";

    // Config parameter names (must match thing-types.xml)
    public static final String CONFIG_CHARGE_POINT_ID = "chargePointId";
    public static final String CONFIG_CONNECTOR_ID = "connectorId";

    /**
     * Representation property of a connector. A connector id alone repeats across chargers, and the
     * framework matches a representation property on thing type plus value without considering the
     * bridge, so it has to carry the charge point id to stay unique.
     */
    public static final String PROPERTY_UNIQUE_ID = "uniqueId";

    public static String uniqueConnectorId(String chargePointId, int connectorId) {
        return chargePointId + ":" + connectorId;
    }
}
