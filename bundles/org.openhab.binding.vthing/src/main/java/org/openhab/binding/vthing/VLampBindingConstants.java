/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vthing;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VLampBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Juergen Weber - Initial contribution
 */
@NonNullByDefault
public class VLampBindingConstants {

    static final String BINDING_ID = "vthing";

    static final String THING_ID = "vlamp";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VLAMP = new ThingTypeUID(BINDING_ID, THING_ID);

    // List of all Channel ids
    public static final String CHANNEL_STATE = "state";

    public static final String CHANNEL_ONTIME = "ontime";

    public static final String CHANNEL_COLOR = "color";
}
