/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Map;
import java.util.regex.Pattern;

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
    private static final Pattern SHELLY_SERVICE_NAME_PATTERN = Pattern
            .compile("^([a-z0-9]*shelly[a-z0-9]*)-([a-z0-9]+)$", Pattern.CASE_INSENSITIVE);

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
    public static final String SHELLYDT_PLUS1G3 = "S3SW-001X16EU";
    public static final String SHELLYDT_PLUS1G4 = "S4SW-001X16EU";
    public static final String SHELLYDT_PLUS1PM = "SNSW-001P16EU";
    public static final String SHELLYDT_PLUS1PMG3 = "S3SW-001P16EU";
    public static final String SHELLYDT_PLUS1PMG4 = "S4SW-001P16EU";
    public static final String SHELLYDT_PLUS1UL = "SNSW-001X15UL";
    public static final String SHELLYDT_PLUS1PMUL = "SNSW-001P15UL";
    public static final String SHELLYDT_PLUS1L = "S3SW-0A1X1EUL";
    public static final String SHELLYDT_PLUS2L = "S3SW-0A2X4EUL";
    public static final String SHELLYDT_PLUS2PM = "SNSW-002P16EU";
    public static final String SHELLYDT_PLUS2PM_2 = "SNSW-102P16EU";
    public static final String SHELLYDT_PLUS2PMG3 = "S3SW-002P16EU";
    public static final String SHELLYDT_PLUS2PMG4 = "S4SW-002P16EU";
    public static final String SHELLYDT_PLUS2PMUL = "SNSW-002P15UL";
    public static final String SHELLYDT_PLUSSHUTTER = "S3SH-0A2P4EU";
    public static final String SHELLYDT_PLUSPLUGS = "SNPL-00112EU";
    public static final String SHELLYDT_PLUSPLUGS_2 = "SNPL-10112EU";
    public static final String SHELLYDT_PLUSPLUGSG3 = "S3PL-00112EU"; // Shelly Plug-S G3, Shelly AZ Plug
    public static final String SHELLYDT_PLUSPLUGOUTDOORSG3 = "S3PL-20112EU";
    public static final String SHELLYDT_PLUSPLUGIT = "SNPL-00110IT";
    public static final String SHELLYDT_PLUSPLUGUK = "SNPL-00112UK";
    public static final String SHELLYDT_PLUSPLUGUS = "SNPL-00116US";
    public static final String SHELLYDT_PLUSI4 = "SNSN-0024X";
    public static final String SHELLYDT_PLUSI4G3 = "S3SN-0024X";
    public static final String SHELLYDT_PLUSI4DC = "SNSN-0D24X";
    public static final String SHELLYDT_PLUSHT = "SNSN-0013A";
    public static final String SHELLYDT_PLUSHTG3 = "S3SN-0U12A";
    public static final String SHELLYDT_PLUSSMOKE = "SNSN-0031Z";
    public static final String SHELLYDT_PLUSUNI = "SNSN-0043X";
    public static final String SHELLYDT_PLUSDIMMERUS = "SNDM-0013US";
    public static final String SHELLYDT_PLUSDIMMER10V = "SNDM-00100WW";
    public static final String SHELLYDT_PLUSDIMMER0110VG3 = "S3DM-0010WW";
    public static final String SHELLYDT_PLUSWALLDISPLAY = "SAWD-0A1XX10EU1";
    public static final String SHELLYDT_PLUSRGBWPM = "SNDC-0D4P10WW";
    public static final String SHELLYDT_PLUSSTRIP = "S4PL-00416EU";
    public static final String SHELLYDT_PLUSEM = "S3EM-002CXCEU";
    public static final String SHELLYDT_PLUS3EM63 = "S3EM-003CXCEU63";

    // Shelly Pro Series
    public static final String SHELLYDT_PRO1 = "SPSW-001XE16EU";
    public static final String SHELLYDT_PRO1_2 = "SPSW-101XE16EU";
    public static final String SHELLYDT_PRO1_3 = "SPSW-201XE16EU";
    public static final String SHELLYDT_PRO1PM = "SPSW-001PE16EU";
    public static final String SHELLYDT_PRO1PM_2 = "SPSW-101PE16EU";
    public static final String SHELLYDT_PRO1PM_3 = "SPSW-201PE16EU";
    public static final String SHELLYDT_PRO1CB = "SPCB-01VENEU";
    public static final String SHELLYDT_PRO2 = "SPSW-002XE16EU";
    public static final String SHELLYDT_PRO2_2 = "SPSW-102XE16EU";
    public static final String SHELLYDT_PRO2_3 = "SPSW-202XE16EU";
    public static final String SHELLYDT_PRO2PM = "SPSW-002PE16EU";
    public static final String SHELLYDT_PRO2PM_2 = "SPSW-102PE16EU";
    public static final String SHELLYDT_PRO2PM_3 = "SPSW-202PE16EU";
    public static final String SHELLYDT_PRO3 = "SPSW-003XE16EU";
    public static final String SHELLYDT_PRO3EM = "SPEM-003CEBEU";
    public static final String SHELLYDT_PROEM50 = "SPEM-002CEBEU50";
    public static final String SHELLYDT_PRO4PM = "SPSW-004PE16EU";
    public static final String SHELLYDT_PRO4PM_2 = "SPSW-104PE16EU";
    public static final String SHELLYDT_4PRO = "SHPSW04P";

    // Shelly Plus Mini Series
    public static final String SHELLYDT_MINI_1 = "SNSW-001X8EU";
    public static final String SHELLYDT_MINI_1G3 = "S3SW-001X8EU";
    public static final String SHELLYDT_MINI_1G4 = "S4SW-001X8EU";
    public static final String SHELLYDT_MINI_1PM = "SNSW-001P8EU";
    public static final String SHELLYDT_MINI_1PMG3 = "S3SW-001P8EU";
    public static final String SHELLYDT_MINI_1PMG4 = "S4SW-001P8EU";
    public static final String SHELLYDT_MINI_PM = "SNPM-001PCEU16";
    public static final String SHELLYDT_MINI_PMG3 = "S3PM-001PCEU16";
    public static final String SHELLYDT_MINI_EM = "S4EM-001PXCEU16"; // PM was renamed to EM in Gen4

    // Shelly BLU Series
    public static final String SHELLYDT_BLUBUTTON = "SBBT";
    public static final String SHELLYDT_BLUDW = "SBDW";
    public static final String SHELLYDT_BLUMOTION = "SBMO";
    public static final String SHELLYDT_BLUHT = "SBHT";
    public static final String SHELLYDT_BLUGW = "SNGW-BT01";
    public static final String SHELLYDT_BLUGWG3 = "S3GW-1DBT001";

    // Thing Type ID prefixes
    public static final String THING_TYPE_SHELLYPLUS_PREFIX = "shellyplus";
    public static final String THING_TYPE_SHELLYPRO_PREFIX = "shellypro";

    // Service name prefixes
    public static final String SERVICE_NAME_SHELLYBLU_PREFIX = "shellyblu";
    public static final String SERVICE_NAME_SHELLYPLUSRANGE_PREFIX = "shellyplusrange";

    private static final String SERVICE_NAME_SHELLY2_PREFIX = "shellyswitch";
    private static final String SERVICE_NAME_SHELLY25_PREFIX = "shellyswitch25";
    private static final String SERVICE_NAME_SHELLYPLUG_PREFIX = "shellyplug";
    private static final String SERVICE_NAME_SHELLYPLUGS_PREFIX = "shellyplugs";
    private static final String SERVICE_NAME_SHELLYPLUGU1_PREFIX = "shellyplugu1"; // Shelly Plug US
    private static final String SERVICE_NAME_SHELLYRGBW2_PREFIX = "shellyrgbw2";
    private static final String SERVICE_NAME_SHELLYMOTION_PREFIX = "shellymotion";

    // Password protected or unknown device
    public static final String THING_TYPE_SHELLYPROTECTED_STR = "shellydevice";
    public static final String THING_TYPE_SHELLYUNKNOWN_STR = "shellyunknown";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SHELLY1 = new ThingTypeUID(BINDING_ID, "shelly1");
    public static final ThingTypeUID THING_TYPE_SHELLY1L = new ThingTypeUID(BINDING_ID, "shelly1l");
    public static final ThingTypeUID THING_TYPE_SHELLY1PM = new ThingTypeUID(BINDING_ID, "shelly1pm");
    public static final ThingTypeUID THING_TYPE_SHELLYEM = new ThingTypeUID(BINDING_ID, "shellyem");
    public static final ThingTypeUID THING_TYPE_SHELLY3EM = new ThingTypeUID(BINDING_ID, "shellyem3");
    public static final ThingTypeUID THING_TYPE_SHELLY2_RELAY = new ThingTypeUID(BINDING_ID, "shelly2-relay");
    public static final ThingTypeUID THING_TYPE_SHELLY2_ROLLER = new ThingTypeUID(BINDING_ID, "shelly2-roller");
    public static final ThingTypeUID THING_TYPE_SHELLY25_RELAY = new ThingTypeUID(BINDING_ID, "shelly25-relay");
    public static final ThingTypeUID THING_TYPE_SHELLY25_ROLLER = new ThingTypeUID(BINDING_ID, "shelly25-roller");
    public static final ThingTypeUID THING_TYPE_SHELLY4PRO = new ThingTypeUID(BINDING_ID, "shelly4pro");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUG = new ThingTypeUID(BINDING_ID, "shellyplug");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUGS = new ThingTypeUID(BINDING_ID, "shellyplugs");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUGU1 = new ThingTypeUID(BINDING_ID, "shellyplugu1");
    public static final ThingTypeUID THING_TYPE_SHELLYUNI = new ThingTypeUID(BINDING_ID, "shellyuni");
    public static final ThingTypeUID THING_TYPE_SHELLYDIMMER = new ThingTypeUID(BINDING_ID, "shellydimmer");
    public static final ThingTypeUID THING_TYPE_SHELLYDIMMER2 = new ThingTypeUID(BINDING_ID, "shellydimmer2");
    public static final ThingTypeUID THING_TYPE_SHELLYIX3 = new ThingTypeUID(BINDING_ID, "shellyix3");
    public static final ThingTypeUID THING_TYPE_SHELLYBULB = new ThingTypeUID(BINDING_ID, "shellybulb");
    public static final ThingTypeUID THING_TYPE_SHELLYDUO = new ThingTypeUID(BINDING_ID, "shellybulbduo");
    public static final ThingTypeUID THING_TYPE_SHELLYVINTAGE = new ThingTypeUID(BINDING_ID, "shellyvintage");
    public static final ThingTypeUID THING_TYPE_SHELLYDUORGBW = new ThingTypeUID(BINDING_ID, "shellycolorbulb");
    public static final ThingTypeUID THING_TYPE_SHELLYHT = new ThingTypeUID(BINDING_ID, "shellyht");
    public static final ThingTypeUID THING_TYPE_SHELLYSENSE = new ThingTypeUID(BINDING_ID, "shellysense");
    public static final ThingTypeUID THING_TYPE_SHELLYSMOKE = new ThingTypeUID(BINDING_ID, "shellysmoke");
    public static final ThingTypeUID THING_TYPE_SHELLYGAS = new ThingTypeUID(BINDING_ID, "shellygas");
    public static final ThingTypeUID THING_TYPE_SHELLYFLOOD = new ThingTypeUID(BINDING_ID, "shellyflood");
    public static final ThingTypeUID THING_TYPE_SHELLYDOORWIN = new ThingTypeUID(BINDING_ID, "shellydw");
    public static final ThingTypeUID THING_TYPE_SHELLYDOORWIN2 = new ThingTypeUID(BINDING_ID, "shellydw2");
    public static final ThingTypeUID THING_TYPE_SHELLYTRV = new ThingTypeUID(BINDING_ID, "shellytrv");
    public static final ThingTypeUID THING_TYPE_SHELLYBUTTON1 = new ThingTypeUID(BINDING_ID, "shellybutton1");
    public static final ThingTypeUID THING_TYPE_SHELLYBUTTON2 = new ThingTypeUID(BINDING_ID, "shellybutton2");
    public static final ThingTypeUID THING_TYPE_SHELLYEYE = new ThingTypeUID(BINDING_ID, "shellyseye");
    public static final ThingTypeUID THING_TYPE_SHELLYMOTION = new ThingTypeUID(BINDING_ID, "shellymotion");
    public static final ThingTypeUID THING_TYPE_SHELLYRGBW2_COLOR = new ThingTypeUID(BINDING_ID, "shellyrgbw2-color");
    public static final ThingTypeUID THING_TYPE_SHELLYRGBW2_WHITE = new ThingTypeUID(BINDING_ID, "shellyrgbw2-white");
    public static final ThingTypeUID THING_TYPE_SHELLYPROTECTED = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYPROTECTED_STR);
    public static final ThingTypeUID THING_TYPE_SHELLYUNKNOWN = new ThingTypeUID(BINDING_ID,
            THING_TYPE_SHELLYUNKNOWN_STR);

    // Shelly Plus
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS1 = new ThingTypeUID(BINDING_ID, "shellyplus1");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS1PM = new ThingTypeUID(BINDING_ID, "shellyplus1pm");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS1L = new ThingTypeUID(BINDING_ID, "shellyplus1l");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS2PM_RELAY = new ThingTypeUID(BINDING_ID,
            "shellyplus2pm-relay");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS2PM_ROLLER = new ThingTypeUID(BINDING_ID,
            "shellyplus2pm-roller");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS2L = new ThingTypeUID(BINDING_ID, "shellyplus2l");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSSHUTTER = new ThingTypeUID(BINDING_ID, "shellyplusshutter");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSI4 = new ThingTypeUID(BINDING_ID, "shellyplusi4");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSI4DC = new ThingTypeUID(BINDING_ID, "shellyplusi4dc");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSHT = new ThingTypeUID(BINDING_ID, "shellyplusht");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSUNI = new ThingTypeUID(BINDING_ID, "shellyplusuni");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSHTG3 = new ThingTypeUID(BINDING_ID, "shellyhtg3");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSSMOKE = new ThingTypeUID(BINDING_ID, "shellyplussmoke");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSPLUGS = new ThingTypeUID(BINDING_ID, "shellyplusplug");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSPLUGUS = new ThingTypeUID(BINDING_ID, "shellyplusplugus");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSDIMMERUS = new ThingTypeUID(BINDING_ID, "shellypluswdus");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSDIMMER10V = new ThingTypeUID(BINDING_ID, "shellyplus10v");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSRGBWPM = new ThingTypeUID(BINDING_ID, "shellyplusrgbwpm");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSSTRIP = new ThingTypeUID(BINDING_ID, "shellyplusstrip");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSEM = new ThingTypeUID(BINDING_ID, "shellyplusem");
    public static final ThingTypeUID THING_TYPE_SHELLYPLUS3EM63 = new ThingTypeUID(BINDING_ID, "shellyplus3em63");

    // Shelly Wall Display
    public static final ThingTypeUID THING_TYPE_SHELLYPLUSWALLDISPLAY = new ThingTypeUID(BINDING_ID,
            "shellywalldisplay");

    // Shelly Plus Mini Series
    public static final ThingTypeUID THING_TYPE_SHELLYMINI_1 = new ThingTypeUID(BINDING_ID, "shelly1mini");
    public static final ThingTypeUID THING_TYPE_SHELLYMINI_PM = new ThingTypeUID(BINDING_ID, "shellypmmini");
    public static final ThingTypeUID THING_TYPE_SHELLYMINI_1PM = new ThingTypeUID(BINDING_ID, "shelly1pmmini");
    public static final ThingTypeUID THING_TYPE_SHELLYMINI_EM = new ThingTypeUID(BINDING_ID, "shellyemmini");

    // Shelly Pro
    public static final ThingTypeUID THING_TYPE_SHELLYPRO1 = new ThingTypeUID(BINDING_ID, "shellypro1");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO1PM = new ThingTypeUID(BINDING_ID, "shellypro1pm");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO1CB = new ThingTypeUID(BINDING_ID, "shellypro1cb");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO2 = new ThingTypeUID(BINDING_ID, "shellypro2");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO2PM_RELAY = new ThingTypeUID(BINDING_ID, "shellypro2pm-relay");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO2PM_ROLLER = new ThingTypeUID(BINDING_ID,
            "shellypro2pm-roller");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO3 = new ThingTypeUID(BINDING_ID, "shellypro3");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO3EM = new ThingTypeUID(BINDING_ID, "shellypro3em");
    public static final ThingTypeUID THING_TYPE_SHELLYPROEM50 = new ThingTypeUID(BINDING_ID, "shellyproem50");
    public static final ThingTypeUID THING_TYPE_SHELLYPRO4PM = new ThingTypeUID(BINDING_ID, "shellypro4pm");

    // Shelly Blu series
    public static final ThingTypeUID THING_TYPE_SHELLYBLUBUTTON = new ThingTypeUID(BINDING_ID, "shellyblubutton");
    public static final ThingTypeUID THING_TYPE_SHELLYBLUDW = new ThingTypeUID(BINDING_ID, "shellybludw");
    public static final ThingTypeUID THING_TYPE_SHELLYBLUMOTION = new ThingTypeUID(BINDING_ID, "shellyblumotion");
    public static final ThingTypeUID THING_TYPE_SHELLYBLUHT = new ThingTypeUID(BINDING_ID, "shellybluht");
    public static final ThingTypeUID THING_TYPE_SHELLYBLUGW = new ThingTypeUID(BINDING_ID, "shellyblugw");

    private static final Map<String, ThingTypeUID> THING_TYPE_BY_DEVICE_TYPE = Map.ofEntries(
            Map.entry(SHELLYDT_1PM, THING_TYPE_SHELLY1PM), //
            Map.entry(SHELLYDT_1L, THING_TYPE_SHELLY1L), //
            Map.entry(SHELLYDT_1, THING_TYPE_SHELLY1), //
            Map.entry(SHELLYDT_SHPRO, THING_TYPE_SHELLY4PRO), //
            Map.entry(SHELLYDT_3EM, THING_TYPE_SHELLY3EM), //
            Map.entry(SHELLYDT_EM, THING_TYPE_SHELLYEM), //
            Map.entry(SHELLYDT_SHPLG_S, THING_TYPE_SHELLYPLUGS), //
            Map.entry(SHELLYDT_SHPLG_U1, THING_TYPE_SHELLYPLUGU1), //
            Map.entry(SHELLYDT_GAS, THING_TYPE_SHELLYGAS), //
            Map.entry(SHELLYDT_DW, THING_TYPE_SHELLYDOORWIN), //
            Map.entry(SHELLYDT_DW2, THING_TYPE_SHELLYDOORWIN2), //
            Map.entry(SHELLYDT_DUO, THING_TYPE_SHELLYDUO), //
            Map.entry(SHELLYDT_DUORGBW, THING_TYPE_SHELLYDUORGBW), //
            Map.entry(SHELLYDT_BULB, THING_TYPE_SHELLYBULB), //
            Map.entry(SHELLYDT_VINTAGE, THING_TYPE_SHELLYVINTAGE), //
            Map.entry(SHELLYDT_DIMMER, THING_TYPE_SHELLYDIMMER), //
            Map.entry(SHELLYDT_DIMMER2, THING_TYPE_SHELLYDIMMER2), //
            Map.entry(SHELLYDT_IX3, THING_TYPE_SHELLYIX3), //
            Map.entry(SHELLYDT_BUTTON1, THING_TYPE_SHELLYBUTTON1),
            Map.entry(SHELLYDT_BUTTON2, THING_TYPE_SHELLYBUTTON2), //
            Map.entry(SHELLYDT_UNI, THING_TYPE_SHELLYUNI), //
            Map.entry(SHELLYDT_HT, THING_TYPE_SHELLYHT), //
            Map.entry(SHELLYDT_TRV, THING_TYPE_SHELLYTRV), //
            Map.entry(SHELLYDT_MOTION, THING_TYPE_SHELLYMOTION),

            // Plus Series
            Map.entry(SHELLYDT_PLUS1, THING_TYPE_SHELLYPLUS1), //
            Map.entry(SHELLYDT_PLUS1G3, THING_TYPE_SHELLYPLUS1), //
            Map.entry(SHELLYDT_PLUS1G4, THING_TYPE_SHELLYPLUS1), //
            Map.entry(SHELLYDT_PLUS1UL, THING_TYPE_SHELLYPLUS1), //
            Map.entry(SHELLYDT_PLUS1PM, THING_TYPE_SHELLYPLUS1PM),
            Map.entry(SHELLYDT_PLUS1PMG3, THING_TYPE_SHELLYPLUS1PM),
            Map.entry(SHELLYDT_PLUS1PMG4, THING_TYPE_SHELLYPLUS1PM),
            Map.entry(SHELLYDT_PLUS1PMUL, THING_TYPE_SHELLYPLUS1PM),
            Map.entry(SHELLYDT_PLUS1L, THING_TYPE_SHELLYPLUS1L), //
            Map.entry(SHELLYDT_PLUS2L, THING_TYPE_SHELLYPLUS2L),
            Map.entry(SHELLYDT_PLUSSHUTTER, THING_TYPE_SHELLYPLUSSHUTTER),
            Map.entry(SHELLYDT_PLUSPLUGS, THING_TYPE_SHELLYPLUSPLUGS),
            Map.entry(SHELLYDT_PLUSPLUGS_2, THING_TYPE_SHELLYPLUSPLUGS),
            Map.entry(SHELLYDT_PLUSPLUGSG3, THING_TYPE_SHELLYPLUSPLUGS),
            Map.entry(SHELLYDT_PLUSPLUGIT, THING_TYPE_SHELLYPLUSPLUGS),
            Map.entry(SHELLYDT_PLUSPLUGOUTDOORSG3, THING_TYPE_SHELLYPLUSPLUGS),
            Map.entry(SHELLYDT_PLUSPLUGUK, THING_TYPE_SHELLYPLUSPLUGS),
            Map.entry(SHELLYDT_PLUSPLUGUS, THING_TYPE_SHELLYPLUSPLUGUS),
            Map.entry(SHELLYDT_PLUSSTRIP, THING_TYPE_SHELLYPLUSSTRIP),
            Map.entry(SHELLYDT_PLUSI4, THING_TYPE_SHELLYPLUSI4), //
            Map.entry(SHELLYDT_PLUSI4G3, THING_TYPE_SHELLYPLUSI4),
            Map.entry(SHELLYDT_PLUSI4DC, THING_TYPE_SHELLYPLUSI4DC),
            Map.entry(SHELLYDT_PLUSHT, THING_TYPE_SHELLYPLUSHT), //
            Map.entry(SHELLYDT_PLUSHTG3, THING_TYPE_SHELLYPLUSHT),
            Map.entry(SHELLYDT_PLUSSMOKE, THING_TYPE_SHELLYPLUSSMOKE),
            Map.entry(SHELLYDT_PLUSUNI, THING_TYPE_SHELLYPLUSUNI),
            Map.entry(SHELLYDT_PLUSDIMMERUS, THING_TYPE_SHELLYPLUSDIMMERUS),
            Map.entry(SHELLYDT_PLUSDIMMER10V, THING_TYPE_SHELLYPLUSDIMMER10V),
            Map.entry(SHELLYDT_PLUSDIMMER0110VG3, THING_TYPE_SHELLYPLUSDIMMER10V),
            Map.entry(SHELLYDT_PLUSRGBWPM, THING_TYPE_SHELLYPLUSRGBWPM),
            Map.entry(SHELLYDT_PLUSEM, THING_TYPE_SHELLYPLUSEM),
            Map.entry(SHELLYDT_PLUS3EM63, THING_TYPE_SHELLYPLUS3EM63),

            // Plus Mini Series
            Map.entry(SHELLYDT_MINI_1, THING_TYPE_SHELLYMINI_1), //
            Map.entry(SHELLYDT_MINI_1G3, THING_TYPE_SHELLYMINI_1),
            Map.entry(SHELLYDT_MINI_1G4, THING_TYPE_SHELLYMINI_1),
            Map.entry(SHELLYDT_MINI_PM, THING_TYPE_SHELLYMINI_PM),
            Map.entry(SHELLYDT_MINI_PMG3, THING_TYPE_SHELLYMINI_PM),
            Map.entry(SHELLYDT_MINI_EM, THING_TYPE_SHELLYMINI_PM),
            Map.entry(SHELLYDT_MINI_1PM, THING_TYPE_SHELLYMINI_1PM),
            Map.entry(SHELLYDT_MINI_1PMG3, THING_TYPE_SHELLYMINI_1PM),
            Map.entry(SHELLYDT_MINI_1PMG4, THING_TYPE_SHELLYMINI_1PM),

            // Pro Series
            Map.entry(SHELLYDT_PRO1, THING_TYPE_SHELLYPRO1), //
            Map.entry(SHELLYDT_PRO1_2, THING_TYPE_SHELLYPRO1), //
            Map.entry(SHELLYDT_PRO1_3, THING_TYPE_SHELLYPRO1), //
            Map.entry(SHELLYDT_PRO1PM, THING_TYPE_SHELLYPRO1PM), //
            Map.entry(SHELLYDT_PRO1PM_2, THING_TYPE_SHELLYPRO1PM),
            Map.entry(SHELLYDT_PRO1PM_3, THING_TYPE_SHELLYPRO1PM), //
            Map.entry(SHELLYDT_PRO1CB, THING_TYPE_SHELLYPRO1CB), //
            Map.entry(SHELLYDT_PRO2, THING_TYPE_SHELLYPRO2), //
            Map.entry(SHELLYDT_PRO2_2, THING_TYPE_SHELLYPRO2), //
            Map.entry(SHELLYDT_PRO2_3, THING_TYPE_SHELLYPRO2), //
            Map.entry(SHELLYDT_PRO3, THING_TYPE_SHELLYPRO3), //
            Map.entry(SHELLYDT_PROEM50, THING_TYPE_SHELLYPROEM50), //
            Map.entry(SHELLYDT_PRO3EM, THING_TYPE_SHELLYPRO3EM), //
            Map.entry(SHELLYDT_PRO4PM, THING_TYPE_SHELLYPRO4PM), //
            Map.entry(SHELLYDT_PRO4PM_2, THING_TYPE_SHELLYPRO4PM), //
            Map.entry(SHELLYDT_4PRO, THING_TYPE_SHELLYPRO4PM), //

            // BLU Series
            Map.entry(SHELLYDT_BLUBUTTON, THING_TYPE_SHELLYBLUBUTTON),
            Map.entry(SHELLYDT_BLUDW, THING_TYPE_SHELLYBLUDW),
            Map.entry(SHELLYDT_BLUMOTION, THING_TYPE_SHELLYBLUMOTION),
            Map.entry(SHELLYDT_BLUHT, THING_TYPE_SHELLYBLUHT), //
            Map.entry(SHELLYDT_BLUGW, THING_TYPE_SHELLYBLUGW), //
            Map.entry(SHELLYDT_BLUGWG3, THING_TYPE_SHELLYBLUGW),

            // Wall displays
            Map.entry(SHELLYDT_PLUSWALLDISPLAY, THING_TYPE_SHELLYPLUSWALLDISPLAY));

    private static final Map<String, ThingTypeUID> RELAY_THING_TYPE_BY_DEVICE_TYPE = Map.ofEntries(
            // Plus Series
            Map.entry(SHELLYDT_PLUS2PM, THING_TYPE_SHELLYPLUS2PM_RELAY),
            Map.entry(SHELLYDT_PLUS2PM_2, THING_TYPE_SHELLYPLUS2PM_RELAY),
            Map.entry(SHELLYDT_PLUS2PMG3, THING_TYPE_SHELLYPLUS2PM_RELAY),
            Map.entry(SHELLYDT_PLUS2PMG4, THING_TYPE_SHELLYPLUS2PM_RELAY),
            Map.entry(SHELLYDT_PLUS2PMUL, THING_TYPE_SHELLYPLUS2PM_RELAY),

            // Pro Series
            Map.entry(SHELLYDT_PRO2PM, THING_TYPE_SHELLYPRO2PM_RELAY),
            Map.entry(SHELLYDT_PRO2PM_2, THING_TYPE_SHELLYPRO2PM_RELAY),
            Map.entry(SHELLYDT_PRO2PM_3, THING_TYPE_SHELLYPRO2PM_RELAY));

    private static final Map<String, ThingTypeUID> ROLLER_THING_TYPE_BY_DEVICE_TYPE = Map.ofEntries(
            // Plus Series
            Map.entry(SHELLYDT_PLUS2PM, THING_TYPE_SHELLYPLUS2PM_ROLLER),
            Map.entry(SHELLYDT_PLUS2PM_2, THING_TYPE_SHELLYPLUS2PM_ROLLER),
            Map.entry(SHELLYDT_PLUS2PMUL, THING_TYPE_SHELLYPLUS2PM_ROLLER),
            Map.entry(SHELLYDT_PLUS2PMG3, THING_TYPE_SHELLYPLUS2PM_ROLLER),
            Map.entry(SHELLYDT_PLUS2PMG4, THING_TYPE_SHELLYPLUS2PM_ROLLER),

            // Pro Series
            Map.entry(SHELLYDT_PRO2PM, THING_TYPE_SHELLYPRO2PM_ROLLER),
            Map.entry(SHELLYDT_PRO2PM_2, THING_TYPE_SHELLYPRO2PM_ROLLER),
            Map.entry(SHELLYDT_PRO2PM_3, THING_TYPE_SHELLYPRO2PM_ROLLER));

    private static final Map<String, ThingTypeUID> THING_TYPE_BY_SERVICE_NAME = Map.ofEntries(
            // Shelly Gen1
            Map.entry("shelly1", THING_TYPE_SHELLY1), //
            Map.entry("shelly1pm", THING_TYPE_SHELLY1PM), //
            Map.entry("shelly1l", THING_TYPE_SHELLY1L), //
            Map.entry("shellyem3", THING_TYPE_SHELLY3EM), // Product name is 3EM
            Map.entry("shellyem", THING_TYPE_SHELLYEM), //
            Map.entry("shelly4pro", THING_TYPE_SHELLY4PRO), //
            Map.entry("shellydimmer2", THING_TYPE_SHELLYDIMMER2), //
            Map.entry("shellydimmer", THING_TYPE_SHELLYDIMMER), //
            Map.entry("shellyix3", THING_TYPE_SHELLYIX3), //
            Map.entry("shellycolorbulb", THING_TYPE_SHELLYDUORGBW),
            Map.entry("shellyvintage", THING_TYPE_SHELLYVINTAGE), //
            Map.entry("shellybulb", THING_TYPE_SHELLYBULB), //
            Map.entry("shellybulbduo", THING_TYPE_SHELLYDUO), //
            Map.entry("shellyht", THING_TYPE_SHELLYHT), //
            Map.entry("shellysmoke", THING_TYPE_SHELLYSMOKE), //
            Map.entry("shellygas", THING_TYPE_SHELLYGAS), //
            Map.entry("shellyflood", THING_TYPE_SHELLYFLOOD), //
            Map.entry("shellytrv", THING_TYPE_SHELLYTRV), //
            Map.entry("shellydw", THING_TYPE_SHELLYDOORWIN), //
            Map.entry("shellydw2", THING_TYPE_SHELLYDOORWIN2), //
            Map.entry("shellysense", THING_TYPE_SHELLYSENSE), //
            Map.entry("shellyseye", THING_TYPE_SHELLYEYE), //
            Map.entry("shellybutton1", THING_TYPE_SHELLYBUTTON1), //
            Map.entry("shellybutton2", THING_TYPE_SHELLYBUTTON2), //
            Map.entry("shellyuni", THING_TYPE_SHELLYUNI), //
            Map.entry("shellymotion2", THING_TYPE_SHELLYMOTION),

            // Shelly Plus Series
            Map.entry("shellyplus1", THING_TYPE_SHELLYPLUS1), //
            Map.entry("shelly1g3", THING_TYPE_SHELLYPLUS1), //
            Map.entry("shelly1g4", THING_TYPE_SHELLYPLUS1), //
            Map.entry("shellyplus1pm", THING_TYPE_SHELLYPLUS1PM), //
            Map.entry("shelly1pmg3", THING_TYPE_SHELLYPLUS1PM), //
            Map.entry("shelly1pmg4", THING_TYPE_SHELLYPLUS1PM), //
            Map.entry("shellyplus1l", THING_TYPE_SHELLYPLUS1L), //
            Map.entry("shellyplus2pm-relay", THING_TYPE_SHELLYPLUS2PM_RELAY),
            Map.entry("shellyplus2pm-roller", THING_TYPE_SHELLYPLUS2PM_ROLLER),
            Map.entry("shellyplusshutter", THING_TYPE_SHELLYPLUSSHUTTER),
            Map.entry("shellyplus2l", THING_TYPE_SHELLYPLUS2L), //
            Map.entry("shellyplugus", THING_TYPE_SHELLYPLUSPLUGUS),
            Map.entry("shellyplusplug", THING_TYPE_SHELLYPLUSPLUGS),
            Map.entry("shellyplusplugus", THING_TYPE_SHELLYPLUSPLUGUS),
            Map.entry("shellyplusi4", THING_TYPE_SHELLYPLUSI4), //
            Map.entry("shellyi4g3", THING_TYPE_SHELLYPLUSI4), //
            Map.entry("shellyplusi4dc", THING_TYPE_SHELLYPLUSI4DC), //
            Map.entry("shellyplusht", THING_TYPE_SHELLYPLUSHT), //
            Map.entry("shellyplussmoke", THING_TYPE_SHELLYPLUSSMOKE),
            Map.entry("shellyplusuni", THING_TYPE_SHELLYPLUSUNI),
            Map.entry("shellypluswdus", THING_TYPE_SHELLYPLUSDIMMERUS),
            Map.entry("shellyplus10v", THING_TYPE_SHELLYPLUSDIMMER10V),
            Map.entry("shellyplusrgbwpm", THING_TYPE_SHELLYPLUSRGBWPM),
            Map.entry("shellyplusstrip", THING_TYPE_SHELLYPLUSSTRIP),
            Map.entry("shellyplusem", THING_TYPE_SHELLYPLUSEM),
            Map.entry("shellyplus3em63", THING_TYPE_SHELLYPLUS3EM63),
            Map.entry("shellywalldisplay", THING_TYPE_SHELLYPLUSWALLDISPLAY),

            // Shelly Plus Mini Series
            Map.entry("shelly1mini", THING_TYPE_SHELLYMINI_1), //
            Map.entry("shelly1minig3", THING_TYPE_SHELLYMINI_1), //
            Map.entry("shelly1minig4", THING_TYPE_SHELLYMINI_1), //
            Map.entry("shellypmmini", THING_TYPE_SHELLYMINI_PM), //
            Map.entry("shellypmminig3", THING_TYPE_SHELLYMINI_PM), //
            Map.entry("shellyemmini", THING_TYPE_SHELLYMINI_EM), // // G4 device was renamed to EM
            Map.entry("shelly1pmmini", THING_TYPE_SHELLYMINI_1PM), //
            Map.entry("shelly1pmminig3", THING_TYPE_SHELLYMINI_1PM),
            Map.entry("shelly1pmminig4", THING_TYPE_SHELLYMINI_1PM),

            // Shelly Pro Series
            Map.entry("shellypro1", THING_TYPE_SHELLYPRO1), //
            Map.entry("shellypro1pm", THING_TYPE_SHELLYPRO1PM), //
            Map.entry("shellypro1cb", THING_TYPE_SHELLYPRO1CB), //
            Map.entry("shellypro2pm-relay", THING_TYPE_SHELLYPRO2PM_RELAY),
            Map.entry("shellypro2pm-roller", THING_TYPE_SHELLYPRO2PM_ROLLER),
            Map.entry("shellypro2", THING_TYPE_SHELLYPRO2), //
            Map.entry("shellyproem50", THING_TYPE_SHELLYPROEM50), //
            Map.entry("shellypro3em", THING_TYPE_SHELLYPRO3EM), //
            Map.entry("shellypro3", THING_TYPE_SHELLYPRO3), //
            Map.entry("shellypro4pm", THING_TYPE_SHELLYPRO4PM),

            // Shelly BLU Series
            Map.entry("shellyblubutton", THING_TYPE_SHELLYBLUBUTTON), //
            Map.entry("shellybludw", THING_TYPE_SHELLYBLUDW), //
            Map.entry("shellyblumotion", THING_TYPE_SHELLYBLUMOTION), //
            Map.entry("shellybluht", THING_TYPE_SHELLYBLUHT), //
            Map.entry("shellyblugw", THING_TYPE_SHELLYBLUGW), //
            Map.entry("shellyblugwg3", THING_TYPE_SHELLYBLUGW),

            // Password protected device
            Map.entry(THING_TYPE_SHELLYPROTECTED_STR, THING_TYPE_SHELLYPROTECTED));

    public static ThingUID getThingUID(String serviceName) {
        return getThingUID(serviceName, "", "");
    }

    public static ThingUID getThingUID(String serviceName, String deviceType, String mode) {
        String deviceId = getDeviceIdOrThrow(serviceName);
        return new ThingUID(getThingTypeUID(serviceName, deviceType, mode), deviceId);
    }

    public static ThingUID getThingUIDForUnknown(String serviceName, String deviceType, String mode) {
        String deviceId = getDeviceIdOrThrow(serviceName);
        return new ThingUID(getThingTypeUID(THING_TYPE_SHELLYPROTECTED_STR + "-" + deviceId, deviceType, mode),
                deviceId);
    }

    private static String getDeviceIdOrThrow(String serviceName) {
        String deviceId = substringAfterLast(serviceName, "-");
        if (deviceId.isEmpty()) {
            throw new IllegalArgumentException("Invalid serviceName format: " + serviceName);
        }
        return deviceId;
    }

    public static ThingTypeUID getThingTypeUID(String serviceName) {
        return getThingTypeUID(serviceName, "", "");
    }

    public static ThingTypeUID getThingTypeUID(String serviceName, String deviceType, String mode) {
        if (THING_TYPE_SHELLYPROTECTED_STR.equals(serviceName)) {
            return THING_TYPE_SHELLYPROTECTED;
        }
        String serviceNameLowerCase = serviceName.toLowerCase();
        String type = substringBefore(serviceNameLowerCase, "-");
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Invalid serviceName format: " + serviceName);
        }

        // First check for special handling
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLY25_PREFIX)) { // Shelly v2.5
            return getRelayOrRollerType(THING_TYPE_SHELLY25_RELAY, THING_TYPE_SHELLY25_ROLLER, mode);
        }
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLY2_PREFIX)) { // Shelly v2
            return getRelayOrRollerType(THING_TYPE_SHELLY2_RELAY, THING_TYPE_SHELLY2_ROLLER, mode);
        }
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYPLUG_PREFIX) && !serviceNameLowerCase.contains("plugus")
                && !serviceNameLowerCase.contains("plugsg3")) {
            // shellyplug-s needs to be mapped to shellyplugs to follow the schema
            // for the thing types: <thing type>-<mode>
            if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYPLUGS_PREFIX)
                    || serviceNameLowerCase.contains("-s")) {
                return THING_TYPE_SHELLYPLUGS;
            }
            if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYPLUGU1_PREFIX)) {
                return THING_TYPE_SHELLYPLUGU1;
            }
            return THING_TYPE_SHELLYPLUG;
        }
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYRGBW2_PREFIX)) {
            return SHELLY_MODE_COLOR.equals(mode) ? THING_TYPE_SHELLYRGBW2_COLOR : THING_TYPE_SHELLYRGBW2_WHITE;
        }
        if (serviceNameLowerCase.startsWith(SERVICE_NAME_SHELLYMOTION_PREFIX)) {
            // depending on firmware release the Motion advertises under shellymotion-xxx or shellymotionsensor-xxxx
            return THING_TYPE_SHELLYMOTION;
        }

        if (!deviceType.isEmpty()) {
            Map<String, ThingTypeUID> deviceTypeMap = switch (mode) {
                case SHELLY_MODE_RELAY -> RELAY_THING_TYPE_BY_DEVICE_TYPE;
                case SHELLY_MODE_ROLLER -> ROLLER_THING_TYPE_BY_DEVICE_TYPE;
                default -> THING_TYPE_BY_DEVICE_TYPE;
            };

            ThingTypeUID res = deviceTypeMap.get(deviceType);
            if (res != null) {
                return res;
            }
        }

        return THING_TYPE_BY_SERVICE_NAME.getOrDefault(type, THING_TYPE_SHELLYUNKNOWN);
    }

    private static ThingTypeUID getRelayOrRollerType(ThingTypeUID relayType, ThingTypeUID rollerType, String mode) {
        return SHELLY_MODE_RELAY.equals(mode) ? relayType : rollerType;
    }

    public static boolean isValidShellyServiceName(String serviceName) {
        return SHELLY_SERVICE_NAME_PATTERN.matcher(serviceName).matches();
    }
}
