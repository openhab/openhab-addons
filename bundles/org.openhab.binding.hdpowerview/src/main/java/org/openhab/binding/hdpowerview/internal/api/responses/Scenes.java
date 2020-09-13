/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * State of all Scenes in an HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class Scenes {

    public @Nullable List<Scene> sceneData;
    public @Nullable List<Integer> sceneIds;

    /*
     * the following SuppressWarnings annotation is because the Eclipse compiler
     * does NOT expect a NonNullByDefault annotation on the inner class, since it is
     * implicitly inherited from the outer class, whereas the Maven compiler always
     * requires an explicit NonNullByDefault annotation on all classes
     */
    @SuppressWarnings("null")
    @NonNullByDefault
    public static class Scene {
        public int id;
        public @Nullable String name;
        public int roomId;
        public int order;
        public int colorId;
        public int iconId;

        public String getName() {
            return new String(Base64.getDecoder().decode(name));
        }
    }
}
