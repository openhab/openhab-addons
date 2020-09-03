/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.openhab.binding.teleinfo.internal.dto.Frame;
import org.openhab.binding.teleinfo.internal.reader.io.TeleinfoInputStream;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoReceiveThread} class defines a thread to decode and fire Teleinfo frames for Serial controller.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoReceiveThread extends Thread {

    private static final int SERIAL_PORT_DELAY_RETRY_IN_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(TeleinfoReceiveThread.class);

    private SerialPort serialPort;
    private @Nullable TeleinfoReceiveThreadListener listener;
    private boolean autoRepairInvalidADPSgroupLine;
    private ExecutorService executorService;

    public TeleinfoReceiveThread(SerialPort serialPort, final TeleinfoSerialControllerHandler listener,
            boolean autoRepairInvalidADPSgroupLine, ExecutorService scheduler) {
        super("OH-binding-TeleinfoReceiveThread-" + listener.getThing().getUID().getId());
        setDaemon(true);
        this.serialPort = serialPort;
        this.listener = listener;
        this.autoRepairInvalidADPSgroupLine = autoRepairInvalidADPSgroupLine;
        this.executorService = scheduler;
    }

    @Override
    public void run() {
        try (TeleinfoInputStream teleinfoStream = new TeleinfoInputStream(serialPort.getInputStream(),
                TeleinfoInputStream.DEFAULT_TIMEOUT_NEXT_HEADER_FRAME_US * 100,
                TeleinfoInputStream.DEFAULT_TIMEOUT_READING_FRAME_US * 100, autoRepairInvalidADPSgroupLine,
                executorService)) {
            while (!interrupted()) {
                TeleinfoReceiveThreadListener listener = this.listener;
                if (listener != null) {
                    try {
                        Frame nextFrame = teleinfoStream.readNextFrame();
                        if (nextFrame != null)
                            listener.onFrameReceived(this, nextFrame);
                    } catch (InvalidFrameException e) {
                        logger.warn("Got invalid frame. Detail: \"{}\"", e.getLocalizedMessage());
                        listener.onInvalidFrameReceived(this, e);
                    } catch (TimeoutException e) {
                        logger.warn("Got timeout during frame reading", e);
                        logger.warn("Retry in progress. Next retry in {} seconds...",
                                SERIAL_PORT_DELAY_RETRY_IN_SECONDS);
                        listener.continueOnReadNextFrameTimeoutException();
                        try {
                            Thread.sleep(SERIAL_PORT_DELAY_RETRY_IN_SECONDS * 1000);
                        } catch (InterruptedException e1) {
                            break;
                        }

                    } catch (IOException e) {
                        logger.warn("Got I/O exception. Detail: \"{}\"", e.getLocalizedMessage(), e);
                        listener.onSerialPortInputStreamIOException(this, e);
                        break;
                    } catch (IllegalStateException e) {
                        logger.warn("Got illegal state exception", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("An error occurred during serial port input stream opening", e);
        }

        serialPort.removeEventListener();
    }

    public @Nullable TeleinfoReceiveThreadListener getListener() {
        return listener;
    }

    public void setListener(@Nullable TeleinfoReceiveThreadListener listener) {
        this.listener = listener;
    }
}
