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
package org.openhab.binding.livisismarthome.internal.client.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown, if the session is not initialized or disconnected.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public class SessionNotFoundException extends ApiException {

    private static final long serialVersionUID = -2621323485648025577L;

    public SessionNotFoundException(String message) {
        super(message);
    }
}
