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
package org.openhab.binding.ws980wifi.internal.discovery;

import static java.lang.Math.toIntExact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joerg Dokupil - Initial contribution
 */
@NonNullByDefault
public class WS980WiFi {

    private Mac wsMac = new Mac("00:00:00:00:00:00");
    private String wsHost = "";
    private String wsDescription = "";
    private @Nullable InetAddress wsIP = null;
    private Integer wsPort = DATA_REQUEST_PORT; // port for TCP/IP requests e.g. for data extraction

    public float tempInside; // in 0,1 Grad C (x10)
    public float tempOutside; // in 0,1 Grad C (x10)
    public float tempDewPoint; // in 0,1 Grad C (x10) Taupunkt
    public float tempWindChill; // in 0,1 Grad C (x10) gefuehlteTemp
    public float heatIndex; // in 0,1 Grad C (x10)
    public float humidityInside; // in % innenFeuchte
    public float humidityOutside; // in % aussenFeuchte
    public float pressureAbsolut; // in 1/10 hPa (x10 = hPa) absDruck
    public float pressureRelative; // in 1/10 hPa (x10 = hPa) relDruck
    public float windDirection; // in Grad windRichtung
    public float windSpeed; // in 1/10 m/s windGeschwindigkeit
    public float windSpeedGust; // 12 Windboe
    public float rainLastHour; // 0,1 mm regenStd
    public float rainLastDay; // 0,1 mm regenTag
    public float rainLastWeek; // 0,1 mm regenWoche
    public float rainLastMonth; // 0,1 mm regenMonat
    public float rainLastYear; // 0,1 mm regenJahr
    public float rainTotal; // 0,1 mm regenGesamt
    public float lightLevel; // 0,1 lux lichtStaerke
    public float uvRaw; // uW/qm uvStaerke
    public float uvIndex; // 1-12

    protected final Logger logger = LoggerFactory.getLogger(WS980WiFi.class);

    public static final int DISCOVERY_DEST_PORT = 46000;
    public static final int DATA_REQUEST_PORT = 45000;
    public static final int DISCOVERY_RECEIVE_BUFFER_SIZE = 128;

    protected WS980WiFi(Mac mac, InetAddress ip, String description, Integer port) {
        this.wsMac = mac;
        this.wsHost = ip.getHostName();
        this.wsDescription = description;
        this.wsIP = ip;
        this.wsPort = port;
    }

    public WS980WiFi(String host, String port) {
        this.wsHost = host;
        this.wsPort = Integer.parseInt(port);
    }

    public String getMac() {
        return wsMac.getMacString();
    }

    public String getIP() {
        return wsIP.getHostAddress();
    }

    public String getHost() {
        return wsHost;
    }

    public String getDescription() {
        return wsDescription;
    }

    public Integer getPort() {
        return wsPort;
    }

    public Boolean refreshValues() {
        byte[] result = tcpipRequest(new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0x0b, (byte) 0x00, (byte) 0x06,
                (byte) 0x04, (byte) 0x04, (byte) 0x19 });
        if (result.length > 0) {
            this.tempInside = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 7, 9)))) / 10;
            this.tempOutside = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 10, 12)))) / 10;
            this.tempDewPoint = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 13, 15)))) / 10;
            this.tempWindChill = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 16, 18)))) / 10;
            this.heatIndex = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 19, 21)))) / 10;
            this.humidityInside = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 22, 23))));
            this.humidityOutside = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 24, 25))));
            this.pressureAbsolut = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 26, 28)))) * 10;
            this.pressureRelative = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 29, 31)))) * 10;
            this.windDirection = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 32, 34))));
            this.windSpeed = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 35, 37)))) / 10;
            this.windSpeedGust = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 38, 40)))) / 10;
            this.rainLastHour = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 41, 45)))) / 10;
            this.rainLastDay = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 46, 50)))) / 10;
            this.rainLastWeek = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 51, 55)))) / 10;
            this.rainLastMonth = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 56, 60)))) / 10;
            this.rainLastYear = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 61, 65)))) / 10;
            this.rainTotal = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 66, 70)))) / 10;
            this.lightLevel = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 71, 75)))) / 10;
            this.uvRaw = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 76, 78))));
            this.uvIndex = (float) (Long.decode("0x" + bytesToHex(Arrays.copyOfRange(result, 79, 80))));
            return true;
        } else {
            return false;
        }
    }

    private byte[] tcpipRequest(byte[] sendBytes) {
        byte[] receive = {};
        try (Socket aSocket = new Socket(wsHost, wsPort);
                OutputStream outStream = aSocket.getOutputStream();
                DataOutputStream mesgToServer = new DataOutputStream(outStream);
                InputStream inStream = aSocket.getInputStream();
                DataInputStream answerFromServer = new DataInputStream(inStream);) {
            mesgToServer.write(sendBytes, 0, sendBytes.length);
            receive = new byte[1024];
            answerFromServer.read(receive);
            return receive;
        } catch (IOException e) {
            logger.trace("---> tcpipRequest created an exception: {}", e.getMessage());
            return receive;
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.trace("---> tcpipRequest created an ArrayIndexOutOfBoundsException");
            return receive;
        }
    }

    /**
     * Class Method to discover objects of the class
     */

    public static WS980WiFi[] discoverDevices(int timeout, InetAddress sourceAddress) {
        final Logger logger = LoggerFactory.getLogger(WS980WiFi.class);
        byte[] receBytes = new byte[DISCOVERY_RECEIVE_BUFFER_SIZE];
        List<WS980WiFi> devices = new ArrayList<WS980WiFi>(50);
        WS980WiFi[] out = new WS980WiFi[0];

        logger.trace("Discovering WS980WIFI devices");

        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setBroadcast(true);
            sock.setReuseAddress(true);

            byte[] localHost = sourceAddress.getAddress();
            localHost[3] = (byte) 0xff;
            InetAddress broadcastAddress = InetAddress.getByAddress(localHost);

            byte[] sendBytes = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0x12, (byte) 0x00, (byte) 0x04,
                    (byte) 0x16 };
            DatagramPacket dpSend = new DatagramPacket(sendBytes, sendBytes.length, broadcastAddress,
                    DISCOVERY_DEST_PORT);
            sock.send(dpSend);
            logger.trace("Broadcast has been sent to {} and port {}", broadcastAddress.toString(), DISCOVERY_DEST_PORT);

            DatagramPacket dpReceive = new DatagramPacket(receBytes, receBytes.length);
            if (timeout == 0) {
                logger.trace("No discovery timeout was set. Blocking thread until answer received");
                sock.receive(dpReceive);
                logger.trace("Answer received");
                buildDevFromData(receBytes, devices);
                logger.trace("data received processed");
            } else {
                logger.trace("Discovery timeout was set to {}", timeout);
                long startTime = System.currentTimeMillis();
                long elapsed;
                while ((elapsed = System.currentTimeMillis() - startTime) < timeout) {
                    logger.trace("Elapsed: {} ms", elapsed);
                    logger.trace("Socket timeout: timeout-elapsed={}", (timeout - elapsed));

                    sock.setSoTimeout((int) (timeout - elapsed));

                    try {
                        logger.trace("Waiting for datagrams");
                        sock.receive(dpReceive);
                    } catch (SocketTimeoutException e) {
                        logger.trace("SocketTimeoutException: Socket timed out {} ms", sock.getSoTimeout());
                        logger.trace("SocketTimeOut after {} ms", System.currentTimeMillis() - startTime);
                        break;
                    }

                    logger.trace("Answer received");
                    buildDevFromData(receBytes, devices);
                    logger.trace("data received processed");
                }
            }
        } catch (IOException e) {
            logger.warn("IOException during discovering ws980wifi");
        }

        logger.trace("Converting list to array: {}", devices.size());
        out = new WS980WiFi[devices.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = devices.get(i);
        }
        logger.trace("End of device discovery: {}", out.length);
        return out;
    }

    private static void buildDevFromData(byte[] receBytes, List<WS980WiFi> devices) {
        final Logger logger = LoggerFactory.getLogger(WS980WiFi.class);
        Mac mac;
        InetAddress ip;
        String description;
        Integer port;

        if (receBytes[0] == (byte) 0xff) {
            logger.trace("Extract data from device response");
            try {
                mac = new Mac(Arrays.copyOfRange(receBytes, 5, 11));
                ip = InetAddress.getByAddress(Arrays.copyOfRange(receBytes, 11, 15));
                Integer byteCount = toIntExact(Long.decode("0x" + bytesToHex(Arrays.copyOfRange(receBytes, 17, 18))));
                description = new String(Arrays.copyOfRange(receBytes, 18, 18 + byteCount), "UTF-8");
                port = toIntExact(Long.decode("0x" + bytesToHex(Arrays.copyOfRange(receBytes, 15, 17))));

                WS980WiFi instance = new WS980WiFi(mac, ip, description, port); // create device object

                if (instance != null) {
                    logger.trace("Adding device ws980wifi to found devices list");
                    devices.add(instance);
                }
            } catch (Exception e) {
                logger.trace("the response from ws908wifi could not be processed, {}", e.getMessage());
            }
        } else {
            logger.trace("invalid WS980WIFI header data in discovery result");
        }
    }

    public static byte[] hexStringToByte(String hex, int bytes) {
        // Make sure the byte [] is always the correct length.
        byte[] key = new byte[bytes];
        // Using i as the distance from the END of the string.
        for (int i = 0; i < hex.length() && (i / 2) < bytes; i++) {
            // Pull out the hex value of the character.
            int nybble = Character.digit(hex.charAt(hex.length() - 1 - i), 16);
            if ((i & 1) != 0) {
                // When i is odd we shift left 4.
                nybble = nybble << 4;
            }
            // Use OR to avoid sign issues.
            key[bytes - 1 - (i / 2)] |= (byte) nybble;
        }
        return key;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
