/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.http;

import static java.lang.String.format;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
final class HttpException extends RuntimeException {
    HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    HttpException(Request request, Throwable e) {
        super(format("Got exception while doing request to \"%s\" with headers \"%s\"", request.getPath(), formatHeaders(request)), e);
    }

    private static String formatHeaders(Request request) {
        return request.getHeaders()
                .stream()
                .map(header -> format("%s=%s", header.getKey(), header.getValue()))
                .reduce("", (acc, h) -> acc + ", " + h);
    }
}
