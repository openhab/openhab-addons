/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.dial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.sony.internal.SonyBindingConstants;

/**
 * The class provides all the constants specific to the DIAL system.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DialConstants {
    /** The constant for the thing type */
    public static final ThingTypeUID THING_TYPE_DIAL = new ThingTypeUID(SonyBindingConstants.BINDING_ID,
            SonyBindingConstants.DIAL_THING_TYPE_PREFIX);

    /** The constant requesting access */
    public static final String ACCESSCODE_RQST = "RQST";

    /** The channel id constants */
    public static final ChannelTypeUID CHANNEL_TITLE_UID = new ChannelTypeUID(SonyBindingConstants.BINDING_ID,
            "dialtitle");
    public static final ChannelTypeUID CHANNEL_ICON_UID = new ChannelTypeUID(SonyBindingConstants.BINDING_ID,
            "dialicon");
    public static final ChannelTypeUID CHANNEL_STATE_UID = new ChannelTypeUID(SonyBindingConstants.BINDING_ID,
            "dialstate");

    /** The name that will be part of the channel identifier */
    static final String CHANNEL_TITLE = "title";
    static final String CHANNEL_ICON = "icon";
    static final String CHANNEL_STATE = "state";

    /** The property for the channel's application id */
    static final String CHANNEL_PROP_APPLID = "applid";
}
