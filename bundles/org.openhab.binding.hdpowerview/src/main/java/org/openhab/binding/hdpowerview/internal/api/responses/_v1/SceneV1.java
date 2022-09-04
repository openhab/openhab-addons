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
package org.openhab.binding.hdpowerview.internal.api.responses._v1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.responses.Scene;

/**
 * Scene object as returned by an HD PowerView hub of Generation 3
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Refactored into separate class
 */
@NonNullByDefault
public class SceneV1 extends Scene {
    public int roomId;
    public int order;
    public int colorId;
    public int iconId;

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SceneV1)) {
            return false;
        }
        SceneV1 other = (SceneV1) o;

        return this.id == other.id && getName().equals(other.getName()) && this.roomId == other.roomId
                && this.order == other.order && this.colorId == other.colorId && this.iconId == other.iconId;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + id;
        result = prime * result + getName().hashCode();
        result = prime * result + roomId;
        result = prime * result + order;
        result = prime * result + colorId;
        result = prime * result + iconId;

        return result;
    }

    @Override
    public int compareTo(Scene other) throws IllegalArgumentException {
        if (other.version() == version()) {
            return Integer.compare(order, ((SceneV1) other).order);
        }
        throw new IllegalArgumentException("Cannot compare scenes from different hub generations");
    }

    @Override
    public int version() {
        return 1;
    }
}
