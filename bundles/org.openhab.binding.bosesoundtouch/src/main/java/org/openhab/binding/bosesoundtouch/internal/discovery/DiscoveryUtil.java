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
package org.openhab.binding.bosesoundtouch.internal.discovery;

import java.io.IOException;

import org.openhab.core.io.net.http.HttpUtil;

/**
 * The {@link DiscoveryUtil} is a static helper class, to get infos from a XML
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class DiscoveryUtil {

    /**
     * Finds the content in an element
     *
     * This is a quick and dirty method, it always delivers the first appearance of content in an element
     */
    public static String getContentOfFirstElement(String content, String element) {
        if (content == null) {
            return "";
        }
        String beginTag = "<" + element + ">";
        String endTag = "</" + element + ">";

        int startIndex = content.indexOf(beginTag) + beginTag.length();
        int endIndex = content.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            return content.substring(startIndex, endIndex);
        } else {
            return "";
        }
    }

    /**
     * Executes an URL and returns to answer
     */
    public static String executeUrl(String url) throws IOException {
        return HttpUtil.executeUrl("GET", url, 5000);
    }
}
