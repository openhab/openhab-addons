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
package org.openhab.binding.upb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Common constants used in the binding.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public final class Constants {
    public static final String BINDING_ID = "upb";
    public static final ThingTypeUID PIM_UID = new ThingTypeUID(BINDING_ID, "serial-pim");
    public static final ThingTypeUID GENERIC_DEVICE_UID = new ThingTypeUID(BINDING_ID, "generic");
    public static final ThingTypeUID VIRTUAL_DEVICE_UID = new ThingTypeUID(BINDING_ID, "virtual");
    public static final ThingTypeUID LEVITON_38A00_DEVICE_UID = new ThingTypeUID(BINDING_ID, "leviton-38a00-1");

    public static final String SCENE_CHANNEL_TYPE_ID = "scene-selection";
    public static final String LINK_CHANNEL_TYPE_ID = "link";
    public static final String DIMMER_TYPE_ID = "dimmer";
    public static final ChannelTypeUID SCENE_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, SCENE_CHANNEL_TYPE_ID);
    public static final ChannelTypeUID LINK_CHANNEL_TYPE_UID = new ChannelTypeUID(BINDING_ID, LINK_CHANNEL_TYPE_ID);
    public static final String LINK_ACTIVATE_CHANNEL_ID = "linkActivated";
    public static final String LINK_DEACTIVATE_CHANNEL_ID = "linkDeactivated";

    public static final String CONFIGURATION_PORT = "port";
    public static final String CONFIGURATION_UNIT_ID = "unitId";
    public static final String CONFIGURATION_NETWORK_ID = "networkId";
    public static final String CONFIGURATION_LINK_ID = "linkId";

    public static final String OFFLINE_CTLR_OFFLINE = "@text/upb.thingstate.controller_offline";
    public static final String OFFLINE_COMM_ERROR = "@text/upb.thingstate.controller_comm_error";
    public static final String OFFLINE_NODE_DEAD = "@text/upb.thingstate.node_dead";
    public static final String OFFLINE_NODE_NOTFOUND = "@text/upb.thingstate.node_notfound";
    public static final String OFFLINE_SERIAL_EXISTS = "@text/upb.thingstate.serial_notfound";
    public static final String OFFLINE_SERIAL_INUSE = "@text/upb.thingstate.serial_inuse";
    public static final String OFFLINE_SERIAL_UNSUPPORTED = "@text/upb.thingstate.serial_unsupported";
    public static final String OFFLINE_SERIAL_LISTENERS = "@text/upb.thingstate.serial_listeners";
    public static final String OFFLINE_SERIAL_PORT_NOT_SET = "@text/upb.thingstate.serial_cfg_port";

    private Constants() {
        // static class
    }
}
