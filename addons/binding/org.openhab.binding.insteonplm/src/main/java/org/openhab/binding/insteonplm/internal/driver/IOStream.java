/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.driver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for implementation for I/O stream with anything that looks
 * like a PLM (e.g. the insteon hubs, serial/usb connection etc)
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @since 1.7.0
 */

public abstract class IOStream {
    private static final Logger logger = LoggerFactory.getLogger(IOStream.class);
    protected InputStream m_in = null;
    protected OutputStream m_out = null;

    /**
     * read data from iostream
     *
     * @param b byte array (output)
     * @param offset offset for placement into byte array
     * @param readSize size to read
     * @return number of bytes read
     */
    public int read(byte[] b, int offset, int readSize) throws InterruptedException {
        int len = 0;
        while (len < 1) {
            try {
                len = m_in.read(b, offset, readSize);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            } catch (IOException e) {
                logger.trace("got exception while reading: {}", e.getMessage());
                while (!reconnect()) {
                    logger.trace("sleeping before reconnecting");
                    Thread.sleep(10000);
                }
            }
        }
        return (len);
    }

    /**
     * Write data to iostream
     *
     * @param b byte array to write
     */
    public void write(byte[] b) {
        try {
            m_out.write(b);
        } catch (IOException e) {
            logger.trace("got exception while writing: {}", e.getMessage());
            while (!reconnect()) {
                try {
                    logger.trace("sleeping before reconnecting");
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    logger.warn("interrupted while sleeping on write reconnect");
                }
            }
        }
    }

    /**
     * Opens the IOStream
     *
     * @return true if open was successful, false if not
     */
    public abstract boolean open();

    /**
     * Closes the IOStream
     */
    public abstract void close();

    /**
     * reconnects the stream
     *
     * @return true if reconnect succeeded
     */
    private synchronized boolean reconnect() {
        close();
        return (open());
    }
}
