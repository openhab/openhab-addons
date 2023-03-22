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
package org.openhab.binding.mikrotik.internal.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.util.Converter;

/**
 * The {@link RouterosL2TPSrvInterface} is a model class for `l2tp-in` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosL2TPSrvInterface extends RouterosInterfaceBase {
    public RouterosL2TPSrvInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.L2TP_SERVER;
    }

    @Override
    public String getApiType() {
        return "l2tp-server";
    }

    @Override
    public boolean hasDetailedReport() {
        return true;
    }

    @Override
    public boolean hasMonitor() {
        return false;
    }

    public @Nullable String getStatus() {
        return getProp("status");
    }

    public @Nullable String getEncoding() {
        return String.format("Encoding: %s", getProp("encoding", "None"));
    }

    public @Nullable String getClientAddress() {
        return getProp("client-address");
    }

    public @Nullable String getUptime() {
        return getProp("uptime");
    }

    public @Nullable LocalDateTime getUptimeStart() {
        return Converter.routerosPeriodBack(getUptime());
    }
}
