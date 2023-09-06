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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession.BoxModel;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link SystemManager} is the Java class used to handle api requests related to system
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SystemManager extends ConfigurableRest<SystemManager.Config, SystemManager.ConfigurationResponse> {

    protected static class ConfigurationResponse extends Response<Config> {
    }

    public static record Sensor(String id, String name, int value) {
        public enum SensorKind {
            FAN,
            TEMP,
            UNKNOWN;
        }

        public SensorKind getKind() {
            String[] elements = id.split("_");
            if (elements.length > 0) {
                String kind = elements[0].replaceAll("\\d", "").toUpperCase();
                try {
                    return SensorKind.valueOf(kind);
                } catch (IllegalArgumentException ignore) { // returning UNKNOWN
                }
            }
            return SensorKind.UNKNOWN;
        }
    }

    private static record Expansion(int slot, boolean probeDone, boolean present, boolean supported, String bundle,
            Type type) {
        private static enum Type {
            UNKNOWN, // unknown module
            DSL_LTE, // xDSL + LTE
            DSL_LTE_EXTERNAL_ANTENNAS, // xDSL + LTE with external antennas switch
            FTTH_P2P, // FTTH P2P
            FTTH_PON, // FTTH PON
            SECURITY; // Security module
        }
    }

    public static record ModelInfo(BoxModel name, String prettyName, boolean hasExpansions, boolean hasLanSfp,
            boolean hasDect, boolean hasHomeAutomation, boolean hasFemtocellExp, boolean hasFixedFemtocell,
            boolean hasVm) {
    }

    public static record Config(String firmwareVersion, MACAddress mac, String serial, String uptime, long uptimeVal,
            String boardName, boolean boxAuthenticated, DiskStatus diskStatus, String userMainStorage,
            List<Sensor> sensors, ModelInfo modelInfo, List<Sensor> fans, List<Expansion> expansions) {
        private static enum DiskStatus {
            NOT_DETECTED,
            DISABLED,
            INITIALIZING,
            ERROR,
            ACTIVE,
            UNKNOWN;
        }
    }

    public SystemManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, ConfigurationResponse.class,
                session.getUriBuilder().path(SYSTEM_PATH), null);
    }

    public void reboot() throws FreeboxException {
        post(REBOOT_ACTION);
    }
}
