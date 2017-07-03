/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.sony.SonyBindingConstants;

// TODO: Auto-generated Javadoc
/**
 * The class provides all the constants specific to the Ircc system.
 *
 * @author Tim Roberts - Initial contribution
 */
public class DialConstants {

    /** The Constant THING_TYPE_DIAL. */
    // The thing constants
    public final static ThingTypeUID THING_TYPE_DIAL = new ThingTypeUID(SonyBindingConstants.BINDING_ID, "dial");

    /** The Constant CHANNEL_STATE. */
    // All the channel constants
    public final static String CHANNEL_STATE = "state";

    /** The Constant CHANNEL_ICON. */
    public final static String CHANNEL_ICON = "icon";
}
