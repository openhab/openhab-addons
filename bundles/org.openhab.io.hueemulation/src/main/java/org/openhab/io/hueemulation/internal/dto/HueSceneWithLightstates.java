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
package org.openhab.io.hueemulation.internal.dto;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Hue API scene object with light states
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueSceneWithLightstates extends HueSceneEntry {

    public Map<String, AbstractHueState> lightstates = new TreeMap<>();

    HueSceneWithLightstates() {
    }

    public HueSceneWithLightstates(HueSceneEntry e) {
        this.type = e.type;
        this.name = e.name;
        this.description = e.description;
        this.owner = e.owner;
        this.recycle = e.recycle;
        this.locked = e.locked;
        this.appdata = e.appdata;
        this.picture = e.picture;
        this.lights = e.lights;
        this.group = e.group;
    }
}
