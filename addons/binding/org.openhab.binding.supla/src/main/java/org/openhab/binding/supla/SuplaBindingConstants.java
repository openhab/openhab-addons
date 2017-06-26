/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SuplaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class SuplaBindingConstants {

    private static final String BINDING_ID = "supla";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "suplaCloudBridge");
    public static final ThingTypeUID SUPLA_ZAMEL_ROW_01_THING_TYPE = new ThingTypeUID(BINDING_ID, "zamel-row-1");

    // List of all Channel ids
    public static final String SWITCH_CHANNEL = "switch-channel";

}
