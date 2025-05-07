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
package org.openhab.binding.onecta.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OnectaIndoorUnitConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaIndoorUnitConstants {

    public static final String PROPERTY_IDU_MODELINFO = "modelInfo";
    public static final String PROPERTY_IDU_SOFTWAREVERSION = "softwareVersion";
    public static final String PROPERTY_IDU_EEPROMVERSION = "eepromVersion";
    public static final String CHANNEL_IDU_ISKEEPDRY = "basic#is-drykeep-setting";
    public static final String CHANNEL_IDU_FANSPEED = "basic#fanmotor-rotation-speed";
    public static final String CHANNEL_IDU_DELTAD = "basic#deltad-value";
    public static final String CHANNEL_IDU_HEATEXCHANGETEMP = "basic#heat-exchanger-temp";
    public static final String CHANNEL_IDU_SUCTIONTEMP = "basic#suction-temp";
}
