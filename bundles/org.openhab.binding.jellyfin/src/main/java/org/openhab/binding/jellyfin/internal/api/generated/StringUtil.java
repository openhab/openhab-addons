/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated;

import java.util.Collection;
import java.util.Iterator;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StringUtil {
    /**
     * Check if the given array contains the given value (with case-insensitive comparison).
     *
     * @param array The array
     * @param value The value to search
     * @return true if the array contains the value
     */
    public static boolean containsIgnoreCase(String[] array, String value) {
        for (String str : array) {
            if (value == null && str == null) {
                return true;
            }
            if (value != null && value.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Join an array of strings with the given separator.
     * <p>
     * Note: This might be replaced by utility method from commons-lang or guava someday
     * if one of those libraries is added as dependency.
     * </p>
     *
     * @param array The array of strings
     * @param separator The separator
     * @return the resulting string
     */
    public static String join(String[] array, String separator) {
        int len = array.length;
        if (len == 0) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        out.append(array[0]);
        for (int i = 1; i < len; i++) {
            out.append(separator).append(array[i]);
        }
        return out.toString();
    }

    /**
     * Join a list of strings with the given separator.
     *
     * @param list The list of strings
     * @param separator The separator
     * @return the resulting string
     */
    public static String join(Collection<String> list, String separator) {
        Iterator<String> iterator = list.iterator();
        StringBuilder out = new StringBuilder();
        if (iterator.hasNext()) {
            out.append(iterator.next());
        }
        while (iterator.hasNext()) {
            out.append(separator).append(iterator.next());
        }
        return out.toString();
    }
}
