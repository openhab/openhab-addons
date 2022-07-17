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
package org.openhab.binding.argoclima.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The class {@code ArgoLocalApiCommunicationException} is thrown in case of any issues when communicating with the Argo
 * HVAC device - specifically in a direct/local mode
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoLocalApiCommunicationException extends ArgoApiCommunicationException {

    private static final long serialVersionUID = 7770599701572999260L;

    public ArgoLocalApiCommunicationException(@Nullable String message) {
        super(message);
    }

    public ArgoLocalApiCommunicationException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
