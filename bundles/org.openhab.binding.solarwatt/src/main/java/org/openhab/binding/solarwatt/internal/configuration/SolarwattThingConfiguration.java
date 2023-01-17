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
package org.openhab.binding.solarwatt.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarwattThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattThingConfiguration {
    /**
     * Guid for the thing that is used by the energy manager
     */
    public String guid = "";
}
