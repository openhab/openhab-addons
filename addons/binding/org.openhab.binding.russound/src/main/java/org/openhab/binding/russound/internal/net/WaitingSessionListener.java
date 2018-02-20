/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.net;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link SocketSessionListener} that allows a caller to wait for a response via
 * {@link #getResponse()}
 *
 * @author Tim Roberts
 */
public class WaitingSessionListener implements SocketSessionListener {

    /**
     * Cache of responses that have occurred
     */
    private BlockingQueue<Object> responses = new ArrayBlockingQueue<Object>(5);

    /**
     * Will return the next response from {@link #responses}. If the response is an exception, that exception will
     * be thrown instead.
     *
     * @return a non-null, possibly empty response
     * @throws IOException an IO exception occurred during reading
     * @throws InterruptedException an interrupted exception occurred during reading
     */
    public String getResponse() throws IOException, InterruptedException {
        // note: russound is inherently single threaded even though it accepts multiple connections
        // if we have another thread sending a lot of commands (such as during startup), our response
        // will not come in until the other commands have been processed. So we need a large wait
        // time for it to be sent to us
        final Object lastResponse = responses.poll(60, TimeUnit.SECONDS);
        if (lastResponse instanceof String) {
            return (String) lastResponse;
        } else if (lastResponse instanceof IOException) {
            throw (IOException) lastResponse;
        } else if (lastResponse == null) {
            throw new IOException("Didn't receive response in time");
        } else {
            return lastResponse.toString();
        }
    }

    @Override
    public void responseReceived(String response) throws InterruptedException {
        responses.put(response);
    }

    @Override
    public void responseException(IOException e) throws InterruptedException {
        responses.put(e);
    }
}
