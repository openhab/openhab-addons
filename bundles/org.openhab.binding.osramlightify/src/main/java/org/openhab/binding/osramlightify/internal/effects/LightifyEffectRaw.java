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

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;

/**
 * Set an effect on a device.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyEffectRaw extends LightifyEffect {

    public LightifyEffectRaw(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name) {
        super(bridgeHandler, deviceHandler, name, false);
    }

    public LightifyEffectRaw(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name, boolean color, byte[] data) {
        super(bridgeHandler, deviceHandler, name, color, data);
    }

    @Override
    protected void param(String key, String value) {
        switch (key) {
            case "colour":
            case "color":
                color = true;
                break;

            case "white":
                color = false;
                break;

            case "data":
                setWritableData(DatatypeConverter.parseHexBinary(value.replaceAll("[:\\s]", "")));
                break;

            case "speed":
                int speed = (int) parseAbsoluteOrPercentage(value);
                if (speed < 0) {
                    speed = 0;
                } else if (speed > 255) {
                    speed = 255;
                }

                writeEnableData();

                for (int i = 0; i < NSTEPS; i++) {
                    setDuration(i, speed);
                }
                break;

            default:
                super.param(key, value);
                break;
        }
    }

    public void start() {
        super.start();
    }
}
