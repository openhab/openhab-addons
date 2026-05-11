/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * The {@link RouterosVethInterface} is a model class for `veth` interface models having casting accessors for
 * data that is specific to this network interface kind. Is a subclass of {@link RouterosInterfaceBase}.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RouterosVethInterface extends RouterosInterfaceBase {
    public RouterosVethInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    public RouterosInterfaceType getDesignedType() {
        return RouterosInterfaceType.VETH;
    }

    @Override
    public String getApiType() {
        return "veth";
    }

    @Override
    public boolean hasDetailedReport() {
        return true;
    }

    @Override
    public @Nullable String getMacAddress() {
        return null;
    }

    @Override
    public boolean hasMonitor() {
        String name = getDefaultName();
        return name != null;
    }

    public @Nullable String getDefaultName() {
        return getProp("default-name");
    }

    public @Nullable String getRate() {
        return getProp("rate");
    }

    public @Nullable String getState() {
        return getProp("status");
    }
}
