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
package org.openhab.binding.electroluxappliance.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ElectroluxApplianceConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxApplianceConfiguration {
    public static final String SERIAL_NUMBER_LABEL = "serialNumber";

    private String serialNumber = "";

    public String getSerialNumber() {
        return serialNumber;
    }
}
