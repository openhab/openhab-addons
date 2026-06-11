/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parses a listBidcosInterfaces message and extracts the type and gateway address.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class ListBidcosInterfacesParser extends CommonRpcParser<Object[], ListBidcosInterfacesParser> {
    private @Nullable String type;
    private @Nullable String gatewayAddress;
    private @Nullable String firmware;
    private @Nullable Integer dutyCycleRatio;

    @SuppressWarnings("unchecked")
    @Override
    public ListBidcosInterfacesParser parse(Object[] message) throws IOException {
        message = unWrapArray(message);
        for (int i = 0; i < message.length; i++) {
            Map<String, ?> mapMessage = (Map<String, ?>) message[i];
            Boolean isDefault = toBoolean(mapMessage.get("DEFAULT"));

            if (isDefault != null && isDefault) {
                type = toString(mapMessage.get("TYPE"));
                firmware = toString(mapMessage.get("FIRMWARE_VERSION"));
                gatewayAddress = getSanitizedAddress(mapMessage.get("ADDRESS"));
                dutyCycleRatio = toInteger(mapMessage.get("DUTY_CYCLE"));
            }
        }
        return this;
    }

    /**
     * Returns the parsed type.
     */
    public String getType() {
        return Objects.requireNonNullElse(type, "");
    }

    /**
     * Returns the parsed gateway address.
     */
    public @Nullable String getGatewayAddress() {
        return gatewayAddress;
    }

    /**
     * Returns the firmware version.
     */
    public String getFirmware() {
        return Objects.requireNonNullElse(firmware, "");
    }

    /**
     * Returns the duty cycle.
     */
    public @Nullable Integer getDutyCycleRatio() {
        return dutyCycleRatio;
    }
}
