/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DSMRBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRBindingConstants {
    // Binding contant
    public static final String BINDING_ID = "dsmr";

    // Bridge device thing
    public final static ThingTypeUID THING_TYPE_DSMR_BRIDGE = new ThingTypeUID(BINDING_ID, "dsmrBridge");

    // The other Thing Types are defined in DSMRMeterTypes

    // Discovery timeout
    public final static int DSMR_DISCOVERY_TIMEOUT = 30;
}
