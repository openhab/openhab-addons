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
package org.openhab.binding.roborock.internal.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tracks outgoing request ids and correlates them with method names.
 *
 * @author reyhard - Initial contribution
 */
@NonNullByDefault
public class RequestCorrelationTracker {
    private final Map<String, Set<Integer>> requestIdsByMethod = new ConcurrentHashMap<>();
    private final Map<Integer, String> methodsByRequestId = new ConcurrentHashMap<>();
    private final Map<Integer, Long> requestTimestampsById = new ConcurrentHashMap<>();

    public void register(String methodName, int requestId) {
        if (requestId <= 0) {
            return;
        }

        methodsByRequestId.put(requestId, methodName);
        requestTimestampsById.put(requestId, System.currentTimeMillis());
        requestIdsByMethod.computeIfAbsent(methodName, ignored -> ConcurrentHashMap.newKeySet()).add(requestId);
    }

    public @Nullable String findMethodByRequestId(int requestId) {
        return methodsByRequestId.get(requestId);
    }

    public boolean isRequestIdInUse(int requestId) {
        return methodsByRequestId.containsKey(requestId);
    }

    public void removeByMethod(String methodName) {
        Set<Integer> requestIds = requestIdsByMethod.remove(methodName);
        if (requestIds != null) {
            for (Integer requestId : requestIds) {
                methodsByRequestId.remove(requestId.intValue(), methodName);
                requestTimestampsById.remove(requestId.intValue());
            }
        }
    }

    public void removeByRequestId(int requestId) {
        String methodName = methodsByRequestId.remove(requestId);
        requestTimestampsById.remove(requestId);
        if (methodName != null) {
            Set<Integer> requestIds = requestIdsByMethod.get(methodName);
            if (requestIds != null) {
                requestIds.remove(Integer.valueOf(requestId));
                if (requestIds.isEmpty()) {
                    requestIdsByMethod.remove(methodName, requestIds);
                }
            }
        }
    }

    public void cleanupExpired(long timeoutMs) {
        if (timeoutMs <= 0) {
            methodsByRequestId.keySet().forEach(this::removeByRequestId);
            return;
        }

        long now = System.currentTimeMillis();
        requestTimestampsById.forEach((requestId, timestamp) -> {
            if (now - timestamp.longValue() >= timeoutMs) {
                removeByRequestId(requestId.intValue());
            }
        });
    }
}
