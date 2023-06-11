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
package org.openhab.binding.ipcamera.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Helper} class has static functions that help the IpCamera binding not need as many external libs.
 *
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class Helper {

    /**
     * The {@link searchString} Used to grab values out of JSON or other quote encapsulated structures without needing
     * an external lib. String may be terminated by ," or }.
     *
     * @author Matthew Skinner - Initial contribution
     */
    public static String searchString(String rawString, String searchedString) {
        String result = "";
        int index = 0;
        index = rawString.indexOf(searchedString);
        if (index != -1) // -1 means "not found"
        {
            result = rawString.substring(index + searchedString.length(), rawString.length());
            index = result.indexOf(',');
            if (index == -1) {
                index = result.indexOf('"');
                if (index == -1) {
                    index = result.indexOf('}');
                    if (index == -1) {
                        return result;
                    } else {
                        return result.substring(0, index);
                    }
                } else {
                    return result.substring(0, index);
                }
            } else {
                result = result.substring(0, index);
                index = result.indexOf('"');
                if (index == -1) {
                    return result;
                } else {
                    return result.substring(0, index);
                }
            }
        }
        return "";
    }

    public static String fetchXML(String message, String sectionHeading, String key) {
        String result = "";
        int sectionHeaderBeginning = 0;
        if (!sectionHeading.isEmpty()) {// looking for a sectionHeading
            sectionHeaderBeginning = message.indexOf(sectionHeading);
        }
        if (sectionHeaderBeginning == -1) {
            return "";
        }
        int startIndex = message.indexOf(key, sectionHeaderBeginning + sectionHeading.length());
        if (startIndex == -1) {
            return "";
        }
        int endIndex = message.indexOf("<", startIndex + key.length());
        if (endIndex > startIndex) {
            result = message.substring(startIndex + key.length(), endIndex);
        }
        // remove any quotes and anything after the quote.
        sectionHeaderBeginning = result.indexOf("\"");
        if (sectionHeaderBeginning > 0) {
            result = result.substring(0, sectionHeaderBeginning);
        }
        // remove any ">" and anything after it.
        sectionHeaderBeginning = result.indexOf(">");
        if (sectionHeaderBeginning > 0) {
            result = result.substring(0, sectionHeaderBeginning);
        }
        if (!key.endsWith(">")) {
            startIndex = result.indexOf(">");
            if (startIndex != -1) {
                return result.substring(startIndex + 1);
            }
        }
        return result;
    }

    /**
     * The {@link encodeSpecialChars} Is used to replace spaces with %20 in Strings meant for URL queries.
     *
     * @author Matthew Skinner - Initial contribution
     */
    public static String encodeSpecialChars(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public static String getLocalIpAddress() {
        String ipAddress = "";
        try {
            for (Enumeration<NetworkInterface> enumNetworks = NetworkInterface.getNetworkInterfaces(); enumNetworks
                    .hasMoreElements();) {
                NetworkInterface networkInterface = enumNetworks.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().toString().length() < 18
                            && inetAddress.isSiteLocalAddress()) {
                        ipAddress = inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return ipAddress;
    }
}
