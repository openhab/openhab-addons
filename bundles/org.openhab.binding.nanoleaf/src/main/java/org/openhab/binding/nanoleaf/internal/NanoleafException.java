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
package org.openhab.binding.nanoleaf.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * General binding exception if something goes wrong.
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class NanoleafException extends Exception {
    private static final long serialVersionUID = 1L;

    public NanoleafException(String message) {
        super(message);
    }

    public NanoleafException(final Throwable cause) {
        super(cause);
    }

    public NanoleafException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
