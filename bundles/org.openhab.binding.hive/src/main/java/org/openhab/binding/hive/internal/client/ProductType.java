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
package org.openhab.binding.hive.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a Hive API product type.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public enum ProductType {
    ACTIONS,
    BOILER_MODULE,
    DAYLIGHT_SD,
    HEATING,
    HOT_WATER,
    HUB,
    THERMOSTAT_UI,
    TRV,
    TRV_GROUP,
    UNKNOWN,

    UNEXPECTED
}
