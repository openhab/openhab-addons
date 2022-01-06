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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosRouterboardInfo} is a model class for RouterOS system info used as bridge thing property values.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosRouterboardInfo extends RouterosBaseData {

    public RouterosRouterboardInfo(Map<String, String> props) {
        super(props);
    }

    public String getFirmware() {
        return String.format("v%s (%s)", getFirmwareVersion(), getFirmwareType());
    }

    public boolean isRouterboard() {
        return getProp("routerboard", "").equals("true");
    }

    public String getModel() {
        return getProp("model", "Unknown");
    }

    public String getSerialNumber() {
        return getProp("serial-number", "XXX");
    }

    public String getFirmwareType() {
        return getProp("firmware-type", "N/A");
    }

    public String getFirmwareVersion() {
        return getProp("current-firmware", "Unknown");
    }
}
