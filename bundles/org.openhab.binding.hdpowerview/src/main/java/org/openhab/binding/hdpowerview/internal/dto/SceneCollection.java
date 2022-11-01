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
package org.openhab.binding.hdpowerview.internal.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * State of a single Scene Collection, as returned by an HD PowerView Hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SceneCollection implements Comparable<SceneCollection> {
    public int id;
    public @Nullable String name;
    public int order;
    public int colorId;
    public int iconId;

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SceneCollection)) {
            return false;
        }
        SceneCollection other = (SceneCollection) o;

        return this.id == other.id && Objects.equals(name, other.name) && this.order == other.order
                && this.colorId == other.colorId && this.iconId == other.iconId;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        String name = this.name;
        result = prime * result + id;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + order;
        result = prime * result + colorId;
        result = prime * result + iconId;

        return result;
    }

    @Override
    public int compareTo(SceneCollection other) {
        return Integer.compare(order, other.order);
    }

    public String getName() {
        return new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8);
    }
}
