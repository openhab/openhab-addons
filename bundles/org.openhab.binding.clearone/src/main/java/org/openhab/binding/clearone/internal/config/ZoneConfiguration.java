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
package org.openhab.binding.clearone.internal.config;

import java.util.List;

/**
 * Configuration class for the ClearOne Zone Thing.
 *
 * @author Garry Mitchell - Initial contribution
 */

public class ZoneConfiguration {

    // Zone Thing constants
    public static final String ZONE_NUMBER = "zone";
    public static final String SELECTABLE_INPUTS = "selectableInputs";
    public static final String CHANNELS = "channels";

    /**
     * The Zone Number. Can be in the range of 1-12. This is a required parameter for a zone.
     */
    public Integer zone;

    /**
     * Selectable Inputs. List of allowable inputs (Input, Expansion and Processing channel types)
     */
    public List<String> selectableInputs;

    /**
     * Channels. Number of inputs/outputs controlled by this Thing. Any changes will be made to X and X+1
     */
    public Integer channels;
}
