/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpResponse.
 *
 * @author Tim Roberts - Initial contribution
 */
public class HttpResponse {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    /** The http status. */
    private final int _httpStatus;

    /** The http reason. */
    private final String _httpReason;

    /** The headers. */
    private final Map<String, String> _headers = new HashMap<String, String>();

    /** The encoding. */
    private final String _encoding;

    /** The contents. */
    private final byte[] _contents;

    /**
     * Instantiates a new http response.
     *
     * @param response the response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    HttpResponse(Response response) throws IOException {
        _httpStatus = response.getStatus();
        _httpReason = response.getStatusInfo().getReasonPhrase();

        _encoding = null;
        if (response.hasEntity()) {
            // logger.debug(">>> has entity");
            InputStream is = response.readEntity(InputStream.class);
            if (is == null) {
                // logger.debug(">>> no inputstream - getting string");
                final String str = response.readEntity(String.class);
                _contents = str.getBytes(Charset.forName("utf-8"));
            } else {
                // logger.debug(">>> inputstream!");
                _contents = IOUtils.toByteArray(is);
            }
        } else {
            // logger.debug(">>> no entity!");
            _contents = null;
        }

        for (String key : response.getHeaders().keySet()) {
            _headers.put(key, response.getHeaderString(key));
        }
    }

    /**
     * Instantiates a new http response.
     *
     * @param httpCode the http code
     * @param msg the msg
     */
    public HttpResponse(int httpCode, String msg) {
        _httpStatus = httpCode;
        _httpReason = msg;
        _encoding = null;
        _contents = null;
    }

    /**
     * Gets the http code.
     *
     * @return the http code
     */
    public int getHttpCode() {
        return _httpStatus;
    }

    /**
     * Gets the response header.
     *
     * @param headerName the header name
     * @return the response header
     */
    public String getResponseHeader(String headerName) {
        return _headers == null ? null : _headers.get(headerName);
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    public String getContent() {
        if (_contents == null) {
            logger.debug(">>> contents null!");
            return "";
        }

        String encoding = _encoding;
        if (StringUtils.isEmpty(encoding)) {
            encoding = "utf-8";
        }

        // Workaround to bug in jetty when encoding includes double quotes "utf-8" vs utf-8
        final Charset charSet = Charset.forName(encoding.replaceAll("\"", ""));

        logger.debug(">>> contents: {}: {}", _contents.length, new String(_contents, charSet));
        return new String(_contents, charSet);
    }

    /**
     * Gets the content as bytes.
     *
     * @return the content as bytes
     */
    public byte[] getContentAsBytes() {
        return _contents;
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
        if (getHttpCode() != HttpStatus.SC_OK) {
            throw createException();
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final String content = getContent();
        if (content == null) {
            return builder.newDocument();
        }

        final InputSource inputSource = new InputSource(new StringReader(content));
        return builder.parse(inputSource);
    }

    /**
     * Creates the exception.
     *
     * @return the IO exception
     */
    public IOException createException() {
        return new IOException(_httpReason);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getHttpCode() + " (" + (_contents == null ? ("http reason: " + _httpReason) : getContent()) + ")";
    }
}
