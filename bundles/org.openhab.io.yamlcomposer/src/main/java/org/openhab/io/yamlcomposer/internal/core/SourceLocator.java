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
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;

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

        try (Scanner scanner = new Scanner(new ByteArrayInputStream(yamlBytes))) {
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
}
