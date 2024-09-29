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
package org.openhab.binding.webexteams.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Signals a general exception in the code.
 * 
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
public class WebexTeamsException extends Exception {
    static final long serialVersionUID = 43L;

    public WebexTeamsException() {
        super();
    }

    public WebexTeamsException(String message) {
        super(message);
    }

    public WebexTeamsException(String message, Throwable cause) {
        super(message, cause);
    }
}
