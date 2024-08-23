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
package org.openhab.binding.lutron.internal.protocol.leap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * Abstract base class for LEAP message body objects
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMessageBody {

    /**
     * Utility method to extract the int from a href String using the supplied Pattern.
     *
     * @return the int or 0 if unable to extract
     */
    protected static int hrefNumber(Pattern pattern, @Nullable String href) {
        if (href == null) {
            return 0;
        }
        Matcher matcher = pattern.matcher(href);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
