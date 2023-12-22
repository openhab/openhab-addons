/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MyUplinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class MyUplinkBindingConstants {

    private static final String BINDING_ID = "myuplink";

    // List of main device types
    public static final String DEVICE_ACCOUNT = "account";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, DEVICE_ACCOUNT);

    // List of all channel groups
    //TODO: add content

    // Channel types
    //TODO: add content
    public static final String CHANNEL_TYPEPREFIX_RW = "rw";


    // Channels with specific handling
    //TODO: add content

    // JSON Keys
    //TODO: add content

    // Write Commands
    //TODO: add content

    // Command Values
    //TODO: add content

    // web request constants
    //TODO: add content

    // URLs
    //TODO: add content

    // Status Keys
    public static final String STATUS_TOKEN_VALIDATED = "@text/status.token.validated";
    public static final String STATUS_WAITING_FOR_BRIDGE = "@text/status.waiting.for.bridge";
    public static final String STATUS_WAITING_FOR_LOGIN = "@text/status.waiting.for.login";
    public static final String STATUS_NO_VALID_DATA = "@text/status.no.valid.data";
    public static final String STATUS_NO_CONNECTION = "@text/status.no.connection";

    // other
    public static final long POLLING_INITIAL_DELAY = 1;

    public static final String PARAMETER_NAME_WRITE_COMMAND = "writeCommand";
    public static final String PARAMETER_NAME_VALIDATION_REGEXP = "validationExpression";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT);
}
