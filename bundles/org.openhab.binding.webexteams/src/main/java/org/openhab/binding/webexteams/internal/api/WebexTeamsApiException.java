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
package org.openhab.binding.webexteams.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webexteams.internal.WebexTeamsException;

/**
 * Signals a general exception with interacting with the Webex API.
 * 
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
public class WebexTeamsApiException extends WebexTeamsException {
    static final long serialVersionUID = 46L;

    public WebexTeamsApiException() {
    }

    public WebexTeamsApiException(String message) {
        super(message);
    }

    public WebexTeamsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
