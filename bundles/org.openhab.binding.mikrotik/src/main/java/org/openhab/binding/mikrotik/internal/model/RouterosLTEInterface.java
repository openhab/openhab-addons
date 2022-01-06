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
 * The {@link RouterosLTEInterface} is a model class for `lte` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosLTEInterface extends RouterosInterfaceBase {
    public RouterosLTEInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.LTE;
    }

    @Override
    public String getApiType() {
        return "lte";
    }

    @Override
    public boolean hasDetailedReport() {
        return false;
    }

    @Override
    public boolean hasMonitor() {
        return false;
    }

    public @Nullable String getStatus() {
        // I only have an RNDIS/HiLink 4G modem which doesn't report status at all. This should be tested/fixed
        // by someone who has PCIe/serial 4G modem.
        return getProp("status");
    }

    public @Nullable String getUptime() {
        // Same as above. Also a custom info command need to be implemented for this to work.
        // https://forum.mikrotik.com/viewtopic.php?t=164035#p808281
        return getProp("session-uptime");
    }

    public @Nullable LocalDateTime getUptimeStart() {
        return Converter.routerosPeriodBack(getUptime());
    }
}
