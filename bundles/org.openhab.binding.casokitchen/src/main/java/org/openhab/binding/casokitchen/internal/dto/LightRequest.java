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

import static org.openhab.binding.casokitchen.internal.CasoKitchenBindingConstants.EMPTY;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LightRequest} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LightRequest {
    public String technicalDeviceId = EMPTY;
    public int zone = -1;
    public boolean lightOn = false;

    public boolean isValid() {
        return !technicalDeviceId.equals(EMPTY) && zone >= 0;
    }
}
