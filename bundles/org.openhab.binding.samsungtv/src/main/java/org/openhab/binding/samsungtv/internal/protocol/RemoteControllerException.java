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
package org.openhab.binding.samsungtv.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for Samsung TV communication
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class RemoteControllerException extends Exception {

    private static final long serialVersionUID = -5292218577704635666L;

    public RemoteControllerException(String message) {
        super(message);
    }

    public RemoteControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteControllerException(Throwable cause) {
        super(cause);
    }
}
