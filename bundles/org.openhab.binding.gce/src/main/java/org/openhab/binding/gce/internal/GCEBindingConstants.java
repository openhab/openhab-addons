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
package org.openhab.binding.gce.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GCEBindingConstants} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class GCEBindingConstants {

    public static final String BINDING_ID = "gce";

    // Bridge Type UID
    public static final ThingTypeUID IPXV3_THING_TYPE = new ThingTypeUID(BINDING_ID, "ipx800v3");

    public static final String CHANNEL_LAST_STATE_DURATION = "duration";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String TRIGGER_CONTACT = "contact-trigger";

    public static final String EVENT_PRESSED = "PRESSED";
    public static final String EVENT_RELEASED = "RELEASED";
    public static final String EVENT_SHORT_PRESS = "SHORT_PRESS";
    public static final String EVENT_LONG_PRESS = "LONG_PRESS";
    public static final String EVENT_PULSE = "PULSE";
}
