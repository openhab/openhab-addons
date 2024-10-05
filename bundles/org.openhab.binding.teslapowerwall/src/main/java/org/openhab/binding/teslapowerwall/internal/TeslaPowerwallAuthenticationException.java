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
package org.openhab.binding.teslapowerwall.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for when an unexpected response is received from the TeslaPowerwall controller.
 *
 * @author Paul Smedley - Initial contribution
 *
 */
@NonNullByDefault
public class TeslaPowerwallAuthenticationException extends Exception {
    private static final long serialVersionUID = 529232811860854017L;

    public TeslaPowerwallAuthenticationException(String message) {
        super(message);
    }

    public TeslaPowerwallAuthenticationException(Throwable ex) {
        super(ex);
    }

    public TeslaPowerwallAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
