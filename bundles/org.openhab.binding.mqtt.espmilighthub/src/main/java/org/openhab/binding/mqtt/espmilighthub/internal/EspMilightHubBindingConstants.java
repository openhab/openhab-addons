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

package org.openhab.binding.mqtt.espmilighthub.internal;

import static org.openhab.binding.mqtt.MqttBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EspMilightHubBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class EspMilightHubBindingConstants {
    public static final String STATES_BASE_TOPIC = "milight/states/";
    public static final String COMMANDS_BASE_TOPIC = "milight/commands/";
    public static final BigDecimal BIG_DECIMAL_100 = new BigDecimal(100);
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RGB_CCT = new ThingTypeUID(BINDING_ID, "rgb_cct");
    public static final ThingTypeUID THING_TYPE_CCT = new ThingTypeUID(BINDING_ID, "cct");
    public static final ThingTypeUID THING_TYPE_RGBW = new ThingTypeUID(BINDING_ID, "rgbw");
    public static final ThingTypeUID THING_TYPE_RGB = new ThingTypeUID(BINDING_ID, "rgb");
    public static final ThingTypeUID THING_TYPE_FUT089 = new ThingTypeUID(BINDING_ID, "fut089");
    public static final ThingTypeUID THING_TYPE_FUT091 = new ThingTypeUID(BINDING_ID, "fut091");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_RGBW, THING_TYPE_RGB_CCT,
            THING_TYPE_FUT089, THING_TYPE_FUT091, THING_TYPE_CCT, THING_TYPE_RGB);

    // Channels
    public static final String CHANNEL_LEVEL = "level";
    public static final String CHANNEL_COLOUR = "colour";
    public static final String CHANNEL_COLOURTEMP = "colourTemperature";
    public static final String CHANNEL_DISCO_MODE = "discoMode";
    public static final String CHANNEL_BULB_MODE = "bulbMode";
    public static final String CHANNEL_COMMAND = "command";
}
