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
package org.openhab.binding.mcd.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SensorThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
public class SensorThingConfiguration {
    private @Nullable String serialNumber;

    /**
     * return serial number as string
     * 
     * @return serial number as string
     */
    public @Nullable String getSerialNumber() {
        return serialNumber;
    }
}
