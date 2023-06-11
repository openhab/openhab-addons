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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
        return !getProp("master-interface", "").isBlank();
    }

    public @Nullable String getCurrentState() {
        return getProp("status");
    }

    public @Nullable String getSSID() {
        return getProp("ssid");
    }

    public @Nullable String getMode() {
        return getProp("mode");
    }

    public @Nullable String getRate() {
        return getProp("band");
    }

    public @Nullable String getInterfaceType() {
        return getProp("interface-type");
    }

    public int getRegisteredClients() {
        Integer registeredClients = getIntProp("registered-clients");
        return registeredClients == null ? 0 : registeredClients;
    }

    public int getAuthorizedClients() {
        Integer authedClients = getIntProp("authenticated-clients");
        return authedClients == null ? 0 : authedClients;
    }
}
