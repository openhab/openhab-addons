/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VigiCruesBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigiCruesBindingConstants {

    public static final String BINDING_ID = "vigicrues";
    public static final String OPENDATASOFT_URL = "https://public.opendatasoft.com/api/records/1.0/search/";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VIGI_CRUES = new ThingTypeUID(BINDING_ID, "station");

    // List of all Channel id's
    public static final String OBSERVATION_TIME = "observation-time";
    public static final String HEIGHT = "height";
    public static final String FLOW = "flow";

    public static final String COMMENT = "comment";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_VIGI_CRUES);
}
