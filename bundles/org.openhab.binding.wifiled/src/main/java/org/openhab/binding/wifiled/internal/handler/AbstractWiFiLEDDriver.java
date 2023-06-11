/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wifiled.internal.handler;

import static org.openhab.binding.wifiled.internal.handler.ClassicWiFiLEDDriver.bytesToHex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract WiFi LED driver.
 *
 * @author Osman Basha - Initial contribution
 * @author Stefan Endrullis
 * @author Ries van Twisk
 */
public abstract class AbstractWiFiLEDDriver {

    public enum Protocol {
        LD382,
        LD382A,
        LD686
    }

    public enum Driver {
        CLASSIC,
        FADING
    }

    public static final Integer DEFAULT_PORT = 5577;

    protected static final int DEFAULT_SOCKET_TIMEOUT = 5000;

    protected Logger logger = LoggerFactory.getLogger(AbstractWiFiLEDDriver.class);
    protected String host;
    protected int port;
    protected Protocol protocol;

    public AbstractWiFiLEDDriver(String host, int port, Protocol protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    /**
     * Allow to cleanup the driver
     */
    public abstract void shutdown();

    public abstract void setColor(HSBType color) throws IOException;

    public abstract void setBrightness(PercentType brightness) throws IOException;

    public abstract void incBrightness(int step) throws IOException;

    public void decBrightness(int step) throws IOException {
        incBrightness(-step);
    }

    public abstract void setWhite(PercentType white) throws IOException;

    public abstract void incWhite(int step) throws IOException;

    public void decWhite(int step) throws IOException {
        incWhite(-step);
    }

    public abstract void setWhite2(PercentType white2) throws IOException;

    public abstract void incWhite2(int step) throws IOException;

    public void decWhite2(int step) throws IOException {
        incWhite2(-step);
    }

    public abstract void setProgram(StringType program) throws IOException;

    public abstract void setProgramSpeed(PercentType speed) throws IOException;

    public abstract void incProgramSpeed(int step) throws IOException;

    public void decProgramSpeed(int step) throws IOException {
        incProgramSpeed(-step);
    }

    public abstract void setPower(OnOffType command) throws IOException;

    public void init() throws IOException {
        getLEDState();
    }

    public abstract LEDStateDTO getLEDStateDTO() throws IOException;

    protected synchronized LEDState getLEDState() throws IOException {
        try (Socket socket = new Socket(host, port);
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            logger.debug("Connected to '{}'", socket);

            socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);

            byte[] data = { (byte) 0x81, (byte) 0x8A, (byte) 0x8B, (byte) 0x96 };
            outputStream.write(data);
            logger.debug("Data sent: '{}'", bytesToHex(data));

            byte[] statusBytes = new byte[14];
            inputStream.readFully(statusBytes);
            logger.debug("Data read: '{}'", bytesToHex(statusBytes));

            // Example response (14 Bytes):
            // 0x81 0x04 0x23 0x26 0x21 0x10 0x45 0x00 0x00 0x00 0x03 0x00 0x00 0x47
            // ..........^--- On/Off.........R....G....B....WW........CW
            // ...............^-- PGM...^---SPEED...............

            int state = statusBytes[2] & 0xFF; // On/Off
            int program = statusBytes[3] & 0xFF;
            int programSpeed = statusBytes[5] & 0xFF;

            // On factory default the controller can be configured
            // with a value of 255 but max should be 31.
            if (programSpeed > 31) {
                programSpeed = 31;
            }

            int red = statusBytes[6] & 0xFF;
            int green = statusBytes[7] & 0xFF;
            int blue = statusBytes[8] & 0xFF;
            int white = statusBytes[9] & 0xFF;
            int white2 = protocol == Protocol.LD686 ? statusBytes[11] & 0xFF : 0;

            logger.debug("RGBW: {},{},{},{}, {}", red, green, blue, white, white2);

            return new LEDState(state, program, programSpeed, red, green, blue, white, white2);
        }
    }

    protected void sendRaw(byte[] data) throws IOException {
        sendRaw(data, 100);
    }

    protected synchronized void sendRaw(byte[] data, int delay) throws IOException {
        try (Socket socket = new Socket(host, port);
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
            logger.debug("Connected to '{}'", socket);

            socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);

            sendRaw(data, outputStream);

            if (delay > 0) {
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void sendRaw(byte[] data, DataOutputStream outputStream) throws IOException {
        byte[] dataWithCS;

        // append 0x0F (if dev.type LD382A)
        if (protocol == Protocol.LD382A || protocol == Protocol.LD686) {
            dataWithCS = new byte[data.length + 2];
            dataWithCS[dataWithCS.length - 2] = 0x0F;
        } else {
            dataWithCS = new byte[data.length + 1];
        }

        // append checksum
        System.arraycopy(data, 0, dataWithCS, 0, data.length);
        int cs = 0;
        for (int i = 0; i < dataWithCS.length - 1; i++) {
            cs += dataWithCS[i];
        }
        cs = cs & 0xFF;
        dataWithCS[dataWithCS.length - 1] = (byte) cs;

        outputStream.write(dataWithCS);
        logger.debug("RAW data sent: '{}'", bytesToHex(dataWithCS));
    }

    protected byte[] getBytesForColor(byte r, byte g, byte b, byte w, byte w2) {
        byte[] bytes;
        if (protocol == Protocol.LD382 || protocol == Protocol.LD382A) {
            bytes = new byte[] { 0x31, r, g, b, w, 0x00 };
        } else if (protocol == Protocol.LD686) {
            bytes = new byte[] { 0x31, r, g, b, w, w2, 0x00 };
        } else {
            throw new UnsupportedOperationException("Protocol " + protocol + " not yet implemented");
        }
        return bytes;
    }

    protected byte[] getBytesForPower(boolean on) {
        return new byte[] { 0x71, on ? (byte) 0x23 : 0x24 };
    }
}
