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
package org.openhab.binding.mikrotik.internal.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.util.Converter;

/**
 * The {@link RouterosSystemResources} is a model class for RouterOS system info having casting accessors for
 * data that is available through bridge thing channels.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosSystemResources extends RouterosBaseData {

    public RouterosSystemResources(Map<String, String> props) {
        super(props);
    }

    public @Nullable Integer getFreeSpace() {
        return getIntProp("free-hdd-space");
    }

    public @Nullable Integer getTotalSpace() {
        return getIntProp("total-hdd-space");
    }

    public @Nullable Integer getSpaceUse() {
        Integer freeSpace = getFreeSpace(), totalSpace = getTotalSpace();
        if (freeSpace == null || totalSpace == null) {
            return null;
        }
        return 100 - Math.round(100F * freeSpace / totalSpace);
    }

    public @Nullable Integer getFreeMem() {
        return getIntProp("free-memory");
    }

    public @Nullable Integer getTotalMem() {
        return getIntProp("total-memory");
    }

    public @Nullable Integer getMemUse() {
        Integer freeMem = getFreeMem(), totalMem = getTotalMem();
        if (freeMem == null || totalMem == null) {
            return null;
        }
        return 100 - Math.round(100F * freeMem / totalMem);
    }

    public @Nullable Integer getCpuLoad() {
        return getIntProp("cpu-load");
    }

    public @Nullable String getUptime() {
        return getProp("uptime");
    }

    public @Nullable LocalDateTime getUptimeStart() {
        return Converter.routerosPeriodBack(getUptime());
    }
}
