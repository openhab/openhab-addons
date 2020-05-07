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
package org.openhab.binding.kaleidescape.internal.communication;

import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KaleidescapeFormatter} is a utility class with formatting methods for Kaleidescape strings
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeFormatter {
    
    public static String formatString(String input) {
        if (!StringUtils.isEmpty(input)) {
            //convert || back to :
            input = input.replace("||", ":");
            
            //fix escaped :
            input = input.replace("\\:", ":");
            
            //fix escaped / 
            input = input.replace("\\/", "/");
            
            //convert \r into comma space 
            input = input.replace("\\r", ", ");
            
            //convert \d146 from review text into apostrophe
            input = input.replace("\\d146", "'");
            
            //convert \d147 & \d148 from review text into double quote
            input = input.replace("\\d147", "\"");
            input = input.replace("\\d148", "\"");
            
            // fix the encoding for k mangled latin1 characters (chars coming in as \dnnn)
            input = input.replaceAll("(?i)\\\\d([0-9]{3})", "\\&#$1;");
            return StringEscapeUtils.unescapeHtml(input);
        }       
        return input;
    }
    
    public static byte[] getRawDataFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        return IOUtils.toByteArray(connection.getInputStream());
    }
    

}
