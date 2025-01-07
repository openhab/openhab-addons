/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roku.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RokuLimitedModeException} extends RokuHttpException
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RokuLimitedModeException extends RokuHttpException {
    private static final long serialVersionUID = 1L;

    public RokuLimitedModeException(String errorMessage, Throwable t) {
        super(errorMessage, t);
    }

    public RokuLimitedModeException(String errorMessage) {
        super(errorMessage);
    }
}
