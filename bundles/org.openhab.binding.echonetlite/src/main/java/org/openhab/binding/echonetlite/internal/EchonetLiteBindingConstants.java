/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.echonetlite.internal;

import java.net.InetSocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EchonetLiteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetLiteBindingConstants {

    public static final int ECHONET_PORT = 3610;
    public static final String DISCOVERY_ADDRESS = "224.0.23.0";
    public static final InstanceKey MANAGEMENT_CONTROLLER_KEY = new InstanceKey(new InetSocketAddress(ECHONET_PORT),
            EchonetClass.MANAGEMENT_CONTROLLER, (byte) 0x01);
    public static final InstanceKey DISCOVERY_KEY = new InstanceKey(
            new InetSocketAddress(DISCOVERY_ADDRESS, ECHONET_PORT), EchonetClass.NODE_PROFILE, (byte) 0x01);
    private static final String BINDING_ID = "echonetlite";
    public static final long POLL_INTERVAL_MS = 30_000_000;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ECHONET_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    public static final StateCodec.OnOffCodec ON_OFF_CODEC_0x30_0x31 = new StateCodec.OnOffCodec(0x30, 0x31);
    public static final StateCodec.OnOffCodec ON_OFF_CODEC_0x41_0x42 = new StateCodec.OnOffCodec(0x41, 0x42);
}
