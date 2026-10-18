/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.yamlcomposer.internal.processors; // Or your specific processor package

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility functions for parsing and tokenizing expressions.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class ExpressionUtils {

    private static final Pattern WORD_DELIMITER_PATTERN = Pattern.compile("^[a-zA-Z_]\\w*$");

    private ExpressionUtils() {
        // Static utility class
    }

    /**
     * Finds the index of a top-level delimiter, ignoring matches inside quotes and brackets.
     *
     * @param line the input string to search
     * @param delimiter the delimiter to find
     * @return the index of the top-level delimiter, or null if not found
     */
    public static @Nullable Integer findTopLevelIndex(String line, String delimiter) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;

        int parenDepth = 0;
        int bracketDepth = 0;
        int braceDepth = 0;

        boolean isWordDelimiter = WORD_DELIMITER_PATTERN.matcher(delimiter).matches();
        int delimiterLength = delimiter.length();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\' && (inSingleQuote || inDoubleQuote)) {
                escaped = true;
                continue;
            }

            // Handle quote toggling
            if (!inDoubleQuote && c == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (!inSingleQuote && c == '"') {
                inDoubleQuote = !inDoubleQuote;
            }

            // Only check delimiters when outside strings and brackets
            if (!inSingleQuote && !inDoubleQuote) {
                switch (c) {
                    case '(':
                        parenDepth++;
                        break;
                    case ')':
                        if (parenDepth > 0)
                            parenDepth--;
                        break;
                    case '[':
                        bracketDepth++;
                        break;
                    case ']':
                        if (bracketDepth > 0)
                            bracketDepth--;
                        break;
                    case '{':
                        braceDepth++;
                        break;
                    case '}':
                        if (braceDepth > 0)
                            braceDepth--;
                        break;
                    default:
                        if (parenDepth == 0 && bracketDepth == 0 && braceDepth == 0) {
                            if (line.startsWith(delimiter, i)) {
                                if (isWordDelimiter) {
                                    // Ensure word boundaries (e.g., don't match 'if' inside 'gift')
                                    Character prevChar = i > 0 ? line.charAt(i - 1) : null;
                                    Character nextChar = (i + delimiterLength < line.length())
                                            ? line.charAt(i + delimiterLength)
                                            : null;

                                    boolean prevIsWord = prevChar != null && isWordChar(prevChar);
                                    boolean nextIsWord = nextChar != null && isWordChar(nextChar);

                                    if (!prevIsWord && !nextIsWord) {
                                        return i;
                                    }
                                } else {
                                    return i;
                                }
                            }
                        }
                        break;
                }
            }
        }

        return null;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
