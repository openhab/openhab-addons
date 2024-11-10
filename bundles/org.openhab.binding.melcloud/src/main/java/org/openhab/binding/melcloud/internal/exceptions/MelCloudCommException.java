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
 * Exception to encapsulate any issues communicating with MELCloud.
 *
 * @author Pauli Anttila - Initial Contribution
 */
@NonNullByDefault
public class MelCloudCommException extends Exception {
    private static final long serialVersionUID = 1L;

    public MelCloudCommException(Throwable cause) {
        super("Error occurred when communicating with MELCloud", cause);
    }

    public MelCloudCommException(String message) {
        super(message);
    }

    public MelCloudCommException(String message, Throwable cause) {
        super(message, cause);
    }
}
