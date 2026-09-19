/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.loqed.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Indicates a transport error while communicating with LOQED.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedCommunicationException extends LoqedApiException {
    private static final long serialVersionUID = 1L;

    public LoqedCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
