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
package org.openhab.binding.wled.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ApiException} will be thrown whenever the wled API can not successfully communicate with the device.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class ApiException extends Exception {
    /**
     * Serial ID of this error class.
     */
    private static final long serialVersionUID = 1238256795216449L;

    /**
     * Basic constructor allowing the storing of a single message.
     *
     * @param message Descriptive message about the error.
     */
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable e) {
        super(message, e);
    }
}
