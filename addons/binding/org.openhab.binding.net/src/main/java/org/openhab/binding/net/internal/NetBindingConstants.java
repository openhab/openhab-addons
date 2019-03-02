/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.net.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NetBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class NetBindingConstants {

    private static final String BINDING_ID = "net";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_UDP = new ThingTypeUID(BINDING_ID, "udp");

    // List of all Channel ids
    public static final String CHANNEL_DATA_RECEIVED = "dataReceived";

    // Supported charsets
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_ASCII = "ASCII";
    public static final String CHARSET_HEX_STRING = "HexString";

}
