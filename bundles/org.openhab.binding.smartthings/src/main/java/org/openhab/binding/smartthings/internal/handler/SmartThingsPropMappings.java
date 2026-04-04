/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * Configuration data for SmartThings
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsPropMappings {
    private static final SmartThingsPropMappings INSTANCE = new SmartThingsPropMappings();

    private final Hashtable<String, Boolean> isPropertiesMap = new Hashtable<String, Boolean>();
    private final Hashtable<String, String> propNameMap = new Hashtable<String, String>();

    public static boolean isProperties(String capaKey) {
        return SmartThingsPropMappings.INSTANCE.isPropertiesInternal(capaKey);
    }

    private boolean isPropertiesInternal(String capaKey) {
        if (isPropertiesMap.containsKey(capaKey)) {
            return true;
        }

        return false;
    }

    public static @Nullable String getPropertyName(String propertyId) {
        return SmartThingsPropMappings.INSTANCE.getPropertyNameInternal(propertyId);
    }

    private @Nullable String getPropertyNameInternal(String propertyId) {
        if (propNameMap.containsKey(propertyId)) {
            return propNameMap.get(propertyId);
        }

        return null;
    }

    public SmartThingsPropMappings() {
        isPropertiesMap.put("ocf", Boolean.TRUE);

        propNameMap.put("di", "Device ID");
        propNameMap.put("dmv", "Data Model Version");
        propNameMap.put("mnmo", "Model Number");
        propNameMap.put("mnmn", "Manufacturer Name");
        propNameMap.put("pi", "Platform ID");
        propNameMap.put("vid", "Vendor ID");
        propNameMap.put("icv", "Ocf Version");
        propNameMap.put("n", "Device Name");

    }
}
