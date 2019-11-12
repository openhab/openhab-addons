/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.serial;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.openhab.binding.teleinfo.internal.reader.io.TeleinfoInputStream;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoReceiveThread} class defines a thread to decode and fire Teleinfo frames for Serial controller.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class TeleinfoReceiveThread extends Thread implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoReceiveThread.class);

    private SerialPort serialPort;
    private TeleinfoReceiveThreadListener listener;

    public TeleinfoReceiveThread(SerialPort serialPort, @NonNull final TeleinfoReceiveThreadListener listener) {
        super("TeleinfoReceiveThread");

        this.serialPort = serialPort;
        this.listener = listener;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        try {
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {
        logger.debug("Starting Teleinfo thread: Receive");
        try (TeleinfoInputStream teleinfoStream = new TeleinfoInputStream(serialPort.getInputStream(),
                TeleinfoInputStream.DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME * 100,
                TeleinfoInputStream.DEFAULT_TIMEOUT_READING_FRAME * 100)) {
            while (!interrupted()) {
                try {
                    Frame nextFrame = teleinfoStream.readNextFrame();
                    listener.onFrameReceived(this, nextFrame);
                } catch (InvalidFrameException e) {
                    logger.error("Got invalid frame. Detail: \"{}\"", e.getLocalizedMessage());
                    listener.onInvalidFrameReceived(this, e);
                } catch (TimeoutException e) {
                    logger.error("Got timeout during frame reading", e);
                    if (!listener.continueOnReadNextFrameTimeoutException(this, e)) {
                        break;
                    }
                    // skipInputStreamBuffer();
                } catch (IOException e) {
                    logger.error("Got I/O exception. Detail: \"{}\"", e.getLocalizedMessage(), e);
                    listener.onSerialPortInputStreamIOException(this, e);
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("An error occurred during serial port input stream opening", e);
        }

        logger.debug("Terminates Teleinfo receive thread");

        serialPort.removeEventListener();
    }

    public TeleinfoReceiveThreadListener getListener() {
        return listener;
    }

    public void setListener(TeleinfoReceiveThreadListener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("null")
    private void skipInputStreamBuffer() throws IOException {
        logger.trace("skipInputStreamBuffer() [start]");
        serialPort.getInputStream().skip(serialPort.getInputStream().available());
        logger.trace("skipInputStreamBuffer() [end]");
    }
}