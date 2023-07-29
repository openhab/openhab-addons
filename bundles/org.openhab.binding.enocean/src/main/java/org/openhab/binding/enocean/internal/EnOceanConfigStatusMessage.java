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
package org.openhab.binding.enocean.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
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
