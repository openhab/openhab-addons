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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.core.util.StringUtils;

/**
 * Class to collect debug details
 *
 * @author Andreas Berger - Initial contribution
 */
public class DebugDetails {
    final byte[] data;

    private final Map<Integer, DebugSegment> segments = new TreeMap<>();

    public DebugDetails(byte[] data, Command command, Protocol protocol) {
        this.data = data;
        addDebugDetails(0, 2, "header");
        addDebugDetails(2, 1, "command: " + command.name());
        addDebugDetails(3, command.getSizeBytes(), "size");
        if (protocol == Protocol.ELV) {
            addDebugDetails(data.length - 2, 1, "ELV checksum");
        }
        addDebugDetails(data.length - 1, 1, "checksum");
    }

    public void addDebugDetails(int start, int length, String description) {
        segments.put(start, new DebugSegment(start, length, description));
    }

    @Override
    public String toString() {
        int padding = segments.values().stream().mapToInt(value -> value.length).max().orElse(0) * 2;
        return "0x" + Utils.toHexString(data, data.length, "") + "\n" + segments.values().stream()
                .map(debugSegment -> debugSegment.toDebugString(padding)).collect(Collectors.joining("\n"));
    }

    private class DebugSegment {
        final int start;
        final int length;
        final String description;

        DebugSegment(int start, int length, String description) {
            this.start = start;
            this.length = length;
            this.description = description;
        }

        @Override
        public String toString() {
            return toDebugString(0);
        }

        private String toDebugString(int padding) {
            String result = "0x";
            String hexString = Utils.toHexString(Arrays.copyOfRange(data, start, start + length), length, "");
            result += StringUtils.padRight(hexString, padding, " ");
            result += ": " + description;
            return result;
        }
    }
}
