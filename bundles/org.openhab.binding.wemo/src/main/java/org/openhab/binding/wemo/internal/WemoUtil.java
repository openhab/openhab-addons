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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String unescape(final String text) {
        StringBuilder result = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char charAt = text.charAt(i);
            if (charAt != '&') {
                result.append(charAt);
                i++;
            } else {
                if (text.startsWith("&amp;", i)) {
                    result.append('&');
                    i += 5;
                } else if (text.startsWith("&apos;", i)) {
                    result.append('\'');
                    i += 6;
                } else if (text.startsWith("&quot;", i)) {
                    result.append('"');
                    i += 6;
                } else if (text.startsWith("&lt;", i)) {
                    result.append('<');
                    i += 4;
                } else if (text.startsWith("&gt;", i)) {
                    result.append('>');
                    i += 4;
                } else {
                    i++;
                }
            }
        }
        return result.toString();
    }

    public static String unescapeXml(final String xml) {
        Pattern xmlEntityRegex = Pattern.compile("&(#?)([^;]+);");
        // Unfortunately, Matcher requires a StringBuffer instead of a StringBuilder
        StringBuffer unescapedOutput = new StringBuffer(xml.length());

        Matcher m = xmlEntityRegex.matcher(xml);
        Map<String, String> builtinEntities = null;
        String entity;
        String hashmark;
        String ent;
        int code;
        while (m.find()) {
            ent = m.group(2);
            hashmark = m.group(1);
            if ((hashmark != null) && (hashmark.length() > 0)) {
                code = Integer.parseInt(ent);
                entity = Character.toString((char) code);
            } else {
                // must be a non-numerical entity
                if (builtinEntities == null) {
                    builtinEntities = buildBuiltinXMLEntityMap();
                }
                entity = builtinEntities.get(ent);
                if (entity == null) {
                    // not a known entity - ignore it
                    entity = "&" + ent + ';';
                }
            }
            m.appendReplacement(unescapedOutput, entity);
        }
        m.appendTail(unescapedOutput);

        return unescapedOutput.toString();
    }

    private static Map<String, String> buildBuiltinXMLEntityMap() {
        Map<String, String> entities = new HashMap<String, String>(10);
        entities.put("lt", "<");
        entities.put("gt", ">");
        entities.put("amp", "&");
        entities.put("apos", "'");
        entities.put("quot", "\"");
        return entities;
    }

    public static String createBinaryStateContent(boolean binaryState) {
        String binary = binaryState ? "1" : "0";
        String content = "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>" + "<u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">" + "<BinaryState>"
                + binary + "</BinaryState>" + "</u:SetBinaryState>" + "</s:Body>" + "</s:Envelope>";
        return content;
    }

    public static String createStateRequestContent(String action, String actionService) {
        String content = "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:" + actionService + ":1\">" + "</u:"
                + action + ">" + "</s:Body>" + "</s:Envelope>";
        return content;
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }
}
