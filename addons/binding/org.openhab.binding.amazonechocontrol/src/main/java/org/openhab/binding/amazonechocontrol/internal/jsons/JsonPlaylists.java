/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.Map;

/**
 * The {@link JsonPlayerState} encapsulate the GSON data of playlist query
 *
 * @author Michael Geramb - Initial contribution
 */
public class JsonPlaylists {

    public Map<String, PlayList[]> playlists;

    public class PlayList {
        public String playlistId;
        public String title;
        public int trackCount;
        public int version;
    }
}
