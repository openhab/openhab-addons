/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * The {@link CeilingDevice} contains methods for handling the ceiling device.
 *
 * @author Coaster Li - Initial contribution
 */
public class CeilingDevice extends DeviceBase {
    private final Logger logger = LoggerFactory.getLogger(CeilingDevice.class);

    public CeilingDevice(String id) {
        super(id);
        mDeviceType = DeviceType.ceiling;
        mConnection = new WifiConnection(this);
        mMinCt = 2700;
        mMaxCt = 6500;
    }

    @Override
    public void onNotify(String msg) {
        JsonObject result = new JsonParser().parse(msg).getAsJsonObject();
        try {
            String id = "-1";
            if (result.has("id")) {
                id = result.get("id").getAsString();
                // for cmd transaction.

                if (mQueryList.contains(id)) {
                    mQueryList.remove(id);
                    // DeviceMethod(MethodAction.PROP, new Object[] { "power", "name", "bright", "ct" });
                    JsonArray status = result.get("result").getAsJsonArray();

                    // power:
                    if (status.get(0).toString().equals("\"off\"")) {
                        mDeviceStatus.setPowerOff(true);
                    } else if (status.get(0).toString().equals("\"on\"")) {
                        mDeviceStatus.setPowerOff(false);
                    }

                    // name:
                    mDeviceStatus.setName(status.get(1).getAsString());

                    // brightness:
                    mDeviceStatus.setBrightness(status.get(2).getAsInt());

                    // ct:
                    mDeviceStatus.setCt(status.get(3).getAsInt());
                }
            }
        } catch (Exception e) {
            logger.debug("Problem setting values: {}", e);
        }

        super.onNotify(msg);
    }
}
