/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.oppo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OppoException} class is used for any exception thrown by the binding
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class OppoException extends Exception {
    private static final long serialVersionUID = 1L;

    public OppoException() {
    }

    public OppoException(String message, Throwable t) {
        super(message, t);
    }

    public OppoException(String message) {
        super(message);
    }
}
