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
package org.openhab.io.neeo.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents an {@link HttpRequest} response
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class HttpResponse {

    /** The http status */
    private final int httpStatus;

    /** The http reason */
    private final String httpReason;

    /** The http headers */
    private final Map<String, String> headers = new HashMap<>();

    /** The contents as a raw byte array */
    private final byte @Nullable [] contents;

    /**
     * Instantiates a new http response from the {@link Response}.
     *
     * @param response the non-null response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    HttpResponse(Response response) throws IOException {
        Objects.requireNonNull(response, "response cannot be null");

        httpStatus = response.getStatus();
        httpReason = response.getStatusInfo().getReasonPhrase();

        if (response.hasEntity()) {
            contents = response.readEntity(InputStream.class).readAllBytes();
        } else {
            contents = null;
        }

        for (String key : response.getHeaders().keySet()) {
            headers.put(key, response.getHeaderString(key));
        }
    }

    /**
     * Instantiates a new http response.
     *
     * @param httpCode the http code
     * @param msg the msg
     */
    HttpResponse(int httpCode, String msg) {
        httpStatus = httpCode;
        httpReason = msg;
        contents = null;
    }

    /**
     * Gets the HTTP status code.
     *
     * @return the HTTP status code
     */
    public int getHttpCode() {
        return httpStatus;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    public String getContent() {
        final byte[] localContents = contents;
        if (localContents == null || localContents.length == 0) {
            return "";
        }

        return new String(localContents, StandardCharsets.UTF_8);
    }

    /**
     * Creates an {@link IOException} from the {@link #httpReason}
     *
     * @return the IO exception
     */
    public IOException createException() {
        return new IOException(httpReason);
    }

    @Override
    public String toString() {
        return getHttpCode() + " (" + (contents == null ? ("http reason: " + httpReason) : getContent()) + ")";
    }
}
