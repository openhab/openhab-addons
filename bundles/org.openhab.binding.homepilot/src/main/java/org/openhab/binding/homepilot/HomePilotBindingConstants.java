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
package org.openhab.binding.homepilot;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HomePilotBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Steffen Stundzig - Initial contribution
 */
public class HomePilotBindingConstants {

    public static final String BINDING_ID = "homepilot";

    public static final String ITEM_TYPE_SWITCH = "switch";
    public static final String ITEM_TYPE_ROLLERSHUTTER = "rollershutter";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, ITEM_TYPE_SWITCH);
    public final static ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, ITEM_TYPE_ROLLERSHUTTER);

    // List of all Channel ids
    public final static String CHANNEL_POSITION = "position";
    public final static String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public final static String CHANNEL_STATE = "state";

    //public final static String PROPERTY_POSITION = CHANNEL_POSITION;

}
