/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link RouterosPPPCliInterface} is a model class for `pppoe-out` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosPPPCliInterface extends RouterosInterfaceBase {
    public RouterosPPPCliInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.PPP_CLIENT;
    }

    @Override
    public String getApiType() {
        return "ppp-client";
    }

    @Override
    public boolean hasDetailedReport() {
        return false;
    }

    @Override
    public boolean hasMonitor() {
        return true;
    }

    @Override
    public @Nullable String getMacAddress() {
        return null;
    }

    public @Nullable String getLocalAddress() {
        return getProp("local-address");
    }

    public @Nullable String getRemoteAddress() {
        return getProp("remote-address");
    }

    public @Nullable String getStatus() {
        return getProp("status");
    }

    public @Nullable String getUptime() {
        return getProp("uptime");
    }

    public @Nullable LocalDateTime getUptimeStart() {
        return Converter.routerosPeriodBack(getUptime());
    }
}
