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
package org.openhab.binding.echonetlite.internal;

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

    public static final long DEFAULT_POLL_INTERVAL_MS = 30_000;
    public static final long DEFAULT_RETRY_TIMEOUT_MS = 1_000;
    public static final int NETWORK_WAIT_TIMEOUT = 250;

    // List of all Thing Type UIDs
    private static final String BINDING_ID = "echonetlite";
    public static final ThingTypeUID THING_TYPE_ECHONET_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_ECHONET_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final StateCodec.OnOffCodec ON_OFF_CODEC_0x30_0x31 = new StateCodec.OnOffCodec(0x30, 0x31);
    public static final StateCodec.OnOffCodec ON_OFF_CODEC_0x41_0x42 = new StateCodec.OnOffCodec(0x41, 0x42);
}
