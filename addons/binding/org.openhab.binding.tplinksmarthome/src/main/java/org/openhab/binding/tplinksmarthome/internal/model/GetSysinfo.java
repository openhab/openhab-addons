/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
