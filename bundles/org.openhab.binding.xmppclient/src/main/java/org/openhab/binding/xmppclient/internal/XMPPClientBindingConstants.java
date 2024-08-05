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
package org.openhab.binding.xmppclient.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link XMPPClientBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pavel Gololobov - Initial contribution
 */
@NonNullByDefault
public class XMPPClientBindingConstants {
    private static final String BINDING_ID = "xmppclient";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_XMPP = new ThingTypeUID(BINDING_ID, "xmppBridge");
    public static final String PUBLISH_TRIGGER_CHANNEL = "publishTrigger";
}
