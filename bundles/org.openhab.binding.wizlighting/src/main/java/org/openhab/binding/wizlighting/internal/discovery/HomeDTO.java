/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This is {@link HomeDTO} Object to parse Discovery response of Home Items.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class HomeDTO {

    static class Light {
        public int discoveredLightId;
        public int discoveredLightHomeId;
        public @Nullable String discoveredLightName;
        public @Nullable String discoveredLightMacAddress;
        public @Nullable String discoveredLightFwVersion;
        public @Nullable String discoveredLightIpAddress;
        public boolean discoveredLightConnectionStatus;
    }

    static class Data {
        public int id;
        public @Nullable String username;
        public Light @Nullable [] lights;
    }

    public boolean success;
    public @Nullable Data data;

    public Light @Nullable [] getLights() {
        Data data = this.data;
        if (data != null) {
            Light[] lights = data.lights;
            int numLights = data.lights.length;
            if (lights != null && numLights > 0) {
                return lights;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
