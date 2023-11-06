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
package org.openhab.io.hueemulation.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Hue API scene object
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueSceneEntry {
    public enum TypeEnum {
        LightScene, // 1.28
        GroupScene, // 1.28
    }

    public TypeEnum type = TypeEnum.LightScene;

    // A unique, editable name given to the group.
    public String name;
    public String description = "";

    public String owner = "";
    public boolean recycle = false;
    public boolean locked = false;

    final int version = 2;

    public String appdata = "";
    public String picture = "";

    // The IDs of the lights that are in the group.
    public @Nullable List<String> lights;
    public @Nullable String group;

    HueSceneEntry() {
        name = "";
    }

    public HueSceneEntry(@Nullable String name) {
        this.name = name != null ? name : "";
    }
}
