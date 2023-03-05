/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown, when the LIVISI SmartHome Client API returns an unknown error.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@NonNullByDefault
public class ApiException extends IOException {

    private static final long serialVersionUID = -3581569381976159265L;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
