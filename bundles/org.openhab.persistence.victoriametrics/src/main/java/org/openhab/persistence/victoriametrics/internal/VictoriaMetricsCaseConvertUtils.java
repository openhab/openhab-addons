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
package org.openhab.persistence.victoriametrics.internal;

/**
 * Utility class for converting camelCase strings to snake_case.
 * This is useful for formatting item names or metadata keys in a way that is compatible with VictoriaMetrics.
 *
 * @author Franz - Initial contribution
 */
public class VictoriaMetricsCaseConvertUtils {

    /**
     * Converts a camelCase string to snake_case.
     *
     * @param str the camelCase string to convert
     * @return the converted snake_case string
     */
    public static String camelToSnake(String str) {
        StringBuilder result = new StringBuilder();
        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else
                result.append(ch);
        }
        return result.toString();
    }
}
