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
package org.openhab.binding.smarther.internal.api.model;

import java.util.List;

/**
 * Smarther API ModuleStatus DTO class.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class ModuleStatus {

    private List<Chronothermostat> chronothermostats;

    public List<Chronothermostat> getChronothermostats() {
        return chronothermostats;
    }

    public boolean hasChronothermostat() {
        return (!chronothermostats.isEmpty() && (chronothermostats.get(0) != null));
    }

    public Chronothermostat toChronothermostat() {
        return (hasChronothermostat()) ? chronothermostats.get(0) : null;
    }

    @Override
    public String toString() {
        return String.format("chronothermostats=[%s]", chronothermostats);
    }

}
