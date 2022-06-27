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
package org.openhab.binding.vesync.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceUnknownException} is thrown if the device information could not be located for the address in
 * relation
 * to the API.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class DeviceUnknownException extends Exception {

    private static final long serialVersionUID = -7786425642285150557L;

    public DeviceUnknownException() {
        super();
    }

    public DeviceUnknownException(final String message) {
        super(message);
    }

    public DeviceUnknownException(final Throwable cause) {
        super(cause);
    }

    public DeviceUnknownException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
