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
     * Converts a camelCase or PascalCase string to snake_case,
     * preserving acronyms like OpenHAB → open_hab, not open_h_a_b.
     *
     * @param str the camelCase string to convert
     * @return the converted snake_case string
     */
    public static String camelToSnake(String str) {
        StringBuilder result = new StringBuilder();
        char[] chars = str.toCharArray();
        result.append(Character.toLowerCase(chars[0]));
        for (int i = 1; i < chars.length; i++) {
            char current = chars[i];
            char prev = chars[i - 1];
            if (Character.isUpperCase(current)) {
                boolean nextIsLower = i + 1 < chars.length && Character.isLowerCase(chars[i + 1]);
                boolean prevIsLower = Character.isLowerCase(prev);
                if (prevIsLower || nextIsLower) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(current));
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }
}
