/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.benqprojector.internal.BenqProjectorBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.benqprojector.internal.BenqProjectorCommandException;
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

    static final String BLOCK_ITM = "Block item";
    static final String ILLEGAL_FMT = "Illegal format";

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
     * @throws BenqProjectorCommandException
     */
    String sendMessage(String data) throws BenqProjectorException, BenqProjectorCommandException;

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
     * @throws BenqProjectorCommandException
     */
    default String sendMsgReadResp(String data, @Nullable InputStream in, @Nullable OutputStream out)
            throws IOException, BenqProjectorException, BenqProjectorCommandException {
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

                    if (resp.contains(UNSUPPORTED_ITM)) {
                        return resp;
                    }

                    if (resp.contains(BLOCK_ITM)) {
                        throw new BenqProjectorCommandException("Block Item received for command: " + data);
                    }

                    if (resp.contains(ILLEGAL_FMT)) {
                        throw new BenqProjectorCommandException(
                                "Illegal Format response received for command: " + data);
                    }

                    // The response is fully received when the second '#' arrives
                    // example: *pow=?# *POW=ON#
                    if (resp.chars().filter(ch -> ch == '#').count() >= 2) {
                        return resp.replaceAll("[\\s\\r\\n*#>]", BLANK).replace(data, BLANK);
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
