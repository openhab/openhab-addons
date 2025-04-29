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
package org.openhab.binding.emby.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Custom exception class to be thrown when number of retries is exceeded.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyHttpRetryExceeded extends Exception {

    // Properly handle serialization warning instead of suppressing it
    private static final long serialVersionUID = 1L;

    public EmbyHttpRetryExceeded(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
