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
 * The {@link RouterosL2TPCliInterface} is a model class for `l2tp-out` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosL2TPCliInterface extends RouterosInterfaceBase {
    public RouterosL2TPCliInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.L2TP_SERVER;
    }

    @Override
    public String getApiType() {
        return "l2tp-client";
    }

    @Override
    public boolean hasDetailedReport() {
        return false;
    }

    @Override
    public boolean hasMonitor() {
        return true;
    }

    public @Nullable String getStatus() {
        return getProp("status");
    }

    public @Nullable String getEncoding() {
        return String.format("Encoding: %s", getProp("encoding", "None"));
    }

    public @Nullable String getServerAddress() {
        return getProp("connect-to");
    }

    public @Nullable String getLocalAddress() {
        return getProp("local-address");
    }

    public @Nullable String getRemoteAddress() {
        return getProp("remote-address");
    }

    public @Nullable String getUptime() {
        return getProp("uptime");
    }

    public @Nullable LocalDateTime getUptimeStart() {
        return Converter.routerosPeriodBack(getUptime());
    }
}
