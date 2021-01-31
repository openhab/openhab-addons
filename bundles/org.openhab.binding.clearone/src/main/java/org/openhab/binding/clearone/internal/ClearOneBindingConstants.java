/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.clearone.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ClearOneBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Garry Mitchell - Initial contribution
 */
@NonNullByDefault
public class ClearOneBindingConstants {

    private static final String BINDING_ID = "clearone";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_STACK = new ThingTypeUID(BINDING_ID, "stack");

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_STACK).collect(Collectors.toSet()));

    public static final ThingTypeUID THING_TYPE_UNIT = new ThingTypeUID(BINDING_ID, "unit");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_STACK, THING_TYPE_UNIT, THING_TYPE_ZONE).collect(Collectors.toSet()));

    // Unit Type IDs
    public static final String XAP_UNIT_TYPE_XAP800 = "5";
    public static final String XAP_UNIT_TYPE_XAP400 = "7";

    // List of all Stack Channel ids
    public static final String BRIDGE_RESET = "bridge_reset";

    // List of all Unit Channel ids
    public static final String MACRO = "macro";
    public static final String PRESET = "preset";

    // List of all Zone Channel ids
    public static final String SOURCE = "source";
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";

    // List of all XAP Commands (not all are supported by this binding)
    public static final String XAP_CMD_AAMB = "AAMB";
    public static final String XAP_CMD_AEC = "AEC";
    public static final String XAP_CMD_AGC = "AGC";
    public static final String XAP_CMD_AGCSET = "AGCSET";
    public static final String XAP_CMD_AMBLVL = "AMBLVL";
    public static final String XAP_CMD_BAUD = "BAUD";
    public static final String XAP_CMD_CGROUP = "CGROUP";
    public static final String XAP_CMD_CHAIRO = "CHAIRO";
    public static final String XAP_CMD_COMPRESS = "COMPRESS";
    public static final String XAP_CMD_COMPSEL = "COMPSEL";
    public static final String XAP_CMD_DECAY = "DECAY";
    public static final String XAP_CMD_DELAYSEL = "DELAYSEL";
    public static final String XAP_CMD_DFLTM = "DFLTM";
    public static final String XAP_CMD_DID = "DID";
    public static final String XAP_CMD_DSPVER = "DSPVER";
    public static final String XAP_CMD_FILTER = "FILTER";
    public static final String XAP_CMD_FILTSEL = "FILTSER";
    public static final String XAP_CMD_FLOW = "FLOW";
    public static final String XAP_CMD_FMP = "FMP";
    public static final String XAP_CMD_FPP = "FPP";
    public static final String XAP_CMD_GAIN = "GAIN";
    public static final String XAP_CMD_GATE = "GATE";
    public static final String XAP_CMD_GHOLD = "GHOLD";
    public static final String XAP_CMD_GOVER = "GOVER";
    public static final String XAP_CMD_GRATIO = "GRATIO";
    public static final String XAP_CMD_GREPORT = "GREPORT";
    public static final String XAP_CMD_LABEL = "LABEL";
    public static final String XAP_CMD_LFP = "LFP";
    public static final String XAP_CMD_LOCK = "LOCK";
    public static final String XAP_CMD_LOCKPRST = "LOCKPRST";
    public static final String XAP_CMD_LOCKPWD = "LOCKPWD";
    public static final String XAP_CMD_LMO = "LMO";
    public static final String XAP_CMD_LVL = "LVL";
    public static final String XAP_CMD_LVLREPORT = "LVLREPORT";
    public static final String XAP_CMD_MACRO = "MACRO";
    public static final String XAP_CMD_MASTER = "MASTER";
    public static final String XAP_CMD_MAX = "MAX";
    public static final String XAP_CMD_MDMODE = "MDMODE";
    public static final String XAP_CMD_MIN = "MIN";
    public static final String XAP_CMD_MINIT = "MINIT";
    public static final String XAP_CMD_MINMAX = "MINMAX";
    public static final String XAP_CMD_MLINE = "MLINE";
    public static final String XAP_CMD_MMAX = "MMAX";
    public static final String XAP_CMD_MPASS = "MPASS";
    public static final String XAP_CMD_MTRX = "MTRX";
    public static final String XAP_CMD_MTRXLVL = "MTRXLVL";
    public static final String XAP_CMD_MUTE = "MUTE";
    public static final String XAP_CMD_NCD = "NCD";
    public static final String XAP_CMD_NCSEL = "NCSEL";
    public static final String XAP_CMD_NLP = "NLP";
    public static final String XAP_CMD_NOM = "NOM";
    public static final String XAP_CMD_OFFA = "OFFA";
    public static final String XAP_CMD_PAA = "PAA";
    public static final String XAP_CMD_PP = "PP";
    public static final String XAP_CMD_PRESET = "PRESET";
    public static final String XAP_CMD_PRGSTRING = "PRGSTRING";
    public static final String XAP_CMD_RAMP = "RAMP";
    public static final String XAP_CMD_REFSET = "REFSET";
    public static final String XAP_CMD_SERECHO = "SERECHO";
    public static final String XAP_CMD_SERMODE = "SERMODE";
    public static final String XAP_CMD_SFTYMUTE = "SFTYMUTE";
    public static final String XAP_CMD_SIGGEN = "SIGGEN";
    public static final String XAP_CMD_SIGTOUT = "SIGTOUT";
    public static final String XAP_CMD_SLVL = "SLVL";
    public static final String XAP_CMD_STRING = "STRING";
    public static final String XAP_CMD_TOUT = "TOUT";
    public static final String XAP_CMD_UID = "UID";
    public static final String XAP_CMD_VER = "VER";

    public static final int XAP_MTRX_MODE_CROSSPOINT_OFF = 0;
    public static final int XAP_MTRX_MODE_CROSSPOINT_ON = 1;
    public static final int XAP_MTRX_MODE_CROSSPOINT_TOGGLE = 2;
    public static final int XAP_MTRX_MODE_NON_GATED = 3;
    public static final int XAP_MTRX_MODE_GATED = 4;

    public static final String XAP_SOURCE_NONE = "NONE";
}
