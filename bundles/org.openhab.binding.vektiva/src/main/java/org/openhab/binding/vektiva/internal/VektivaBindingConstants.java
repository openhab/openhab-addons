/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.vektiva.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VektivaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class VektivaBindingConstants {

    private static final String BINDING_ID = "vektiva";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SMARWI = new ThingTypeUID(BINDING_ID, "smarwi");

    // List of all Channel ids
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_STATUS = "status";

    // commands
    public static final String COMMAND_OPEN = "open";
    public static final String COMMAND_CLOSE = "close";
    public static final String COMMAND_STOP = "stop";

    // response
    public static final String RESPONSE_OK = "OK";

    // constants
    public static final String NA = "N/A";
}
