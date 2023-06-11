/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.openuv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Will be thrown for cloud errors
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVException extends Exception {
    private static final long serialVersionUID = -1411477662081482350L;
    private static final String ERROR_QUOTA_EXCEEDED = "Daily API quota exceeded";
    private static final String ERROR_WRONG_KEY = "User with API Key not found";

    public OpenUVException(String message) {
        super(message);
    }

    private boolean checkMatches(String message) {
        String currentMessage = getMessage();
        return currentMessage != null && currentMessage.startsWith(message);
    }

    public boolean isApiKeyError() {
        return checkMatches(ERROR_WRONG_KEY);
    }

    public boolean isQuotaError() {
        return checkMatches(ERROR_QUOTA_EXCEEDED);
    }
}
