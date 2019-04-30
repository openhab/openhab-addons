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
 * Exception for Lightify errors.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public class LightifyException extends Exception {

    private static final long serialVersionUID = 1L;

    public LightifyException() {
        super();
    }

    public LightifyException(String message) {
        super(message);
    }

    public LightifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightifyException(Throwable cause) {
        super(cause);
    }

}
