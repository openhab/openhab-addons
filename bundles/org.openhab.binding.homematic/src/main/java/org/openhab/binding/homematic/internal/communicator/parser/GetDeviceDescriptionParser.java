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
import java.util.Map;

/**
 * Parses a getDeviceDescription message and extracts the type and firmware version.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetDeviceDescriptionParser extends CommonRpcParser<Object[], GetDeviceDescriptionParser> {
    private String type;
    private String firmware;
    private String deviceInterface;

    @SuppressWarnings("unchecked")
    @Override
    public GetDeviceDescriptionParser parse(Object[] message) throws IOException {
        if (message != null && message.length > 0 && message[0] instanceof Map) {
            Map<String, ?> mapMessage = (Map<String, ?>) message[0];
            type = toString(mapMessage.get("TYPE"));
            firmware = toString(mapMessage.get("FIRMWARE"));
            deviceInterface = toString(mapMessage.get("INTERFACE"));
        }
        return this;
    }

    /**
     * Returns the parsed type.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the parsed firmware version.
     */
    public String getFirmware() {
        return firmware == null ? "" : firmware;
    }

    /**
     * Returns the interface of the device.
     */
    public String getDeviceInterface() {
        return deviceInterface;
    }
}
