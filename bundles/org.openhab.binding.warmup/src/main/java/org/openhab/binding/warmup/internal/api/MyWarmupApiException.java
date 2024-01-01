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
package org.openhab.binding.warmup.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown in case of api problems.
 *
 * @author James Melville - Initial contribution
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class MyWarmupApiException extends Exception {

    public MyWarmupApiException(@Nullable String message) {
        super(message);
    }

    public MyWarmupApiException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public MyWarmupApiException(@Nullable Throwable cause) {
        super(cause);
    }
}
