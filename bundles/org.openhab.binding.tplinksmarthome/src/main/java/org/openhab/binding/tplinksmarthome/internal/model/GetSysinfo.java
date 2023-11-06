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
package org.openhab.binding.tplinksmarthome.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data class for getting the tp-Link Smart Plug state.
 * Only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class GetSysinfo {

    public static class System {
        private Sysinfo getSysinfo = new Sysinfo();

        public Sysinfo getGetSysinfo() {
            return getSysinfo;
        }

        @Override
        public String toString() {
            return "get_sysinfo:{" + getSysinfo + "}";
        }
    }

    private System system = new System();

    public Sysinfo getSysinfo() {
        return system.getGetSysinfo();
    }

    @Override
    public String toString() {
        return "GetSysinfo {system:{" + system + "}}";
    }
}
