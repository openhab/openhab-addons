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
package org.openhab.binding.souliss;

/**
 * Network constants This class contains Souliss constants. Original version is
 * taken from SoulissApp. For scope of this binding not all constants are used.
 *
 * @author Alessandro Del Pex
 * @since 1.7.0
 */
public class SoulissBindingUDPConstants {

    public static final String TAG = "SoulissApp";

    public static final int SOULISS_BINDING_LOCAL_PORT = 0;
    public static final int SOULISS_GATEWAY_DEFAULT_PORT = 230;

    public static final Object SOULISS_DEFAULT_NODE_INDEX = 70;
    public static final Object SOULISS_DEFAULT_USER_INDEX = 120;

    public static final String BROADCASTADDR = "255.255.255.255";

    public static final byte Souliss_UDP_function_force = 0x33;
    public static final byte Souliss_UDP_function_force_massive = 0x34;

    public static final byte Souliss_UDP_function_subscribe = 0x21;
    public static final byte Souliss_UDP_function_subscribe_resp = 0x31;
    public static final byte Souliss_UDP_function_poll = 0x27;
    public static final byte Souliss_UDP_function_poll_resp = 0x37;
    public static final byte Souliss_UDP_function_typreq = 0x22;
    public static final byte Souliss_UDP_function_typreq_resp = 0x32;
    public static final byte Souliss_UDP_function_healthyReq = 0x25;
    public static final byte Souliss_UDP_function_healthy_resp = 0x35;

    public static final byte Souliss_UDP_function_ping = 0x8;
    public static final byte Souliss_UDP_function_ping_resp = 0x18;

    public static final byte Souliss_UDP_function_discover_GW_node_bcast = 0x28;
    public static final byte Souliss_UDP_function_discover_GW_node_bcas_resp = 0x38;

    public static final int Souliss_UDP_function_db_struct = 0x26;
    public static final int Souliss_UDP_function_db_struct_resp = 0x36;

    public static final int Souliss_UDP_function_ActionMessage = 0x72;

    public static final Byte[] PING_PAYLOAD = { Souliss_UDP_function_ping, 0, 0, 0, 0 };
    public static final Byte[] PING_DISCOVER_BCAST_PAYLOAD = { Souliss_UDP_function_discover_GW_node_bcast, 0, 0, 0,
            0 };
    public static final Byte[] DBSTRUCT_PAYLOAD = { Souliss_UDP_function_db_struct, 0, 0, 0, 0 };

}
