/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal.lib.device;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceFactory} creates device handler classes.
 *
 * @author Coaster Li - Initial contribution
 */
public class DeviceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceFactory.class);

    private static final String TAG = DeviceFactory.class.getSimpleName();

    public static DeviceBase build(String model, String id) {
        DeviceType type = DeviceType.valueOf(model);
        switch (type) {
            case ceiling:
                return new CeilingDevice(id);
            case ceiling1:
                return new CeilingDevice(id);
            case ceiling3:
                return new CeilingDevice(id);
            case color:
                return new WonderDevice(id);
            case mono:
                return new MonoDevice(id);
            case ct_bulb:
                return new CtBulbDevice(id);
            case stripe:
                return new PitayaDevice(id);
            default:
                return null;
        }
    }

    public static DeviceBase build(Map<String, String> bulbInfo) {
        DeviceType type = DeviceType.valueOf(bulbInfo.get("model"));
        DeviceBase device;
        switch (type) {
            case ceiling:
                device = new CeilingDevice(bulbInfo.get("id"));
                break;
            case ceiling1:
                device = new CeilingDevice(bulbInfo.get("id"));
                break;
            case ceiling3:
                device = new CeilingDevice(bulbInfo.get("id"));
                break;
            case color:
                device = new WonderDevice(bulbInfo.get("id"));
                break;
            case mono:
                device = new MonoDevice(bulbInfo.get("id"));
                break;
            case ct_bulb:
                device = new CtBulbDevice(bulbInfo.get("id"));
                break;
            case stripe:
                device = new PitayaDevice(bulbInfo.get("id"));
                break;
            default:
                return null;
        }
        Map<String, Object> infos = new HashMap<>();
        infos.putAll(bulbInfo);
        device.setBulbInfo(infos);
        LOGGER.debug("{}: DeviceFactory Device = {}", TAG, bulbInfo.get("Location"));
        // TODO enhancement!!!
        String[] addressInfo = bulbInfo.get("Location").split(":");
        device.setAddress(addressInfo[1].substring(2));
        device.setPort(Integer.parseInt(addressInfo[2]));
        device.setOnline(true);
        LOGGER.debug("{}: DeviceFactory Device info = {}, port = {}", TAG, device.getAddress(), device.getPort());
        return device;
    }

}
