/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * A list of Scenes, as returned by the HD Power View Hub
 *
 * @author Andy Lintner - Initial contribution
 */
public class Scenes {

    public List<Scene> sceneData;
    public List<String> sceneIds;

    public static class Scene {
        public int id;
        String name;
        public int roomId;
        public int order;
        public int colorId;
        public int iconId;

        public String getName() {
            return new String(Base64.getDecoder().decode(name));
        }
    }

}
