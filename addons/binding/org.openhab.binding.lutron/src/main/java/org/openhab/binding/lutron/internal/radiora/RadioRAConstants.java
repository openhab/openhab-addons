/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.lutron.LutronBindingConstants;

/**
 * The {@link RadioRAConstants} class defines common constants for RadioRA classic devices
 *
 * @author Jeff Lauterbach - Initial contribution
 */
public class RadioRAConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RS232 = new ThingTypeUID(LutronBindingConstants.BINDING_ID, "ra-rs232");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(LutronBindingConstants.BINDING_ID,
            "ra-dimmer");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(LutronBindingConstants.BINDING_ID,
            "ra-switch");
    public static final ThingTypeUID THING_TYPE_PHANTOM = new ThingTypeUID(LutronBindingConstants.BINDING_ID,
            "ra-phantomButton");

}
