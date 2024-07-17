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
package org.openhab.binding.mffan.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RestApiException} is an exception thrown from the REST API.
 *
 * @author Mark Brooks - Initial contribution
 */
@NonNullByDefault
public class RestApiException extends Exception {
    private static final long serialVersionUID = -6340681561578357625L;

    public RestApiException(String message) {
        super(message);
    }

    public RestApiException(Throwable throwable) {
        super(throwable);
    }
}
