/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for holding the set of parameters used by the get_network_info response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

@NonNullByDefault
public class GetNetworkInfo {
    public int id;

    public @NonNullByDefault({}) Result[] result;

    public class Result {
        public String ssid = "";
        public String ip = "";
        public String mac = "";
        public String bssid = "";
        public int rssi;
   }

    public GetNetworkInfo() {
    }
}
