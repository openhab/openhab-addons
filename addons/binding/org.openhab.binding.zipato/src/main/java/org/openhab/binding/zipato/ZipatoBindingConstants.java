/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zipato;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ZipatoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Hartwig - Initial contribution
 */
public class ZipatoBindingConstants {

    public static final String BINDING_ID = "zipato";

    // List of all Thing Type UIDs
    public final static ThingTypeUID ZIPATO_THING_CAMERA_UID = new ThingTypeUID(BINDING_ID, "camera");
    public final static ThingTypeUID ZIPATO_THING_SWITCH_UID = new ThingTypeUID(BINDING_ID, "switch");
    public final static ThingTypeUID ZIPATO_THING_SENSOR_UID = new ThingTypeUID(BINDING_ID, "sensor");
    public final static ThingTypeUID ZIPATO_CONTROLLER_UID = new ThingTypeUID(BINDING_ID, "zipabox");
    public final static String ZIPATO_THING_SWITCH = BINDING_ID + ":switch";
    public final static String ZIPATO_THING_SENSOR = BINDING_ID + ":sensor";
    public final static String ZIPATO_THING_CAMERA = BINDING_ID + ":camera";
    public final static String ZIPATO_CONTROLLER = BINDING_ID + ":zipabox";
    public final static String ZIPATO_THING_CHANNEL_SENSOR_BINARY = "binary";
    public final static String ZIPATO_THING_CHANNEL_SENSOR = "sensor";
    public final static String ZIPATO_THING_CHANNEL_SWITCH = "switch";
    public final static String ZIPATO_THING_CHANNEL_CAMERA = "camera";

    // List of all Channel ids
    public final static String CHANNEL_SENSOR_DEVICE = "sensor";
    public final static String CHANNEL_SWITCH_DEVICE = "switch";
    public final static String CHANNEL_CAMERA_DEVICE = "switch";
    public static int CONFIGURATION_POLLTIME_S = 5;
    public static int CONFIGURATION_CONNECT_TIME_S = 5;
}
