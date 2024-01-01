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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@code ModuleStatus} class defines the dto for Smarther API module status object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class ModuleStatus {

    private List<Chronothermostat> chronothermostats;

    /**
     * Returns the chronothermostat details of this module status.
     *
     * @return the chronothermostat details
     */
    public List<Chronothermostat> getChronothermostats() {
        return chronothermostats;
    }

    /**
     * Returns the first chronothermostat item contained in this module status.
     *
     * @return the first chronothermostat item, or {@code null} in case of no item found
     */
    public @Nullable Chronothermostat toChronothermostat() {
        return (!chronothermostats.isEmpty() && chronothermostats.get(0) != null) ? chronothermostats.get(0) : null;
    }

    @Override
    public String toString() {
        return String.format("chronothermostats=[%s]", chronothermostats);
    }
}
