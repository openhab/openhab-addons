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
package org.openhab.binding.mqtt.generic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MqttBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttBindingConstants {

    public static final String BINDING_ID = "mqtt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID GENERIC_MQTT_THING = new ThingTypeUID(BINDING_ID, "topic");

    // Generic thing channel types
    public static final String COLOR_RGB = "colorRGB";
    public static final String COLOR_HSB = "colorHSB";
    public static final String COLOR = "color";
    public static final String CONTACT = "contact";
    public static final String DIMMER = "dimmer";
    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String SWITCH = "switch";
    public static final String IMAGE = "image";
    public static final String LOCATION = "location";
    public static final String DATETIME = "datetime";
    public static final String ROLLERSHUTTER = "rollershutter";
    public static final String TRIGGER = "trigger";
}
