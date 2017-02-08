/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
    public final static ThingTypeUID THING_TYPE_INDEGO = new ThingTypeUID(BINDING_ID, "indego");

    // List of all Channel ids
    public final static String STATE = "state";
    public final static String TEXTUAL_STATE = "textualstate";
    public final static String MOWED = "mowed";
    public final static String ERRORCODE = "errorcode";
    public final static String STATECODE = "statecode";
    public final static String READY = "ready";

}
