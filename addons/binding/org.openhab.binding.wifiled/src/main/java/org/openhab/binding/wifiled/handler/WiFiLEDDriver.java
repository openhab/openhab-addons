/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WiFiLEDDriver} class is responsible for the communication with the WiFi LED controller.
 * It's used for sending color or program settings and also extracting the data out of the received telegrams.
 *
 * @author Osman Basha - Initial contribution
 */
public class WiFiLEDDriver {

    public enum Protocol {
        LD382,
        LD382A;
    }

    private static final int DEFAULT_SOCKET_TIMEOUT = 5000;

    public static final Integer DEFAULT_PORT = 5577;

    private Logger logger = LoggerFactory.getLogger(WiFiLEDDriver.class);
    private String host;
    private int port;
    private Protocol protocol;

    public WiFiLEDDriver(String host, int port, Protocol protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    public synchronized LEDStateDTO getLEDState() throws IOException {
        try (Socket socket = new Socket(host, port)) {
            logger.debug("Connected to '{}'", socket);

            socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            byte[] data = { (byte) 0x81, (byte) 0x8A, (byte) 0x8B, (byte) 0x96 };
            outputStream.write(data);
            logger.debug("Data sent: '{}'", bytesToHex(data));

            byte[] statusBytes = new byte[14];
            inputStream.readFully(statusBytes);
            logger.debug("Data read: '{}'", bytesToHex(statusBytes));

            // Example response (14 Bytes):
            // 0x81 0x04 0x23 0x26 0x21 0x10 0x45 0x00 0x00 0x00 0x03 0x00 0x00 0x47
            // ..........^--- On/Off.........R....G....B....WW..
            // ...............^-- PGM...^---SPEED...............

            int state = statusBytes[2] & 0xFF; // On/Off
            int program = statusBytes[3] & 0xFF;
            int programSpeed = statusBytes[5] & 0xFF;

            int red = statusBytes[6] & 0xFF;
            int green = statusBytes[7] & 0xFF;
            int blue = statusBytes[8] & 0xFF;
            int white = statusBytes[9] & 0xFF;
            LEDStateDTO ledState = LEDStateDTO.valueOf(state, program, programSpeed, red, green, blue, white);

            logger.debug("RGBW: {},{},{},{} -> LEDState: {}", red, green, blue, white, ledState);

            Thread.sleep(100);
            return ledState;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public synchronized void setColor(HSBType color) throws IOException {
        logger.debug("Setting color to {}", color);

        LEDStateDTO ledState = getLEDState().withColor(color).withoutProgram();
        sendLEDData(ledState);
    }

    public synchronized void setBrightness(PercentType brightness) throws IOException {
        logger.debug("Setting brightness to {}", brightness);

        LEDStateDTO ledState = getLEDState().withBrightness(brightness).withoutProgram();
        sendLEDData(ledState);
    }

    public synchronized void incBrightness(int step) throws IOException {
        logger.debug("Changing brightness by {}", step);

        LEDStateDTO ledState = getLEDState().withIncrementedBrightness(step).withoutProgram();
        sendLEDData(ledState);
    }

    public synchronized void decBrightness(int step) throws IOException {
        incBrightness(-step);
    }

    public synchronized void setWhite(PercentType white) throws IOException {
        logger.debug("Setting (warm) white LED to {}", white);

        LEDStateDTO ledState = getLEDState().withWhite(white).withoutProgram();
        sendLEDData(ledState);
    }

    public synchronized void incWhite(int step) throws IOException {
        logger.debug("Changing white by {}", step);

        LEDStateDTO ledState = getLEDState().withIncrementedWhite(step).withoutProgram();
        sendLEDData(ledState);
    }

    public synchronized void decWhite(int step) throws IOException {
        incWhite(-step);
    }

    public synchronized void setOn() throws IOException {
        logger.debug("Setting on");

        byte[] data = { 0x71, 0x23 };
        sendRaw(data);
    }

    public synchronized void setOff() throws IOException {
        logger.debug("Setting off");

        byte[] data = { 0x71, 0x24 };
        sendRaw(data);
    }

    public synchronized void setProgram(StringType program) throws IOException {
        logger.debug("Setting program '{}'", program);

        LEDStateDTO ledState = getLEDState().withProgram(program);
        sendLEDData(ledState);
    }

    public synchronized void setProgramSpeed(PercentType speed) throws IOException {
        logger.debug("Setting program speed to {}", speed);

        LEDStateDTO ledState = getLEDState().withProgramSpeed(speed);
        if (speed.equals(PercentType.ZERO)) {
            ledState = ledState.withoutProgram();
        }
        sendLEDData(ledState);
    }

    public synchronized void incProgramSpeed(int step) throws IOException {
        logger.debug("Changing program speed by {}", step);

        LEDStateDTO ledState = getLEDState().withIncrementedProgramSpeed(step);
        sendLEDData(ledState);
    }

    public synchronized void decProgramSpeed(int step) throws IOException {
        incProgramSpeed(-step);
    }

    private void sendLEDData(LEDStateDTO ledState) throws IOException {
        logger.debug("Setting LED State to {}", ledState);

        int program = Integer.valueOf(ledState.getProgram().toString());
        if (program == 0x61) {
            // "normal" program: set color etc.
            byte r = (byte) (ledState.getColor().getRed() & 0xFF);
            byte g = (byte) (ledState.getColor().getGreen() & 0xFF);
            byte b = (byte) (ledState.getColor().getBlue() & 0xFF);
            byte w = (byte) ((ledState.getWhite().intValue() * 255 / 100) & 0xFF);
            byte[] bytes = new byte[] { 0x31, r, g, b, w, 0x00 };
            sendRaw(bytes);
        } else {
            // program selected
            byte p = (byte) (program & 0xFF);
            byte s = (byte) (((100 - ledState.getProgramSpeed().intValue()) * 0x1F / 100) & 0xFF);
            byte[] data = { 0x61, p, s };
            sendRaw(data);
        }
    }

    private void sendRaw(byte[] data) throws IOException {
        byte[] dataWithCS;

        // append 0x0F (if dev.type LD382A)
        if (protocol.equals(Protocol.LD382A)) {
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

        try (Socket socket = new Socket(host, port)) {
            logger.debug("Connected to '{}'", socket);

            socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);

            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(dataWithCS);
            logger.debug("RAW data sent: '{}'", bytesToHex(dataWithCS));

            Thread.sleep(100);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static final String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            builder.append(String.format("%02x ", bytes[i]));
        }
        String string = builder.toString();
        return string.substring(0, string.length() - 1);
    }

}
