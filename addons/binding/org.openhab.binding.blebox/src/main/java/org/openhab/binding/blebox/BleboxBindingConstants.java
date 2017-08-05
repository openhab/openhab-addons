/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link BleboxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class BleboxBindingConstants {

    public static final String BINDING_ID = "blebox";

    // List of device types
    public static final String SWITCHBOX = "switchBox";
    public static final String SWITCHBOXD = "switchBoxD";
    public static final String DIMMERBOX = "dimmerBox";
    public static final String WLIGHTBOX = "wLightBox";
    public static final String WLIGHTBOXS = "wLightBoxS";
    public static final String GATEBOX = "gateBox";

    // List of all Bridge Thing Type UIDs
    public final static ThingTypeUID SWITCHBOX_THING_TYPE = new ThingTypeUID(BINDING_ID, SWITCHBOX);
    public final static ThingTypeUID SWITCHBOXD_THING_TYPE = new ThingTypeUID(BINDING_ID, SWITCHBOXD);
    public final static ThingTypeUID DIMMERBOX_THING_TYPE = new ThingTypeUID(BINDING_ID, DIMMERBOX);
    public final static ThingTypeUID WLIGHTBOX_THING_TYPE = new ThingTypeUID(BINDING_ID, WLIGHTBOX);
    public final static ThingTypeUID WLIGHTBOXS_THING_TYPE = new ThingTypeUID(BINDING_ID, WLIGHTBOXS);
    public final static ThingTypeUID GATEBOX_THING_TYPE = new ThingTypeUID(BINDING_ID, GATEBOX);

    // Set of all supported Bridge Type UIDs
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(SWITCHBOX_THING_TYPE,
            SWITCHBOXD_THING_TYPE, DIMMERBOX_THING_TYPE, WLIGHTBOX_THING_TYPE, WLIGHTBOXS_THING_TYPE,
            GATEBOX_THING_TYPE);

    // List of all Channel ids
    public final static String CHANNEL_BRIGHTNESS = "brightness";
    public final static String CHANNEL_COLOR = "color";

    public final static String CHANNEL_SWITCH0 = "switch0";
    public final static String CHANNEL_SWITCH1 = "switch1";

    public final static String CHANNEL_POSITION = "position";

}
