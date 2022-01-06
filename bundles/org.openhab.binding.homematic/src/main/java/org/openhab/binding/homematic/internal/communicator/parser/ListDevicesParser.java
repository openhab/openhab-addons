/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.CONFIGURATION_CHANNEL_NUMBER;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
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
        Map<String, HmDevice> devices = new HashMap<>();

        for (int i = 0; i < message.length; i++) {
            Map<String, ?> data = (Map<String, ?>) message[i];
            if (MiscUtils.isDevice(toString(data.get("ADDRESS")), true)) {
                String address = getSanitizedAddress(data.get("ADDRESS"));
                String type = MiscUtils.validateCharacters(toString(data.get("TYPE")), "Device type", "-");
                String id = toString(data.get("ID"));
                String firmware = toString(data.get("FIRMWARE"));

                HmDevice device = new HmDevice(address, hmInterface, type, config.getGatewayInfo().getId(), id,
                        firmware);
                device.addChannel(new HmChannel(type, CONFIGURATION_CHANNEL_NUMBER));
                devices.put(address, device);
            } else {
                // channel
                String deviceAddress = getSanitizedAddress(data.get("PARENT"));
                HmDevice device = devices.get(deviceAddress);

                String type = toString(data.get("TYPE"));
                Integer number = toInteger(data.get("INDEX"));

                device.addChannel(new HmChannel(type, number));
            }
        }
        return devices.values();
    }
}
