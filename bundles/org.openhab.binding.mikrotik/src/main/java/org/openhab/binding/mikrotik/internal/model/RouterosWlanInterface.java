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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosWlanInterface} is a model class for `waln` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosWlanInterface extends RouterosInterfaceBase {
    public RouterosWlanInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.WLAN;
    }

    @Override
    public String getApiType() {
        return "wireless";
    }

    @Override
    public boolean hasDetailedReport() {
        return true;
    }

    @Override
    public boolean hasMonitor() {
        return true;
    }

    public boolean isMaster() {
        return StringUtils.isBlank(propMap.get("master-interface"));
    }

    // data={rx-error=0, fp-tx-packet=0, tx-queue-drop=0, .id=*1, max-l2mtu=2290, type=wlan, rx-packet=68030,
    // fp-rx-packet=68030, last-link-up-time=jan/27/2021 20:01:00, fp-rx-byte=26551550, running=true, slave=true,
    // last-link-down-time=jan/18/2021 16:36:58, tx-drop=0, disabled=false, rx-byte=26551550, tx-packet=180956,
    // mac-address=B8:69:F4:A5:87:38, l2mtu=1600, rx-drop=0, fp-tx-byte=0, default-name=wlan1, mtu=1500,
    // tx-byte=67312230, actual-mtu=1500, link-downs=31, name=wlan1, tx-error=0}

    public String getCurrentState() {
        return propMap.get("status");
    }

    public String getSSID() {
        return propMap.get("ssid");
    }

    public String getMode() {
        return propMap.get("mode");
    }

    public String getRate() {
        return propMap.get("band");
    }

    public String getInterfaceType() {
        return propMap.get("interface-type");
    }

    public int getRegisteredClients() {
        return Integer.parseInt(propMap.get("registered-clients"));
    }

    public int getAuthorizedClients() {
        return Integer.parseInt(propMap.get("authenticated-clients"));
    }
}
