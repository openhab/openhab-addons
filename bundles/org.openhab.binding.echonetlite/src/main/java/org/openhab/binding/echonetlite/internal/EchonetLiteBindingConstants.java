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
    public static final long DEFAULT_RETRY_TIMEOUT_MS = 2_000;
    public static final int NETWORK_WAIT_TIMEOUT = 250;

    // List of all Thing Type UIDs
    public static final String BINDING_ID = "echonetlite";
    public static final ThingTypeUID THING_TYPE_ECHONET_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_ECHONET_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final StateCodec.OnOffCodec ON_OFF_CODEC_30_31 = new StateCodec.OnOffCodec(0x30, 0x31);
    public static final StateCodec.OnOffCodec ON_OFF_CODEC_41_42 = new StateCodec.OnOffCodec(0x41, 0x42);

    public static final String PROPERTY_NAME_INSTANCE_KEY = "instanceKey";
    public static final String PROPERTY_NAME_HOSTNAME = "hostname";
    public static final String PROPERTY_NAME_PORT = "port";
    public static final String PROPERTY_NAME_GROUP_CODE = "groupCode";
    public static final String PROPERTY_NAME_CLASS_CODE = "classCode";
    public static final String PROPERTY_NAME_INSTANCE = "instance";
    public static final int OFFLINE_TIMEOUT_COUNT = 2;
}
