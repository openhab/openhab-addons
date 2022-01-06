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
package org.openhab.binding.lcn.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LcnBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnBindingConstants {
    /** The scope name of this binding */
    public static final String BINDING_ID = "lcn";
    /**
     * Firmware version of the measurement processing since 2013. It has more variables and thresholds and event-based
     * variable updates.
     */
    public static final int FIRMWARE_2013 = 0x170206;
    /** Firmware version which supports controlling all 4 outputs simultaneously */
    public static final int FIRMWARE_2014 = 0x180501;
    /** List of all Thing Type UIDs */
    public static final ThingTypeUID THING_TYPE_PCK_GATEWAY = new ThingTypeUID(BINDING_ID, "pckGateway");
    public static final ThingTypeUID THING_TYPE_MODULE = new ThingTypeUID(BINDING_ID, "module");
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, "group");
    /** Regex for address in PCK protocol */
    public static final String ADDRESS_REGEX = "[:=%]M(?<segId>\\d{3})(?<modId>\\d{3})";
    public static final Pattern MEASUREMENT_PATTERN_BEFORE_2013 = Pattern
            .compile(LcnBindingConstants.ADDRESS_REGEX + "\\.(?<value>\\d{5})");
    /** LCN coding for ACK */
    public static final int CODE_ACK = -1;
}
