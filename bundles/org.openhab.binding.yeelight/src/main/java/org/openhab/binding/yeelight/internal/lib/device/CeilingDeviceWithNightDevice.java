/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.yeelight.internal.lib.device;

import org.openhab.binding.yeelight.internal.lib.enums.ActiveMode;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.openhab.binding.yeelight.internal.lib.enums.MethodAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link CeilingDeviceWithNightDevice} contains methods for handling the ceiling device with ambient light.
 *
 * @author Nikita Pogudalov - Initial contribution
 */
public class CeilingDeviceWithNightDevice extends CeilingDevice implements DeviceWithNightlight {
    private final Logger logger = LoggerFactory.getLogger(CeilingDeviceWithNightDevice.class);

    public CeilingDeviceWithNightDevice(String id) {
        super(id);

        mDeviceType = DeviceType.ceiling1;
    }

    @Override
    public void onNotify(String msg) {
        logger.debug("Got state: {}", msg);

        JsonObject result = JsonParser.parseString(msg).getAsJsonObject();

        if (result.has("id")) {
            String id = result.get("id").getAsString();
            // for cmd transaction.

            if (mQueryList.contains(id)) {
                JsonArray status = result.get("result").getAsJsonArray();

                final int activeMode = status.get(8).getAsInt();
                mDeviceStatus.setActiveMode(ActiveMode.values()[activeMode]);
            }
        }

        super.onNotify(msg);
    }

    @Override
    public void toggleNightlightMode(boolean turnOn) {
        if (turnOn) {
            mConnection.invoke(
                    new DeviceMethod(MethodAction.SCENE, new Object[] { "nightlight", mDeviceStatus.getBrightness() }));
        } else {
            mConnection.invoke(MethodFactory.buildCTMethod(mDeviceStatus.getCt(), DeviceMethod.EFFECT_SMOOTH, 500));
        }
    }
}
