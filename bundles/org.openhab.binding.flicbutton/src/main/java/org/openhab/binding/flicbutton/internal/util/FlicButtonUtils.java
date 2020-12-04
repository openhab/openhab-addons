/**
 * Copyright (c) 2016 - 2020 Patrick Fink
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3
 * with the GNU Classpath Exception 2.0 which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0 WITH Classpath-exception-2.0
 */
package org.openhab.binding.flicbutton.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.ThingUID;

import io.flic.fliclib.javaclient.Bdaddr;

/**
 *
 * @author Patrick Fink
 *
 */
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
