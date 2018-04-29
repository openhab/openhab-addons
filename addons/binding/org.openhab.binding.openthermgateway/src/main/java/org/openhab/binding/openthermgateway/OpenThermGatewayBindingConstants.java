/**
 * Copyright (c) 2018,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openthermgateway;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenThermGatewayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class OpenThermGatewayBindingConstants {

    private static final String BINDING_ID = "openthermgateway";

    // List of all Thing Type UIDs
    // public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    public static final ThingTypeUID OPENTHERMGATEWAY_THING_TYPE = new ThingTypeUID(BINDING_ID, "main");

    // List of all Channel ids
    public static final String CHANNEL_CENTRAL_HEATING_ENABLE = "ch_enable";
    public static final String CHANNEL_DOMESTIC_HOT_WATER_ENABLE = "dhw_enable";
    public static final String CHANNEL_COOLING_ENABLED = "cooling_enabled";
    public static final String CHANNEL_4 = "otc_active";
    public static final String CHANNEL_CENTRAL_HEATING_2_ENABLED = "ch2_enable";
    public static final String CHANNEL_CENTRAL_HEATING_MODE = "ch_mode";

    public static final String CHANNEL_DOMESTIC_HOT_WATER_MODE = "dhw_mode";

}
