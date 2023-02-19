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
package org.openhab.binding.sonos.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link StringUtils} class defines some static string utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class StringUtils {

    /**
     * Simple method to escape XML special characters in String.
     * There are five XML Special characters which needs to be escaped :
     * & - &amp;
     * < - &lt;
     * > - &gt;
     * " - &quot;
     * ' - &apos;
     */
    public static String escapeXml(String xml) {
        xml = xml.replaceAll("&", "&amp;");
        xml = xml.replaceAll("<", "&lt;");
        xml = xml.replaceAll(">", "&gt;");
        xml = xml.replaceAll("\"", "&quot;");
        xml = xml.replaceAll("'", "&apos;");
        return xml;
    }

    /**
     * Simple method to un escape XML special characters in String.
     * There are five XML Special characters which needs to be escaped :
     * & - &amp;
     * < - &lt;
     * > - &gt;
     * " - &quot;
     * ' - &apos;
     */
    public static String unEscapeXml(String xml) {
        xml = xml.replaceAll("&amp;", "&");
        xml = xml.replaceAll("&lt;", "<");
        xml = xml.replaceAll("&gt;", ">");
        xml = xml.replaceAll("&quot;", "\"");
        xml = xml.replaceAll("&apos;", "'");
        return xml;
    }
}
