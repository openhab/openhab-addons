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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The top-level groups of the {@code get_livedata_info} response of the Ecowitt HTTP API, together with the way
 * their entries are keyed:
 * <ul>
 * <li>{@link Keying#CODE}: a JSON array of {@code {id, val, unit}} keyed by the same item codes as the TCP
 * protocol (e.g. {@code "0x02"}), or by HTTP-only string/decimal ids (e.g. {@code "srain_piezo"}, {@code "3"}).</li>
 * <li>{@link Keying#FIELD}: a single JSON object whose named fields each hold one value.</li>
 * <li>{@link Keying#CHANNEL}: a JSON array of per-channel objects, each with a {@code channel} and named value
 * fields.</li>
 * </ul>
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum HttpGroup {
    COMMON_LIST("common_list", Keying.CODE),
    RAIN("rain", Keying.CODE),
    PIEZO_RAIN("piezoRain", Keying.CODE),
    WH25("wh25", Keying.FIELD),
    LIGHTNING("lightning", Keying.FIELD),
    CO2("co2", Keying.FIELD),
    CH_AISLE("ch_aisle", Keying.CHANNEL),
    CH_TEMP("ch_temp", Keying.CHANNEL),
    CH_SOIL("ch_soil", Keying.CHANNEL),
    CH_LEAF("ch_leaf", Keying.CHANNEL),
    CH_PM25("ch_pm25", Keying.CHANNEL),
    CH_LEAK("ch_leak", Keying.CHANNEL),
    CH_LDS("ch_lds", Keying.CHANNEL);

    public enum Keying {
        CODE,
        FIELD,
        CHANNEL
    }

    private final String jsonKey;
    private final Keying keying;

    HttpGroup(String jsonKey, Keying keying) {
        this.jsonKey = jsonKey;
        this.keying = keying;
    }

    public String getJsonKey() {
        return jsonKey;
    }

    public Keying getKeying() {
        return keying;
    }
}
