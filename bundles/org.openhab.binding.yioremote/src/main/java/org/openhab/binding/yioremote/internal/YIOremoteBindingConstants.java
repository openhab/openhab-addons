/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YIOremoteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteBindingConstants {

    private static final String BINDING_ID = "yioremote";

    // List of all used global variables
    public static final WebSocketClient yiodockwebSocketClient = new WebSocketClient();
    public static @Nullable Session yiodockwebSocketClientSession = null;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

}
