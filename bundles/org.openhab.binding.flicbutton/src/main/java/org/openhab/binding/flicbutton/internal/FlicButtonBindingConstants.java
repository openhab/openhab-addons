/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.flicbutton.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FlicButtonBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Fink - Initial contribution
 */
@NonNullByDefault
public class FlicButtonBindingConstants {

    public static final String BINDING_ID = "flicbutton";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "flicd-bridge");
    public static final ThingTypeUID FLICBUTTON_THING_TYPE = new ThingTypeUID(BINDING_ID, "button");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(FLICBUTTON_THING_TYPE);

    // List of all configuration options
    public static final String CONFIG_HOST_NAME = "hostname";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_ADDRESS = "address";

    // List of all Channel ids
    public static final String CHANNEL_ID_RAWBUTTON_EVENTS = "rawbutton";
    public static final String CHANNEL_ID_BUTTON_EVENTS = "button";
    public static final String CHANNEL_ID_BATTERY_LEVEL = "battery-level";

    // Other stuff
    public static final int BUTTON_OFFLINE_GRACE_PERIOD_SECONDS = 60;

    public static final Map<String, String> FLIC_OPENHAB_TRIGGER_EVENT_MAP = Collections
            .unmodifiableMap(new HashMap<>() {
                {
                    put("ButtonSingleClick", CommonTriggerEvents.SHORT_PRESSED);
                    put("ButtonDoubleClick", CommonTriggerEvents.DOUBLE_PRESSED);
                    put("ButtonHold", CommonTriggerEvents.LONG_PRESSED);
                    put("ButtonDown", CommonTriggerEvents.PRESSED);
                    put("ButtonUp", CommonTriggerEvents.RELEASED);
                }
            });
}
