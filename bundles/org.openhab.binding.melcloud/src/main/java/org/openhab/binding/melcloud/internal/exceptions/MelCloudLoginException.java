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
package org.openhab.binding.melcloud.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception to encapsulate any login issues with MELCloud.
 *
 * @author Pauli Anttila - Initial Contribution
 */
@NonNullByDefault
public class MelCloudLoginException extends Exception {
    private static final long serialVersionUID = 1L;

    public MelCloudLoginException(Throwable cause) {
        super("Error occurred during login to MELCloud", cause);
    }

    public MelCloudLoginException(String message) {
        super(message);
    }

    public MelCloudLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
