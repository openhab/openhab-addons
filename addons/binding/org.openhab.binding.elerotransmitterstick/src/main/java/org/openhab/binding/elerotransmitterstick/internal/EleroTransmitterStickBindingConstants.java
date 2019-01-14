/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elerotransmitterstick.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EleroTransmitterStickBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Volker Bier - Initial contribution
 */
public class EleroTransmitterStickBindingConstants {

    public static final String BINDING_ID = "elerotransmitterstick";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_STICK = new ThingTypeUID(BINDING_ID, "elerostick");
    public final static ThingTypeUID THING_TYPE_ELERO_CHANNEL = new ThingTypeUID(BINDING_ID, "elerochannel");

    public static final String CONTROL_CHANNEL = "control";
    public static final String STATUS_CHANNEL = "status";

    // channel id property for elero channels.
    public static final String PROPERTY_CHANNEL_ID = "channelId";
}
