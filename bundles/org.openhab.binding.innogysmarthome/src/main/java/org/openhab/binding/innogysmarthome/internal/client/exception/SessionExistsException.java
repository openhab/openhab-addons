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
package org.openhab.binding.innogysmarthome.internal.client.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown, when a session already exists while initializing a new session.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public class SessionExistsException extends ApiException {

    private static final long serialVersionUID = 1L;

    public SessionExistsException() {
    }

    /**
     * @param message
     */
    public SessionExistsException(String message) {
        super(message);
    }
}
