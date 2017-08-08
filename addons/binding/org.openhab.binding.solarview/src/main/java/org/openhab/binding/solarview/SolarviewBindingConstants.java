/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SolarviewBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Guenther Schreiner - Initial contribution
 */
public class SolarviewBindingConstants {

    /** Basis identification */
    private static final String BINDING_ID = "solarview";

    // List of all Strings
    public static final String BRIDGE_TYPE = "solarviewServer";

    public static final String THING_ENERGY_PRODUCTION = "energyProduction";
    public static final String THING_ENERGY_INJECTION = "energyInjection";
    public static final String THING_ENERGY_IMPORT = "energyImport";

    public static final String THING_PRODUCTION_INVERTER_ONE = "productionInverterOne";
    public static final String THING_PRODUCTION_INVERTER_TWO = "productionInverterTwo";
    public static final String THING_PRODUCTION_INVERTER_THREE = "productionInverterTHree";
    public static final String THING_PRODUCTION_INVERTER_FOUR = "productionInverterFour";
    public static final String THING_PRODUCTION_INVERTER_FIVE = "productionInverterFive";
    public static final String THING_PRODUCTION_INVERTER_SIX = "productionInverterSix";
    public static final String THING_PRODUCTION_INVERTER_SEVEN = "productionInverterSeven";
    public static final String THING_PRODUCTION_INVERTER_EIGHT = "productionInverterEight";
    public static final String THING_PRODUCTION_INVERTER_NINE = "productionInverterNine";

    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE);

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_PRODUCTION = new ThingTypeUID(BINDING_ID,
            THING_ENERGY_PRODUCTION);
    public static final ThingTypeUID THING_TYPE_ENERGY_INJECTION = new ThingTypeUID(BINDING_ID, THING_ENERGY_INJECTION);
    public static final ThingTypeUID THING_TYPE_ENERGY_IMPORT = new ThingTypeUID(BINDING_ID, THING_ENERGY_IMPORT);

    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_ONE = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_ONE);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_TWO = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_TWO);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_THREE = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_THREE);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_FOUR = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_FOUR);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_FIVE = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_FIVE);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_SIX = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_SIX);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_SEVEN = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_SEVEN);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_EIGHT = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_EIGHT);
    public static final ThingTypeUID THING_TYPE_PRODUCTION_INVERTER_NINE = new ThingTypeUID(BINDING_ID,
            THING_PRODUCTION_INVERTER_NINE);

    // List of all Channel ids
    public static final String CHANNEL_KT0 = "KT0";
    public static final String CHANNEL_KYR = "KYR";
    public static final String CHANNEL_KMT = "KMT";
    public static final String CHANNEL_KDY = "KDY";
    public static final String CHANNEL_PAC = "PAC";
    public static final String CHANNEL_UDC = "UDC";
    public static final String CHANNEL_IDC = "IDC";
    public static final String CHANNEL_UDCB = "UDCB";
    public static final String CHANNEL_IDCB = "IDCB";
    public static final String CHANNEL_UDCC = "UDCC";
    public static final String CHANNEL_IDCC = "IDCC";
    public static final String CHANNEL_UL1 = "UL1";
    public static final String CHANNEL_IL1 = "IL1";
    public static final String CHANNEL_TKK = "TKK";

}
/**
 * end-of-SolarviewBindingConstants.java
 */
