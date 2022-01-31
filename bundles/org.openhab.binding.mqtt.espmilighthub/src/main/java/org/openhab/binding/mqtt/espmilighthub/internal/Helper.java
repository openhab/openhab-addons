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
package org.openhab.binding.mqtt.espmilighthub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Helper} Removes the need for any external JSON libs
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class Helper {
    /**
     * resolveJSON will return a value from any key/path that you give and the string can be terminated by any ,}"
     * characters.
     *
     */
    public static String resolveJSON(String messageJSON, String jsonPath, int resultMaxLength) {
        String result = "";
        int index = 0;
        index = messageJSON.indexOf(jsonPath);
        if (index != -1) {
            if ((index + jsonPath.length() + resultMaxLength) > messageJSON.length()) {
                result = (messageJSON.substring(index + jsonPath.length(), messageJSON.length()));
            } else {
                result = (messageJSON.substring(index + jsonPath.length(),
                        index + jsonPath.length() + resultMaxLength));
            }
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
}
