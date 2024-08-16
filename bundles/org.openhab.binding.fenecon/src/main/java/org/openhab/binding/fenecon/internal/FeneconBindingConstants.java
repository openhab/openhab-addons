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
package org.openhab.binding.fenecon.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FeneconBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class FeneconBindingConstants {

    private static final String BINDING_ID = "fenecon";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HOME_DEVICE = new ThingTypeUID(BINDING_ID, "home-device");

    // List of all FENECON Addresses
    public static final String STATE_ADDRESS = "_sum/State";
    public static final String ESS_SOC_ADDRESS = "_sum/EssSoc";
    public static final String CONSUMPTION_ACTIVE_POWER_ADDRESS = "_sum/ConsumptionActivePower";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE1_ADDRESS = "_sum/ConsumptionActivePowerL1";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE2_ADDRESS = "_sum/ConsumptionActivePowerL2";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE3_ADDRESS = "_sum/ConsumptionActivePowerL3";
    public static final String CONSUMPTION_MAX_ACTIVE_POWER_ADDRESS = "_sum/ConsumptionMaxActivePower";
    public static final String PRODUCTION_MAX_ACTIVE_POWER_ADDRESS = "_sum/ProductionMaxActivePower";
    public static final String PRODUCTION_ACTIVE_POWER_ADDRESS = "_sum/ProductionActivePower";
    public static final String GRID_ACTIVE_POWER_ADDRESS = "_sum/GridActivePower";
    public static final String ESS_DISCHARGE_POWER_ADDRESS = "_sum/EssDischargePower";
    public static final String GRID_MODE_ADDRESS = "_sum/GridMode";
    public static final String GRID_SELL_ACTIVE_ENERGY_ADDRESS = "_sum/GridSellActiveEnergy";
    public static final String GRID_BUY_ACTIVE_ENERGY_ADDRESS = "_sum/GridBuyActiveEnergy";
    // Group of all FENECON Addresses
    public static final List<String> ADDRESSES = List.of(STATE_ADDRESS, GRID_MODE_ADDRESS,
            CONSUMPTION_ACTIVE_POWER_ADDRESS, CONSUMPTION_ACTIVE_POWER_PHASE1_ADDRESS,
            CONSUMPTION_ACTIVE_POWER_PHASE2_ADDRESS, CONSUMPTION_ACTIVE_POWER_PHASE3_ADDRESS,
            CONSUMPTION_MAX_ACTIVE_POWER_ADDRESS, PRODUCTION_MAX_ACTIVE_POWER_ADDRESS, PRODUCTION_ACTIVE_POWER_ADDRESS,
            GRID_ACTIVE_POWER_ADDRESS, GRID_BUY_ACTIVE_ENERGY_ADDRESS, GRID_SELL_ACTIVE_ENERGY_ADDRESS, ESS_SOC_ADDRESS,
            ESS_DISCHARGE_POWER_ADDRESS);

    // List of all Channel IDs
    public static final String STATE_CHANNEL = "state";
    public static final String ESS_SOC_CHANNEL = "ess-soc";
    public static final String CONSUMPTION_ACTIVE_POWER_CHANNEL = "consumption-active-power";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE1_CHANNEL = "consumption-active-power-l1";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE2_CHANNEL = "consumption-active-power-l2";
    public static final String CONSUMPTION_ACTIVE_POWER_PHASE3_CHANNEL = "consumption-active-power-l3";
    public static final String CONSUMPTION_MAX_ACTIVE_POWER_CHANNEL = "consumption-max-active-power";
    public static final String PRODUCTION_MAX_ACTIVE_POWER_CHANNEL = "production-max-active-power";
    public static final String PRODUCTION_ACTIVE_POWER_CHANNEL = "production-active-power";
    public static final String EXPORT_TO_GRID_POWER_CHANNEL = "export-to-grid-power";
    public static final String IMPORT_FROM_GRID_POWER_CHANNEL = "import-from-grid-power";
    public static final String ESS_CHARGER_POWER_CHANNEL = "charger-power";
    public static final String ESS_DISCHARGER_POWER_CHANNEL = "discharger-power";
    public static final String EMERGENCY_POWER_MODE_CHANNEL = "emergency-power-mode";
    public static final String EXPORTED_TO_GRID_ENERGY_CHANNEL = "exported-to-grid-energy";
    public static final String IMPORTED_FROM_GRID_ENERGY_CHANNEL = "imported-from-grid-energy";
    public static final String LAST_UPDATE_CHANNEL = "last-update";
}
