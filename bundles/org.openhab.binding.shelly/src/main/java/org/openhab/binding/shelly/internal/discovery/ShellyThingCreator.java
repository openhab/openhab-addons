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
package org.openhab.binding.shelly.internal.discovery;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.BINDING_ID;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link ShellyThingCreator} maps the device id into the thing type id
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingCreator {
    // Device Types
    public static final String SHELLYDT_1 = "SHSW-1";
    public static final String SHELLYDT_1PM = "SHSW-PM";
    public static final String SHELLYDT_1L = "SHSW-L";
    public static final String SHELLYDT_SHPLG = "SHPLG-1";
    public static final String SHELLYDT_SHPLG_S = "SHPLG-S";
    public static final String SHELLYDT_SHPLG_U1 = "SHPLG-U1";
    public static final String SHELLYDT_SHELLY2 = "SHSW-21";
    public static final String SHELLYDT_SHELLY25 = "SHSW-25";
    public static final String SHELLYDT_SHPRO = "SHSW-44";
    public static final String SHELLYDT_EM = "SHEM";
    public static final String SHELLYDT_3EM = "SHEM-3";
    public static final String SHELLYDT_HT = "SHHT-1";
    public static final String SHELLYDT_DW = "SHDW-1";
    public static final String SHELLYDT_DW2 = "SHDW-2";
    public static final String SHELLYDT_SENSE = "SHSEN-1";
    public static final String SHELLYDT_MOTION = "SHMOS-01";
    public static final String SHELLYDT_MOTION2 = "SHMOS-02";
    public static final String SHELLYDT_GAS = "SHGS-1";
    public static final String SHELLYDT_DIMMER = "SHDM-1";
    public static final String SHELLYDT_DIMMER2 = "SHDM-2";
    public static final String SHELLYDT_IX3 = "SHIX3-1";
    public static final String SHELLYDT_BULB = "SHBLB-1";
    public static final String SHELLYDT_DUO = "SHBDUO-1";
    public static final String SHELLYDT_DUORGBW = "SHCB-1";
    public static final String SHELLYDT_VINTAGE = "SHVIN-1";
    public static final String SHELLYDT_RGBW2 = "SHRGBW2";
    public static final String SHELLYDT_BUTTON1 = "SHBTN-1";
    public static final String SHELLYDT_BUTTON2 = "SHBTN-2";
    public static final String SHELLYDT_UNI = "SHUNI-1";
    public static final String SHELLYDT_TRV = "SHTRV-01";

    // Shelly Plus Series
    public static final String SHELLYDT_PLUS1 = "SNSW-001X16EU";
    public static final String SHELLYDT_PLUS1PM = "SNSW-001P16EU";
    public static final String SHELLYDT_PLUS1UL = "SNSW-001X15UL";
    public static final String SHELLYDT_PLUS1PMUL = "SNSW-001P15UL";
    public static final String SHELLYDT_PLUS2PM_RELAY = "SNSW-002P16EU-relay";
    public static final String SHELLYDT_PLUS2PM_ROLLER = "SNSW-002P16EU-roller";
    public static final String SHELLYDT_PLUS2PM_RELAY_2 = "SNSW-102P16EU-relay";
    public static final String SHELLYDT_PLUS2PM_ROLLER_2 = "SNSW-102P16EU-roller";
    public static final String SHELLYDT_PLUSPLUGS = "SNPL-00112EU";
    public static final String SHELLYDT_PLUSPLUGIT = "SNPL-00110IT";
    public static final String SHELLYDT_PLUSPLUGUK = "SNPL-00112UK";
    public static final String SHELLYDT_PLUSPLUGUS = "SNPL-00116US";
    public static final String SHELLYDT_PLUSI4 = "SNSN-0024X";
    public static final String SHELLYDT_PLUSI4DC = "SNSN-0D24X";
    public static final String SHELLYDT_PLUSHT = "SNSN-0013A";
    public static final String SHELLYDT_PLUSSMOKE = "SNSN-0031Z";

    // Shelly Pro Series
    public static final String SHELLYDT_PRO1 = "SPSW-001XE16EU";
    public static final String SHELLYDT_PRO1_2 = "SPSW-101XE16EU";
    public static final String SHELLYDT_PRO1_3 = "SPSW-201XE16EU";
    public static final String SHELLYDT_PRO1PM = "SPSW-001PE16EU";
    public static final String SHELLYDT_PRO1PM_2 = "SPSW-101PE16EU";
    public static final String SHELLYDT_PRO1PM_3 = "SPSW-201PE16EU";
    public static final String SHELLYDT_PRO2_RELAY = "SPSW-002XE16EU-relay";
    public static final String SHELLYDT_PRO2_RELAY_2 = "SPSW-102XE16EU-relay";
    public static final String SHELLYDT_PRO2_RELAY_3 = "SPSW-202XE16EU-relay";
    public static final String SHELLYDT_PRO2PM_RELAY = "SPSW-002PE16EU-relay";
    public static final String SHELLYDT_PRO2PM_ROLLER = "SPSW-002PE16EU-roller";
    public static final String SHELLYDT_PRO2PM_RELAY_2 = "SPSW-102PE16EU-relay";
    public static final String SHELLYDT_PRO2PM_ROLLER_2 = "SPSW-102PE16EU-roller";
    public static final String SHELLYDT_PRO2PM_RELAY_3 = "SPSW-202PE16EU-relay";
    public static final String SHELLYDT_PRO2PM_ROLLER_3 = "SPSW-202PE16EU-roller";
    public static final String SHELLYDT_PRO3 = "SPSW-003XE16EU";
    public static final String SHELLYDT_PRO3EM = "SPEM-003CEBEU";
    public static final String SHELLYDT_PRO4PM = "SPSW-004PE16EU";
    public static final String SHELLYDT_PRO4PM_2 = "SPSW-104PE16EU";

    // Thing names
    public static final String THING_TYPE_SHELLY1_STR = "shelly1";
    public static final String THING_TYPE_SHELLY1L_STR = "shelly1l";
    public static final String THING_TYPE_SHELLY1PM_STR = "shelly1pm";
    public static final String THING_TYPE_SHELLYEM_STR = "shellyem";
    public static final String THING_TYPE_SHELLY3EM_STR = "shellyem3"; // bad: misspelled product name, it's 3EM
    public static final String THING_TYPE_SHELLY2_PREFIX = "shellyswitch";
    public static final String THING_TYPE_SHELLY2_RELAY_STR = "shelly2-relay";
    public static final String THING_TYPE_SHELLY2_ROLLER_STR = "shelly2-roller";
    public static final String THING_TYPE_SHELLY25_PREFIX = "shellyswitch25";
    public static final String THING_TYPE_SHELLY25_RELAY_STR = "shelly25-relay";
    public static final String THING_TYPE_SHELLY25_ROLLER_STR = "shelly25-roller";
    public static final String THING_TYPE_SHELLY4PRO_STR = "shelly4pro";
    public static final String THING_TYPE_SHELLYPLUG_STR = "shellyplug";
    public static final String THING_TYPE_SHELLYPLUGS_STR = "shellyplugs";
    public static final String THING_TYPE_SHELLYPLUGU1_STR = "shellyplugu1"; // Shely Plug US
    public static final String THING_TYPE_SHELLYDIMMER_STR = "shellydimmer";
    public static final String THING_TYPE_SHELLYDIMMER2_STR = "shellydimmer2";
    public static final String THING_TYPE_SHELLYIX3_STR = "shellyix3";
    public static final String THING_TYPE_SHELLYBULB_STR = "shellybulb";
    public static final String THING_TYPE_SHELLYDUO_STR = "shellybulbduo";
    public static final String THING_TYPE_SHELLYVINTAGE_STR = "shellyvintage";
    public static final String THING_TYPE_SHELLYRGBW2_PREFIX = "shellyrgbw2";
    public static final String THING_TYPE_SHELLYRGBW2_COLOR_STR = THING_TYPE_SHELLYRGBW2_PREFIX + "-color";
    public static final String THING_TYPE_SHELLYRGBW2_WHITE_STR = THING_TYPE_SHELLYRGBW2_PREFIX + "-white";
    public static final String THING_TYPE_SHELLYDUORGBW_STR = "shellycolorbulb";
    public static final String THING_TYPE_SHELLYHT_STR = "shellyht";
    public static final String THING_TYPE_SHELLYSMOKE_STR = "shellysmoke";
    public static final String THING_TYPE_SHELLYGAS_STR = "shellygas";
    public static final String THING_TYPE_SHELLYFLOOD_STR = "shellyflood";
    public static final String THING_TYPE_SHELLYDOORWIN_STR = "shellydw";
    public static final String THING_TYPE_SHELLYDOORWIN2_STR = "shellydw2";
    public static final String THING_TYPE_SHELLYEYE_STR = "shellyseye";
    public static final String THING_TYPE_SHELLYSENSE_STR = "shellysense";
    public static final String THING_TYPE_SHELLYTRV_STR = "shellytrv";
    public static final String THING_TYPE_SHELLYMOTION_STR = "shellymotion";
    public static final String THING_TYPE_SHELLYMOTION2_STR = "shellymotion2";
    public static final String THING_TYPE_SHELLYBUTTON1_STR = "shellybutton1";
    public static final String THING_TYPE_SHELLYBUTTON2_STR = "shellybutton2";
    public static final String THING_TYPE_SHELLYUNI_STR = "shellyuni";

    // Shelly Plus Seriens
    public static final String THING_TYPE_SHELLYPLUS1_STR = "shellyplus1";
    public static final String THING_TYPE_SHELLYPLUS1PM_STR = "shellyplus1pm";
    public static final String THING_TYPE_SHELLYPLUS2PM_RELAY_STR = "shellyplus2pm-relay";
    public static final String THING_TYPE_SHELLYPLUS2PM_ROLLER_STR = "shellyplus2pm-roller";
    public static final String THING_TYPE_SHELLYPLUSI4_STR = "shellyplusi4";
    public static final String THING_TYPE_SHELLYPLUSI4DC_STR = "shellyplusi4dc";
    public static final String THING_TYPE_SHELLYPLUSHT_STR = "shellyplusht";
    public static final String THING_TYPE_SHELLYPLUSSMOKE_STR = "shellyplussmoke";
    public static final String THING_TYPE_SHELLYPLUSPLUGS_STR = "shellyplusplug";
    public static final String THING_TYPE_SHELLYPLUSPLUGUS_STR = "shellyplusplugus";

    // Shelly Pro Series
    public static final String THING_TYPE_SHELLYPRO1_STR = "shellypro1";
    public static final String THING_TYPE_SHELLYPRO1PM_STR = "shellypro1pm";
    public static final String THING_TYPE_SHELLYPRO2_RELAY_STR = "shellypro2-relay";
    public static final String THING_TYPE_SHELLYPRO2PM_RELAY_STR = "shellypro2pm-relay";
    public static final String THING_TYPE_SHELLYPRO2PM_ROLLER_STR = "shellypro2pm-roller";
    public static final String THING_TYPE_SHELLYPRO3_STR = "shellypro3";
    public static final String THING_TYPE_SHELLYPRO3EM_STR = "shellypro3em";
    public static final String THING_TYPE_SHELLYPRO4PM_STR = "shellypro4pm";

    public static final String THING_TYPE_SHELLYPROTECTED_STR = "shellydevice";
    public static final String THING_TYPE_SHELLYUNKNOWN_STR = "shellyunknown";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SHELLY1 = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLY1_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY1L = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLY1L_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY1PM = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLY1PM_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYEM = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYEM_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY3EM = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLY3EM_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY2_RELAY = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLY2_RELAY_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY2_ROLLER = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLY2_ROLLER_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY25_RELAY = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLY25_RELAY_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY25_ROLLER = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLY25_ROLLER_STR);
    public static final ThingTypeUID THING_TYPE_SHELLY4PRO = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLY4PRO_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUG = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYPLUG_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUGS = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYPLUGS_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUGU1 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUGU1_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYUNI = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYUNI_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYDIMMER = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYDIMMER_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYDIMMER2 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYDIMMER2_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYIX3 = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYIX3_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYBULB = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYBULB_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYDUO = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYDUO_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYVINTAGE = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYVINTAGE_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYDUORGBW = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYDUORGBW_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYHT = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYHT_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYSENSE = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYSENSE_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYSMOKE = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYSMOKE_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYGAS = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYGAS_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYFLOOD = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYFLOOD_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYDOORWIN = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYDOORWIN_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYDOORWIN2 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYDOORWIN2_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYTRV = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYTRV_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYBUTTON1 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYBUTTON1_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYBUTTON2 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYBUTTON2_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYEYE = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYEYE_STR);
    public static final ThingTypeUID THING_TYPE_SHELLMOTION = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYMOTION_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYRGBW2_COLOR = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYRGBW2_COLOR_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYRGBW2_WHITE = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYRGBW2_WHITE_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPROTECTED = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPROTECTED_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYUNKNOWN = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYUNKNOWN_STR);

    // Shelly Plus/Pro
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS1 = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYPLUS1_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS1PM = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUS1PM_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS2PM_RELAY = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUS2PM_RELAY_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS2PM_ROLLER = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUS2PM_ROLLER_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSI4 = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUSI4_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSI4DC = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUSI4DC_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSHT = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUSHT_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSSMOKE = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUSSMOKE_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSPLUGS = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUSPLUGS_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSPLUGUS = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPLUSPLUGUS_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO1 = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYPRO1_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO1PM = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPRO1PM_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO2_RELAY = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPRO2_RELAY_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO2PM_RELAY = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPRO2PM_RELAY_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO2PM_ROLLER = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPRO2PM_ROLLER_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO3 = new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYPRO3_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO3EM = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPRO3EM_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYPRO4PM = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPRO4PM_STR);

    private static final Map<String, String> THING_TYPE_MAPPING = new LinkedHashMap<>();
    static {
        // mapping by device type id
        THING_TYPE_MAPPING.put(SHELLYDT_1PM, THING_TYPE_SHELLY1PM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_1L, THING_TYPE_SHELLY1L_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_1, THING_TYPE_SHELLY1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_3EM, THING_TYPE_SHELLY3EM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_EM, THING_TYPE_SHELLYEM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_SHPLG_S, THING_TYPE_SHELLYPLUGS_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_SHPLG_U1, THING_TYPE_SHELLYPLUGU1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_GAS, THING_TYPE_SHELLYGAS_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_DW, THING_TYPE_SHELLYDOORWIN_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_DW2, THING_TYPE_SHELLYDOORWIN2_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_DUO, THING_TYPE_SHELLYDUO_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_DUORGBW, THING_TYPE_SHELLYDUORGBW_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_BULB, THING_TYPE_SHELLYBULB_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_VINTAGE, THING_TYPE_SHELLYVINTAGE_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_DIMMER, THING_TYPE_SHELLYDIMMER_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_DIMMER2, THING_TYPE_SHELLYDIMMER2_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_IX3, THING_TYPE_SHELLYIX3_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_BUTTON1, THING_TYPE_SHELLYBUTTON1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_BUTTON2, THING_TYPE_SHELLYBUTTON2_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_UNI, THING_TYPE_SHELLYUNI_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_HT, THING_TYPE_SHELLYHT_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_TRV, THING_TYPE_SHELLYTRV_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_MOTION, THING_TYPE_SHELLYMOTION_STR);

        // Plus Series
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS1, THING_TYPE_SHELLYPLUS1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS1PM, THING_TYPE_SHELLYPLUS1PM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS1UL, THING_TYPE_SHELLYPLUS1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS1PMUL, THING_TYPE_SHELLYPLUS1PM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS2PM_RELAY, THING_TYPE_SHELLYPLUS2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS2PM_ROLLER, THING_TYPE_SHELLYPLUS2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS2PM_RELAY_2, THING_TYPE_SHELLYPLUS2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUS2PM_ROLLER_2, THING_TYPE_SHELLYPLUS2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSPLUGS, THING_TYPE_SHELLYPLUSPLUGS_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSPLUGIT, THING_TYPE_SHELLYPLUSPLUGS_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSPLUGUK, THING_TYPE_SHELLYPLUSPLUGS_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSPLUGUS, THING_TYPE_SHELLYPLUSPLUGUS_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSI4DC, THING_TYPE_SHELLYPLUSI4DC_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSI4, THING_TYPE_SHELLYPLUSI4_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSHT, THING_TYPE_SHELLYPLUSHT_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PLUSSMOKE, THING_TYPE_SHELLYPLUSSMOKE_STR);

        // Pro Series
        THING_TYPE_MAPPING.put(SHELLYDT_PRO1, THING_TYPE_SHELLYPRO1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO1_2, THING_TYPE_SHELLYPRO1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO1_3, THING_TYPE_SHELLYPRO1_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO1PM, THING_TYPE_SHELLYPRO1PM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO1PM_2, THING_TYPE_SHELLYPRO1PM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO1PM_3, THING_TYPE_SHELLYPRO1PM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2_RELAY, THING_TYPE_SHELLYPRO2_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2_RELAY_2, THING_TYPE_SHELLYPRO2_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2_RELAY_3, THING_TYPE_SHELLYPRO2_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2PM_RELAY, THING_TYPE_SHELLYPRO2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2PM_RELAY_2, THING_TYPE_SHELLYPRO2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2PM_RELAY_3, THING_TYPE_SHELLYPRO2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2PM_ROLLER, THING_TYPE_SHELLYPRO2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2PM_ROLLER_2, THING_TYPE_SHELLYPRO2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO2PM_ROLLER_3, THING_TYPE_SHELLYPRO2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO3, THING_TYPE_SHELLYPRO3_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO3EM, THING_TYPE_SHELLYPRO3EM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO4PM, THING_TYPE_SHELLYPRO4PM_STR);
        THING_TYPE_MAPPING.put(SHELLYDT_PRO4PM_2, THING_TYPE_SHELLYPRO4PM_STR);

        // mapping by thing type
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLY1_STR, THING_TYPE_SHELLY1_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLY1PM_STR, THING_TYPE_SHELLY1PM_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLY1L_STR, THING_TYPE_SHELLY1L_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLY3EM_STR, THING_TYPE_SHELLY3EM_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYEM_STR, THING_TYPE_SHELLYEM_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLY4PRO_STR, THING_TYPE_SHELLY4PRO_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYDIMMER2_STR, THING_TYPE_SHELLYDIMMER2_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYDIMMER_STR, THING_TYPE_SHELLYDIMMER_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYIX3_STR, THING_TYPE_SHELLYIX3_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYDUORGBW_STR, THING_TYPE_SHELLYDUORGBW_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYDUO_STR, THING_TYPE_SHELLYDUO_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYVINTAGE_STR, THING_TYPE_SHELLYVINTAGE_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYBULB_STR, THING_TYPE_SHELLYBULB_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYDUO_STR, THING_TYPE_SHELLYDUO_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYHT_STR, THING_TYPE_SHELLYHT_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYSMOKE_STR, THING_TYPE_SHELLYSMOKE_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYGAS_STR, THING_TYPE_SHELLYGAS_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYFLOOD_STR, THING_TYPE_SHELLYFLOOD_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYTRV_STR, THING_TYPE_SHELLYTRV_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYDOORWIN_STR, THING_TYPE_SHELLYDOORWIN_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYDOORWIN2_STR, THING_TYPE_SHELLYDOORWIN2_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYSENSE_STR, THING_TYPE_SHELLYSENSE_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYEYE_STR, THING_TYPE_SHELLYEYE_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYBUTTON1_STR, THING_TYPE_SHELLYBUTTON1_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYBUTTON2_STR, THING_TYPE_SHELLYBUTTON2_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYUNI_STR, THING_TYPE_SHELLYUNI_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYMOTION2_STR, THING_TYPE_SHELLYMOTION_STR);

        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUS1PM_STR, THING_TYPE_SHELLYPLUS1PM_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUS1_STR, THING_TYPE_SHELLYPLUS1_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUS1PM_STR, THING_TYPE_SHELLYPLUS1PM_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUS2PM_RELAY_STR, THING_TYPE_SHELLYPLUS2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUS2PM_ROLLER_STR, THING_TYPE_SHELLYPLUS2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUS2PM_RELAY_STR, THING_TYPE_SHELLYPLUS2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUS2PM_ROLLER_STR, THING_TYPE_SHELLYPLUS2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUSPLUGS_STR, THING_TYPE_SHELLYPLUSPLUGS_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUSPLUGUS_STR, THING_TYPE_SHELLYPLUSPLUGUS_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUSI4DC_STR, THING_TYPE_SHELLYPLUSI4DC_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUSI4_STR, THING_TYPE_SHELLYPLUSI4_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUSHT_STR, THING_TYPE_SHELLYPLUSHT_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPLUSSMOKE_STR, THING_TYPE_SHELLYPLUSSMOKE_STR);

        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO1_STR, THING_TYPE_SHELLYPRO1_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO1PM_STR, THING_TYPE_SHELLYPRO1PM_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO2PM_RELAY_STR, THING_TYPE_SHELLYPRO2PM_RELAY_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO2PM_ROLLER_STR, THING_TYPE_SHELLYPRO2PM_ROLLER_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO2_RELAY_STR, THING_TYPE_SHELLYPRO2_RELAY_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO3EM_STR, THING_TYPE_SHELLYPRO3EM_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO3_STR, THING_TYPE_SHELLYPRO3_STR);
        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPRO4PM_STR, THING_TYPE_SHELLYPRO4PM_STR);

        THING_TYPE_MAPPING.put(THING_TYPE_SHELLYPROTECTED_STR, THING_TYPE_SHELLYPROTECTED_STR);
    }

    public static ThingUID getThingUID(String serviceName, String deviceType, String mode, boolean unknown) {
        String devid = substringAfterLast(serviceName, "-");
        if (devid.isEmpty()) {
            throw new IllegalArgumentException("serviceName has improper format: " + serviceName);
        }
        return new ThingUID(!unknown ? getThingTypeUID(serviceName, deviceType, mode)
                : getThingTypeUID(THING_TYPE_SHELLYPROTECTED_STR + "-" + devid, deviceType, mode), devid);
    }

    public static ThingTypeUID getThingTypeUID(String serviceName, String deviceType, String mode) {
        return new ThingTypeUID(BINDING_ID, getThingType(serviceName, deviceType, mode));
    }

    public static ThingTypeUID getUnknownTTUID() {
        return new ThingTypeUID(BINDING_ID, THING_TYPE_SHELLYPROTECTED_STR);
    }

    public static String getThingType(String hostname, String deviceType, String mode) {
        if (THING_TYPE_SHELLYPROTECTED_STR.equals(hostname)) {
            return THING_TYPE_SHELLYPROTECTED_STR;
        }
        String name = hostname.toLowerCase();
        String type = substringBefore(name, "-").toLowerCase();
        String devid = substringAfterLast(name, "-");
        if (devid.isEmpty() || type.isEmpty()) {
            throw new IllegalArgumentException("Invalid device name format: " + hostname);
        }

        // First check for special handling
        if (name.startsWith(THING_TYPE_SHELLY25_PREFIX)) { // Shelly v2.5
            return mode.equals(SHELLY_MODE_RELAY) ? THING_TYPE_SHELLY25_RELAY_STR : THING_TYPE_SHELLY25_ROLLER_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLY2_PREFIX)) { // Shelly v2
            return mode.equals(SHELLY_MODE_RELAY) ? THING_TYPE_SHELLY2_RELAY_STR : THING_TYPE_SHELLY2_ROLLER_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYPLUG_STR)) {
            // shellyplug-s needs to be mapped to shellyplugs to follow the schema
            // for the thing types: <thing type>-<mode>
            if (name.startsWith(THING_TYPE_SHELLYPLUGS_STR) || name.contains("-s")) {
                return THING_TYPE_SHELLYPLUGS_STR;
            }
            if (name.startsWith(THING_TYPE_SHELLYPLUGU1_STR)) {
                return THING_TYPE_SHELLYPLUGU1_STR;
            }
            return THING_TYPE_SHELLYPLUG_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYRGBW2_PREFIX)) {
            return mode.equals(SHELLY_MODE_COLOR) ? THING_TYPE_SHELLYRGBW2_COLOR_STR : THING_TYPE_SHELLYRGBW2_WHITE_STR;
        }
        if (name.startsWith(THING_TYPE_SHELLYMOTION_STR)) {
            // depending on firmware release the Motion advertises under shellymotion-xxx or shellymotionsensor-xxxx
            return THING_TYPE_SHELLYMOTION_STR;
        }

        // Check general mapping
        if (!deviceType.isEmpty()) {
            String res = THING_TYPE_MAPPING.get(deviceType); // by device type
            if (res != null) {
                return res;
            }

            String dt = mode.equals(SHELLY_MODE_RELAY) || mode.equals(SHELLY_MODE_ROLLER) ? deviceType + "-" + mode
                    : deviceType;
            res = THING_TYPE_MAPPING.get(dt); // <DT>-relay / <DT>-roller
            if (res != null) {
                return res;
            }
        }

        String res = THING_TYPE_MAPPING.get(type);
        if (res != null) {
            return res;
        }
        return THING_TYPE_SHELLYUNKNOWN_STR;
    }
}
