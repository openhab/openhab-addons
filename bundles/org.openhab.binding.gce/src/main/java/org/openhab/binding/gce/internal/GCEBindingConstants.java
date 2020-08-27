/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link GCEBindingConstants} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class GCEBindingConstants {

    public static final String BINDING_ID = "gce";

    // List of Bridge Type UIDs
    public static final ThingTypeUID IPXV3_THING_TYPE = new ThingTypeUID(BINDING_ID, "ipx800v3");

    // Module Properties
    public static final String DIGITAL_INPUT = "I";
    public static final String ANALOG_INPUT = "A";
    public static final String COUNTER = "C";
    public static final String RELAY_OUTPUT = "O";

    public static final String LAST_STATE_DURATION_CHANNEL_NAME = "duration";
    public static final String CHANNEL_TYPE_PUSH_BUTTON_TRIGGER = "pushButtonTrigger";

    public static final String EVENT_PRESSED = "PRESSED";
    public static final String EVENT_RELEASED = "RELEASED";
    public static final String EVENT_SHORT_PRESS = "SHORT_PRESS";
    public static final String EVENT_LONG_PRESS = "LONG_PRESS";
    public static final String EVENT_PULSE = "PULSE";

    // List of adressable things
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(IPXV3_THING_TYPE);
}
