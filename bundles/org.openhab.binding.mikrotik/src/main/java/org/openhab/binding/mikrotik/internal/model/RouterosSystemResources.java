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
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openhab.binding.mikrotik.internal.util.Converter;

/**
 * The {@link RouterosSystemResources} is a model class for RouterOS system info having casting accessors for
 * data that is available through bridge thing channels.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosSystemResources {
    protected Map<String, String> propMap;

    public RouterosSystemResources(Map<String, String> props) {
        this.propMap = props;
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

    public @Nullable Integer getCpuLoad() {
        if (propMap.containsKey("cpu-load")) {
            String loadPercent = propMap.get("cpu-load").replace("%", "");
            return Integer.parseInt(loadPercent);
        }
        return null;
    }

    public String getUptime() {
        return propMap.get("uptime");
    }

    public @Nullable DateTime getUptimeStart() {
        if (propMap.containsKey("uptime")) {
            Period uptime = Converter.fromRouterosPeriod(getUptime());
            return DateTime.now().minus(uptime);
        }
        return null;
    }
}
