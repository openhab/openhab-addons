/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for getting the tp-Link Smart Plug energy state.
 * Only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class GetRealtime extends ContextState {

    public static class EMeter {
        private Realtime getRealtime = new Realtime();

        public Realtime getRealtime() {
            return getRealtime;
        }

        @Override
        public String toString() {
            return "get_realtime:{" + getRealtime + "}";
        }
    }

    @SerializedName(value = "emeter", alternate = "smartlife.iot.common.emeter")
    private EMeter emeter = new EMeter();

    public Realtime getRealtime() {
        return emeter.getRealtime();
    }

    @Override
    public String toString() {
        return "GetRealtime {emeter:{" + emeter + "}}";
    }
}
