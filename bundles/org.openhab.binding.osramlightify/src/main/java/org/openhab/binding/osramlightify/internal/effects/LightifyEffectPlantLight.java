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

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;

import org.openhab.binding.osramlightify.internal.messages.LightifySetColorMessage;

/**
 * Set a "plant light" effect on a device.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyEffectPlantLight extends LightifyEffect {

    public LightifyEffectPlantLight(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name) {
        super(bridgeHandler, deviceHandler, name);
    }

    @Override
    public void start() {
        bridgeHandler.sendMessage(new LightifySetColorMessage(deviceHandler, new int [] { 0x99, 0x1a, 0x4d, 0xff }));
    }
}
