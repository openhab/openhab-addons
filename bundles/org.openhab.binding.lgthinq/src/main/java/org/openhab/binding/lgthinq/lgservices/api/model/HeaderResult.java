/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HeaderResult} class
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class HeaderResult {
    private final String returnedCode;
    private final String returnedMessage;

    public HeaderResult(String returnedCode, String returnedMessage) {
        this.returnedCode = returnedCode;
        this.returnedMessage = returnedMessage;
    }

    public String getReturnedCode() {
        return returnedCode;
    }

    public String getReturnedMessage() {
        return returnedMessage;
    }
}
