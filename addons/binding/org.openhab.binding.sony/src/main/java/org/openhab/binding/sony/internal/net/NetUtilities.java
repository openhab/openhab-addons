/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.jupnp.model.types.UDN;

// TODO: Auto-generated Javadoc
/**
 * The Class NetUtilities.
 *
 * @author Tim Roberts - Initial contribution
 */
public class NetUtilities {
    // private final static Logger logger = LoggerFactory.getLogger(NetUtilities.class);

    /**
     * Gets the device id.
     *
     * @return the device id
     */
    public static String getDeviceId() {
        return getDeviceId("00-11-22-33-44-55");
    }

    /**
     * Gets the device id.
     *
     * @param uniqueId the unique id
     * @return the device id
     */
    public static String getDeviceId(String uniqueId) {
        // Must be "MediaRemote" - AVs enforce this
        return "MediaRemote:" + uniqueId;
    }

    /**
     * Gets the device name.
     *
     * @param deviceId the device id
     * @return the device name
     */
    public static String getDeviceName(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            deviceId = getDeviceId();
        }
        return "OpenHAB (" + deviceId + ")";
    }

    /**
     * Creates the auth header.
     *
     * @param accessCode the access code
     * @return the header
     */
    public static Header createAuthHeader(int accessCode) {
        final String authCode = java.util.Base64.getEncoder()
                .encodeToString((":" + StringUtils.leftPad(Integer.toString(accessCode), 4, "0")).getBytes());
        return new Header("Authorization", "Basic " + authCode);
    }

    /**
     * Creates the http request.
     *
     * @return the http request
     */
    public static HttpRequest createHttpRequest() {
        return createHttpRequest(null, null);
    }

    /**
     * Creates the http request.
     *
     * @param accessCode the access code
     * @return the http request
     */
    public static HttpRequest createHttpRequest(Integer accessCode) {
        return createHttpRequest(accessCode, null);
    }

    /**
     * Creates the http request.
     *
     * @param accessCode the access code
     * @param deviceIdHeader the device id header
     * @return the http request
     */
    public static HttpRequest createHttpRequest(Integer accessCode, String deviceIdHeader) {

        final String deviceId = getDeviceId();
        final String deviceName = getDeviceName(deviceId);

        if (StringUtils.isEmpty(deviceIdHeader)) {
            deviceIdHeader = "CERS-DEVICE-ID";
        }

        final HttpRequest httpRequest = new HttpRequest(deviceName, deviceId);

        httpRequest.addHeader("User-Agent", "OpenHab/Sony/Binding");
        httpRequest.addHeader("X-CERS-DEVICE-INFO", "OpenHab/Sony/Binding");
        httpRequest.addHeader("Connection", "close");

        httpRequest.addHeader("X-" + deviceIdHeader, deviceId);

        if (accessCode != null) {
            // pre-shared key compatibility
            httpRequest.addHeader("X-Auth-PSK", accessCode.toString());
        }

        return httpRequest;
    }

    /**
     * Send wol.
     *
     * @param ipAddress the ip address
     * @param macAddress the mac address
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void sendWol(String ipAddress, String macAddress) throws IOException {
        if (macAddress == null || macAddress.length() == 0) {
            return;
        }

        if (ipAddress == null || ipAddress.length() == 0) {
            return;
        }

        final byte[] macBytes = new byte[6];
        final String[] hex = macAddress.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }

        final byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        // logger.debug("Sending WOL to " + ipAddress + " (" + macAddress + ")");
        final InetAddress address = InetAddress.getByName(ipAddress);
        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
        final DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }

    // public static String generateMacLikeAddress() {
    // final Random rand = new Random();
    // final byte[] macAddr = new byte[6];
    // rand.nextBytes(macAddr);
    //
    // macAddr[0] = (byte) (macAddr[0] & (byte) 254); // zeroing last 2 bytes to make it unicast and locally
    // // adminstrated
    //
    // final StringBuilder sb = new StringBuilder(18);
    // for (byte b : macAddr) {
    //
    // if (sb.length() > 0) {
    // sb.append(":");
    // }
    //
    // sb.append(String.format("%02x", b));
    // }
    //
    // return sb.toString();
    // }
    //
    // public static String getMacAddress() {
    // try {
    //
    // for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
    // NetworkInterface netif = en.nextElement();
    // try {
    // final byte[] mac = netif.getHardwareAddress();
    // if (mac != null) {
    //
    // final StringBuilder sb = new StringBuilder();
    // for (int i = 0; i < mac.length; i++) {
    // sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    // }
    // return sb.toString();
    // }
    // } catch (SocketException e) {
    //
    // }
    // }
    //
    // // } catch (UnknownHostException e) {
    // // return null;
    // } catch (SocketException e) {
    // // return null;
    // }
    //
    // }

    /**
     * Gets the mac address.
     *
     * @param wakeOnLanBytes the wake on lan bytes
     * @return the mac address
     */
    public static String getMacAddress(byte[] wakeOnLanBytes) {
        if (wakeOnLanBytes != null && wakeOnLanBytes.length >= 12) {
            final StringBuffer macAddress = new StringBuffer(16);
            for (int i = 6; i < 12; i++) {
                macAddress.append(StringUtils.leftPad(Integer.toHexString(wakeOnLanBytes[i]), 2, '0'));
                macAddress.append(":");
            }
            macAddress.deleteCharAt(macAddress.length() - 1);
            return macAddress.toString();
        }
        return null;
    }

    /**
     * Gets the mac address.
     *
     * @param udn the udn
     * @return the mac address
     */
    public static String getMacAddress(UDN udn) {
        final String udnId = udn.getIdentifierString();
        if (udnId != null) {
            final int idx = udnId.lastIndexOf('-');
            if (idx >= 0) {
                final String mac = udnId.substring(idx + 1);
                if (mac.length() == 12) {
                    final StringBuffer macAddress = new StringBuffer(17);
                    for (int i = 0; i < mac.length(); i += 2) {
                        macAddress.append(mac.substring(i, i + 2));
                        macAddress.append(":");
                    }
                    macAddress.deleteCharAt(macAddress.length() - 1);
                    return macAddress.toString();
                }
            }
        }
        return null;
    }

    /**
     * Gets the uri.
     *
     * @param baseUri the base uri
     * @param otherUri the other uri
     * @return the uri
     */
    public static URI getUri(URI baseUri, String otherUri) {
        return baseUri.resolve(otherUri);
        // try {
        // URI other = new URI(otherUri);
        // if (other.isAbsolute()) {
        // return other;
        // }
        // } catch (URISyntaxException e) {
        // }
        //
        // final String basepath = baseUri.getPath();
        // return baseUri.resolve(basepath + (otherUri.startsWith("/") || basepath.endsWith("/") ? "" : "/") +
        // otherUri);
    }

    // public static HttpResponse sendJsonRequest(String address, String json) throws IOException {
    //
    // Client client = ClientBuilder.newClient();
    // WebTarget target = client.target(address);
    //
    // final Response response = target.request().post(Entity.json(json));
    //
    // try {
    // return new HttpResponse(response);
    // } finally {
    // response.close();
    // client.close();
    // }
    // }

    /**
     * Send socket request.
     *
     * @param ipAddress the ip address
     * @param port the port
     * @param request the request
     * @param callback the callback
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void sendSocketRequest(String ipAddress, int port, String request, SocketSessionListener callback)
            throws IOException {
        final Socket socket = new Socket();
        try {
            socket.setSoTimeout(3000);
            socket.connect(new InetSocketAddress(ipAddress, port));

            PrintStream ps = new PrintStream(socket.getOutputStream());
            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ps.println(request + "\n");
            ps.flush();

            int c;
            StringBuilder sb = new StringBuilder(100);
            while (true) {
                try {

                    c = bf.read();
                    if (c == -1) {
                        final String str = sb.toString();
                        callback.responseReceived(str);
                        break;
                    }
                    final char ch = (char) c;
                    if (ch == '\n') {
                        final String str = sb.toString();
                        sb.setLength(0);
                        if (callback.responseReceived(str)) {
                            break;
                        }
                    }
                    sb.append(ch);
                } catch (SocketTimeoutException e) {
                    final String str = sb.toString();
                    callback.responseReceived(str);
                    break;

                } catch (IOException e) {
                    callback.responseException(e);
                    break;
                }
            }
        } finally {
            socket.close();
        }
    }
}
