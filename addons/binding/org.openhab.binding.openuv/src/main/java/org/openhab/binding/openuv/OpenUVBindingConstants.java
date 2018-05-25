/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenUVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVBindingConstants {

    public static final String BINDING_ID = "openuv";
    public static final String LOCAL = "local";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OPENUV = new ThingTypeUID(BINDING_ID, "openuv");

    // List of all Channel id's
    public static final String UVINDEX = "UVIndex";
    public static final String UVCOLOR = "UVColor";
    public static final String UVMAX = "UVMax";
    public static final String UVMAXTIME = "UVMaxTime";
    public static final String UVMAXEVENT = "UVMaxEvent";
    public static final String OZONE = "Ozone";
    public static final String OZONETIME = "OzoneTime";
    public static final String UVTIME = "UVTime";
    public static final String SAFEEXPOSURE = "SafeExposure";

    public static final String PROPERTY_INDEX = "Index";
}
