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
package org.openhab.binding.unifi.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link UniFiException} represents a binding specific {@link Exception}.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiException extends Exception {

    private static final long serialVersionUID = -7422254981644510570L;

    public UniFiException(final String message) {
        super(message);
    }

    public UniFiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UniFiException(final @Nullable Throwable cause) {
        super(cause);
    }
}
