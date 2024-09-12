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
import org.eclipse.jdt.annotation.Nullable;

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
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.CAP;
    }

    @Override
    public boolean hasDetailedReport() {
        return true;
    }

    @Override
    public boolean hasMonitor() {
        return false;
    }

    public boolean isMaster() {
        return "false".equals(getProp("slave", ""));
    }

    public boolean isDynamic() {
        return "true".equals(getProp("dynamic", ""));
    }

    public boolean isBound() {
        return "true".equals(getProp("bound", ""));
    }

    public boolean isActive() {
        return "false".equals(getProp("inactive", ""));
    }

    public @Nullable String getCurrentState() {
        return getProp("current-state");
    }

    public @Nullable String getRateSet() {
        return getProp("current-basic-rate-set");
    }

    public @Nullable Integer getRegisteredClients() {
        return getIntProp("current-registered-clients");
    }

    public @Nullable Integer getAuthorizedClients() {
        return getIntProp("current-authorized-clients");
    }
}
