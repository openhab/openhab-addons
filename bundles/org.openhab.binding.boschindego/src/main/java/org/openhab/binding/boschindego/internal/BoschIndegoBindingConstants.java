/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschindego.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BoschIndegoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Fleck - Initial contribution
 */
@NonNullByDefault
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
