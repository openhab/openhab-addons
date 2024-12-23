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
package org.openhab.binding.freeathome.internal.util;

import static org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class UidUtils {

    public static ChannelTypeUID generateChannelTypeUID(String valueType, boolean isReadOnly) {
        String channelNameString;

        if (isReadOnly) {
            channelNameString = valueType + "-ro";
        } else {
            channelNameString = valueType;
        }

        return new ChannelTypeUID(BINDING_ID, channelNameString);
    }

    public static ThingTypeUID generateThingUID() {
        return new ThingTypeUID(BINDING_ID, DEVICE_TYPE_ID);
    }
}
