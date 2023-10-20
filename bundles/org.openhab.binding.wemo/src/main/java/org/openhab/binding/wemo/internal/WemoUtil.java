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
package org.openhab.binding.wemo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * {@link WemoUtil} implements some helper functions.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class WemoUtil {

    public static String substringBefore(@Nullable String string, String pattern) {
        if (string != null) {
            int pos = string.indexOf(pattern);
            if (pos > 0) {
                return string.substring(0, pos);
            }
        }
        return "";
    }

    public static String substringBetween(@Nullable String string, String begin, String end) {
        if (string != null) {
            int s = string.indexOf(begin);
            if (s != -1) {
                String result = string.substring(s + begin.length());
                return substringBefore(result, end);
            }
        }
        return "";
    }

    public static String createBinaryStateContent(boolean binaryState) {
        String binary = binaryState ? "1" : "0";
        return """
                <?xml version="1.0"?>\
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">\
                <s:Body>\
                <u:SetBinaryState xmlns:u="urn:Belkin:service:basicevent:1">\
                <BinaryState>\
                """
                + binary + "</BinaryState>" + "</u:SetBinaryState>" + "</s:Body>" + "</s:Envelope>";
    }

    public static String createStateRequestContent(String action, String actionService) {
        return """
                <?xml version="1.0"?>\
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">\
                <s:Body>\
                <u:\
                """
                + action + " xmlns:u=\"urn:Belkin:service:" + actionService + ":1\">" + "</u:" + action + ">"
                + "</s:Body>" + "</s:Envelope>";
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData cd) {
            return cd.getData();
        }
        return "?";
    }
}
