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
package org.openhab.binding.nikobus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NikobusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusBindingConstants {

    private static final String BINDING_ID = "nikobus";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_PCLINK = new ThingTypeUID(BINDING_ID, "pc-link");

    public static final ThingTypeUID THING_TYPE_PUSH_BUTTON = new ThingTypeUID(BINDING_ID, "push-button");
    public static final ThingTypeUID THING_TYPE_SWITCH_MODULE = new ThingTypeUID(BINDING_ID, "switch-module");
    public static final ThingTypeUID THING_TYPE_DIMMER_MODULE = new ThingTypeUID(BINDING_ID, "dimmer-module");
    public static final ThingTypeUID THING_TYPE_ROLLERSHUTTER_MODULE = new ThingTypeUID(BINDING_ID,
            "rollershutter-module");

    // List of all Channel ids
    public static final String CHANNEL_BUTTON = "button";
    public static final String CHANNEL_TRIGGER_FILTER = "trigger-filter";
    public static final String CHANNEL_TRIGGER_BUTTON = "trigger-button";
    public static final String CHANNEL_OUTPUT_PREFIX = "output-";

    // Configuration parameters
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";
    public static final String CONFIG_IMPACTED_MODULES = "impactedModules";
    public static final String CONFIG_ADDRESS = "address";
    public static final String CONFIG_PORT_NAME = "port";
}
