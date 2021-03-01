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
package org.openhab.binding.flicbutton.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.ThingUID;

import io.flic.fliclib.javaclient.Bdaddr;

/**
 *
 * @author Patrick Fink - Initial contribution
 *
 */
@NonNullByDefault
public class FlicButtonUtils {
    public static final Map<String, String> flicOpenhabTriggerEventMap = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                {
                    put("ButtonSingleClick", CommonTriggerEvents.SHORT_PRESSED);
                    put("ButtonDoubleClick", CommonTriggerEvents.DOUBLE_PRESSED);
                    put("ButtonHold", CommonTriggerEvents.LONG_PRESSED);
                    put("ButtonDown", CommonTriggerEvents.PRESSED);
                    put("ButtonUp", CommonTriggerEvents.RELEASED);
                }
            });

    public static ThingUID getThingUIDFromBdAddr(Bdaddr bdaddr, ThingUID bridgeUID) {
        String thingID = bdaddr.toString().replace(":", "-");
        return new ThingUID(FlicButtonBindingConstants.FLICBUTTON_THING_TYPE, bridgeUID, thingID);
    }

    public static Bdaddr getBdAddrFromThingUID(ThingUID thingUID) {
        String bdaddrRaw = thingUID.getId().replace("-", ":");
        return new Bdaddr(bdaddrRaw);
    }
}
