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
package org.openhab.binding.rachio.internal.api.json;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Smart Hose valve list response.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValveListResponse {
    public List<RachioValve> valves = new ArrayList<>();

    public static RachioValveListResponse fromJson(String json) {
        RachioValveListResponse response = new RachioValveListResponse();
        response.valves.addAll(
                RachioSmartHoseJsonParser.parseArray(json, RachioValve.class, "valves", "items", "data", "results"));
        return response;
    }
}
