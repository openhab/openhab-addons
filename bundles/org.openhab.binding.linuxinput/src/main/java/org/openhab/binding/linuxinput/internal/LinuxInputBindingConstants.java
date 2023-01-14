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
package org.openhab.binding.linuxinput.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Constants shared by the LinuxInput binding components.
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
public class LinuxInputBindingConstants {
    private LinuxInputBindingConstants() {
    }

    public static final String BINDING_ID = "linuxinput";

    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "input-device");
    public static final ChannelTypeUID CHANNEL_TYPE_KEY_PRESS = new ChannelTypeUID(BINDING_ID, "key-press");
    public static final ChannelTypeUID CHANNEL_TYPE_KEY = new ChannelTypeUID(BINDING_ID, "key");
    public static final String CHANNEL_GROUP_KEYPRESSES_ID = "keypresses";
}
