/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sinope.internal;

/**
 * The {@link SinopeConfigStatusMessage} defines
 * the keys to be used for {@link org.openhab.core.config.core.status.ConfigStatusMessage}s.
 *
 * @author Pascal Larin - Initial Contribution
 *
 */
public enum SinopeConfigStatusMessage {
    HOST_MISSING("missing-host-configuration"),
    PORT_MISSING("missing-port-configuration"),
    GATEWAY_ID_INVALID("invalid-gateway-id-configuration"),
    API_KEY_INVALID("invalid-api-key-configuration");

    private String messageKey;

    private SinopeConfigStatusMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
