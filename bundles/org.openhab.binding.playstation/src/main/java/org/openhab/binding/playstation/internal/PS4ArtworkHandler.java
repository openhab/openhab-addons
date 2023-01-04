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
package org.openhab.binding.playstation.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PS4ArtworkHandler} is responsible for fetching and caching
 * application artwork.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class PS4ArtworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PS4ArtworkHandler.class);
    private static final File ARTWORK_CACHE_FOLDER;

    /** Service pid */
    private static final String SERVICE_PID = "org.openhab.binding.playstation";

    /** Cache folder under $userdata */
    private static final String CACHE_FOLDER_NAME = "cache";

    /** Some countries use EN as language in the PS Store, this is to minimize requests */
    private static boolean useLanguageEn = false;

    private PS4ArtworkHandler() {
        // No need to instantiate
    }

    static {
        // create cache folder
        File userData = new File(OpenHAB.getUserDataFolder());
        File homeFolder = new File(userData, CACHE_FOLDER_NAME);

        if (!homeFolder.exists()) {
            homeFolder.mkdirs();
        }
        LOGGER.debug("Using home folder: {}", homeFolder.getAbsolutePath());

        // create binding folder
        File cacheFolder = new File(homeFolder, SERVICE_PID);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        LOGGER.debug("Using cache folder {}", cacheFolder.getAbsolutePath());
        ARTWORK_CACHE_FOLDER = cacheFolder;
    }

    /**
     * Builds an artwork request string for the specified TitleId, also takes into account if the language should be
     * from the specified locale or just "en".
     *
     * @param locale The country and language to use for the store look up.
     * @param titleId The Title ID of the Application/game.
     * @param size The size of the artwork.
     * @return A https request as a String.
     */
    private static String buildArtworkRequest(Locale locale, String titleId, Integer size) {
        String language = useLanguageEn ? "en" : locale.getLanguage();
        return "https://store.playstation.com/store/api/chihiro/00_09_000/titlecontainer/" + locale.getCountry() + "/"
                + language + "/999/" + titleId + "_00/image?w=" + size.toString() + "&h=" + size.toString();
    }

    /**
     * Fetch artwork for PS4 application. First looks for the file on disc, if the file is not on the disc it checks
     * PlayStation store
     *
     * @param titleid Title ID of application.
     * @param size Size (width & height) of art work in pixels , max 1024.
     * @param locale Locale used on PlayStation store to find art work.
     * @return A JPEG image as a RawType if an art work file is found otherwise null.
     */
    public static @Nullable RawType fetchArtworkForTitleid(String titleId, Integer size, Locale locale) {
        return fetchArtworkForTitleid(titleId, size, locale, false);
    }

    /**
     * Fetch artwork for PS4 application. First looks for the file on disc, if the file is not on the disc it checks
     * PlayStation store
     *
     * @param titleid Title ID of application.
     * @param size Size (width & height) of art work in pixels , max 1024.
     * @param locale Locale used on PlayStation store to find art work.
     * @param forceRefetch When true, tries to re-fetch art work from PlayStation store, sometimes the art work is
     *            updated along with the game.
     * @return A JPEG image as a RawType if an art work file is found otherwise null.
     */
    public static @Nullable RawType fetchArtworkForTitleid(String titleId, Integer size, Locale locale,
            boolean forceRefetch) {
        // Try to find the image in the cache first, then try to download it from PlayStation Store.
        RawType artwork = null;
        if (titleId.isEmpty()) {
            return artwork;
        }
        String artworkFilename = titleId + "_" + size.toString() + ".jpg";
        File artworkFileInCache = new File(ARTWORK_CACHE_FOLDER, artworkFilename);
        if (artworkFileInCache.exists() && !forceRefetch) {
            LOGGER.trace("Artwork file {} was found in cache.", artworkFileInCache.getName());
            int length = (int) artworkFileInCache.length();
            byte[] fileBuffer = new byte[length];
            try (FileInputStream fis = new FileInputStream(artworkFileInCache)) {
                fis.read(fileBuffer, 0, length);
                artwork = new RawType(fileBuffer, "image/jpeg");
            } catch (FileNotFoundException ex) {
                LOGGER.debug("Could not find {} in cache. {}", artworkFileInCache, ex.getMessage());
            } catch (IOException ex) {
                LOGGER.debug("Could not read {} from cache. {}", artworkFileInCache, ex.getMessage());
            }
            if (artwork != null) {
                return artwork;
            }
        }
        String request = buildArtworkRequest(locale, titleId, size);
        artwork = HttpUtil.downloadImage(request);
        if (artwork == null) {
            // If artwork is not found for specified language/"en", try the other way around.
            useLanguageEn = !useLanguageEn;
            request = buildArtworkRequest(locale, titleId, size);
            artwork = HttpUtil.downloadImage(request);
        }
        if (artwork != null) {
            try (FileOutputStream fos = new FileOutputStream(artworkFileInCache)) {
                LOGGER.debug("Caching artwork file {}", artworkFileInCache.getName());
                fos.write(artwork.getBytes(), 0, artwork.getBytes().length);
            } catch (FileNotFoundException ex) {
                LOGGER.debug("Could not create {} in cache. {}", artworkFileInCache, ex.getMessage());
            } catch (IOException ex) {
                LOGGER.debug("Could not write {} to cache. {}", artworkFileInCache, ex.getMessage());
            }
        } else {
            LOGGER.debug("Could not download artwork file from {}", request);
        }
        return artwork;
    }
}
