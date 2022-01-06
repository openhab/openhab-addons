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
package org.openhab.binding.ecotouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EcoTouchBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sebastian Held - Initial contribution
 */
@NonNullByDefault
public class EcoTouchBindingConstants {

    private static final String BINDING_ID = "ecotouch";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GEO = new ThingTypeUID(BINDING_ID, "geo");
    public static final ThingTypeUID THING_TYPE_AIR = new ThingTypeUID(BINDING_ID, "air");
}
