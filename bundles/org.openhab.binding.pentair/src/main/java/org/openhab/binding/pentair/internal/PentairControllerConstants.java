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
package org.openhab.binding.pentair.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants used for the Controller class
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairControllerConstants {

    //@formatter:off

    public static final Map<Integer, String> LIGHTMODES = MapUtils.mapOf(
            0, "OFF",
            1, "ON",
            128, "COLORSYNC",
            144, "COLORSWIM",
            160, "COLORSET",
            177, "PARTY",
            178, "ROMANCE",
            179, "CARIBBEAN",
            180, "AMERICAN",
            181, "SUNSET",
            182, "ROYAL",
            193, "BLUE",
            194, "GREEN",
            195, "RED",
            196, "WHITE",
            197, "MAGENTA");
    public static final Map<String, Integer> LIGHTMODES_INV = MapUtils.invertMap(LIGHTMODES);

    public static final Map<Integer, String> HEATMODE = MapUtils.mapOf(
            0, "NONE",
            1, "HEATER",
            2, "SOLARPREFERRED",
            3, "SOLAR"
            );
    public static final Map<String, Integer> HEATMODE_INV = MapUtils.invertMap(HEATMODE);

    //@formatter:on
}
