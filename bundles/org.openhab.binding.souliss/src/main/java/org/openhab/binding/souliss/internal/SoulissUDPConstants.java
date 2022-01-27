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
package org.openhab.binding.souliss.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Network constants. The class {@link SoulissUDPConstants} contains Souliss constants. Original version is
 * taken from SoulissApp. For scope of this binding not all constants are used.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 * @author Alessandro Del Pex - @since 1.7.0
 */
@NonNullByDefault
public class SoulissUDPConstants {

    public static final String TAG = "SoulissApp";

    public static final int SOULISS_BINDING_LOCAL_PORT = 0;
    public static final int SOULISS_GATEWAY_DEFAULT_PORT = 230;

    public static final Integer SOULISS_DEFAULT_NODE_INDEX = 70;
    public static final Integer SOULISS_DEFAULT_USER_INDEX = 120;

    public static final String BROADCASTADDR = "255.255.255.255";

    public static final byte SOULISS_UDP_FUNCTION_FORCE = 0x33;
    public static final byte SOULISS_UDP_FUNCTION_FORCE_MASSIVE = 0x34;

    public static final byte SOULISS_UDP_FUNCTION_SUBSCRIBE_REQ = 0x21;
    public static final byte SOULISS_UDP_FUNCTION_SUBSCRIBE_RESP = 0x31;
    public static final byte SOULISS_UDP_FUNCTION_POLL_REQ = 0x27;
    public static final byte SOULISS_UDP_FUNCTION_POLL_RESP = 0x37;
    public static final byte SOULISS_UDP_FUNCTION_TYP_REQ = 0x22;
    public static final byte SOULISS_UDP_FUNCTION_TYP_RESP = 0x32;
    public static final byte SOULISS_UDP_FUNCTION_HEALTHY_REQ = 0x25;
    public static final byte SOULISS_UDP_FUNCTION_HEALTHY_RESP = 0x35;

    public static final byte SOULISS_UDP_FUNCTION_PING_REQ = 0x8;
    public static final byte SOULISS_UDP_FUNCTION_PING_RESP = 0x18;

    public static final byte SOULISS_UDP_FUNCTION_DISCOVER_GW_NODE_BCAST_REQ = 0x28;
    public static final byte SOULISS_UDP_FUNCTION_DISCOVER_GW_NODE_BCAST_RESP = 0x38;

    public static final int SOULISS_UDP_FUNCTION_DBSTRUCT_REQ = 0x26;
    public static final int SOULISS_UDP_FUNCTION_DBSTRUCT_RESP = 0x36;

    public static final int SOULISS_UDP_FUNCTION_ACTION_MESSAGE = 0x72;

    protected static final Byte[] PING_PAYLOAD = { SOULISS_UDP_FUNCTION_PING_REQ, 0, 0, 0, 0 };
    protected static final Byte[] PING_DISCOVER_BCAST_PAYLOAD = { SOULISS_UDP_FUNCTION_DISCOVER_GW_NODE_BCAST_REQ, 0, 0,
            0, 0 };
    protected static final Byte[] DBSTRUCT_PAYLOAD = { SOULISS_UDP_FUNCTION_DBSTRUCT_REQ, 0, 0, 0, 0 };

    // private constructor
    private SoulissUDPConstants() {
    }
}
