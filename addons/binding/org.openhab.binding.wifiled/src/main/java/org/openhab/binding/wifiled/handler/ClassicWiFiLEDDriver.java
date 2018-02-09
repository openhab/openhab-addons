/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * The {@link ClassicWiFiLEDDriver} class is responsible for the communication with the WiFi LED controller.
 * It's used for sending color or program settings and also extracting the data out of the received telegrams.
 *
 * @author Osman Basha - Initial contribution
 * @author Stefan Endrullis
 * @author Ries van Twisk
 */
public class ClassicWiFiLEDDriver extends AbstractWiFiLEDDriver {

    public ClassicWiFiLEDDriver(String host, int port, Protocol protocol) {
        super(host, port, protocol);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public synchronized LEDStateDTO getLEDStateDTO() throws IOException {
        LEDState s = getLEDState();

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            throw new IOException(e);
        }

        return LEDStateDTO.valueOf(s.state, s.program, s.programSpeed, s.red, s.green, s.blue, s.white, s.white2);
    }

    @Override
    public synchronized void setColor(HSBType color) throws IOException {
        logger.debug("Setting color to {}", color);

        LEDStateDTO ledState = getLEDStateDTO().withColor(color).withoutProgram();
        sendLEDData(ledState);
    }

    @Override
    public synchronized void setBrightness(PercentType brightness) throws IOException {
        logger.debug("Setting brightness to {}", brightness);

        LEDStateDTO ledState = getLEDStateDTO().withBrightness(brightness).withoutProgram();
        sendLEDData(ledState);
    }

    @Override
    public synchronized void incBrightness(int step) throws IOException {
        logger.debug("Changing brightness by {}", step);

        LEDStateDTO ledState = getLEDStateDTO().withIncrementedBrightness(step).withoutProgram();
        sendLEDData(ledState);
    }

    @Override
    public synchronized void setWhite(PercentType white) throws IOException {
        logger.debug("Setting (warm) white LED to {}", white);

        LEDStateDTO ledState = getLEDStateDTO().withWhite(white).withoutProgram();
        sendLEDData(ledState);
    }

    @Override
    public synchronized void incWhite(int step) throws IOException {
        logger.debug("Changing white by {}", step);

        LEDStateDTO ledState = getLEDStateDTO().withIncrementedWhite(step).withoutProgram();
        sendLEDData(ledState);
    }

    @Override
    public void setWhite2(PercentType white2) throws IOException {
        logger.debug("Setting (warm) white 2 LED to {}", white2);

        LEDStateDTO ledState = getLEDStateDTO().withWhite2(white2).withoutProgram();
        sendLEDData(ledState);
    }

    @Override
    public void incWhite2(int step) throws IOException {
        logger.debug("Changing white by {}", step);

        LEDStateDTO ledState = getLEDStateDTO().withIncrementedWhite2(step).withoutProgram();
        sendLEDData(ledState);
    }

    @Override
    public void setPower(OnOffType command) throws IOException {
        logger.debug("Power {}", command.name());

        sendRaw(getBytesForPower(command == OnOffType.ON));
    }

    @Override
    public synchronized void setProgram(StringType program) throws IOException {
        logger.debug("Setting program '{}'", program);

        LEDStateDTO ledState = getLEDStateDTO().withProgram(program);
        sendLEDData(ledState);
    }

    @Override
    public synchronized void setProgramSpeed(PercentType speed) throws IOException {
        logger.debug("Setting program speed to {}", speed);

        LEDStateDTO ledState = getLEDStateDTO().withProgramSpeed(speed);
        if (speed.equals(PercentType.ZERO)) {
            ledState = ledState.withoutProgram();
        }
        sendLEDData(ledState);
    }

    @Override
    public synchronized void incProgramSpeed(int step) throws IOException {
        logger.debug("Changing program speed by {}", step);

        LEDStateDTO ledState = getLEDStateDTO().withIncrementedProgramSpeed(step);
        sendLEDData(ledState);
    }

    private void sendLEDData(LEDStateDTO ledState) throws IOException {
        logger.debug("Setting LED State to {}", ledState);

        int program = Integer.valueOf(ledState.getProgram().toString());
        if (program == 0x61) {
            // "normal" program: set color etc.
            byte r = (byte) (ledState.getColor().getRed() & 0xFF);
            byte g = (byte) (ledState.getColor().getGreen() & 0xFF);
            byte b = (byte) (ledState.getColor().getBlue() & 0xFF);
            byte w = (byte) (((int) (ledState.getWhite().doubleValue() * 255 / 100)) & 0xFF);
            byte w2 = (byte) (((int) (ledState.getWhite2().doubleValue() * 255 / 100)) & 0xFF);

            byte[] bytes = getBytesForColor(r, g, b, w, w2);

            sendRaw(bytes);
        } else {
            // program selected
            byte p = (byte) (program & 0xFF);
            byte s = (byte) (((100 - ledState.getProgramSpeed().intValue()) * 0x1F / 100) & 0xFF);
            byte[] data = { 0x61, p, s };
            sendRaw(data);
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
