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
 * The {@link RouterosCapInterface} is a model class for `cap` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosCapInterface extends RouterosInterfaceBase {
    public RouterosCapInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    protected RouterosInterfaceType[] getDesignedTypes() {
        return new RouterosInterfaceType[] { RouterosInterfaceType.CAP };
    }

    public boolean isMaster() {
        return propMap.get("slave").equals("false");
    }

    public boolean isDynamic() {
        return propMap.get("dynamic").equals("true");
    }

    public boolean isBound() {
        return propMap.get("bound").equals("true");
    }

    public boolean isActive() {
        return propMap.get("inactive").equals("false");
    }

    public String getCurrentState() {
        return propMap.get("current-state");
    }

    public int getRegisteredClients() {
        return Integer.parseInt(propMap.get("current-registered-clients"));
    }

    public int getAuthorizedClients() {
        return Integer.parseInt(propMap.get("current-authorized-clients"));
    }
}
