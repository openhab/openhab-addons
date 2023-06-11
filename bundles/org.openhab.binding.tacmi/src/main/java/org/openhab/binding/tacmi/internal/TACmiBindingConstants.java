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
package org.openhab.binding.tacmi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link TACmiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class TACmiBindingConstants {

    public static final String BINDING_ID = "tacmi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CMI = new ThingTypeUID(BINDING_ID, "cmi");
    public static final ThingTypeUID THING_TYPE_COE_BRIDGE = new ThingTypeUID(BINDING_ID, "coe-bridge");
    public static final ThingTypeUID THING_TYPE_CMI_SCHEMA = new ThingTypeUID(BINDING_ID, "cmiSchema");

    public static final String CONFIG_DESCRIPTION_API_SCHEMA_DEFAULTS = "channel-type:tacmi:schemaApiDefaults";

    public static final ChannelTypeUID CHANNEL_TYPE_COE_DIGITAL_IN_UID = new ChannelTypeUID(BINDING_ID,
            "coe-digital-in");
    public static final ChannelTypeUID CHANNEL_TYPE_COE_ANALOG_IN_UID = new ChannelTypeUID(BINDING_ID, "coe-analog-in");

    public static final ChannelTypeUID CHANNEL_TYPE_COE_DIGITAL_OUT_UID = new ChannelTypeUID(BINDING_ID,
            "coe-digital-out");
    public static final ChannelTypeUID CHANNEL_TYPE_COE_ANALOG_OUT_UID = new ChannelTypeUID(BINDING_ID,
            "coe-analog-out");

    public static final ChannelTypeUID CHANNEL_TYPE_SCHEME_SWITCH_RO_UID = new ChannelTypeUID(BINDING_ID,
            "schema-switch-ro");
    public static final ChannelTypeUID CHANNEL_TYPE_SCHEME_SWITCH_RW_UID = new ChannelTypeUID(BINDING_ID,
            "schema-switch-rw");
    public static final ChannelTypeUID CHANNEL_TYPE_SCHEME_NUMERIC_RO_UID = new ChannelTypeUID(BINDING_ID,
            "schema-numeric-ro");
    public static final ChannelTypeUID CHANNEL_TYPE_SCHEME_STATE_RO_UID = new ChannelTypeUID(BINDING_ID,
            "schema-state-ro");

    // Channel specific configuration items
    public static final String CHANNEL_CONFIG_OUTPUT = "output";
}
