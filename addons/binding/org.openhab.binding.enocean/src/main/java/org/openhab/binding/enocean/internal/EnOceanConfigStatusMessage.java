/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public enum EnOceanConfigStatusMessage {
    PORT_MISSING("missing-port-configuration"),
    SENDERID_MISSING("missing-senderId-configuration"),
    SENDERID_MALFORMED("malformed-senderId-configuration");

    private String messageKey;

    private EnOceanConfigStatusMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}