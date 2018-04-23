/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.networkcamera;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NetworkCameraBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NetworkCameraBindingConstants {

    public static final String BINDING_ID = "networkcamera";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_MOTIONDETECTION = new ThingTypeUID(BINDING_ID, "motiondetection");

    // List of all Channel ids
    public final static String IMAGE = "image";
    public final static String MOTION_TRIGGER = "motion-trigger";
}
