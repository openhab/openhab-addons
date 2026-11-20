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
package org.openhab.binding.energycharts.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnergyChartsBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class EnergyChartsBindingConstants {
    public static final String BINDING_ID = "energycharts";

    public static final ThingTypeUID THING_TYPE_PRICES = new ThingTypeUID(BINDING_ID, "prices");

    public static final String CHANNEL_SPOT_PRICE = "spot-price";
}
