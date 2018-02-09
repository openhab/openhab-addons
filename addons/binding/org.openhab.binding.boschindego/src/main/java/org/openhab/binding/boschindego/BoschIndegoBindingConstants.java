/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.boschindego;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BoschIndegoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Fleck - Initial contribution
 */
public class BoschIndegoBindingConstants {

    public static final String BINDING_ID = "boschindego";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INDEGO = new ThingTypeUID(BINDING_ID, "indego");

    // List of all Channel ids
    public static final String STATE = "state";
    public static final String TEXTUAL_STATE = "textualstate";
    public static final String MOWED = "mowed";
    public static final String ERRORCODE = "errorcode";
    public static final String STATECODE = "statecode";
    public static final String READY = "ready";

}
