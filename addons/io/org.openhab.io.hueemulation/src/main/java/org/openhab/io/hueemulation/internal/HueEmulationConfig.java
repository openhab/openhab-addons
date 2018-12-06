/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public boolean createNewUserOnEveryEndpoint = true;
    public static final String CONFIG_CREATE_NEW_USER_ON_THE_FLY = "createNewUserOnEveryEndpoint";
    /** Pairing timeout in seconds */
    public int pairingTimeout = 60;
    public @Nullable String discoveryIp;
    public int discoveryHttpPort = 0;
    /** Comma separated list of tags */
    public String restrictToTagsSwitches = "Switchable";
    /** Comma separated list of tags */
    public String restrictToTagsColorLights = "ColorLighting";
    /** Comma separated list of tags */
    public String restrictToTagsWhiteLights = "Lighting";

    public Set<String> switchTags() {
        return Stream.of(restrictToTagsSwitches.split(",")).collect(Collectors.toSet());
    }

    public Set<String> colorTags() {
        return Stream.of(restrictToTagsColorLights.split(",")).collect(Collectors.toSet());
    }

    public Set<String> whiteTags() {
        return Stream.of(restrictToTagsWhiteLights.split(",")).collect(Collectors.toSet());
    }
}
