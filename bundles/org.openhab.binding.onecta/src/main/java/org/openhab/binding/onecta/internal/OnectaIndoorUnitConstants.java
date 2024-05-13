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

    public static final String PROPERTY_IDU_NAME = "name";
    public static final String CHANNEL_IDU_MODELINFO = "basic#modelinfo";
    public static final String CHANNEL_IDU_SOFTWAREVERSION = "basic#softwareversion";
    public static final String CHANNEL_IDU_EEPROMVERSION = "basic#eepromversion";
    public static final String CHANNEL_IDU_ISKEEPDRY = "basic#isdrykeepsetting";
    public static final String CHANNEL_IDU_FANSPEED = "basic#fanmotorratationspeed";
    public static final String CHANNEL_IDU_DELTAD = "basic#deltadvalue";
    public static final String CHANNEL_IDU_HEATEXCHANGETEMP = "basic#heatexchangertemp";
    public static final String CHANNEL_IDU_SUCTIONTEMP = "basic#suctiontemp";
}
