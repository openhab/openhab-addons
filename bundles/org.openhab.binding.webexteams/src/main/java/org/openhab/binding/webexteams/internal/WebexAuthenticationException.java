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
 * Signals an issue with API authentication.
 * 
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
public class WebexAuthenticationException extends WebexTeamsException {
    static final long serialVersionUID = 44L;

    public WebexAuthenticationException() {
        super();
    }

    public WebexAuthenticationException(String msg) {
        super(msg);
    }

    public WebexAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }
}
