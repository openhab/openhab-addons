/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Utils} class defines an HTTP Server for authentication callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final List<Integer> PORTS = new ArrayList<>();
    private static int port = 8090;

    public static final Gson GSON = new Gson();

    /**
     * Get free port without other Thread interference
     *
     * @return
     */
    public static synchronized int getFreePort() {
        while (PORTS.contains(port)) {
            port++;
        }
        PORTS.add(port);
        return port;
    }

    public static synchronized void addPort(int portNr) {
        if (PORTS.contains(portNr)) {
            LOGGER.warn("Port {} already occupied", portNr);
        }
        PORTS.add(portNr);
    }

    public static synchronized void removePort(int portNr) {
        PORTS.remove(Integer.valueOf(portNr));
    }

    public static String getCallbackIP() throws SocketException {
        // https://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
        // https://stackoverflow.com/questions/1062041/ip-address-not-obtained-in-java
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                .hasMoreElements();) {
            NetworkInterface iface = ifaces.nextElement();
            try {
                if (!iface.isLoopback()) {
                    if (iface.isUp()) {
                        for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses
                                .hasMoreElements();) {
                            InetAddress address = addresses.nextElement();
                            if (address instanceof Inet4Address) {
                                return address.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException se) {
                // Calling one network interface failed - continue searching
                LOGGER.trace("Network {} failed {}", iface.getName(), se.getMessage());
            }
        }
        throw new SocketException("IP address not detected");
    }

    public static String getCallbackAddress(String callbackIP, int callbackPort) {
        return "http://" + callbackIP + Constants.COLON + callbackPort + Constants.CALLBACK_ENDPOINT;
    }

    public static String getRestAPIServer(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.REST_API_BASE_PA;
            case Constants.REGION_CHINA:
                return Constants.REST_API_BASE_CN;
            case Constants.REGION_NORAM:
                return Constants.REST_API_BASE_NA;
            default:
                return Constants.REST_API_BASE;
        }
    }

    public static String getLoginServer(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.LOGIN_BASE_URI_PA;
            case Constants.REGION_CHINA:
                return Constants.LOGIN_BASE_URI_CN;
            case Constants.REGION_NORAM:
                return Constants.LOGIN_BASE_URI_NA;
            default:
                return Constants.LOGIN_BASE_URI;
        }
    }

    public static String getWebsocketServer(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.WEBSOCKET_API_BASE_PA;
            case Constants.REGION_CHINA:
                return Constants.WEBSOCKET_API_BASE_CN;
            case Constants.REGION_NORAM:
                return Constants.WEBSOCKET_API_BASE_PA;
            default:
                return Constants.WEBSOCKET_API_BASE;
        }
    }

    public static String getApplication(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.X_APPLICATIONNAME_AP;
            case Constants.REGION_CHINA:
                return Constants.X_APPLICATIONNAME_CN;
            case Constants.REGION_NORAM:
                return Constants.X_APPLICATIONNAME_US;
            default:
                return Constants.X_APPLICATIONNAME;
        }
    }

    public static String getRisApplicationVersion(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.RIS_APPLICATION_VERSION_PA;
            case Constants.REGION_CHINA:
                return Constants.RIS_APPLICATION_VERSION_CN;
            case Constants.REGION_NORAM:
                return Constants.RIS_APPLICATION_VERSION_NA;
            default:
                return Constants.RIS_APPLICATION_VERSION;
        }
    }

    public static String getUserAgent(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.WEBSOCKET_USER_AGENT_PA;
            case Constants.REGION_CHINA:
                return Constants.WEBSOCKET_USER_AGENT_CN;
            default:
                return Constants.WEBSOCKET_USER_AGENT;
        }
    }

    public static String getRisSDKVersion(String region) {
        switch (region) {
            case Constants.REGION_CHINA:
                return Constants.RIS_SDK_VERSION_CN;
            default:
                return Constants.RIS_SDK_VERSION;
        }
    }

    public static String getAuthURL(String region) {
        return getRestAPIServer(region) + "/v1/login";
    }

    public static String getTokenUrl(String region) {
        return getLoginServer(region) + "/as/token.oauth2";
    }

    public static String getLoginAppId(String region) {
        switch (region) {
            case Constants.REGION_CHINA:
                return Constants.LOGIN_APP_ID_CN;
            default:
                return Constants.LOGIN_APP_ID;
        }
    }

    /** Read the object from Base64 string. */
    public static Object fromString(String s) {
        try {
            byte[] data = Base64.getDecoder().decode(s);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Error converting string to token {}", e.getMessage());
        }
        return Constants.NOT_SET;
    }

    /** Write the object to a Base64 string. */
    public static String toString(Serializable o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            LOGGER.warn("Error converting token to string {}", e.getMessage());
        }
        return Constants.NOT_SET;
    }
}
