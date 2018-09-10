/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv.internal.protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteController} is responsible for sending key codes to Panasonic TV
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@Deprecated
public class RemoteController {

    private static final int CONNECTION_TIMEOUT = 500;

    private final Logger logger = LoggerFactory.getLogger(RemoteController.class);

    private String host;
    private int port;

    /**
     * Create and initialize remote controller instance.
     *
     * @param host     Host name of the TV.
     * @param port     TCP port of the remote controller protocol.
     * @param appName  Application name used to send key codes.
     * @param uniqueId Unique Id used to send key codes.
     */
    public RemoteController(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * This methods sends the command to the TV
     *
     * @return HTTP response code from the TV (should be 200)
     * @throws RemoteControllerException
     */
    private int sendKeyCode(String keyCode) throws RemoteControllerException {

        final String soaprequest_skeleton = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<s:Body><u:X_SendKey xmlns:u=\"urn:panasonic-com:service:p00NetworkControl:1\">"
                + "<X_KeyEvent>%s</X_KeyEvent></u:X_SendKey></s:Body></s:Envelope>\r";
        String soaprequest = String.format(soaprequest_skeleton, keyCode);

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
        } catch (Exception e) {
            throw new RemoteControllerException("Connection failed", e);
        }

        try {

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));

            String header = "POST /nrc/control_0/ HTTP/1.1\r\n";
            header = header + "Host: " + this.host + ":" + this.port + "\r\n";
            header = header + "SOAPACTION: \"urn:panasonic-com:service:p00NetworkControl:1#X_SendKey\"\r\n";
            header = header + "Content-Type: text/xml; charset=\"utf-8\"\r\n";
            header = header + "Content-Length: " + soaprequest.length() + "\r\n";
            header = header + "\r\n";

            String request = header + soaprequest;

            logger.debug("Request send to TV " + this.host + ": " + request);

            wr.write(header);
            wr.write(soaprequest);

            wr.flush();

            InputStream inFromServer = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inFromServer));

            String response = reader.readLine();

            logger.debug("TV Response from " + this.host + ": " + response);

            return Integer.parseInt(response.split(" ")[1]);
        } catch (IOException e) {
            logger.error("Exception during communication to the TV: " + e.getStackTrace());
        } catch (Exception e) {
            logger.error("Exception in binding during execution of command: " + e.getStackTrace());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
        }
        return 0;
    }
}
