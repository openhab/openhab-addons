/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import jcifs.util.Base64;

/**
 * A list of Scenes, as returned by the HD Power View Hub
 *
 * @author Andy Lintner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scenes {

    public List<Scene> sceneData;
    public List<String> sceneIds;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scene {
        public int id;
        @JsonSerialize(include = Inclusion.ALWAYS)
        String name;
        public int roomId;
        public int order;
        public int colorId;
        public int iconId;

        public String getName() {
            return new String(Base64.decode(name));
        }
    }

}
