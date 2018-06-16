/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.lib.device;

import java.util.HashMap;

import org.openhab.binding.yeelight.lib.CommonLogger;
import org.openhab.binding.yeelight.lib.enums.DeviceType;

public class DeviceFactory {

    private static final String TAG = DeviceFactory.class.getSimpleName();

    public static DeviceBase build(String model, String id) {
        DeviceType type = DeviceType.valueOf(model);
        switch (type) {
            case ceiling:
                return new CeilingDevice(id);
            case ceiling1:
                return new CeilingDevice(id);
            case color:
                return new WonderDevice(id);
            case mono:
                return new MonoDevice(id);
            case ct_bulb:
                return new MonoDevice(id);
            case stripe:
                return new PitayaDevice(id);
            default:
                return null;
        }
    }

    public static DeviceBase build(HashMap<String, String> bulbInfo) {
        DeviceType type = DeviceType.valueOf(bulbInfo.get("model"));
        DeviceBase device;
        switch (type) {
            case ceiling:
                device = new CeilingDevice(bulbInfo.get("id"));
                break;
            case ceiling1:
                device = new CeilingDevice(bulbInfo.get("id"));
                break;
            case color:
                device = new WonderDevice(bulbInfo.get("id"));
                break;
            case mono:
                device = new MonoDevice(bulbInfo.get("id"));
                break;
            case ct_bulb:
                device = new MonoDevice(bulbInfo.get("id"));
                break;
            case stripe:
                device = new PitayaDevice(bulbInfo.get("id"));
                break;
            default:
                return null;
        }
        HashMap<String, Object> infos = new HashMap<>();
        infos.putAll(bulbInfo);
        device.setBulbInfo(infos);
        CommonLogger.debug(TAG + ": DeviceFactory Device = " + bulbInfo.get("Location"));
        // TODO enhancement!!!
        String[] addressInfo = bulbInfo.get("Location").split(":");
        device.setAddress(addressInfo[1].substring(2));
        device.setPort(Integer.parseInt(addressInfo[2]));
        device.setOnline(true);
        CommonLogger
                .debug(TAG + ": DeviceFactory Device info = " + device.getAddress() + ", port = " + device.getPort());
        return device;
    }

}
