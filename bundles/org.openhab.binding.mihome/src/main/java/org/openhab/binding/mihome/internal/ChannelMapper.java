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
package org.openhab.binding.mihome.internal;

import java.util.HashMap;
import java.util.Map;

import org.openhab.core.thing.CommonTriggerEvents;

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
        SYSTEM_BUTTON_MAP.put("BOTH_CLICK", CommonTriggerEvents.SHORT_PRESSED);
        SYSTEM_BUTTON_MAP.put("DOUBLE_CLICK", CommonTriggerEvents.DOUBLE_PRESSED);
        SYSTEM_BUTTON_MAP.put("LONG_CLICK_PRESS", CommonTriggerEvents.LONG_PRESSED);
        SYSTEM_BUTTON_MAP.put("LONG_CLICK", CommonTriggerEvents.LONG_PRESSED);
        SYSTEM_BUTTON_MAP.put("LONG_BOTH_CLICK", CommonTriggerEvents.LONG_PRESSED);
        SYSTEM_BUTTON_MAP.put("LONG_CLICK_RELEASE", "LONG_RELEASED");
    }

    public static String getChannelEvent(String reportedString) {
        String ret = SYSTEM_BUTTON_MAP.get(reportedString);
        if (ret != null) {
            return ret;
        } else {
            return reportedString;
        }
    }
}
