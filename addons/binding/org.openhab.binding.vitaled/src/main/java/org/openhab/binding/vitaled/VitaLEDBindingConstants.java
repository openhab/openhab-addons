/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	/**
	 * Binding ID
	 */
	public static final String BINDING_ID = "vitaled";

	/**
	 * ThingTypeUID
	 */
	public static final ThingTypeUID THING_TYPE_VITA_LED = new ThingTypeUID(BINDING_ID, "vitaled");

	/**
	 * Host parameter ipAddress
	 */
	public static final String HOST_PARAMETER = "ipAddress";

	/**
	 * Host parameter port
	 */
	public static final String PORT_PARAMETER = "port";

	/**
	 * Refresh interval parameter
	 */
	public static final String REFRESH_INTERVAL_PARAMETER = "refreshInterval";

	public static final String ZONE = "zone";

	public static final String ACHROMATIC_LIGHT = "achromaticLight";
	public static final String INTENSITY = "intensity";

	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String BLUE = "blue";
	public static final String WHITE = "white";
	public static final String COLOR = "color";

	public static final String COLOUR_SATURATION = "colourSaturation";
	public static final String SPEED = "speed";
	public static final String COLOUR_GRADIENT_INTENSITY = "colourGradientIntensity";

	public static final String X_COORDINATE = "xCoord";
	public static final String Y_COORDINATE = "yCoord";

	public static final String SCENE1 = "scene1";
	public static final String SCENE2 = "scene2";
	public static final String SCENE3 = "scene3";
	public static final String SCENE4 = "scene4";
	public static final String SCENE5 = "scene5";
	public static final String SCENE6 = "scene6";

	public static final String ACTIVE_MODE = "activeMode";

	public static final String CHROMATIC_REFERER = "erweitert/farblicht-d.html";
	public static final String ACHROMATIC_REFERER = "erweitert/weisslicht-d.html";
	public static final String COLOUR_GRADIENTS_REFERER = "erweitert/farbverlauf-d.html";
	public static final String COLOUR_TRIANGLE_REFERER = "erweitert/farbdreieck-d.html";
	public static final String SCENE_REFERER = "user-d.html";

}
