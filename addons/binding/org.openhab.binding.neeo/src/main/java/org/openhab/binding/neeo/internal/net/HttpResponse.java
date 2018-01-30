/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class represents an {@link HttpRequest} response
 *
 * @author Tim Roberts - Initial contribution
 */
public class HttpResponse {

    /** The http status */
    private final int httpStatus;

    /** The http reason */
    private final String httpReason;

    /** The http headers */
    private final Map<String, String> headers = new HashMap<>();

    /** The http encoding */
    private final String encoding;

    /** The contents as a raw byte array */
    private final byte[] contents;

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

        encoding = null;
        if (response.hasEntity()) {
            InputStream is = response.readEntity(InputStream.class);
            contents = IOUtils.toByteArray(is);
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
        encoding = null;
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
        if (contents == null) {
            return "";
        }

        // Workaround to bug in jetty when encoding includes double quotes "utf-8" vs utf-8
        String theEncoding = encoding;
        if (StringUtils.isEmpty(encoding)) {
            theEncoding = "utf-8";
        }
        final Charset charSet = Charset.forName(theEncoding.replaceAll("\"", ""));
        return new String(contents, charSet);
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
