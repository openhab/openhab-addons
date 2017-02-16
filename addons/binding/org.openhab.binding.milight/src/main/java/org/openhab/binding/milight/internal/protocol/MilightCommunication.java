/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.openhab.binding.milight.internal.MilightThingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This milight protocol implementation class is able to do the following tasks with Milight compatible systems:
 * <ul>
 * <li>Switching bulbs on and off.</li>
 * <li>Change color temperature of a bulb, what results in a white color.</li>
 * <li>Change the brightness of a bulb without changing the color.</li>
 * <li>Change the RGB values of a bulb.</li>
 * </ul>
 * The class is state-less, use {@link MilightThingState} instead.
 *
 * @author David Gräff - Converted to pure Communication/Protocol implementation class
 * @author Hans-Joerg Merk
 * @author Kai Kreuzer
 * @since 1.3.0
 */
public class MilightCommunication {

    private static final Logger logger = LoggerFactory.getLogger(MilightCommunication.class);

    private static final int rgbwLevels = 26;
    private static final int wLevels = 11;

    private final String bridgeId;
    final DatagramPacket packet;
    final DatagramSocket datagramSocket;

    public MilightCommunication(InetAddress addr, int port, String bridgeId) throws SocketException {
        this.bridgeId = bridgeId;
        byte[] a = new byte[0];
        packet = new DatagramPacket(a, a.length, addr, port);
        datagramSocket = new DatagramSocket();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    public int setBrightness(int bulb, int value, int oldPercent) {
        if (value <= 0) {
            return setOff(bulb);
        } else if (value >= 100) {
            return setFull(bulb);
        }

        // White Bulbs: 11 levels of brightness + Off.
        if (bulb < 5) {
            final int newLevel = (int) Math.ceil((value * wLevels) / 100.0);

            // When turning on start from full brightness
            int oldLevel;
            if (oldPercent == 0) {
                setFull(bulb);
                oldLevel = wLevels;
            } else {
                oldLevel = (int) Math.ceil((oldPercent * wLevels) / 100.0);
            }

            final int repeatCount = Math.abs(newLevel - oldLevel);
            logger.debug("milight: dim from '{}' with command '{}' via '{}' steps.", String.valueOf(oldPercent),
                    String.valueOf(value), repeatCount);
            if (newLevel > oldLevel) {
                for (int i = 0; i < repeatCount; i++) {
                    sleep(50);
                    increaseBrightness(bulb, -1);
                }
            } else if (newLevel < oldLevel) {
                for (int i = 0; i < repeatCount; i++) {
                    sleep(50);
                    decreaseBrightness(bulb, -1);
                }
            }
            // Old RGB Bulbs: 9 levels of brightness + Off.
        } else if (bulb == 5) {
            if (value > oldPercent) {
                int repeatCount = (value - oldPercent) / 10;
                for (int i = 0; i < repeatCount; i++) {
                    sleep(100);
                    increaseBrightness(bulb, -1);

                }
            } else if (value < oldPercent) {
                int repeatCount = (oldPercent - value) / 10;
                for (int i = 0; i < repeatCount; i++) {
                    sleep(100);
                    decreaseBrightness(bulb, -1);
                }
            }
            // RGBW Bulbs:
        } else if (bulb > 5) {
            int newCommand = (int) Math.ceil((value * rgbwLevels) / 100.0) + 1;
            setOn(bulb);
            sleep(100);
            String messageBytes = "4E:" + Integer.toHexString(newCommand) + ":55";
            logger.debug("milight: send dimming packet '{}' to RGBW bulb channel '{}'", messageBytes, bulb);
            sendMessage(messageBytes);
        }
        return value;
    }

    public int setDiscoSpeed(int bulb, int value, int oldPercent) {
        if (value > oldPercent) {
            int repeatCount = (value - oldPercent) / 10;
            for (int i = 0; i < repeatCount; i++) {
                sleep(100);
                increaseSpeed(bulb);
            }
        } else if (value < oldPercent) {
            int repeatCount = (oldPercent - value) / 10;
            for (int i = 0; i < repeatCount; i++) {
                sleep(100);
                decreaseSpeed(bulb);
            }
        }

        return value;
    }

    public int setDiscoMode(int bulb, int value, int oldPercent) {
        // Make sure lights are on and engage current bulb via a preceding ON command:
        setOn(bulb);

        if (bulb != 5) {
            return 0;
        }

        if (value > oldPercent) {
            int repeatCount = (value - oldPercent) / 10;
            for (int i = 0; i < repeatCount; i++) {
                sleep(100);
                nextDiscoMode(bulb, oldPercent);
            }
        } else if (value < oldPercent) {
            int repeatCount = (oldPercent - value) / 10;
            for (int i = 0; i < repeatCount; i++) {
                sleep(100);
                previousDiscoMode(bulb, oldPercent);
            }
        }

        return value;
    }

    public int setColorTemperature(int bulb, int value, int oldPercent) {
        // White Bulbs: 11 levels of temperature + Off.
        if (bulb < 5) {
            int newLevel;
            int oldLevel;
            // Reset bulb to known state
            if (value <= 0) {
                value = 0;
                newLevel = 1;
                oldLevel = wLevels;
            } else if (value >= 100) {
                value = 100;
                newLevel = wLevels;
                oldLevel = 1;
            } else {
                newLevel = (int) Math.ceil((value * wLevels) / 100.0);
                oldLevel = (int) Math.ceil((oldPercent * wLevels) / 100.0);
            }

            final int repeatCount = Math.abs(newLevel - oldLevel);
            logger.debug("milight: dim from '{}' with command '{}' via '{}' steps.", oldPercent, value, repeatCount);
            if (newLevel > oldLevel) {
                for (int i = 0; i < repeatCount; i++) {
                    sleep(50);
                    warmer(bulb, -1);
                }
            } else if (newLevel < oldLevel) {
                for (int i = 0; i < repeatCount; i++) {
                    sleep(50);
                    cooler(bulb, -1);
                }
            }
            // Old RGB Bulbs: 9 levels of brightness + Off.
        } else if (bulb == 5) {
            if (value > oldPercent) {
                int repeatCount = (value - oldPercent) / 10;
                for (int i = 0; i < repeatCount; i++) {
                    sleep(100);
                    warmer(bulb, -1);
                }
            } else if (value < oldPercent) {
                int repeatCount = (oldPercent - value) / 10;
                for (int i = 0; i < repeatCount; i++) {
                    sleep(100);
                    cooler(bulb, -1);
                }
            }
        }
        return value;
    }

    public int increaseBrightness(int bulb, int oldPercent) {
        logger.debug("milight: sendIncrease");
        String messageBytes = null;
        switch (bulb) {
            // increase brightness of white bulbs
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                messageBytes = "3C:00:55";
                break;
            // increase brightness of rgb bulbs
            case 5:
                messageBytes = "23:00:55";
                break;
        }
        int currentPercent = oldPercent;
        int newPercent = currentPercent + 10;
        if (newPercent > 100) {
            newPercent = 100;
        }

        if (bulb > 5) {
            int increasePercent = (int) Math.ceil((newPercent * rgbwLevels) / 100.0) + 1;
            messageBytes = "4E:" + Integer.toHexString(increasePercent) + ":55";
            logger.debug("Bulb '{}' set to '{}' dimming Steps", bulb, increasePercent);
        }
        setOn(bulb);
        sleep(100);
        sendMessage(messageBytes);
        return newPercent;
    }

    public int decreaseBrightness(int bulb, int oldPercent) {
        logger.debug("milight: sendDecrease");
        String messageBytes = null;
        switch (bulb) {
            // decrease brightness of white bulbs
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                messageBytes = "34:00:55";
                break;
            // decrease brightness of rgb bulbs
            case 5:
                messageBytes = "24:00:55";
                break;
        }
        int newPercent = oldPercent - 10;
        if (newPercent < 0) {
            newPercent = 0;
        }

        if (oldPercent != -1 && newPercent == 0) {
            setOff(bulb);
        } else {
            if (bulb > 5) {
                int decreasePercent = (int) Math.ceil((newPercent * rgbwLevels) / 100.0) + 1;
                messageBytes = "4E:" + Integer.toHexString(decreasePercent) + ":55";
                logger.debug("Bulb '{}' set to '{}' dimming Steps", bulb, decreasePercent);
            }
            setOn(bulb);
            sleep(100);
            sendMessage(messageBytes);
        }
        return newPercent;
    }

    public int warmer(int bulb, int oldPercent) {
        logger.debug("milight: sendWarmer");
        int newPercent = oldPercent + 10;
        if (newPercent > 100) {
            newPercent = 100;
        }
        String messageBytes = "3E:00:55";
        setOn(bulb);
        sleep(100);
        sendMessage(messageBytes);
        return newPercent;
    }

    public int cooler(int bulb, int oldPercent) {
        logger.debug("milight: sendCooler");
        int newPercent = oldPercent - 10;
        if (newPercent < 0) {
            newPercent = 0;
        }

        String messageBytes = "3F:00:55";
        setOn(bulb);
        sleep(100);
        sendMessage(messageBytes);
        return newPercent;
    }

    public int nextDiscoMode(int bulb, int oldPercent) {
        logger.debug("milight: sendDiscoModeUp");
        if (bulb < 6) {
            String messageBytes = "27:00:55";
            setOn(bulb);
            sleep(100);
            sendMessage(messageBytes);
        }
        if (bulb > 5) {
            String messageBytes = "4D:00:55";
            setOn(bulb);
            sleep(100);
            sendMessage(messageBytes);
        }
        return Math.max(oldPercent + 10, 100);
    }

    public int previousDiscoMode(int bulb, int oldPercent) {
        logger.debug("milight: sendDiscoModeDown");
        String messageBytes = "28:00:55";
        setOn(bulb);
        sleep(100);
        sendMessage(messageBytes);
        return Math.min(oldPercent - 10, 0);
    }

    public void increaseSpeed(int bulb) {
        logger.debug("milight: sendIncreaseSpeed");
        String messageBytes = null;
        switch (bulb) {
            case 5:
                // message increaseSpeed rgb bulbs
                messageBytes = "25:00:55";
                break;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                // message increaseSpeed rgb-w bulbs
                messageBytes = "44:00:55";
                break;
        }
        setOn(bulb);
        sleep(100);
        sendMessage(messageBytes);
    }

    public void decreaseSpeed(int bulb) {
        logger.debug("milight: sendDecreaseSpeed");
        String messageBytes = null;
        switch (bulb) {
            case 5:
                // message decreaseSpeed rgb bulbs
                messageBytes = "26:00:55";
                break;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                // message decreaseSpeed rgb-w bulbs
                messageBytes = "43:00:55";
                break;
        }
        setOn(bulb);
        sleep(100);
        sendMessage(messageBytes);
    }

    public void setNightMode(int bulb) {
        logger.debug("milight: sendNightMode");
        String messageBytes = null;
        String messageBytes2 = null;
        switch (bulb) {
            case 0:
                // message nightMode all white bulbs
                messageBytes = "B9:00:55";
                break;
            case 1:
                // message nightMode white bulb channel 1
                messageBytes = "BB:00:55";
                break;
            case 2:
                // message nightMode white bulb channel 2
                messageBytes = "B3:00:55";
                break;
            case 3:
                // message nightMode white bulb channel 3
                messageBytes = "BA:00:55";
                break;
            case 4:
                // message nightMode white bulb channel 4
                messageBytes = "B6:00:55";
                break;
            case 6:
                // message nightMode all RGBW bulbs
                messageBytes = "41:00:55";
                messageBytes2 = "C1:00:55";
                break;
            case 7:
                // message nightMode RGBW bulb channel 1
                messageBytes = "46:00:55";
                messageBytes2 = "C6:00:55";
                break;
            case 8:
                // message nightMode RGBW bulb channel 2
                messageBytes = "48:00:55";
                messageBytes2 = "C8:00:55";
                break;
            case 9:
                // message nightMode RGBW bulb channel 3
                messageBytes = "4A:00:55";
                messageBytes2 = "CA:00:55";
                break;
            case 10:
                // message nightMode RGBW bulb channel 4
                messageBytes = "4C:00:55";
                messageBytes2 = "CC:00:55";
                break;
        }
        sendMessage(messageBytes);

        // nightMode for RGBW bulbs requires second message 100ms later.
        if (bulb >= 6 && bulb <= 10) {
            sleep(100);
            sendMessage(messageBytes2);
        }

    }

    public void setWhiteMode(int bulb) {
        logger.debug("milight: sendWhiteMode");
        String messageBytes = null;
        switch (bulb) {
            case 6:
                // message whiteMode all RGBW bulbs
                messageBytes = "C2:00:55";
                break;
            case 7:
                // message whiteMode RGBW bulb channel 1
                messageBytes = "C5:00:55";
                break;
            case 8:
                // message whiteMode RGBW bulb channel 2
                messageBytes = "C7:00:55";
                break;
            case 9:
                // message whiteMode RGBW bulb channel 3
                messageBytes = "C9:00:55";
                break;
            case 10:
                // message whiteMode RGBW bulb channel 4
                messageBytes = "CB:00:55";
                break;
        }
        setOn(bulb);
        sleep(100);
        sendMessage(messageBytes);
    }

    public int setFull(int bulb) {
        logger.debug("milight: sendFull");
        String messageBytes = null;
        switch (bulb) {
            case 0:
                // message fullBright all white bulbs
                messageBytes = "B5:00:55";
                break;
            case 1:
                // message fullBright white bulb channel 1
                messageBytes = "B8:00:55";
                break;
            case 2:
                // message fullBright white bulb channel 2
                messageBytes = "BD:00:55";
                break;
            case 3:
                // message fullBright white bulb channel 3
                messageBytes = "B7:00:55";
                break;
            case 4:
                // message fullBright white bulb channel 4
                messageBytes = "B2:00:55";
                break;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                setOn(bulb);
                sleep(100);
                int fullPercent = rgbwLevels + 1;
                messageBytes = "4E:" + Integer.toHexString(fullPercent) + ":55";
                logger.debug("Bulb '{}' set to '{}' dimming Steps", bulb, fullPercent);
                break;
        }
        sendMessage(messageBytes);
        return 100;
    }

    public void setOn(int bulb) {
        logger.debug("milight: sendOn");
        String messageBytes = null;
        switch (bulb) {
            case 0:
                // message all white bulbs ON
                messageBytes = "35:00:55";
                break;
            case 1:
                // message white bulb channel 1 ON
                messageBytes = "38:00:55";
                break;
            case 2:
                // message white bulb channel 2 ON
                messageBytes = "3D:00:55";
                break;
            case 3:
                // message white bulb channel 3 ON
                messageBytes = "37:00:55";
                break;
            case 4:
                // message white bulb channel 4 ON
                messageBytes = "32:00:55";
                break;
            case 5:
                // message rgb bulbs ON
                messageBytes = "22:00:55";
                break;
            case 6:
                // message all rgb-w bulbs ON
                messageBytes = "42:00:55";
                break;
            case 7:
                // message rgb-w bulbs channel1 ON
                messageBytes = "45:00:55";
                break;
            case 8:
                // message rgb-w bulbs channel2 ON
                messageBytes = "47:00:55";
                break;
            case 9:
                // message rgb-w bulbs channel3 ON
                messageBytes = "49:00:55";
                break;
            case 10:
                // message rgb-w bulbs channel4 ON
                messageBytes = "4B:00:55";
                break;
        }
        sendMessage(messageBytes);
    }

    public int setOff(int bulb) {
        logger.debug("milight: sendOff");
        String messageBytes = null;
        switch (bulb) {
            case 0:
                // message all white bulbs OFF
                messageBytes = "39:00:55";
                break;
            case 1:
                // message white bulb channel 1 OFF
                messageBytes = "3B:00:55";
                break;
            case 2:
                // message white bulb channel 2 OFF
                messageBytes = "33:00:55";
                break;
            case 3:
                // message white bulb channel 3 OFF
                messageBytes = "3A:00:55";
                break;
            case 4:
                // message white bulb channel 4 OFF
                messageBytes = "36:00:55";
                break;
            case 5:
                // message rgb bulbs OFF
                messageBytes = "21:00:55";
                break;
            case 6:
                // message all rgb-w bulbs OFF
                messageBytes = "41:00:55";
                break;
            case 7:
                // message rgb-w bulbs channel1 OFF
                messageBytes = "46:00:55";
                break;
            case 8:
                // message rgb-w bulbs channel2 OFF
                messageBytes = "48:00:55";
                break;
            case 9:
                // message rgb-w bulbs channel3 OFF
                messageBytes = "4A:00:55";
                break;
            case 10:
                // message rgb-w bulbs channel4 OFF
                messageBytes = "4C:00:55";
                break;
        }
        sendMessage(messageBytes);
        return 0;
    }

    /**
     *
     * @param bulb A bulb id (0..5 white, 6..10 rgb)
     * @param hue A value from 0 to 360
     */
    public void setColor(int bulb, int hue) {
        logger.debug("milight: sendColor");

        // we have to map [0,360] to [0,0xFF], where red equals hue=0 and the milight color 0xB0 (=176)
        Integer milightColorNo = (256 + 176 - (int) (hue / 360.0 * 255.0)) % 256;
        if (bulb == 5) {
            String messageBytes = "20:" + Integer.toHexString(milightColorNo) + ":55";
            sendMessage(messageBytes);
        }
        if (bulb > 5) {
            setOn(bulb);
            sleep(100);
            String messageBytes = "40:" + Integer.toHexString(milightColorNo) + ":55";
            sendMessage(messageBytes);
        }
    }

    protected void sendMessage(String messageBytes) {
        byte[] buffer = getMessageBytes(messageBytes);
        packet.setData(buffer);
        try {
            datagramSocket.send(packet);
            logger.debug("Sent packet '{}' to bridge '{}' ({})",
                    new Object[] { messageBytes, getBridgeId(), packet.getAddress().getHostAddress() });
        } catch (Exception e) {
            logger.error("Failed to send Message to '{}': ",
                    new Object[] { packet.getAddress().getHostAddress(), e.getMessage() });
        }
    }

    private byte[] getMessageBytes(String messageBytes) {
        logger.debug("milight: messageBytes to transform: '{}'", messageBytes);
        if (messageBytes == null) {
            logger.error("messageBytes must not be null");
            return null;
        }

        byte[] buffer = new byte[3];
        String[] hex = messageBytes.split("(\\:|\\-)");

        int hexIndex = 0;
        for (hexIndex = 0; hexIndex < 3; hexIndex++) {
            buffer[hexIndex] = (byte) Integer.parseInt(hex[hexIndex], 16);
        }
        return buffer;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public InetAddress getAddr() {
        return packet.getAddress();
    }

    public int getPort() {
        return packet.getPort();
    }
}
