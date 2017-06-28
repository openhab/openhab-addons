/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla;

import com.google.common.collect.ImmutableSet;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Set;

/**
 * The {@link SuplaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class SuplaBindingConstants {

    public static final String BINDING_ID = "supla";

    // IDs
    public static final String SUPLA_DEVICE_THING_ID = "supla-io-device";
    public static final String BRIDGE_THING_ID = "suplaCloudBridge";
    public static final String ONE_CHANNEL_RELAY_THING_ID = "one-channel-relay";
    public static final String TWO_CHANNEL_RELAY_THING_ID = "two-channel-relay";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_THING_ID);
    public static final ThingTypeUID SUPLA_IO_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, SUPLA_DEVICE_THING_ID);
    public static final ThingTypeUID ONE_CHANNEL_RELAY_THING_TYPE = new ThingTypeUID(BINDING_ID, ONE_CHANNEL_RELAY_THING_ID);
    public static final ThingTypeUID TWO_CHANNEL_RELAY_THING_TYPE = new ThingTypeUID(BINDING_ID, TWO_CHANNEL_RELAY_THING_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(BRIDGE_THING_TYPE, ONE_CHANNEL_RELAY_THING_TYPE, TWO_CHANNEL_RELAY_THING_TYPE);

    // Channels IDs
    public static final String SWITCH_CHANNEL_ID = "switch-channel";

    // List of all Channel ids
    public static final String SWITCH_1_CHANNEL = "switch-1";
    public static final String SWITCH_2_CHANNEL = "switch-2";

    // Supla Consts
    // Channel
    public static final String SUPLA_IO_DEVICE_ID = "supla-io-device-id";
    public static final String RELAY_CHANNEL_TYPE = "TYPE_RELAY";
}
