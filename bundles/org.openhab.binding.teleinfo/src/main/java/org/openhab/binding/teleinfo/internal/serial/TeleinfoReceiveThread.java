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
package org.openhab.binding.teleinfo.internal.serial;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.reader.io.TeleinfoInputStream;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoReceiveThread} class defines a thread to decode and fire Teleinfo frames for Serial controller.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoReceiveThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoReceiveThread.class);

    private SerialPort serialPort;
    private @Nullable TeleinfoReceiveThreadListener listener;
    private boolean autoRepairInvalidADPSgroupLine;
    private final TeleinfoTicMode ticMode;
    private final boolean verifyChecksum;

    public TeleinfoReceiveThread(SerialPort serialPort, final TeleinfoSerialControllerHandler listener,
            boolean autoRepairInvalidADPSgroupLine, TeleinfoTicMode ticMode, boolean verifyChecksum) {
        super("OH-binding-TeleinfoReceiveThread-" + listener.getThing().getUID().getId());
        setDaemon(true);
        this.serialPort = serialPort;
        this.listener = listener;
        this.autoRepairInvalidADPSgroupLine = autoRepairInvalidADPSgroupLine;
        this.ticMode = ticMode;
        this.verifyChecksum = verifyChecksum;
    }

    @Override
    public void run() {
        try (TeleinfoInputStream teleinfoStream = new TeleinfoInputStream(serialPort.getInputStream(),
                autoRepairInvalidADPSgroupLine, ticMode, verifyChecksum)) {
            while (!interrupted()) {
                TeleinfoReceiveThreadListener listener = this.listener;
                if (listener != null) {
                    try {
                        Frame nextFrame = teleinfoStream.readNextFrame();
                        if (nextFrame != null) {
                            listener.onFrameReceived(nextFrame);
                        }
                    } catch (InvalidFrameException e) {
                        logger.warn("Got invalid frame. Detail: \"{}\"", e.getLocalizedMessage());
                        listener.onInvalidFrameReceived(this, e);
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
