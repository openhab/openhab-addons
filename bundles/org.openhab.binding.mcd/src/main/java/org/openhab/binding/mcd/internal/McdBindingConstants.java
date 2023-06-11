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
package org.openhab.binding.mcd.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link McdBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
public class McdBindingConstants {

    private static final String BINDING_ID = "mcd";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MCD_BRIDGE = new ThingTypeUID(BINDING_ID, "mcdBridge");
    public static final ThingTypeUID THING_TYPE_SENSOR = new ThingTypeUID(BINDING_ID, "mcdSensor");

    // List of all Channel ids
    public static final String LAST_VALUE = "lastValue";
    public static final String SEND_EVENT = "sendEvent";
}
