/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.vigicrues.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VigiCruesBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigiCruesBindingConstants {

    // List of Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_STATION = new ThingTypeUID("vigicrues", "station");

    // List of all Channel id's
    public static final String OBSERVATION_TIME = "observation-time";
    public static final String HEIGHT = "height";
    public static final String FLOW = "flow";
    public static final String ALERT = "alert";
    public static final String COMMENT = "comment";
    public static final String RELATIVE_PREFIX = "relative";
    public static final String RELATIVE_HEIGHT = RELATIVE_PREFIX + "-" + HEIGHT;
    public static final String RELATIVE_FLOW = RELATIVE_PREFIX + "-" + FLOW;
    public static final String SHORT_COMMENT = "short-" + COMMENT;

    // List of properties Labels
    public static final String TRONCON = "Tronçon";
    public static final String DISTANCE = "Distance";
    public static final String RIVER = "Cours";
    public static final String LOCATION = "Location";
    public static final String FLOOD = "Crue";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_STATION);
}
