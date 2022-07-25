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
package org.openhab.binding.shelly.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link ShellyVersionDTO} compares 2 version strings.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyVersionDTO {
    private class VersionTokenizer {
        private final String versionString;
        private final int length;

        private int position;
        private int number;
        private String suffix = "";

        public VersionTokenizer(@Nullable String versionString) {
            if (versionString == null) {
                throw new IllegalArgumentException("versionString is null");
            }

            this.versionString = versionString;
            length = versionString.length();
        }

        private boolean moveNext() {
            number = 0;
            suffix = "";

            // No more characters
            if (position >= length) {
                return false;
            }

            while (position < length) {
                char c = versionString.charAt(position);
                if (c < '0' || c > '9') {
                    break;
                }
                number = number * 10 + (c - '0');
                position++;
            }

            int suffixStart = position;

            while (position < length) {
                char c = versionString.charAt(position);
                if (c == '.') {
                    break;
                }
                position++;
            }

            suffix = versionString.substring(suffixStart, position);

            if (position < length) {
                position++;
            }

            return true;
        }

        private int getNumber() {
            return number;
        }

        private String getSuffix() {
            return suffix;
        }
    }

    public boolean equals(String s1, String s2) {
        return compare(s1, s2) == 0;
    }

    public int compare(String version1, String version2) {
        VersionTokenizer tokenizer1 = new VersionTokenizer(version1);
        VersionTokenizer tokenizer2 = new VersionTokenizer(version2);

        int number1 = 0, number2 = 0;
        String suffix1 = "", suffix2 = "";

        while (tokenizer1.moveNext()) {
            if (!tokenizer2.moveNext()) {
                do {
                    number1 = tokenizer1.getNumber();
                    suffix1 = tokenizer1.getSuffix();
                    if (number1 != 0 || suffix1.length() != 0) {
                        // Version one is longer than number two, and non-zero
                        return 1;
                    }
                } while (tokenizer1.moveNext());

                // Version one is longer than version two, but zero
                return 0;
            }

            number1 = tokenizer1.getNumber();
            suffix1 = tokenizer1.getSuffix();
            number2 = tokenizer2.getNumber();
            suffix2 = tokenizer2.getSuffix();

            if (number1 < number2) {
                // Number one is less than number two
                return -1;
            }
            if (number1 > number2) {
                // Number one is greater than number two
                return 1;
            }

            boolean empty1 = suffix1.length() == 0;
            boolean empty2 = suffix2.length() == 0;

            if (empty1 && empty2) {
                continue;
            } // No suffixes
            if (empty1) {
                return 1;
            } // First suffix is empty (1.2 > 1.2b)
            if (empty2) {
                return -1;
            } // Second suffix is empty (1.2a < 1.2)

            // Lexical comparison of suffixes
            int result = suffix1.compareTo(suffix2);
            if (result != 0) {
                return result;
            }

        }

        while (tokenizer2.moveNext()) {
            number2 = tokenizer2.getNumber();
            suffix2 = tokenizer2.getSuffix();
            if (number2 != 0 || suffix2.length() != 0) {
                // Version one is longer than version two, and non-zero
                return -1;
            }
        }
        // Version two is longer than version one, but zero
        return 0;
    }

    public boolean checkBeta(@Nullable String version) {
        if (version == null) {
            return false;
        }
        return version.isEmpty() || version.contains("???") || version.toLowerCase().contains("master")
                || (version.toLowerCase().contains("-rc") || version.toLowerCase().contains("beta"));
    }
}
