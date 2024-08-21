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
package org.openhab.binding.sonyprojector.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SonyProjectorBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Added channels and a new thing type
 */
@NonNullByDefault
public class SonyProjectorBindingConstants {

    private static final String BINDING_ID = "sonyprojector";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ETHERNET = new ThingTypeUID(BINDING_ID, "ethernetconnection");
    public static final ThingTypeUID THING_TYPE_SERIAL = new ThingTypeUID(BINDING_ID, "serialconnection");
    public static final ThingTypeUID THING_TYPE_SERIAL_OVER_IP = new ThingTypeUID(BINDING_ID, "serialoveripconnection");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_POWERSTATE = "powerstate";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_CALIBRATION_PRESET = "calibrationpreset";
    public static final String CHANNEL_CONTRAST = "contrast";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_HUE = "hue";
    public static final String CHANNEL_SHARPNESS = "sharpness";
    public static final String CHANNEL_COLOR_TEMP = "colortemperature";
    public static final String CHANNEL_IRIS_MODE = "irismode";
    public static final String CHANNEL_IRIS_MANUAL = "irismanual";
    public static final String CHANNEL_IRIS_SENSITIVITY = "irissensitivity";
    public static final String CHANNEL_LAMP_CONTROL = "lampcontrol";
    public static final String CHANNEL_FILM_PROJECTION = "filmprojection";
    public static final String CHANNEL_MOTION_ENHANCER = "motionenhancer";
    public static final String CHANNEL_CONTRAST_ENHANCER = "contrastenhancer";
    public static final String CHANNEL_FILM_MODE = "filmmode";
    public static final String CHANNEL_GAMMA_CORRECTION = "gammacorrection";
    public static final String CHANNEL_COLOR_SPACE = "colorspace";
    public static final String CHANNEL_NR = "nr";
    public static final String CHANNEL_BLOCK_NR = "blocknr";
    public static final String CHANNEL_MOSQUITO_NR = "mosquitonr";
    public static final String CHANNEL_MPEG_NR = "mpegnr";
    public static final String CHANNEL_XVCOLOR = "xvcolor";
    public static final String CHANNEL_PICTURE_MUTING = "picturemuting";
    public static final String CHANNEL_ASPECT = "aspect";
    public static final String CHANNEL_OVERSCAN = "overscan";
    public static final String CHANNEL_PICTURE_POSITION = "pictureposition";
    public static final String CHANNEL_LAMP_USE_TIME = "lampusetime";
    public static final String CHANNEL_IR_COMMAND = "ircommand";
}
