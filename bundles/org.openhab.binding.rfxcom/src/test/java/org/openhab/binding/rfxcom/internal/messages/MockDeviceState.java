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
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.rfxcom.internal.handler.DeviceState;
import org.openhab.core.types.Type;

/**
 * Mock implementation for the DeviceState
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class MockDeviceState implements DeviceState {
    private final Map<String, Type> stateMap = new ConcurrentHashMap<>();

    @Override
    public Type getLastState(String channelId) {
        return stateMap.get(channelId);
    }

    public void set(String channelId, Type state) {
        stateMap.put(channelId, state);
    }
}
