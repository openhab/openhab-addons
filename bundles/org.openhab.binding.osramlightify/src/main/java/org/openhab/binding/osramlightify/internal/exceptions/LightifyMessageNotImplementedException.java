/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception raised when a received message has not been implemented.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyMessageNotImplementedException extends LightifyException {

    private static final long serialVersionUID = 1L;

    public LightifyMessageNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightifyMessageNotImplementedException(String message) {
        super(message);
    }
}
