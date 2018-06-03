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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmParamsetType;

/**
 * Parses a event received from a Homematic gateway.
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
            String[] configParts = StringUtils.trimToEmpty(addressWithChannel).split(":");
            address = getSanitizedAddress(configParts[0]);
            if (configParts.length > 1) {
                channel = NumberUtils.createInteger(configParts[1]);
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
