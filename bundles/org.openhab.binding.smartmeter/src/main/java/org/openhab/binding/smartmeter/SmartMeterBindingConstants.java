/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.smartmeter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartmeter.internal.ObisCode;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SmlReaderBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@NonNullByDefault
public class SmartMeterBindingConstants {

    public static final String BINDING_ID = "smartmeter";
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SMLREADER = new ThingTypeUID(BINDING_ID, "meter");
    public static final String CONFIGURATION_PORT = "port";
    public static final String CONFIGURATION_SERIAL_MODE = "mode";
    public static final String CONFIGURATION_BAUDRATE = "baudrate";
    public static final String CONFIGURATION_CONFORMITY = "conformity";
    public static final String CONFIGURATION_INIT_MESSAGE = "initMessage";
    public static final String CONFIGURATION_CONVERSION = "conversionRatio";
    public static final String CONFIGURATION_CHANNEL_NEGATE = "negate";
    public static final String CHANNEL_PROPERTY_OBIS = "obis";
    public static final String OBIS_PATTERN_CHANNELID = getObisChannelIdPattern(ObisCode.OBIS_PATTERN);
    /** Obis format */
    public static final String OBIS_FORMAT_MINIMAL = "%d-%d:%d.%d.%d";
    /** Obis format */
    public static final String OBIS_FORMAT = OBIS_FORMAT_MINIMAL + "*%d";
    public static final String CHANNEL_TYPE_METERREADER_OBIS = "channel-type:" + BINDING_ID + ":obis";

    public static String getObisChannelIdPattern(String obis) {
        return obis.replaceAll("\\.", "-").replaceAll(":|\\*", "_");
    }

    public static String getObisChannelId(String obis) {
        return getObisChannelIdPattern(obis).replaceAll("[^\\w-]", "");
    }
}
