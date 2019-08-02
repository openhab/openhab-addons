/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sinope.internal;

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;

/**
 * The {@link SinopeConfigStatusMessage} defines
 * the keys to be used for {@link ConfigStatusMessage}s.
 *
 * @author Pascal Larin
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
