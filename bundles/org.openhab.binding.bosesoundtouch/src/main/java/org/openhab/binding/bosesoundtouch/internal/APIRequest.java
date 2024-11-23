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
package org.openhab.binding.bosesoundtouch.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link APIRequest} class handles the API requests
 *
 * @author Thomas Traunbauer - Initial contribution
 */

@NonNullByDefault
public enum APIRequest {
    KEY("key"),
    SELECT("select"),
    SOURCES("sources"),
    BASSCAPABILITIES("bassCapabilities"),
    BASS("bass"),
    GET_ZONE("getZone"),
    SET_ZONE("setZone"),
    ADD_ZONE_SLAVE("addZoneSlave"),
    REMOVE_ZONE_SLAVE("removeZoneSlave"),
    NOW_PLAYING("now_playing"),
    TRACK_INFO("trackInfo"),
    VOLUME("volume"),
    PRESETS("presets"),
    INFO("info"),
    NAME("name"),
    GET_GROUP("getGroup");

    private String name;

    private APIRequest(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
