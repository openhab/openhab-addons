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
package org.openhab.binding.eltako.internal.misc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EltakoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Wenske - Initial contribution
 */
@NonNullByDefault
public class EltakoBindingConstants {

    // binding
    public static final String BINDING_ID = "eltako";

    // bridge
    public final static ThingTypeUID THING_TYPE_FAM14 = new ThingTypeUID(BINDING_ID, "FAM14");
    public final static ThingTypeUID THING_TYPE_FGW14 = new ThingTypeUID(BINDING_ID, "FGW14");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DSZ14DRS = new ThingTypeUID(BINDING_ID, "DSZ14DRS");
    public static final ThingTypeUID THING_TYPE_DSZ14WDRS = new ThingTypeUID(BINDING_ID, "DSZ14WDRS");
    public static final ThingTypeUID THING_TYPE_F2L14 = new ThingTypeUID(BINDING_ID, "F2L14");
    public static final ThingTypeUID THING_TYPE_BGW14 = new ThingTypeUID(BINDING_ID, "BGW14");
    public static final ThingTypeUID THING_TYPE_F3Z14D = new ThingTypeUID(BINDING_ID, "F3Z14D");
    public static final ThingTypeUID THING_TYPE_F4HK14 = new ThingTypeUID(BINDING_ID, "F4HK14");
    public static final ThingTypeUID THING_TYPE_FAE14LPR = new ThingTypeUID(BINDING_ID, "FAE14LPR");
    public static final ThingTypeUID THING_TYPE_FAE14SSR = new ThingTypeUID(BINDING_ID, "FAE14SSR");
    public static final ThingTypeUID THING_TYPE_FDG14_BROADCAST = new ThingTypeUID(BINDING_ID, "FDG14-Broadcast");
    public static final ThingTypeUID THING_TYPE_FDG14 = new ThingTypeUID(BINDING_ID, "FDG14");
    public static final ThingTypeUID THING_TYPE_FFR14 = new ThingTypeUID(BINDING_ID, "FFR14");
    public static final ThingTypeUID THING_TYPE_FGSM14 = new ThingTypeUID(BINDING_ID, "FGSM14");
    public static final ThingTypeUID THING_TYPE_FHK14 = new ThingTypeUID(BINDING_ID, "FHK14");
    public static final ThingTypeUID THING_TYPE_FMS14 = new ThingTypeUID(BINDING_ID, "FMS14");
    public static final ThingTypeUID THING_TYPE_FMSR14 = new ThingTypeUID(BINDING_ID, "FMSR14");
    public static final ThingTypeUID THING_TYPE_FMZ14 = new ThingTypeUID(BINDING_ID, "FMZ14");
    public static final ThingTypeUID THING_TYPE_FPLG14 = new ThingTypeUID(BINDING_ID, "FPLG14");
    public static final ThingTypeUID THING_TYPE_FPLT14 = new ThingTypeUID(BINDING_ID, "FPLT14");
    public static final ThingTypeUID THING_TYPE_FSB14 = new ThingTypeUID(BINDING_ID, "FSB14");
    public static final ThingTypeUID THING_TYPE_FSDG14 = new ThingTypeUID(BINDING_ID, "FSDG14");
    public static final ThingTypeUID THING_TYPE_FSG14_1_10V = new ThingTypeUID(BINDING_ID, "FSG14-1-10V");
    public static final ThingTypeUID THING_TYPE_FSR14_2X = new ThingTypeUID(BINDING_ID, "FSR14-2x");
    public static final ThingTypeUID THING_TYPE_FSR14_4X = new ThingTypeUID(BINDING_ID, "FSR14-4x");
    public static final ThingTypeUID THING_TYPE_FSR14SSR = new ThingTypeUID(BINDING_ID, "FSR14SSR");
    public static final ThingTypeUID THING_TYPE_FSU14 = new ThingTypeUID(BINDING_ID, "FSU14");
    public static final ThingTypeUID THING_TYPE_FTD14 = new ThingTypeUID(BINDING_ID, "FTD14");
    public static final ThingTypeUID THING_TYPE_FTN14 = new ThingTypeUID(BINDING_ID, "FTN14");
    public static final ThingTypeUID THING_TYPE_FTS14GBZ = new ThingTypeUID(BINDING_ID, "FTS14GBZ");
    public static final ThingTypeUID THING_TYPE_FTS14KS = new ThingTypeUID(BINDING_ID, "FTS14KS");
    public static final ThingTypeUID THING_TYPE_FTS14TG = new ThingTypeUID(BINDING_ID, "FTS14TG");
    public static final ThingTypeUID THING_TYPE_FUD14_800W = new ThingTypeUID(BINDING_ID, "FUD14-800W");
    public static final ThingTypeUID THING_TYPE_FUD14 = new ThingTypeUID(BINDING_ID, "FUD14");
    public static final ThingTypeUID THING_TYPE_FWG14MS = new ThingTypeUID(BINDING_ID, "FWG14MS");
    public static final ThingTypeUID THING_TYPE_FWZ14_65A = new ThingTypeUID(BINDING_ID, "FWZ14-65A");
    public static final ThingTypeUID THING_TYPE_FZK14 = new ThingTypeUID(BINDING_ID, "FZK14");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(
            THING_TYPE_DSZ14DRS, THING_TYPE_DSZ14WDRS, THING_TYPE_F2L14, THING_TYPE_BGW14, THING_TYPE_F3Z14D,
            THING_TYPE_F4HK14, THING_TYPE_FAE14LPR, THING_TYPE_FAE14SSR, THING_TYPE_FDG14_BROADCAST, THING_TYPE_FDG14,
            THING_TYPE_FFR14, THING_TYPE_FGSM14, THING_TYPE_FHK14, THING_TYPE_FMS14, THING_TYPE_FMSR14,
            THING_TYPE_FMZ14, THING_TYPE_FPLG14, THING_TYPE_FPLT14, THING_TYPE_FSB14, THING_TYPE_FSDG14,
            THING_TYPE_FSG14_1_10V, THING_TYPE_FSR14_2X, THING_TYPE_FSR14_4X, THING_TYPE_FSR14SSR, THING_TYPE_FSU14,
            THING_TYPE_FTD14, THING_TYPE_FTN14, THING_TYPE_FTS14GBZ, THING_TYPE_FTS14KS, THING_TYPE_FTS14TG,
            THING_TYPE_FUD14_800W, THING_TYPE_FUD14, THING_TYPE_FWG14MS, THING_TYPE_FWZ14_65A, THING_TYPE_FZK14));

    // List of all FUD14 channel IDs
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_SPEED = "speed";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_BLOCKING = "blocking";

    // List of all FSB14 channel IDs
    public static final String CHANNEL_RUNTIME = "runtime";
    public static final String CHANNEL_CONTROL = "control";

    // List of all configuration parameters
    public static final String GENERIC_HARDWARE_VERSION = "HardwareVersion";
    public static final String GENERIC_DEVICE_ID = "DeviceId";

    // Bridge config properties
    public static final String SERIALCOMPORT = "SerialComPort";
    public static final String FAM14_HARDWARE_VERSION = "Version";

    // Bridge config properties
}
