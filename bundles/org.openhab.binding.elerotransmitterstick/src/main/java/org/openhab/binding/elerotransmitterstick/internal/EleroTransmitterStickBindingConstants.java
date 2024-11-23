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
package org.openhab.binding.elerotransmitterstick.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EleroTransmitterStickBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Volker Bier - Initial contribution
 */
public class EleroTransmitterStickBindingConstants {

    public static final String BINDING_ID = "elerotransmitterstick";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_STICK = new ThingTypeUID(BINDING_ID, "elerostick");
    public static final ThingTypeUID THING_TYPE_ELERO_CHANNEL = new ThingTypeUID(BINDING_ID, "elerochannel");

    public static final String CONTROL_CHANNEL = "control";
    public static final String STATUS_CHANNEL = "status";

    // channel id property for elero channels.
    public static final String PROPERTY_CHANNEL_ID = "channelId";
}
