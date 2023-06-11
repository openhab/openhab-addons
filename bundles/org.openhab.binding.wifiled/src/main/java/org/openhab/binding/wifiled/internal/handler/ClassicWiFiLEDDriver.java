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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link ClassicWiFiLEDDriver} class is responsible for the communication with the WiFi LED controller.
 * It's used for sending color or program settings and also extracting the data out of the received telegrams.
 *
 * @author Osman Basha - Initial contribution
 * @author Stefan Endrullis
 * @author Ries van Twisk - Prevent flashes during classic driver color + white updates
 */
public class ClassicWiFiLEDDriver extends AbstractWiFiLEDDriver {

    private static final int WAIT_UPDATE_LED_FOR_MS = 25;
    private final WiFiLEDHandler wifiLedHandler;
    private final ExecutorService updateScheduler = Executors.newSingleThreadExecutor();
    private Future<Boolean> ledUpdateFuture = CompletableFuture.completedFuture(null);
    private LEDStateDTO cachedLedStatus = null;

    public ClassicWiFiLEDDriver(WiFiLEDHandler wifiLedHandler, String host, int port, Protocol protocol) {
        super(host, port, protocol);
        this.wifiLedHandler = wifiLedHandler;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public synchronized LEDStateDTO getLEDStateDTO() throws IOException {
        if (ledUpdateFuture.isDone()) {
            LEDState s = getLEDState();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            return LEDStateDTO.valueOf(s.state, s.program, s.programSpeed, s.red, s.green, s.blue, s.white, s.white2);
        } else {
            return cachedLedStatus;
        }
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

    private synchronized void sendLEDData(final LEDStateDTO ledState) {
        cachedLedStatus = ledState;
        if (!ledUpdateFuture.isDone()) {
            ledUpdateFuture.cancel(true);
        }

        final byte[] bytes;
        int program = Integer.valueOf(ledState.getProgram().toString());
        if (program == 0x61) {
            // "normal" program: set color etc.
            byte r = (byte) (ledState.getRGB() >> 16 & 0xFF);
            byte g = (byte) (ledState.getRGB() >> 8 & 0xFF);
            byte b = (byte) (ledState.getRGB() & 0xFF);
            byte w = (byte) (((int) (ledState.getWhite().doubleValue() * 255 / 100)) & 0xFF);
            byte w2 = (byte) (((int) (ledState.getWhite2().doubleValue() * 255 / 100)) & 0xFF);

            bytes = getBytesForColor(r, g, b, w, w2);
        } else {
            // program selected
            byte p = (byte) (program & 0xFF);
            byte s = (byte) (((100 - ledState.getProgramSpeed().intValue()) * 0x1F / 100) & 0xFF);
            bytes = new byte[] { 0x61, p, s };
        }

        ledUpdateFuture = updateScheduler.submit(() -> {
            try {
                Thread.sleep(WAIT_UPDATE_LED_FOR_MS);
                logger.debug("Setting LED State to {}", bytesToHex(bytes));
                sendRaw(bytes);
                return true;
            } catch (IOException e) {
                logger.debug("Exception occurred while sending command to LED", e);
                wifiLedHandler.reportCommunicationError(e);
            } catch (InterruptedException e) {
                // Ignore, this is expected
            }
            return false;
        });
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(String.format("%02x ", aByte));
        }
        String string = builder.toString();
        return string.substring(0, string.length() - 1);
    }
}
