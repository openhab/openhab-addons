/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.queue;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.FAVORITE_FILE_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Class used to model favorites, with and without full meta data. If metadata exists, it will be in UpnpEntry.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpFavorite {

    private final Logger logger = LoggerFactory.getLogger(UpnpFavorite.class);

    /**
     * Inner class used for streaming a favorite to disk as a json object.
     *
     */
    private class Favorite {
        String name;
        String uri;
        @Nullable
        UpnpEntry entry;

        Favorite(String name, String uri, @Nullable UpnpEntry entry) {
            this.name = name;
            this.uri = uri;
            this.entry = entry;
        }
    }

    private volatile Favorite favorite;

    private final Gson gson = new Gson();

    /**
     * Create a new favorite from provide URI and details. If {@link UpnpEntry} entry is null, no metadata will be
     * available with the favorite.
     *
     * @param name
     * @param uri
     * @param entry
     */
    public UpnpFavorite(String name, String uri, @Nullable UpnpEntry entry) {
        favorite = new Favorite(name, uri, entry);
    }

    /**
     * Create a new favorite from a file copy stored on disk. If the favorite cannot be read from disk, an empty
     * favorite is created.
     *
     * @param name
     * @param path
     */
    public UpnpFavorite(String name, @Nullable String path) {
        String fileName = path + name + FAVORITE_FILE_EXTENSION;
        File file = new File(fileName);

        Favorite fav = null;

        if ((path != null) && file.exists()) {
            try {
                logger.debug("Reading contents of {}", file.getAbsolutePath());
                final byte[] contents = Files.readAllBytes(file.toPath());
                final String json = new String(contents, StandardCharsets.UTF_8);

                fav = gson.fromJson(json, Favorite.class);
            } catch (JsonParseException | UnsupportedOperationException e) {
                logger.debug("JsonParseException reading {}: {}", file.toPath(), e.getMessage(), e);
            } catch (IOException e) {
                logger.debug("IOException reading favorite {} from {}", name, file.toPath());
            }
        }

        favorite = (fav != null) ? fav : new Favorite(name, "", null);
    }

    /**
     * @return name of favorite
     */
    public String getName() {
        return favorite.name;
    }

    /**
     * @return URI of favorite
     */
    public String getUri() {
        return favorite.uri;
    }

    /**
     * @return {@link UpnpEntry} known details of favorite
     */
    @Nullable
    public UpnpEntry getUpnpEntry() {
        return favorite.entry;
    }

    /**
     * Save the favorite to disk.
     *
     * @param name
     * @param path
     */
    public void saveFavorite(String name, @Nullable String path) {
        if (path == null) {
            return;
        }

        String fileName = path + name + FAVORITE_FILE_EXTENSION;
        File file = new File(fileName);

        try {
            // ensure full path exists
            file.getParentFile().mkdirs();

            String json = gson.toJson(favorite);
            final byte[] contents = json.getBytes(StandardCharsets.UTF_8);
            Files.write(file.toPath(), contents);
        } catch (IOException e) {
            logger.debug("IOException writing favorite {} to {}", name, file.toPath());
        }
    }
}
