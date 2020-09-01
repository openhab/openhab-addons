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

package org.openhab.binding.ipcamera.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * The {@link Helper} class has static functions that help the IpCamera binding not need as many external libs.
 *
 *
 * @author Matthew Skinner - Initial contribution
 */
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

    public static String encodeSpecialChars(String text) {
        String processed = text;
        try {
            processed = URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {

        }
        return processed;
    }

}
