/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.stiebelheatpump.protocol;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ByteStreamPipe class that runs the read thread to read bytes from the heat
 * pump connector
 *
 * @author Peter Kreutzer
 */
public class ByteStreamPipe implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ByteStreamPipe.class);

    private boolean running = true;
    private InputStream in = null;
    private CircularByteBuffer buffer;
    private Thread taskThread;

    public ByteStreamPipe(InputStream in, CircularByteBuffer buffer) {
        this.in = in;
        this.buffer = buffer;
    }

    public void startTask() {
        taskThread = new Thread(this);
        taskThread.start();
    }

    public void stopTask() {
        taskThread.interrupt();
        try {
            in.close();
        } catch (IOException e) {
            logger.error("Error while closing COM port.", e);
        }
        try {
            taskThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                if (in.available() > 0) {
                    byte readByte = (byte) in.read();
                    logger.trace(String.format("Received %02X", readByte));
                    buffer.put(readByte);
                }
            } catch (Exception e) {
                logger.error("Error while reading from COM port. Stopping.", e);
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        running = false;
        try {
            in.close();
        } catch (IOException e) {
            logger.error("Error while closing COM port.", e);
        }
    }
}
