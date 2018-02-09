/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceConfiguration;
import org.slf4j.Logger;

/**
 * The L message contains real time information about all MAX! devices.
 *
 * @author Andreas Heil (info@aheil.de)
 * @author Marcel Verpaalen - OH2 update
 *
 * @since 1.4.0
 */
public final class L_Message extends Message {

    public L_Message(String raw) {
        super(raw);
    }

    public Collection<? extends Device> getDevices(List<DeviceConfiguration> configurations) {

        List<Device> devices = new ArrayList<Device>();

        byte[] decodedRawMessage = Base64.decodeBase64(getPayload().getBytes());

        MaxTokenizer tokenizer = new MaxTokenizer(decodedRawMessage);

        while (tokenizer.hasMoreElements()) {
            byte[] token = tokenizer.nextElement();
            Device tempDevice = Device.create(token, configurations);
            if (tempDevice != null) {
                devices.add(tempDevice);
            }
        }

        return devices;
    }

    public Collection<? extends Device> updateDevices(List<Device> devices, List<DeviceConfiguration> configurations) {

        byte[] decodedRawMessage = Base64.decodeBase64(getPayload().getBytes());

        MaxTokenizer tokenizer = new MaxTokenizer(decodedRawMessage);

        while (tokenizer.hasMoreElements()) {
            byte[] token = tokenizer.nextElement();
            String rfAddress = Utils.toHex(token[0] & 0xFF, token[1] & 0xFF, token[2] & 0xFF);
            // logger.debug("token: "+token+" rfaddress: "+rfAddress);

            Device foundDevice = null;
            for (Device device : devices) {
                // logger.debug(device.getRFAddress().toUpperCase()+ " vs "+rfAddress);
                if (device.getRFAddress().toUpperCase().equals(rfAddress)) {
                    // logger.debug("Updating device..."+rfAddress);
                    foundDevice = device;
                }
            }
            if (foundDevice != null) {
                foundDevice = Device.update(token, configurations, foundDevice);
                // devices.remove(token);
                // devices.add(foundDevice);
            } else {
                Device tempDevice = Device.create(token, configurations);
                if (tempDevice != null) {
                    devices.add(tempDevice);
                }
            }
        }

        return devices;
    }

    @Override
    public void debug(Logger logger) {
        logger.trace("=== L_Message === ");
        logger.trace("\tRAW: {}", this.getPayload());
    }

    @Override
    public MessageType getType() {
        return MessageType.L;
    }
}
