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
    public final static String CHANNEL_1 = "channel1";

    // Properties to track information about the thing.
    public final static String PROPERTY_INSTEON_ADDRESS = "address";
    // This is a comma delimited string.
    public final static String PROPERTY_INSTEON_FEATURES = "feature";
    // This is a comma delimited string.
    public final static String PROPERTY_INSTEON_FEATURE_GROUPS = "featureGroups";
    public final static String PROPERTY_INSTEON_PRODUCT_KEY = "productkey";
}
