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
package org.openhab.io.hueemulation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The configuration for {@link HueEmulationService}.
 *
 * @author David Graeff - Initial Contribution
 */
@NonNullByDefault
public class HueEmulationConfig {
    public boolean pairingEnabled = false;
    public static final String CONFIG_PAIRING_ENABLED = "pairingEnabled";

    /**
     * The Amazon echos have no means to recreate a new api key and they don't care about the 403-forbidden http status
     * code. If the addon has pruned its api-key list, echos will not be able to discover new devices. Set this option
     * to just create a new user on the fly.
     */
    public boolean createNewUserOnEveryEndpoint = false;
    public static final String CONFIG_CREATE_NEW_USER_ON_THE_FLY = "createNewUserOnEveryEndpoint";
    public boolean temporarilyEmulateV1bridge = false;
    public static final String CONFIG_EMULATE_V1 = "temporarilyEmulateV1bridge";
    public boolean permanentV1bridge = false;

    /** Pairing timeout in seconds */
    public int pairingTimeout = 60;
    /**
     * The field discoveryIps was named discoveryIp in the frontend for some time and thus user probably
     * have it in their local config saved under the non plural version.
     */
    public @Nullable String discoveryIp;
    public int discoveryHttpPort = 0;
    /** Comma separated list of tags */
    public String restrictToTagsSwitches = "Switchable";
    /** Comma separated list of tags */
    public String restrictToTagsColorLights = "ColorLighting";
    /** Comma separated list of tags */
    public String restrictToTagsWhiteLights = "Lighting";
    /** Comma separated list of tags */
    public String ignoreItemsWithTags = "internal";

    public static final String CONFIG_UUID = "uuid";
    public String uuid = "";
    public String devicename = "openHAB";
}
