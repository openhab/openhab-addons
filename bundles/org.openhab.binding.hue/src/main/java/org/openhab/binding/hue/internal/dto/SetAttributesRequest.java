/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
@NonNullByDefault
public class SetAttributesRequest {
    private final @Nullable String name;
    private final @Nullable List<String> lights;

    public SetAttributesRequest(String name) {
        this(name, null);
    }

    public SetAttributesRequest(List<HueObject> lights) {
        this(null, lights);
    }

    public SetAttributesRequest(@Nullable String name, @Nullable List<HueObject> lights) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Name can be at most 32 characters long");
        } else if (lights != null && (lights.isEmpty() || lights.size() > 16)) {
            throw new IllegalArgumentException("Group cannot be empty and cannot have more than 16 lights");
        }

        this.name = name;
        this.lights = lights == null ? null : lights.stream().map(l -> l.getId()).collect(Collectors.toList());
    }
}
