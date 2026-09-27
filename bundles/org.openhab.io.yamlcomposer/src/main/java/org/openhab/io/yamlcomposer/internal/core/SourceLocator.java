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
package org.openhab.io.yamlcomposer.internal.core;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper to find positions in YAML source for logging.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class SourceLocator {
    private final byte[] yamlBytes;

    public SourceLocator(byte[] yamlBytes) {
        this.yamlBytes = yamlBytes;
    }

    public record FilePosition(int line, int column) {
        @Override
        public String toString() {
            if (line < 0) {
                return "";
            }
            return line + ":" + column;
        }

        public static FilePosition empty() {
            return new FilePosition(-1, -1);
        }

        public boolean isEmpty() {
            return FilePosition.empty().equals(this);
        }
    }

    /**
     * Find the position of a given key in the YAML file.
     *
     * @param keys the sequence of keys representing the path in the YAML structure
     * @return the Position of the last key in the sequence, or (-1, -1) if not
     *         found
     */
    public FilePosition findPosition(String... keys) {
        if (keys.length == 0) {
            return FilePosition.empty();
        }

        try (Scanner scanner = new Scanner(new ByteArrayInputStream(yamlBytes), StandardCharsets.UTF_8)) {
            int lineNumber = 1;
            int keyIndex = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int lineOffset = 0; // Tracks our horizontal position in the current line
                String searchArea = line;
                boolean foundInLine;

                do {
                    foundInLine = false;
                    String targetKey = keys[keyIndex] + ":";
                    int matchIndex = searchArea.indexOf(targetKey);

                    if (matchIndex != -1) {
                        // Calculate the column:
                        // segments already skipped + position in current segment + length of key
                        int columnAtEndOfKey = lineOffset + matchIndex + targetKey.length();

                        if (keyIndex == keys.length - 1) {
                            return new FilePosition(lineNumber, columnAtEndOfKey + 1); // +1 for 1-based indexing
                        }

                        // Prepare for next key on the same line
                        lineOffset += matchIndex + targetKey.length();
                        searchArea = searchArea.substring(matchIndex + targetKey.length());
                        keyIndex++;
                        foundInLine = true;
                    }
                } while (foundInLine && keyIndex < keys.length);

                lineNumber++;
            }
        }
        return FilePosition.empty();
    }

    /**
     * Finds the position of a key-value pair matching a hierarchical sequence of keys.
     * <p>
     * <b>Wildcard & Key Matching Rules:</b>
     * <ul>
     * <li>A key segment consisting solely of {@code "*"} acts as a full-segment wildcard,
     * matching any single key name at that specific nesting/indentation level
     * (e.g., matching any package ID under {@code "dynamic_packages"}).</li>
     * <li>Partial wildcard globs (such as {@code "pkg_*"} or {@code "*_source"}) are <b>not</b> expanded;
     * they are evaluated as exact, literal key strings.</li>
     * </ul>
     *
     * @param expectedValue expected value for the leaf key (coerced to String comparison)
     * @param keys sequence of keys leading to the target key (the final element is the target leaf key)
     * @return the {@link FilePosition} pointing to the 1-based start column of the scalar value,
     *         or {@link FilePosition#empty()} if not found
     */
    public FilePosition findKeyValuePosition(@Nullable Object expectedValue, String... keys) {
        if (keys.length == 0 || expectedValue == null) {
            return FilePosition.empty();
        }

        String expectedStr = String.valueOf(expectedValue);

        try (Scanner scanner = new Scanner(new ByteArrayInputStream(yamlBytes), StandardCharsets.UTF_8)) {
            int lineNumber = 1;

            int[] matchedIndents = new int[keys.length];
            int currentLevel = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int indent = getIndentation(line);
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    lineNumber++;
                    continue;
                }

                // Pop out of deeper nesting levels when indentation drops back
                while (currentLevel > 0 && indent <= matchedIndents[currentLevel - 1]) {
                    currentLevel--;
                }

                String targetKey = keys[currentLevel];
                boolean isLeaf = (currentLevel == keys.length - 1);

                if (isLeaf) {
                    String leafKeyHeader = targetKey + ":";
                    int matchIdx = line.indexOf(leafKeyHeader);
                    if (matchIdx != -1) {
                        String rawAfterColon = line.substring(matchIdx + leafKeyHeader.length());
                        if (matchesYamlValue(rawAfterColon, expectedStr)) {
                            int leadingSpaceCount = 0;
                            while (leadingSpaceCount < rawAfterColon.length()
                                    && Character.isWhitespace(rawAfterColon.charAt(leadingSpaceCount))) {
                                leadingSpaceCount++;
                            }
                            int valueStartColumn = matchIdx + leafKeyHeader.length() + leadingSpaceCount + 1;
                            return new FilePosition(lineNumber, valueStartColumn);
                        }
                    }
                } else {
                    boolean matchesSegment = "*".equals(targetKey) || isKeyHeader(trimmed, targetKey);
                    if (matchesSegment) {
                        matchedIndents[currentLevel] = indent;
                        currentLevel++;
                    }
                }

                lineNumber++;
            }
        }
        return FilePosition.empty();
    }

    private int getIndentation(String line) {
        int count = 0;
        while (count < line.length() && Character.isWhitespace(line.charAt(count))) {
            count++;
        }
        return count;
    }

    private boolean isKeyHeader(String trimmedLine, String key) {
        if (trimmedLine.startsWith(key + ":")) {
            return true;
        }
        return trimmedLine.startsWith("\"" + key + "\":") || trimmedLine.startsWith("'" + key + "':");
    }

    private boolean matchesYamlValue(String rawAfterColon, String expectedValue) {
        String trimmed = rawAfterColon.trim();

        int commentIdx = -1;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == '#' && !inSingleQuote && !inDoubleQuote) {
                commentIdx = i;
                break;
            }
        }

        if (commentIdx != -1) {
            trimmed = trimmed.substring(0, commentIdx).trim();
        }

        if (trimmed.length() >= 2) {
            if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                    || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
        }

        return trimmed.equals(expectedValue);
    }
}
