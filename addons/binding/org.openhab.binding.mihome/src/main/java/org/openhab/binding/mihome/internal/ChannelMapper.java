/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.CommonTriggerEvents;

/**
 * Maps the various JSON Strings reported from the devices to Channels
 *
 * @author Dieter Schmidt - Initial contribution
 */
public class ChannelMapper {

    private static final Map<String, String> SYSTEM_BUTTON_MAP = new HashMap<>();
    static {
        // Alphabetical order
        SYSTEM_BUTTON_MAP.put("CLICK", CommonTriggerEvents.SHORT_PRESSED);
        SYSTEM_BUTTON_MAP.put("DOUBLE_CLICK", CommonTriggerEvents.DOUBLE_PRESSED);
        SYSTEM_BUTTON_MAP.put("LONG_CLICK_PRESS", CommonTriggerEvents.LONG_PRESSED);
        SYSTEM_BUTTON_MAP.put("LONG_CLICK_RELEASE", "LONG_RELEASED");
    }

    public static String getChannelEvent(String reportedString) {
        String ret = SYSTEM_BUTTON_MAP.get(reportedString);
        if (ret != null) {
            return ret;
        } else {
            return "UNKNOWN_EVENT";
        }
    }
}
