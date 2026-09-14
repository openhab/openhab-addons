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
package org.openhab.io.yamlcomposer.internal.expression;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Safely parses and transforms Ruby-style range literals ([start..end] and [start...end])
 * into standard range function calls while ignoring occurrences inside string literals.
 * Supports literal values, variables, arithmetic expressions, and bracketed property/array indexing.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class RangeExpressionTransformer {

    private RangeExpressionTransformer() {
    }

    public static String transform(String expression) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int length = expression.length();

        while (i < length) {
            char c = expression.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                if (i == 0 || expression.charAt(i - 1) != '\\') {
                    inSingleQuote = !inSingleQuote;
                }
                result.append(c);
                i++;
            } else if (c == '"' && !inSingleQuote) {
                if (i == 0 || expression.charAt(i - 1) != '\\') {
                    inDoubleQuote = !inDoubleQuote;
                }
                result.append(c);
                i++;
            } else if (c == '[' && !inSingleQuote && !inDoubleQuote) {
                RangeMatch match = parseRangeAt(expression, i);
                if (match != null) {
                    String startExpr = match.startExpr.trim();
                    String endExpr = match.endExpr.trim();

                    String translatedEnd;
                    if (match.exclusive) {
                        translatedEnd = endExpr;
                    } else if (endExpr.matches("-?\\d+")) {
                        translatedEnd = String.valueOf(Integer.parseInt(endExpr) + 1);
                    } else {
                        translatedEnd = "(" + endExpr + ") + 1";
                    }

                    result.append("range(").append(startExpr).append(", ").append(translatedEnd).append(")");
                    i = match.endIndex;
                } else {
                    result.append(c);
                    i++;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    private static class RangeMatch {
        final String startExpr;
        final String endExpr;
        final boolean exclusive;
        final int endIndex;

        RangeMatch(String startExpr, String endExpr, boolean exclusive, int endIndex) {
            this.startExpr = startExpr;
            this.endExpr = endExpr;
            this.exclusive = exclusive;
            this.endIndex = endIndex;
        }
    }

    private static @Nullable RangeMatch parseRangeAt(String expr, int startIndex) {
        int depth = 0;
        boolean inSingle = false;
        boolean inDouble = false;

        int dotsStart = -1;
        int dotsEnd = -1;
        boolean exclusive = false;

        for (int i = startIndex; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (c == '\'' && !inDouble) {
                if (i == 0 || expr.charAt(i - 1) != '\\') {
                    inSingle = !inSingle;
                }
            } else if (c == '"' && !inSingle) {
                if (i == 0 || expr.charAt(i - 1) != '\\') {
                    inDouble = !inDouble;
                }
            } else if (!inSingle && !inDouble) {
                if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        if (dotsStart != -1) {
                            String startExpr = expr.substring(startIndex + 1, dotsStart);
                            String endExpr = expr.substring(dotsEnd, i);
                            if (!startExpr.isBlank() && !endExpr.isBlank()) {
                                return new RangeMatch(startExpr, endExpr, exclusive, i + 1);
                            }
                        }
                        return null;
                    }
                } else if (depth == 1 && dotsStart == -1 && c == '.') {
                    if (i + 2 < expr.length() && expr.charAt(i + 1) == '.' && expr.charAt(i + 2) == '.') {
                        dotsStart = i;
                        dotsEnd = i + 3;
                        exclusive = true;
                        i += 2;
                    } else if (i + 1 < expr.length() && expr.charAt(i + 1) == '.') {
                        dotsStart = i;
                        dotsEnd = i + 2;
                        exclusive = false;
                        i += 1;
                    }
                }
            }
        }
        return null;
    }
}
