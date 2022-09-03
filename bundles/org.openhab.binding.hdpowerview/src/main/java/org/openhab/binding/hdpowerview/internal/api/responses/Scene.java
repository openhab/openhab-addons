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
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Scene object as returned by an HD PowerView hub
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public abstract class Scene implements Comparable<Scene> {
    // fields that are common to Generation 1/2 and 3 hubs
    public int id;
    public @Nullable String name;

    @Override
    public abstract int compareTo(Scene other);

    public String getName() {
        return new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8);
    }

    public abstract int version();
}
