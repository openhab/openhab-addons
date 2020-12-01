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
package org.openhab.binding.sony.internal.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.RawType;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;

/**
 * This class provides utility methods related to general network activities
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NetUtil {
    /**
     * Gets the remote device identifier. Sony only requires it to be similar to a mac address and constant across
     * sessions. Sony AVs require the "MediaRemote:" part of the device ID (all other devices don't care).
     *
     * @return the non-null, non-empty device id
     */
    public static String getDeviceId() {
        return "MediaRemote:00-11-22-33-44-55";
    }

    /**
     * Gets the remote device name. The remote name will simply be "openHab({{getDeviceId()}})"
     *
     * @return the non-null, non-empty device name
     */
    public static String getDeviceName() {
        return "openHAB (" + getDeviceId() + ")";
    }

    /**
     * Creates an authorization header using the specified access code
     *
     * @param accessCode a non-null, non-empty access code
     * @return the non-null header
     */
    public static Header createAuthHeader(final String accessCode) {
        Validate.notEmpty(accessCode, "accessCode cannot be empty");

        final String authCode = Base64.getEncoder()
                .encodeToString((":" + StringUtils.leftPad(accessCode, 4, "0")).getBytes());
        return new Header("Authorization", "Basic " + authCode);
    }

    /**
     * Creates an access code header using the specified access code
     *
     * @param accessCode a non-null, non-empty access code
     * @return the non-null header
     */
    public static Header createAccessCodeHeader(final String accessCode) {
        Validate.notEmpty(accessCode, "accessCode cannot be empty");
        return new Header("X-Auth-PSK", accessCode);
    }

    /**
     * Returns the base url (protocol://domain{:port}) for a given url
     * 
     * @param url a non-null URL
     * @return the base URL
     */
    public static String toBaseUrl(final URL url) {
        Objects.requireNonNull(url, "url cannot be null");

        final String protocol = url.getProtocol();
        final String host = url.getHost();
        final int port = url.getPort();

        return port == -1 ? String.format("%s://%s", protocol, host)
                : String.format("%s://%s:%d", protocol, host, port);
    }

    /**
     * Creates a 'sony' URI out of a base URI and a service name
     * 
     * @param baseUri a non-null base URI
     * @param serviceName a non-null, non-empty service name
     * @return a string representing the 'sony' URI for the service
     */
    public static String getSonyUri(final URI baseUri, final String serviceName) {
        Objects.requireNonNull(baseUri, "baseUri cannot be null");
        Validate.notEmpty(serviceName, "serviceName cannot be empty");

        final String protocol = baseUri.getScheme();
        final String host = baseUri.getHost();

        return String.format("%s://%s/sony/%s", protocol, host, serviceName);
    }

    /**
     * Creates a 'sony' URL out of a base URL and a service name
     * 
     * @param baseUri a non-null base URL
     * @param serviceName a non-null, non-empty service name
     * @return a string representing the 'sony' URL for the service
     */
    public static String getSonyUrl(final URL baseUrl, final String serviceName) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        Validate.notEmpty(serviceName, "serviceName cannot be empty");

        // Note: we repeat the getSonyUri logic here - we don't use toUri()
        // because it will introduce a stupid exception we need to catch
        final String protocol = baseUrl.getProtocol();
        final String host = baseUrl.getHost();

        return String.format("%s://%s/sony/%s", protocol, host, serviceName);
    }

    /**
     * Send a wake on lan (WOL) packet to the specified ipAddress and macAddress
     *
     * @param ipAddress the non-null, non-empty ip address
     * @param macAddress the non-null, non-empty mac address
     * @throws IOException if an IO exception occurs sending the WOL packet
     */
    public static void sendWol(final String ipAddress, final String macAddress) throws IOException {
        Validate.notEmpty(ipAddress, "ipAddress cannot be empty");
        Validate.notEmpty(macAddress, "macAddress cannot be empty");

        final byte[] macBytes = new byte[6];
        final String[] hex = macAddress.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (final NumberFormatException e) {
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

        // Resolve the ipaddress (in case it's a name)
        final InetAddress address = InetAddress.getByName(ipAddress);

        final byte[] addrBytes = address.getAddress();
        addrBytes[addrBytes.length - 1] = (byte) 0xff;
        final InetAddress broadcast = InetAddress.getByAddress(addrBytes);

        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, 9);
        final DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }

    /**
     * Determines if the specified address is potentially formatted as a mac address or not
     *
     * @param potentialMacAddress a possibly null, possibly empty mac address
     * @return true if formatted like a mac address, false otherwise
     */
    public static boolean isMacAddress(final @Nullable String potentialMacAddress) {
        if (potentialMacAddress == null || potentialMacAddress.length() != 17) {
            return false;
        }
        for (int i = 5; i >= 1; i--) {
            final char c = potentialMacAddress.charAt(i * 3 - 1);
            if (c != ':' && c != '-') {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the mac address represented by the byte array or null if not a WOL representation
     *
     * @param wakeOnLanBytes the possibly null wake on lan bytes
     * @return the mac address or null if not a mac address
     */
    public @Nullable static String getMacAddress(final byte @Nullable [] wakeOnLanBytes) {
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
     * Get's the URL that is relative to another URL
     *
     * @param baseUrl the non-null base url
     * @param otherUrl the non-null, non-empty other url
     * @return the combined URL or null if a malformed
     */
    public static @Nullable URL getUrl(final URL baseUrl, final String otherUrl) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        Validate.notEmpty(otherUrl, "otherUrl cannot be empty");
        try {
            return new URL(baseUrl, otherUrl);
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    /**
     * Gets an raw type from the given transport and url
     * 
     * @param transport a non-null http transport to use
     * @param url a possibly null, possibly empty URL to use
     * @return a rawtype (with correct mime) or null if not found (or URL was null/empty)
     */
    public static @Nullable RawType getRawType(final SonyHttpTransport transport, final @Nullable String url) {
        Objects.requireNonNull(transport, "transport is not null");

        byte[] iconData = null;
        String mimeType = RawType.DEFAULT_MIME_TYPE;

        if (url != null && StringUtils.isNotEmpty(url)) {
            final HttpResponse resp = transport.executeGet(url);
            if (resp.getHttpCode() == HttpStatus.OK_200) {
                iconData = resp.getContentAsBytes();
                mimeType = resp.getResponseHeader(HttpHeader.CONTENT_TYPE.asString());
                if (StringUtils.isEmpty(mimeType)) {
                    // probably a 'content' header of value 'Content-Type: image/png' instead
                    mimeType = resp.getResponseHeader("content");
                    final int idx = mimeType.indexOf(":");
                    if (idx >= 0) {
                        mimeType = mimeType.substring(idx + 1).trim();
                    }
                }
            }
        }

        return iconData == null ? null : new RawType(iconData, mimeType);
    }

    /**
     * Send a request to the specified ipaddress/port using a socket connection. Any results will be sent back via the
     * callback.
     *
     * @param ipAddress the non-null, non-empty ip address
     * @param port the port
     * @param request the non-null, non-empty request
     * @param callback the non-null callback
     * @throws IOException if an IO exception occurs sending the request
     */
    public static void sendSocketRequest(final String ipAddress, final int port, final String request,
            final SocketSessionListener callback) throws IOException {
        Validate.notEmpty(ipAddress, "ipAddress cannot be empty");
        Validate.notEmpty(request, "request cannot be empty");
        Objects.requireNonNull(callback, "callback cannot be null");

        final Socket socket = new Socket();
        try {
            socket.setSoTimeout(10000);
            socket.connect(new InetSocketAddress(ipAddress, port));

            final PrintStream ps = new PrintStream(socket.getOutputStream());
            final BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ps.print(request + "\n");
            ps.flush();

            int c;
            final StringBuilder sb = new StringBuilder(100);
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
                } catch (final SocketTimeoutException e) {
                    final String str = sb.toString();
                    callback.responseReceived(str);
                    break;

                } catch (final IOException e) {
                    callback.responseException(e);
                    break;
                }
            }
        } finally {
            socket.close();
        }
    }
}
