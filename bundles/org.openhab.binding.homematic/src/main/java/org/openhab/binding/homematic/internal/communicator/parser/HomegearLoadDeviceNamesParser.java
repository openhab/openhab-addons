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
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.homematic.internal.model.HmDevice;

/**
 * Parses a Homegear message containing names for devices.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomegearLoadDeviceNamesParser extends CommonRpcParser<Object[], Void> {
    private Collection<HmDevice> devices;

    public HomegearLoadDeviceNamesParser(Collection<HmDevice> devices) {
        this.devices = devices;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Void parse(Object[] message) throws IOException {
        Map<String, HmDevice> devicesById = new HashMap<>();
        for (HmDevice device : devices) {
            devicesById.put(device.getHomegearId(), device);
        }

        message = (Object[]) message[0];
        for (int i = 0; i < message.length; i++) {
            Map<String, ?> data = (Map<String, ?>) message[i];
            String id = toString(data.get("ID"));
            String name = toString(data.get("NAME"));

            HmDevice device = devicesById.get(getSanitizedAddress(id));
            if (device != null) {
                device.setName(name);
            }

        }

        return null;
    }
}
