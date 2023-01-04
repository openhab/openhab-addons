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

import java.io.IOException;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;

/**
 * Parses an event received from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class EventParser extends CommonRpcParser<Object[], HmDatapointInfo> {
    private Object value;

    @Override
    public HmDatapointInfo parse(Object[] message) throws IOException {
        String address;
        Integer channel = 0;
        String addressWithChannel = toString(message[1]);
        if ("".equals(addressWithChannel)) {
            address = HmDevice.ADDRESS_GATEWAY_EXTRAS;
            channel = HmChannel.CHANNEL_NUMBER_VARIABLE;
        } else {
            String addrChannel = addressWithChannel == null ? "" : addressWithChannel.trim();
            String[] configParts = addrChannel.split(":");
            address = getSanitizedAddress(configParts[0]);
            if (configParts.length > 1) {
                channel = configParts[1] == null ? null : Integer.valueOf(configParts[1]);
            }
        }

        String name = toString(message[2]);
        value = message[3];

        return new HmDatapointInfo(address, HmParamsetType.VALUES, channel, name);
    }

    /**
     * Returns the value of the event.
     */
    public Object getValue() {
        return value;
    }
}
