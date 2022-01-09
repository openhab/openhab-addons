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
package org.openhab.binding.lutron.internal.radiora;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.LutronBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RadioRAConstants} class defines common constants for RadioRA classic devices
 *
 * @author Jeff Lauterbach - Initial contribution
 */
@NonNullByDefault
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
