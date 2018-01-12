/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmInterface;

/**
 * Parses a list devices message and generates device and channel metadata.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ListDevicesParser extends CommonRpcParser<Object[], Collection<HmDevice>> {
    private HmInterface hmInterface;
    private HomematicConfig config;

    public ListDevicesParser(HmInterface hmInterface, HomematicConfig config) {
        this.hmInterface = hmInterface;
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<HmDevice> parse(Object[] message) throws IOException {
        message = (Object[]) message[0];
        Map<String, HmDevice> devices = new HashMap<String, HmDevice>();

        for (int i = 0; i < message.length; i++) {
            Map<String, ?> data = (Map<String, ?>) message[i];
            boolean isDevice = !StringUtils.contains(toString(data.get("ADDRESS")), ":");

            if (isDevice) {
                HmDevice device = new HmDevice();
                device.setAddress(getAddress(data.get("ADDRESS")));
                device.setType(toString(data.get("TYPE")));
                device.setHomegearId(toString(data.get("ID")));
                device.setFirmware(toString(data.get("FIRMWARE")));
                device.setHmInterface(hmInterface);
                device.setGatewayId(config.getGatewayInfo().getId());

                devices.put(device.getAddress(), device);
            } else {
                // channel
                String deviceAddress = getAddress(data.get("PARENT"));
                HmDevice device = devices.get(deviceAddress);

                HmChannel channel = new HmChannel();
                channel.setNumber(toInteger(data.get("INDEX")));
                channel.setType(toString(data.get("TYPE")));

                device.addChannel(channel);
            }
        }
        return devices.values();
    }
}
