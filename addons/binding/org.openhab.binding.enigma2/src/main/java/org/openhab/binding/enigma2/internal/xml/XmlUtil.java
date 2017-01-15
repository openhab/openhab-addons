/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal.xml;

/**
 * The {@link XmlUtil} is a static helper class, to get infos from a XML
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class XmlUtil {

    /**
     * Processes an string containing xml and returning the content of a
     * specific tag (alyways lowercase)
     */
    public static String getContentOfElement(String content, String element) {

        final String beginTag = "<" + element + ">";
        final String endTag = "</" + element + ">";

        final int startIndex = content.indexOf(beginTag) + beginTag.length();
        final int endIndex = content.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            return content.substring(startIndex, endIndex);
        } else {
            return null;
        }
    }
}
