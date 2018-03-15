/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.facerecognition;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FaceRecognitionBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Philipp Meisberger - Initial contribution
 */
@NonNullByDefault
public class FaceRecognitionBindingConstants {

    private static final String BINDING_ID = "facerecognition";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FACE = new ThingTypeUID(BINDING_ID, "facerecognition");

    // List of all Channel ids
    public static final String CHANNEL_IMAGE = "live#image";

    public static final String CHANNEL_ACCESS_GRANTED_IMAGE = "accessGranted#image";
    public static final String CHANNEL_ACCESS_GRANTED_USERNAME = "accessGranted#username";
    public static final String CHANNEL_ACCESS_GRANTED_SCORE = "accessGranted#score";
    public static final String CHANNEL_ACCESS_GRANTED_TIME = "accessGranted#time";

    public static final String CHANNEL_ACCESS_DENIED_IMAGE = "accessDenied#image";
    public static final String CHANNEL_ACCESS_DENIED_USERNAME = "accessDenied#username";
    public static final String CHANNEL_ACCESS_DENIED_SCORE = "accessDenied#score";
    public static final String CHANNEL_ACCESS_DENIED_TIME = "accessDenied#time";

    // Unknown user constants
    public static final int UNKNOWN_USER_LABEL = -1;
    public static final String UNKNOWN_USER_NAME = "UNKNOWN";
}
