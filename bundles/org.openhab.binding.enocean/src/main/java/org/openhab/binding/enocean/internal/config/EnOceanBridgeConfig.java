/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.config;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanBridgeConfig {

    public enum ESPVersion {
        UNKNOWN("unknown"),
        ESP3("ESP3"),
        ESP2("ESP2");

        private String version;

        ESPVersion(String version) {
            this.version = version;
        }

        public static ESPVersion getESPVersion(String espVersion) {
            for (ESPVersion version : values()) {
                if (version.version.equalsIgnoreCase(espVersion)) {
                    return version;
                }
            }

            return ESPVersion.UNKNOWN;
        }
    }

    public String path;

    public String espVersion;
    public boolean rs485;
    public String rs485BaseId;

    public Integer nextSenderId;

    public boolean enableSmack;
    public boolean sendTeachOuts;

    public EnOceanBridgeConfig() {
        espVersion = "ESP3";
        sendTeachOuts = false;
        enableSmack = true;
        nextSenderId = null;
    }

    public ESPVersion getESPVersion() {
        return ESPVersion.getESPVersion(espVersion);
    }
}
