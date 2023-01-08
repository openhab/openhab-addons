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
package org.openhab.binding.unifiedremote.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link UnifiedRemoteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class UnifiedRemoteBindingConstants {

    private static final String BINDING_ID = "unifiedremote";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UNIFIED_REMOTE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(THING_TYPE_UNIFIED_REMOTE_SERVER);

    // List of all Channel ids
    public static final String MOUSE_CHANNEL = "mouse-move";
    public static final String SEND_KEY_CHANNEL = "send-key";

    // List of all Parameters
    public static final String PARAMETER_MAC_ADDRESS = "macAddress";
    public static final String PARAMETER_HOSTNAME = "host";
    public static final String PARAMETER_TCP_PORT = "udpPort";
    public static final String PARAMETER_UDP_PORT = "tcpPort";
}
