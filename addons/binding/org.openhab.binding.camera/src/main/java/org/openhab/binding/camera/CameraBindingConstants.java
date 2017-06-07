/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.camera;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link CameraBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Hartwig - Initial contribution
 */
public class CameraBindingConstants {

    public static final String BINDING_ID = "camera";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "camera");

    // List of all Channel ids
    public final static String CHANNEL_IMAGE = "image";
    public static int CONFIGURATION_POLLTIME_S = 10;

}
