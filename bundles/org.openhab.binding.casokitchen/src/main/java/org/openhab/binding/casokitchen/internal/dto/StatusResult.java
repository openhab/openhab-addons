/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.casokitchen.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.casokitchen.internal.CasoKitchenBindingConstants;

/**
 * The {@link CasoConfiguration2} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class StatusResult {
    public int temperature1 = -1;
    public int targetTemperature1 = -1;
    public boolean power1 = false;
    public boolean light1 = false;

    public int temperature2 = -1;
    public int targetTemperature2 = -1;
    public boolean power2 = false;
    public boolean light2 = false;

    public String logTimestampUtc = CasoKitchenBindingConstants.EMPTY;
    public String hint = CasoKitchenBindingConstants.EMPTY;
}
