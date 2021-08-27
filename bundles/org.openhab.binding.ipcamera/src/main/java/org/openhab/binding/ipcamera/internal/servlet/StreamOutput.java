/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.ipcamera.internal.servlet;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link StreamOutput} Streams mjpeg out to a client
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class StreamOutput {
    private final HttpServletResponse response;
    private final String boundary = "thisMjpegStream";
    private final String contentType = "multipart/x-mixed-replace; boundary=" + boundary;
    private final ServletOutputStream output;
    private boolean connected = false;

    public StreamOutput(HttpServletResponse response) throws IOException {
        this.response = response;
        output = response.getOutputStream();
    }

    public void sendFrame(byte[] currentSnapshot) throws IOException {
        String header = "--" + boundary + "\r\n" + "Content-Type: image/jpeg" + "\r\n" + "Content-Length: "
                + currentSnapshot.length + "\r\n\r\n";
        if (!connected) {
            response.setContentType(contentType);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Expose-Headers", "*");
            // iOS needs to have two jpgs sent for the picture to appear instantly.
            output.write(header.getBytes());
            output.write(currentSnapshot);
            output.write("\r\n".getBytes());
            connected = true;
        }
        output.write(header.getBytes());
        output.write(currentSnapshot);
        output.write("\r\n".getBytes());
    }
}
