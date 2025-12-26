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
package org.openhab.binding.smartthings.internal.statehandler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;

/**
 * A factory for creating converters based on the itemType.
 *
 * @author Laurent Arnal - Initial contribution
 */

@NonNullByDefault
public class SmartthingsStateHandlerFactory {
    private static Map<String, SmartthingsStateHandler> stateHandlerCache = new HashMap<>();

    public static void registerStateHandler() {
        registerStateHandler(SmartthingsBindingConstants.THING_LIGHT, new SmartthingsStateHandlerLight());
    }

    private static void registerStateHandler(String key, SmartthingsStateHandler tp) {
        stateHandlerCache.put(key, tp);
    }

    /**
     * Returns the converter for an itemType.
     */
    public static @Nullable SmartthingsStateHandler getStateHandler(String deviceType) {
        SmartthingsStateHandler stateHandler = stateHandlerCache.get(deviceType);
        return stateHandler;
    }
}
