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
import org.joda.time.DateTime;

@NonNullByDefault
public class RouterosPPPoECliInterface extends RouterosInterfaceBase {
    public RouterosPPPoECliInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    protected RouterosInterfaceType[] getDesignedTypes() {
        return new RouterosInterfaceType[] { RouterosInterfaceType.PPPOE_CLIENT };
    }

    public String getUptime() {
        return propMap.get("uptime");
    } // TODO monitor once

    public DateTime calculateUptimeStart() {
        return DateTime.now().minusHours(2); // TODO
    }
}
