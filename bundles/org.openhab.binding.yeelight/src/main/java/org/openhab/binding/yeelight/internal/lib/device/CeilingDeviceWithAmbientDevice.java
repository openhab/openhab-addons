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
 * The {@link CeilingDeviceWithAmbientDevice} contains methods for handling the ceiling device with ambient light.
 *
 * @author Viktor Koop - Initial contribution
 */
public class CeilingDeviceWithAmbientDevice extends CeilingDevice
        implements DeviceWithAmbientLight, DeviceWithNightlight {
    private final Logger logger = LoggerFactory.getLogger(CeilingDeviceWithAmbientDevice.class);

    public CeilingDeviceWithAmbientDevice(String id) {
        super(id);

        mDeviceType = DeviceType.ceiling4;
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

                final String backgroundPowerState = status.get(4).toString();
                if ("\"off\"".equals(backgroundPowerState)) {
                    mDeviceStatus.setBackgroundIsPowerOff(true);
                } else if ("\"on\"".equals(backgroundPowerState)) {
                    mDeviceStatus.setBackgroundIsPowerOff(false);
                }

                final int backgroundBrightness = status.get(5).getAsInt();
                mDeviceStatus.setBackgroundBrightness(backgroundBrightness);

                final int backgroundHue = status.get(6).getAsInt();
                mDeviceStatus.setBackgroundHue(backgroundHue);

                final int backgroundSaturation = status.get(7).getAsInt();
                mDeviceStatus.setBackgroundSat(backgroundSaturation);

                final int activeMode = status.get(8).getAsInt();
                mDeviceStatus.setActiveMode(ActiveMode.values()[activeMode]);
            }
        }

        super.onNotify(msg);
    }

    @Override
    public void setBackgroundColor(int hue, int saturation, int duration) {
        mConnection
                .invoke(MethodFactory.buildBackgroundHSVMethod(hue, saturation, DeviceMethod.EFFECT_SMOOTH, duration));
    }

    @Override
    public void setBackgroundBrightness(int brightness, int duration) {
        mConnection
                .invoke(MethodFactory.buildBackgroundBrightnessMethd(brightness, DeviceMethod.EFFECT_SMOOTH, duration));
    }

    @Override
    public void setBackgroundPower(boolean on, int duration) {
        mConnection.invoke(new DeviceMethod(MethodAction.BG_SWITCH,
                new Object[] { on ? "on" : "off", DeviceMethod.EFFECT_SMOOTH, duration }));
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
