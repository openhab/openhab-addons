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
package org.openhab.binding.tuya.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SchemaReloadException} is thrown when a device schema cannot be reloaded from the cloud. The message
 * describes the reason in a form that can be shown to the user.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
public class SchemaReloadException extends Exception {
    private static final long serialVersionUID = 1L;

    public SchemaReloadException(String message) {
        super(message);
    }

    public SchemaReloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
