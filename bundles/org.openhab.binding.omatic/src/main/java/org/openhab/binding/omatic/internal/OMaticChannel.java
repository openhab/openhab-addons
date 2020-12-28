/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.omatic.internal;

import java.util.stream.Stream;

/**
 * The {@link OMaticChannel} class defines channel enum.
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
public enum OMaticChannel {
    POWER,
    ENERGY1,
    ENERGY2,
    COST1,
    COST2,
    MAX_POWER,
    TOTAL_ENERGY1,
    TOTAL_ENERGY2,
    TOTAL_COST1,
    TOTAL_COST2,
    TIME,
    TOTAL_TIME,
    TOTAL_TIME_STR,
    COMPLETED,
    STARTED,
    TIME_STR,
    STATE,
    RUNNING,
    RESET,
    DISABLE;

    private static final String DASH = "-";
    private static final String UNDERSCORE = "_";

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    @SuppressWarnings("null")
    public static Stream<OMaticChannel> stream() {
        return Stream.of(OMaticChannel.values());
    }

    public static OMaticChannel fromString(String str) {
        return OMaticChannel.stream()
                .filter(channelList -> str.replaceAll(DASH, UNDERSCORE).equalsIgnoreCase(channelList.name()))
                .findFirst().get();
    }
}
