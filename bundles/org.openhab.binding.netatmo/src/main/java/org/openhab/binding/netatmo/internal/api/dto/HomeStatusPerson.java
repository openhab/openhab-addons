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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

/**
 * The {@link HomeStatusPerson} provides Person informations returned by getHomeData endpoint
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeStatusPerson extends NAThing {
    private boolean outOfSight;

    @Override
    public ModuleType getType() {
        return ModuleType.PERSON;
    }

    public boolean atHome() {
        return !outOfSight;
    }
}
