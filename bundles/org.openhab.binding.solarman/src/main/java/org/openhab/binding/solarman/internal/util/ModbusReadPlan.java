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
package org.openhab.binding.solarman.internal.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.defmodel.Request;

/**
 * Merges overlapping windows by function and splits them at the Modbus 125-register limit.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public final class ModbusReadPlan {
    private ModbusReadPlan() {
    }

    public static List<Request> normalize(List<Request> requests) {
        Map<Integer, List<Request>> functions = new TreeMap<>();
        for (Request request : requests) {
            if (request.getStart() < 0 || request.getEnd() > 65535 || request.getEnd() < request.getStart()) {
                throw new IllegalArgumentException("Invalid Modbus read register range");
            }
            Objects.requireNonNull(functions.computeIfAbsent(request.getMbFunctioncode(), key -> new ArrayList<>()))
                    .add(request);
        }
        List<Request> result = new ArrayList<>();
        functions.forEach((function, windows) -> {
            windows.sort(Comparator.comparingInt(Request::getStart));
            int start = -1;
            int end = -1;
            for (Request window : windows) {
                if (start >= 0 && window.getStart() > end + 1) {
                    append(result, function, start, end);
                    start = -1;
                }
                if (start < 0) {
                    start = window.getStart();
                    end = window.getEnd();
                } else {
                    end = Math.max(end, window.getEnd());
                }
            }
            if (start >= 0) {
                append(result, function, start, end);
            }
        });
        return result;
    }

    private static void append(List<Request> result, int function, int first, int last) {
        for (int start = first; start <= last; start += 125) {
            result.add(new Request(function, start, Math.min(last, start + 124)));
        }
    }
}
