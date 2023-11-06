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
 * Parses a listBidcosInterfaces message and extracts the type and gateway address.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ListBidcosInterfacesParser extends CommonRpcParser<Object[], ListBidcosInterfacesParser> {
    private String type;
    private String gatewayAddress;
    private String firmware;
    private Integer dutyCycleRatio;

    @SuppressWarnings("unchecked")
    @Override
    public ListBidcosInterfacesParser parse(Object[] message) throws IOException {
        if (message != null && message.length > 0) {
            message = (Object[]) message[0];
            for (int i = 0; i < message.length; i++) {
                Map<String, ?> mapMessage = (Map<String, ?>) message[i];
                boolean isDefault = toBoolean(mapMessage.get("DEFAULT"));

                if (isDefault) {
                    type = toString(mapMessage.get("TYPE"));
                    firmware = toString(mapMessage.get("FIRMWARE_VERSION"));
                    gatewayAddress = getSanitizedAddress(mapMessage.get("ADDRESS"));
                    dutyCycleRatio = toInteger(mapMessage.get("DUTY_CYCLE"));
                }
            }
        }
        return this;
    }

    /**
     * Returns the parsed type.
     */
    public String getType() {
        return type == null ? "" : type;
    }

    /**
     * Returns the parsed gateway address.
     */
    public String getGatewayAddress() {
        return gatewayAddress;
    }

    /**
     * Returns the firmware version.
     */
    public String getFirmware() {
        return firmware == null ? "" : firmware;
    }

    /**
     * Returns the duty cycle.
     */
    public Integer getDutyCycleRatio() {
        return dutyCycleRatio;
    }
}
