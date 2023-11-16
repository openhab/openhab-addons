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
package org.openhab.binding.benqprojector.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.benqprojector.internal.BenqProjectorException;

/**
 * Base class for BenQ projector communication.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public interface BenqProjectorConnector {
    static final int TIMEOUT_MS = 5 * 1000;

    static final String START = "\r*";
    static final String END = "#\r";
    static final String BLANK = "";

    /**
     * Procedure for connecting to projector.
     *
     * @throws BenqProjectorException
     */
    void connect() throws BenqProjectorException;

    /**
     * Procedure for disconnecting to projector controller.
     *
     * @throws BenqProjectorException
     */
    void disconnect() throws BenqProjectorException;

    /**
     * Procedure for sending raw data to projector.
     *
     * @param data
     *            Message to send.
     *
     * @throws BenqProjectorException
     */
    String sendMessage(String data) throws BenqProjectorException;

    /**
     * Common method called by the Serial or Tcp connector to send the message to the projector, wait for a response and
     * return it after processing.
     *
     * @param data
     *            Message to send.
     * @param in
     *            The connector's input stream.
     * @param out
     *            The connector's output stream.
     *
     * @throws BenqProjectorException
     */
    default String sendMsgReadResp(String data, @Nullable InputStream in, @Nullable OutputStream out)
            throws IOException, BenqProjectorException {
        String resp = BLANK;

        if (in != null && out != null) {
            out.write((START + data + END).getBytes(StandardCharsets.US_ASCII));
            out.flush();

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0;

            while (elapsedTime < TIMEOUT_MS) {
                int availableBytes = in.available();
                if (availableBytes > 0) {
                    byte[] tmpData = new byte[availableBytes];
                    int readBytes = in.read(tmpData, 0, availableBytes);
                    resp = resp.concat(new String(tmpData, 0, readBytes, StandardCharsets.US_ASCII));
                    if (resp.contains(END)) {
                        return resp.replaceAll("[\\r\\n*#>]", BLANK).replace(data, BLANK);
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new BenqProjectorException(e);
                    }
                }

                elapsedTime = System.currentTimeMillis() - startTime;
            }
        }
        return resp;
    }
}
