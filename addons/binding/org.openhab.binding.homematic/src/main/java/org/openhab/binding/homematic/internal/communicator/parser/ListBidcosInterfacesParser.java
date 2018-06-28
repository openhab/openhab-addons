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
                }
            }
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
     * Returns the parsed gateway address.
     */
    public String getGatewayAddress() {
        return gatewayAddress;
    }

    /**
     * Returns the firmware version.
     */
    public String getFirmware() {
        return firmware;
    }
}
