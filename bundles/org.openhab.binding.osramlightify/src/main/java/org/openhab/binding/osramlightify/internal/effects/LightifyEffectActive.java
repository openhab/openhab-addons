/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal.effects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import org.eclipse.smarthome.core.library.types.DecimalType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;

/**
 * Set an "active" effect on a device.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyEffectActive extends LightifyEffect {

    public LightifyEffectActive(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name) {
        super(bridgeHandler, deviceHandler, name);
    }

    @Override
    public void start() {
        // We send back through the device handler rather than simply sending a SET_TEMPERATURE
        // message via the bridge handler because the device handler is trying to keep the absolute
        // and percentage temperature channels in sync.
        deviceHandler.setTemperature(bridgeHandler, new DecimalType(6500));
    }
}
