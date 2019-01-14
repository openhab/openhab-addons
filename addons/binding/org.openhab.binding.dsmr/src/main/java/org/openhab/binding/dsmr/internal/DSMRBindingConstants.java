/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Removed time constants
 */
@NonNullByDefault
public final class DSMRBindingConstants {
    /**
     * Binding id.
     */
    public static final String BINDING_ID = "dsmr";

    /**
     * Key to use to identify serial port.
     */
    public static final String DSMR_PORT_NAME = "org.openhab.binding.dsmr";

    /**
     * Bridge device thing
     */
    public static final ThingTypeUID THING_TYPE_DSMR_BRIDGE = new ThingTypeUID(BINDING_ID, "dsmrBridge");

    private DSMRBindingConstants() {
        // Constants class
    }
}
