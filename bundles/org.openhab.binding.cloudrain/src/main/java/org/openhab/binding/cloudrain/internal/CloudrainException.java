/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link CloudrainException} for notifying error situations in the binding
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainException extends Exception {

    private static final long serialVersionUID = 4006926323913996382L;

    /**
     * Creates a new Cloudrain Exception
     */
    public CloudrainException() {
        super();
    }

    /**
     * Creates a new Cloudrain Exception with message and cause
     *
     * @param message the exception message
     * @param cause the original throwable
     */
    public CloudrainException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new Cloudrain Exception with a message
     *
     * @param message the exception message
     */
    public CloudrainException(@Nullable String message) {
        super(message);
    }

    /**
     * Creates a new Cloudrain Exception with a causing Throwable
     *
     * @param cause the original throwable
     */
    public CloudrainException(@Nullable Throwable cause) {
        super(cause);
    }
}
