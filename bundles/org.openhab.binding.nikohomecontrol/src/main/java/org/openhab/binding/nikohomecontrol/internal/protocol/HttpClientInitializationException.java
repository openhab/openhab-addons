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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link HttpClientInitializationException} used for HTTP connection exceptions.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class HttpClientInitializationException extends Exception {

    private static final long serialVersionUID = 6823948572039847561L;

    public HttpClientInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
