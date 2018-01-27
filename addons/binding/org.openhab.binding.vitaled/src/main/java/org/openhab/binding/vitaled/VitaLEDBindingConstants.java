/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.vitaled;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VitaLEDBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Salein - Initial contribution
 */
@NonNullByDefault
public class VitaLEDBindingConstants {

    public static final String BINDING_ID = "vitaled";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_VITA_LED = new ThingTypeUID(BINDING_ID, "vitaled");

    // List of thing parameters names
    public final static String HOST_PARAMETER = "ipAddress";
    public final static String TCP_PORT_PARAMETER = "port";
    public final static String REFRESH_INTERVAL = "refreshInterval";
    public final static String ZONE = "zone";

    public final static String ACHROMATIC_LIGHT = "achromaticLight";
    public final static String INTENSITY = "intensity";

    public final static String RED = "red";
    public final static String GREEN = "green";
    public final static String BLUE = "blue";
    public final static String WHITE = "white";

    public final static String COLOUR_SATURATION = "colourSaturation";
    public final static String SPEED = "speed";
    public final static String COLOUR_GRADIENT_INTENSITY = "colourGradientIntensity";

    public final static String X_COORDINATE = "xCoord";
    public final static String Y_COORDINATE = "yCoord";

    public final static String SCENE1 = "scene1";
    public final static String SCENE2 = "scene2";
    public final static String SCENE3 = "scene3";
    public final static String SCENE4 = "scene4";
    public final static String SCENE5 = "scene5";
    public final static String SCENE6 = "scene6";

    public final static String ACTIVE_MODE = "activeMode";

    public final static String CHROMATIC_REFERER = "erweitert/farblicht-d.html";
    public final static String ACHROMATIC_REFERER = "erweitert/weisslicht-d.html";
    public final static String COLOUR_GRADIENTS_REFERER = "erweitert/farbverlauf-d.html";
    public final static String COLOUR_TRIANGLE_REFERER = "erweitert/farbdreieck-d.html";
    public final static String SCENE_REFERER = "user-d.html";

}
