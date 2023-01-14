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
package org.openhab.binding.max.internal.message;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceConfiguration;
import org.slf4j.Logger;

/**
 * The L message contains real time information about all MAX! devices.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 * @author Marcel Verpaalen - OH2 update
 *
 */
@NonNullByDefault
public final class LMessage extends Message {

    public LMessage(String raw) {
        super(raw);
    }

    public Collection<? extends Device> getDevices(List<DeviceConfiguration> configurations) {
        final List<Device> devices = new ArrayList<>();

        final byte[] decodedRawMessage = Base64.getDecoder()
                .decode(getPayload().trim().getBytes(StandardCharsets.UTF_8));

        final MaxTokenizer tokenizer = new MaxTokenizer(decodedRawMessage);

        while (tokenizer.hasMoreElements()) {
            byte[] token = tokenizer.nextElement();
            final Device tempDevice = Device.create(token, configurations);
            if (tempDevice != null) {
                devices.add(tempDevice);
            }
        }

        return devices;
    }

    public Collection<? extends Device> updateDevices(List<Device> devices, List<DeviceConfiguration> configurations) {
        byte[] decodedRawMessage = Base64.getDecoder().decode(getPayload().trim().getBytes(StandardCharsets.UTF_8));

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
        logger.trace("=== L Message === ");
        logger.trace("\tRAW: {}", this.getPayload());
    }

    @Override
    public MessageType getType() {
        return MessageType.L;
    }
}
