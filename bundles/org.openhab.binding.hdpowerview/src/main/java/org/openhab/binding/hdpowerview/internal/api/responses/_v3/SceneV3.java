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
package org.openhab.binding.hdpowerview.internal.api.responses._v3;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.responses.Scene;

/**
 * Scene object as returned by an HD PowerView hub of Generation 3
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SceneV3 extends Scene {
    public @Nullable String ptName;
    public @Nullable String color;
    public @Nullable String icon;
    public @Nullable List<Integer> roomIds;

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SceneV3)) {
            return false;
        }
        SceneV3 other = (SceneV3) o;
        String color = this.color;
        String icon = this.icon;

        // TODO check roomIds as well ??
        return this.id == other.id && getName().equals(other.getName()) && (color != null && color.equals(other.color))
                && (icon != null && icon.equals(other.icon));
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;

        String color = this.color;
        String icon = this.icon;

        // TODO hash roomIds as well ??
        result = prime * result + id;
        result = prime * result + getName().hashCode();
        result = prime * result + (color == null ? 0 : color.hashCode());
        result = prime * result + (icon == null ? 0 : icon.hashCode());

        return result;
    }

    @Override
    public String getName() {
        return String.join(" ", super.getName(), ptName);
    }

    @Override
    public int compareTo(Scene other) throws IllegalArgumentException {
        if (other.version() == version()) {
            // TODO fix this code..
            return this.equals(other) ? 0 : Integer.MAX_VALUE;
        }
        throw new IllegalArgumentException("Cannot compare scenes from different hub generations");
    }

    @Override
    public int version() {
        return 3;
    }
}
