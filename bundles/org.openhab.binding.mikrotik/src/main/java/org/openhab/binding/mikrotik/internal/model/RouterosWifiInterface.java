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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosWifiInterface} is a model class for `wifi` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RouterosWifiInterface extends RouterosWlanInterface {

    public RouterosWifiInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.WIFI;
    }

    @Override
    public String getApiType() {
        return "wifi";
    }

    @Override
    public int getRegisteredClients() {
        Integer registeredClients = getIntProp("registered-peers");
        return registeredClients == null ? 0 : registeredClients;
    }

    @Override
    public int getAuthorizedClients() {
        Integer authedClients = getIntProp("authorized-peers");
        return authedClients == null ? 0 : authedClients;
    }
}
