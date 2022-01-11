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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * State of all Scene Collections in an HD PowerView hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class SceneCollections {

    public @Nullable List<SceneCollection> sceneCollectionData;
    public @Nullable List<Integer> sceneCollectionIds;

    /*
     * the following SuppressWarnings annotation is because the Eclipse compiler
     * does NOT expect a NonNullByDefault annotation on the inner class, since it is
     * implicitly inherited from the outer class, whereas the Maven compiler always
     * requires an explicit NonNullByDefault annotation on all classes
     */
    @SuppressWarnings("null")
    @NonNullByDefault
    public static class SceneCollection implements Comparable<SceneCollection> {
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

            return this.id == other.id && this.name.equals(other.name) && this.order == other.order
                    && this.colorId == other.colorId && this.iconId == other.iconId;
        }

        @Override
        public final int hashCode() {
            final int prime = 31;
            int result = 1;
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
}
