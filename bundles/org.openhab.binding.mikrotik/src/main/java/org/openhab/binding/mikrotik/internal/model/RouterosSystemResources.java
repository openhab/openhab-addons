/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class RouterosSystemResources {
    protected Map<String, String> propMap;

    public RouterosSystemResources(Map<String, String> props) {
        this.propMap = props;
    }

    public String getUptime() {
        return propMap.get("uptime");
    }

    public int getFreeSpace() {
        return Integer.parseInt(propMap.get("free-hdd-space"));
    }

    public int getTotalSpace() {
        return Integer.parseInt(propMap.get("total-hdd-space"));
    }

    public int getSpaceUse() {
        return 15;
    }

    public int getFreeMem() {
        return Integer.parseInt(propMap.get("free-memory"));
    }

    public int getTotalMem() {
        return Integer.parseInt(propMap.get("total-memory"));
    }

    public int getMemUse() {
        return 18;
    }

    public String getCpuLoad() {
        return propMap.get("cpu-load");
    }
}
