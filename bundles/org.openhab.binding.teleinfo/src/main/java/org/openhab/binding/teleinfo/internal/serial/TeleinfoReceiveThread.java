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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
    private List<TeleinfoReceiveThreadListener> listeners = new ArrayList<>();

    public TeleinfoReceiveThread(SerialPort serialPort) {
        super("TeleinfoReceiveThread");

        this.serialPort = serialPort;
    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
        try {
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {
        logger.debug("Starting Teleinfo thread: Receive");
        try {
            // // Initialise all the statistics channels
            // updateState(new ChannelUID(getThing().getUID(), CHANNEL_SERIAL_SOF), new DecimalType(SOFCount));

            TeleinfoInputStream teleinfoStream = new TeleinfoInputStream(serialPort.getInputStream(),
                    TeleinfoInputStream.DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME * 10,
                    TeleinfoInputStream.DEFAULT_TIMEOUT_READING_FRAME * 10);
            while (!interrupted()) {
                Frame nextFrame;

                try {
                    nextFrame = teleinfoStream.readNextFrame();
                } catch (TimeoutException e) {
                    logger.error("Got timeout {} during receiving. exiting thread.", e.getLocalizedMessage());
                    break;
                } catch (InvalidFrameException e) {
                    logger.error("Got invalid frame {} during receiving. exiting thread.", e.getLocalizedMessage());
                    break;
                } catch (IOException e) {
                    logger.error("Got I/O exception {} during receiving. exiting thread.", e.getLocalizedMessage());
                    break;
                }

                fireOnFrameReceivedEvent(nextFrame);
            }
        } catch (Exception e) {
            logger.error("Exception during Teleinfo receive thread. ", e);
        }

        logger.debug("Stopped Teleinfo receive thread");

        serialPort.removeEventListener();
    }

    public void addListener(final TeleinfoReceiveThreadListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final TeleinfoReceiveThreadListener listener) {
        listeners.remove(listener);
    }

    private void fireOnFrameReceivedEvent(final Frame frame) {
        for (TeleinfoReceiveThreadListener listener : listeners) {
            listener.onFrameReceived(this, frame);
        }
    }
}