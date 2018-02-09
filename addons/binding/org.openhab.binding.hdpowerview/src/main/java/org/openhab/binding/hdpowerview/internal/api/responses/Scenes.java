/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.util.Base64;
import java.util.List;

/**
 * A list of Scenes, as returned by the HD Power View Hub
 *
 * @author Andy Lintner
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
