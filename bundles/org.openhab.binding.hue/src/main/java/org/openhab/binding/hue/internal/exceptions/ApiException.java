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
package org.openhab.binding.hue.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when the API returns an unknown error.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class ApiException extends Exception {
    public ApiException() {
    }

    public ApiException(String message) {
        super(message);
    }
}
