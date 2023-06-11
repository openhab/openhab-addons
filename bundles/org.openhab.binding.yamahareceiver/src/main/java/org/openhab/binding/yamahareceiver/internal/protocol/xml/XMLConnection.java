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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Optional;

import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All other protocol classes in this directory use this class for communication. An object
 * of HttpXMLSendReceive is always bound to a specific host.
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Minor refactor
 *
 */
public class XMLConnection extends AbstractConnection {
    private Logger logger = LoggerFactory.getLogger(XMLConnection.class);

    private static final String XML_GET = "<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"GET\">";
    private static final String XML_PUT = "<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\">";
    private static final String XML_END = "</YAMAHA_AV>";
    private static final String HEADER_CHARSET_PART = "charset=";

    private static final int CONNECTION_TIMEOUT_MS = 5000;

    public XMLConnection(String host) {
        super(host);
    }

    @FunctionalInterface
    public interface CheckedConsumer<T, R> {
        R apply(T t) throws IOException;
    }

    private <T> T postMessage(String prefix, String message, String suffix,
            CheckedConsumer<HttpURLConnection, T> responseConsumer) throws IOException {
        if (message.startsWith("<?xml")) {
            throw new IOException("No pre-formatted xml allowed!");
        }
        message = prefix + message + suffix;

        writeTraceFile(message);

        URL url = createCrlUrl();
        logger.debug("Making POST to {} with payload: {}", url, message);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", Integer.toString(message.length()));

            // Set a timeout in case the device is not reachable (went offline)
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(message);
                wr.flush();
            }

            if (connection.getResponseCode() != 200) {
                throw new IOException("Changing a value on the Yamaha AVR failed: " + message);
            }

            return responseConsumer.apply(connection);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Post the given xml message
     *
     * @param message XML formatted message excluding < ?xml > or <YAMAHA_AV> tags.
     * @throws IOException
     */
    @Override
    public void send(String message) throws IOException {
        postMessage(XML_PUT, message, XML_END, c -> null);
    }

    /**
     * Post the given xml message and return the response as string.
     *
     * @param message XML formatted message excluding <?xml> or <YAMAHA_AV> tags.
     * @return Return the response as text or throws an exception if the connection failed.
     * @throws IOException
     */
    @Override
    public String sendReceive(final String message) throws IOException {
        return postMessage(XML_GET, message, XML_END, c -> consumeResponse(c));
    }

    private String consumeResponse(HttpURLConnection connection) throws IOException {
        // Read response

        Charset responseCharset = getResponseCharset(connection, StandardCharsets.UTF_8);
        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), responseCharset))) {
            String line;
            StringBuilder responseBuffer = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                responseBuffer.append(line);
                responseBuffer.append('\r');
            }
            String response = responseBuffer.toString();
            writeTraceFile(response);
            return response;
        }
    }

    public String getResponse(String path) throws IOException {
        URL url = createBaseUrl(path);
        logger.debug("Making GET to {}", url);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);

            if (connection.getResponseCode() != 200) {
                throw new IOException("Request failed");
            }

            return consumeResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Charset getResponseCharset(HttpURLConnection connection, Charset defaultCharset) {
        // See https://stackoverflow.com/a/3934280/1906057

        Charset charset = defaultCharset;

        String contentType = connection.getContentType();
        String[] values = contentType.split(";"); // values.length should be 2

        // Example:
        // Content-Type:text/xml; charset="utf-8"

        Optional<String> charsetName = Arrays.stream(values).map(x -> x.trim())
                .filter(x -> x.toLowerCase().startsWith(HEADER_CHARSET_PART))
                .map(x -> x.substring(HEADER_CHARSET_PART.length() + 1, x.length() - 1)).findFirst();

        if (charsetName.isPresent() && !charsetName.get().isEmpty()) {
            try {
                charset = Charset.forName(charsetName.get());
            } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
                logger.warn("The charset {} provided in the response {} is not supported", charsetName, contentType);
            }
        }

        logger.trace("The charset {} will be used to parse the response", charset);
        return charset;
    }

    /**
     * Creates an {@link URL} object to the Yamaha control endpoint
     *
     * @return
     * @throws MalformedURLException
     */
    private URL createCrlUrl() throws MalformedURLException {
        return createBaseUrl("/YamahaRemoteControl/ctrl");
    }

    /**
     * Creates an {@link URL} object to Yamaha
     *
     * @return
     * @throws MalformedURLException
     */
    private URL createBaseUrl(String path) throws MalformedURLException {
        return new URL("http://" + host + path);
    }
}
