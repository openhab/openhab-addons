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

/**
 * The {@link RouterosRouterboardInfo} is a model class for RouterOS system info used as bridge thing property values.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosRouterboardInfo {
    protected Map<String, String> propMap;

    public RouterosRouterboardInfo(Map<String, String> props) {
        this.propMap = props;
    }

    public String getFirmware() {
        return String.format("v%s (%s)", getFirmwareVersion(), getFirmwareType());
    }

    public boolean isRouterboard() {
        return propMap.get("routerboard").equals("true");
    }

    public String getModel() {
        return propMap.get("model");
    }

    public String getSerialNumber() {
        return propMap.get("serial-number");
    }

    public String getFirmwareType() {
        return propMap.get("firmware-type");
    }

    public String getFirmwareVersion() {
        return propMap.get("current-firmware");
    }
}
