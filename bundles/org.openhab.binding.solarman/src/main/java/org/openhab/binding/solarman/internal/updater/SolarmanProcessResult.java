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
package org.openhab.binding.solarman.internal.updater;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.defmodel.Request;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanProcessResult {
    private final Map<Request, Map<Integer, byte[]>> successfulRequestMap;
    private final Map<Request, SolarmanException> exceptionRequestMap;

    public SolarmanProcessResult() {
        this(Collections.emptyMap(), Collections.emptyMap());
    }

    private SolarmanProcessResult(Map<Request, Map<Integer, byte[]>> successfulRequestMap,
            Map<Request, SolarmanException> exceptionRequestMap) {
        this.successfulRequestMap = successfulRequestMap;
        this.exceptionRequestMap = exceptionRequestMap;
    }

    public static SolarmanProcessResult merge(SolarmanProcessResult result1, SolarmanProcessResult result2) {
        return new SolarmanProcessResult(mergeMaps(result1.successfulRequestMap, result2.successfulRequestMap),
                mergeMaps(result1.exceptionRequestMap, result2.exceptionRequestMap));
    }

    public static SolarmanProcessResult ofValue(Request request, Map<Integer, byte[]> readRegisters) {
        return new SolarmanProcessResult(Collections.singletonMap(request, readRegisters), new HashMap<>());
    }

    public static SolarmanProcessResult ofException(Request request, SolarmanException solarmanException) {
        return new SolarmanProcessResult(new HashMap<>(), Collections.singletonMap(request, solarmanException));
    }

    public boolean hasSuccessfulResponses() {
        return !successfulRequestMap.isEmpty();
    }

    public Map<Integer, byte[]> getReadRegistersMap() {
        return successfulRequestMap.values().stream().reduce(new HashMap<>(), SolarmanProcessResult::mergeMaps);
    }

    private static <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
    }

    @Override
    public String toString() {
        if (!successfulRequestMap.isEmpty() && exceptionRequestMap.isEmpty()) {
            return String.format("Successfully executed %d requests", successfulRequestMap.size());
        } else if (successfulRequestMap.isEmpty() && !exceptionRequestMap.isEmpty()) {
            return String.format("Error fetching data from logger, here are the errors:\n%s",
                    buildErrorReport(exceptionRequestMap));
        } else if (!successfulRequestMap.isEmpty()) {
            return String.format("Successfully executed %d requests, but %d requests failed with:\n%s",
                    successfulRequestMap.size(), exceptionRequestMap.size(), buildErrorReport(exceptionRequestMap));
        } else {
            return "Empty SolarmanProcessResult";
        }
    }

    private String buildErrorReport(Map<Request, SolarmanException> exceptionRequestMap) {
        return exceptionRequestMap.entrySet().stream().map(entry -> String.format("\tRequest %s returned error: %s\n",
                entry.getKey().toString(), entry.getValue().getMessage())).reduce("", String::concat);
    }
}
