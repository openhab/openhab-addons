/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The class encapsulates an http response and provides helper methods
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class HttpResponse {

    /** The relation constant for NEXT */
    public static final String REL_NEXT = "next";

    /** The encoding being used */
    private static final String ENCODING = "utf-8";

    /** The character set for the encoding */
    private static final Charset CHARSET = Charset.forName(ENCODING);

    /** The error pattern identifying a SOAP error (and provides groups to get the code/desc) */
    private static final Pattern SOAPERRORPATTERN = Pattern.compile(
            ".*<errorCode>(.*)</errorCode>.*<errorDescription>(.*)</errorDescription>.*",
            Pattern.DOTALL | Pattern.MULTILINE);

    /** The http status code */
    private final int httpStatus;

    /** The http reason */
    private final @Nullable String httpReason;

    /** The headers from the response */
    private final Map<String, String> headers = new HashMap<>();

    /** The contents of the response */
    private final byte @Nullable [] contents;

    /** Map of relation to URI for any links shown (may be empty but never null) */
    private final Map<String, URI> links;

    /**
     * Instantiates a new http response from the specified {@link Response}
     *
     * @param response the non-null response
     * @throws IOException if an IO exception occurs reading from the client
     */
    HttpResponse(final Response response) throws IOException {
        Objects.requireNonNull(response, "response cannot be null");

        httpStatus = response.getStatus();
        httpReason = response.getStatusInfo().getReasonPhrase();

        if (response.hasEntity()) {
            final InputStream is = response.readEntity(InputStream.class);
            contents = IOUtils.toByteArray(is);
        } else {
            contents = null;
        }

        for (final String key : response.getHeaders().keySet()) {
            headers.put(key, response.getHeaderString(key));
        }

        links = response.getLinks().stream().collect(Collectors.toMap(k -> k.getRel(), v -> v.getUri()));
    }

    /**
     * Instantiates a new http response from the given http code and message
     *
     * @param httpCode the http code
     * @param msg the possibly null, possibly empty msg
     */
    public HttpResponse(final int httpCode, final @Nullable String msg) {
        httpStatus = httpCode;
        httpReason = msg;
        contents = null;
        links = new HashMap<>();
    }

    /**
     * Gets the http status code
     *
     * @return the http status code
     */
    public int getHttpCode() {
        return httpStatus;
    }

    /**
     * Gets the response header corresponding to the name
     *
     * @param headerName the non-null, non-empty header name
     * @return the response header or null if none
     */
    public @Nullable String getResponseHeader(final String headerName) {
        Validate.notEmpty(headerName, "headerName cannot be empty");
        return headers.get(headerName);
    }

    /**
     * Gets the content or an empty string if no content
     *
     * @return the content or an empty string if no content
     */
    public String getContent() {
        if (contents == null) {
            return "";
        }

        return new String(contents, CHARSET);
    }

    /**
     * Returns the http reason
     *
     * @return the http reason or null if none
     */
    public @Nullable String getHttpReason() {
        return httpReason;
    }

    /**
     * Gets the content as bytes or null if no content
     *
     * @return a possibly null content as bytes
     */
    public byte @Nullable [] getContentAsBytes() {
        return contents;
    }

    /**
     * Gets the content as xml.
     *
     * @return the content as xml
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Document getContentAsXml() throws ParserConfigurationException, SAXException, IOException {
        if (getHttpCode() != HttpStatus.OK_200) {
            throw createException();
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final String content = getContent();
        if (StringUtils.isEmpty(content)) {
            return builder.newDocument();
        }

        final InputSource inputSource = new InputSource(new StringReader(content));
        return builder.parse(inputSource);
    }

    /**
     * A poor mans attempt to parse out the error code/error description from a SOAP response (don't need the full SOAP
     * stack)
     *
     * @param xml a possibly null, possibly empty XML to parse
     * @return a SOAPError if found, null otherwise
     */
    public @Nullable SOAPError getSOAPError() {
        if (StringUtils.isEmpty(httpReason)) {
            return null;
        }

        final Matcher m = SOAPERRORPATTERN.matcher(httpReason);
        if (m.find() && m.groupCount() >= 2) {
            final String code = m.group(1);
            final String desc = m.group(2);

            if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(desc)) {
                return new SOAPError(code, desc);
            }
        }
        final Matcher m2 = SOAPERRORPATTERN.matcher(getContent());
        if (m2.find() && m2.groupCount() >= 2) {
            final String code = m2.group(1);
            final String desc = m2.group(2);

            if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(desc)) {
                return new SOAPError(code, desc);
            }
        }
        return null;
    }

    /**
     * Returns the link associated with the relation
     * 
     * @param rel a non-null, non-empty relation
     * @return a possibly null URI associated with the relation
     */
    public @Nullable URI getLink(final String rel) {
        Validate.notEmpty(rel, "rel cannot be empty");
        return links == null ? null : links.get(rel);
    }

    /**
     * Creates the exception from the http reason
     *
     * @return the IO exception representing the http reason
     */
    public IOException createException() {
        return new IOException(httpReason);
    }

    @Override
    public String toString() {
        return getHttpCode() + " (" + (contents == null ? ("http reason: " + httpReason) : getContent()) + ")";
    }

    /**
     * This class represents a SOAP error
     */
    @NonNullByDefault
    public class SOAPError {
        /** The soap error code */
        private final String soapCode;

        /** The soap error description */
        private final String soapDescription;

        /**
         * Creates the soap error from the code/description
         *
         * @param soapCode the non-null, non-empty SOAP error code
         * @param soapDescription the non-null, non-empty SOAP error description
         */
        private SOAPError(final String soapCode, final String soapDescription) {
            Validate.notEmpty(soapCode, "soapCode cannot be empty");
            Validate.notEmpty(soapDescription, "soapDescription cannot be empty");
            this.soapCode = soapCode;
            this.soapDescription = soapDescription;
        }

        /**
         * Returns the SOAP error code
         *
         * @return the non-null, non-empty SOAP error code
         */
        public String getSoapCode() {
            return soapCode;
        }

        /**
         * Returns the SOAP error description
         *
         * @return the non-null, non-empty SOAP error description
         */
        public String getSoapDescription() {
            return soapDescription;
        }
    }
}
