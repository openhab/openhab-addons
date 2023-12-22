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
package org.openhab.binding.salus.internal.rest;

import java.io.IOException;
import java.io.Serial;
import java.io.UncheckedIOException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@SuppressWarnings("SerializableHasSerializationMethods")
final class HttpIOException extends UncheckedIOException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HttpIOException(String method, String url, IOException cause) {
        super("Exception occurred when querying URL " + method + " " + url + "!", cause);
    }
}