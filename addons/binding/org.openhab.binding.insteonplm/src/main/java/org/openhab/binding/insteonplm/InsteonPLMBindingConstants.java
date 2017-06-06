/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link InsteonPLMBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */
public class InsteonPLMBindingConstants {

    public static final String BINDING_ID = "insteonplm";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Channel ids
    public final static String CHANNEL_LAST_MESSAGE_RECIEVED = "lastMessageReceived";
    public final static String CHANNEL_SUCCESSFULY_SENT = "successfulySent";
    public final static String CHANNEL_ERROR_SENDING = "errorSent";
    public final static String CHANNEL_PENDING_WRITE = "pendingWrite";

    // Properties to track information about the thing.
    public final static String PROPERTY_INSTEON_ADDRESS = "address";
    public final static String PROPERTY_INSTEON_PRODUCT_KEY = "productkey";
    public final static String PROPERTY_INSTEON_CATEGORY = "deviceCategory";
    public final static String PROPERTY_INSTEON_SUBCATEGORY = "deviceSubCategory";
    public static final String PROPERTY_INSTEON_MODEL = "model";

    // Properties for channels to help configure them.
    public final static String PROPERTY_ONOFF_EXTENDED_NEEDED = "ext";
    public final static String PROPERTY_ONOFF_EXTENDED_DATA1 = "d1";
    public final static String PROPERTY_ONOFF_EXTENDED_DATA2 = "d2";
    public final static String PROPERTY_ONOFF_EXTENDED_DATA3 = "d3";

    public static final Object PROPERTY_CHANNEL_FEATURE = "feature";
    public static final Object PROPERTY_CHANNEL_POLL_HANDLER = "pollHandler";
    public static final Object PROPERTY_INSTEON_X10 = "x10";

    public enum ExtendedData {
        extendedNone,
        extendedCrc1,
        extendedCrc2
    }
}