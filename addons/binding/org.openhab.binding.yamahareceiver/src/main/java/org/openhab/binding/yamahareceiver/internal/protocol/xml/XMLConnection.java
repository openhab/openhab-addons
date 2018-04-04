/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

    public final static String XML_GET = "<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"GET\">";
    public final static String XML_PUT = "<?xml version=\"1.0\" encoding=\"utf-8\"?><YAMAHA_AV cmd=\"PUT\">";
    public final static String XML_END = "</YAMAHA_AV>";

    public XMLConnection(String host) {
        super(host);
    }

    /**
     * Post the given xml message
     *
     * @param message XML formatted message excluding < ?xml > or <YAMAHA_AV> tags.
     * @throws IOException
     */
    @Override
    public void send(String message) throws IOException {
        HttpURLConnection connection = null;
        if (message.startsWith("<?xml")) {
            throw new IOException("No preformatted xml allowed!");
        }

        message = XML_PUT + message + XML_END;

        writeTraceFile(message);

        try {
            URL url = createCrlUrl();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(message.length()));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            try {
                wr.writeBytes(message);
                wr.flush();
            } finally {
                wr.close();
            }

            if (connection.getResponseCode() != 200) {
                throw new IOException("Changing a value on the Yamaha AVR failed: " + message);
            }

        } catch (IOException e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Post the given xml message and return the response as string.
     *
     * @param message XML formatted message excluding <?xml> or <YAMAHA_AV> tags.
     * @return Return the response as text or throws an exception if the connection failed.
     * @throws IOException
     */
    @Override
    public String sendReceive(String message) throws IOException {
        HttpURLConnection connection = null;
        if (message.startsWith("<?xml")) {
            throw new IOException("No preformatted xml allowed!");
        }

        message = XML_GET + message + XML_END;

        writeTraceFile(message);

        try {
            URL url = createCrlUrl();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(message.length()));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            try {
                wr.writeBytes(message);
                wr.flush();
            } finally {
                wr.close();
            }

            // Read response
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            try {
                String line;
                StringBuffer responseBuffer = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    responseBuffer.append(line);
                    responseBuffer.append('\r');
                }
                String response = responseBuffer.toString();

                writeTraceFile(message);
                return response;
            } finally {
                rd.close();
            }
        } catch (Exception e) {
            logger.warn("post failed on: {}", message);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Creates an {@link URL} object to the Yamaha control endpoint
     * @return
     * @throws MalformedURLException
     */
    private URL createCrlUrl() throws MalformedURLException {
        return new URL("http://" + host + "/YamahaRemoteControl/ctrl");
    }
}
