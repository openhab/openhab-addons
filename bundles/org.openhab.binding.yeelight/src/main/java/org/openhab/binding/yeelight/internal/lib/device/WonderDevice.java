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

import org.openhab.binding.yeelight.internal.lib.device.connection.WifiConnection;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link WonderDevice} contains methods for handling the color bulb device.
 *
 * @author Coaster Li - Initial contribution
 */
public class WonderDevice extends DeviceBase {
    private final Logger logger = LoggerFactory.getLogger(WonderDevice.class);

    public WonderDevice(String id) {
        super(id);
        mDeviceType = DeviceType.color;
        mConnection = new WifiConnection(this);
        mMinCt = 1700;
        mMaxCt = 6500;
    }

    @Override
    public void onNotify(String msg) {
        JsonObject result = JsonParser.parseString(msg).getAsJsonObject();
        try {
            if (result.has("id")) {
                String id = result.get("id").getAsString();
                // for cmd transaction.

                if (mQueryList.contains(id)) {
                    mQueryList.remove(id);
                    // DeviceMethod(MethodAction.PROP,
                    // new Object[] { "power", "name", "bright", "ct", "rgb", "hue", "sat" });
                    JsonArray status = result.get("result").getAsJsonArray();

                    // power:
                    if ("\"off\"".equals(status.get(0).toString())) {
                        mDeviceStatus.setPowerOff(true);
                    } else if ("\"on\"".equals(status.get(0).toString())) {
                        mDeviceStatus.setPowerOff(false);
                    }

                    // name:
                    mDeviceStatus.setName(status.get(1).getAsString());

                    // brightness:
                    mDeviceStatus.setBrightness(status.get(2).getAsInt());

                    // ct:
                    mDeviceStatus.setCt(status.get(3).getAsInt());

                    // color:
                    int color = status.get(4).getAsInt();
                    mDeviceStatus.setColor(color);
                    mDeviceStatus.setR((color >> 16) & 0xFF);
                    mDeviceStatus.setG((color >> 8) & 0xFF);
                    mDeviceStatus.setB(color & 0xFF);
                    mDeviceStatus.setColor(color);
                    mDeviceStatus.setHue(status.get(5).getAsInt());
                    mDeviceStatus.setSat(status.get(6).getAsInt());
                }
            }
        } catch (Exception e) {
            logger.debug("Exception", e);
        }

        super.onNotify(msg);
    }
}
