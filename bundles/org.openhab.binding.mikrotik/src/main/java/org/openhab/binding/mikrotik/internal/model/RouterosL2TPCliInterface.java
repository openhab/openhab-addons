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

    public String getStatus() {
        return propMap.get("status");
    }

    public String getEncoding() {
        return String.format("Encoding: %s", propMap.getOrDefault("encoding", "None"));
    }

    public String getServerAddress() {
        return propMap.get("connect-to");
    }

    public String getLocalAddress() {
        return propMap.get("local-address");
    }

    public String getRemoteAddress() {
        return propMap.get("remote-address");
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
