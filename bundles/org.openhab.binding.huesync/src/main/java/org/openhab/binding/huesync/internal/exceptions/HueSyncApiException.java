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
package org.openhab.binding.huesync.internal.exceptions;

import org.openhab.binding.huesync.internal.i18n.HueSyncLocalizer;

/**
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class HueSyncApiException extends Exception {
    private String key;

    public HueSyncApiException(String message) {
        super(message);

        if (message.startsWith("@text")) {
            key = message;
        }
    }

    public String getLogMessage() {
        return this.key == null ? this.getMessage() : HueSyncLocalizer.getResourceString(key);
    }
}
