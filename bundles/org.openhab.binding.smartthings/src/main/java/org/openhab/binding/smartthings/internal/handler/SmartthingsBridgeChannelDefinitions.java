/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.handler;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration data for Smartthings hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsBridgeChannelDefinitions {
    private static final SmartthingsBridgeChannelDefinitions INSTANCE = new SmartthingsBridgeChannelDefinitions();

    private final Hashtable<String, SmartthingsBridgeChannelDef> channelDefs = new Hashtable<String, SmartthingsBridgeChannelDef>();

    public class SmartthingsBridgeChannelDef {

        public SmartthingsBridgeChannelDef(String tp) {
            this.tp = tp;
        }

        public String tp = "";
    }

    public static @Nullable SmartthingsBridgeChannelDef getChannelDefs(String key) {
        return INSTANCE.getChannelDef(key);
    }

    public @Nullable SmartthingsBridgeChannelDef getChannelDef(String key) {
        if (channelDefs.containsKey(key)) {
            return channelDefs.get(key);
        }

        return null;
    }

    public SmartthingsBridgeChannelDefinitions() {
        channelDefs.put("switch", new SmartthingsBridgeChannelDef("Switch"));
        channelDefs.put("level", new SmartthingsBridgeChannelDef("Dimmer"));
        channelDefs.put("color", new SmartthingsBridgeChannelDef("Color"));
        channelDefs.put("colorValue", new SmartthingsBridgeChannelDef("Color"));
        channelDefs.put("contact", new SmartthingsBridgeChannelDef("Contact"));
    }
}
