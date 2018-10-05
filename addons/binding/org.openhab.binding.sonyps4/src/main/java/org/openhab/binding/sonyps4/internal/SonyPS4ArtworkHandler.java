/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonyps4.internal;

import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.io.net.http.HttpUtil;

/**
 * The {@link SonyPS4ArtworkHandler} is responsible for fetching and caching
 * application artwork.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
public class SonyPS4ArtworkHandler {

    private SonyPS4ArtworkHandler() {
        throw new IllegalStateException("Utility class");
    }

    static RawType fetchArtworkForTitleid(String titleid, Integer size) {
        RawType artwork = null;
        // Try to find the image in the cache first, then try to download it from PlayStation Store.
        artwork = HttpUtil
                .downloadImage("https://store.playstation.com/store/api/chihiro/00_09_000/titlecontainer/US/en/999/"
                        + titleid + "_00/image?w=" + size.toString() + "&h=" + size.toString(), 1000);
        return artwork;
    }

}
