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
package org.openhab.binding.sonyprojector.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SonyProjectorException} class is used for any exception thrown by the binding
 *
 * @author Markus Wehrle - Initial contribution
 */
@NonNullByDefault
public class SonyProjectorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SonyProjectorException(String message) {
        super(message);
    }

    public SonyProjectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
