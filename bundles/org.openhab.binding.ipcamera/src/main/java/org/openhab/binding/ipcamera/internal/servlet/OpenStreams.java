/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenStreams} Keeps track of all open mjpeg streams so the byte[] can be given to all FIFO buffers to allow
 * 1 to many streams without needing to open more than 1 source stream.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class OpenStreams {
    private List<StreamOutput> openStreams = Collections.synchronizedList(new ArrayList<>());
    public String boundary = "thisMjpegStream";

    public synchronized void addStream(StreamOutput stream) {
        openStreams.add(stream);
    }

    public synchronized void removeStream(StreamOutput stream) {
        openStreams.remove(stream);
    }

    public synchronized int getNumberOfStreams() {
        return openStreams.size();
    }

    public synchronized boolean isEmpty() {
        return openStreams.isEmpty();
    }

    public synchronized void updateContentType(String contentType, String boundary) {
        this.boundary = boundary;
        for (StreamOutput stream : openStreams) {
            stream.updateContentType(contentType);
        }
    }

    public synchronized void queueFrame(byte[] frame) {
        for (StreamOutput stream : openStreams) {
            stream.queueFrame(frame);
        }
    }

    public synchronized void closeAllStreams() {
        for (StreamOutput stream : openStreams) {
            stream.close();
        }
        openStreams.clear();
    }
}
