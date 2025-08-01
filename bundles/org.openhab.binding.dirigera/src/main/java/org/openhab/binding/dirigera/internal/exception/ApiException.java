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
package org.openhab.binding.dirigera.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ApiException} thrown in case of problems accessing API
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = -9075334430125847975L;

    public ApiException(String message) {
        super(message);
    }
}
