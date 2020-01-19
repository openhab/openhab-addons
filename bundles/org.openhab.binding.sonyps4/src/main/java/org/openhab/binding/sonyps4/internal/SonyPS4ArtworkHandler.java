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
package org.openhab.binding.sonyps4.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPS4ArtworkHandler} is responsible for fetching and caching
 * application artwork.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class SonyPS4ArtworkHandler {

    private final Logger logger = LoggerFactory.getLogger(SonyPS4ArtworkHandler.class);
    private final File artworkCacheFolder;

    /**
     * Service id
     */
    static final String SERVICE_ID = "sonyps4";

    /**
     * Service category
     */
    static final String SERVICE_CATEGORY = "binding";

    /**
     * Service pid
     */
    static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;

    /**
     * Cache folder under $userdata
     */
    private static final String CACHE_FOLDER_NAME = "cache";

    SonyPS4ArtworkHandler() {
        // create home folder
        File userData = new File(ConfigConstants.getUserDataFolder());
        File homeFolder = new File(userData, CACHE_FOLDER_NAME);

        if (!homeFolder.exists()) {
            homeFolder.mkdirs();
        }
        logger.debug("Using home folder: {}", homeFolder.getAbsolutePath());

        // create cache folder
        File cacheFolder = new File(homeFolder, SERVICE_PID);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        logger.debug("Using cache folder {}", cacheFolder.getAbsolutePath());
        this.artworkCacheFolder = cacheFolder;
    }

    @Nullable
    RawType fetchArtworkForTitleid(String titleid, Integer size, Locale locale) {
        // Try to find the image in the cache first, then try to download it from PlayStation Store.
        RawType artwork = null;
        if (titleid.isEmpty()) {
            return artwork;
        }
        String artworkFilename = titleid + "_" + size.toString() + ".jpg";
        File artworkFileInCache = new File(artworkCacheFolder, artworkFilename);
        if (artworkFileInCache.exists()) {
            logger.debug("Artwork file {} was found in cache.", artworkFileInCache.getName());
            int length = (int) artworkFileInCache.length();
            byte[] fileBuffer = new byte[length];
            try (FileInputStream fis = new FileInputStream(artworkFileInCache)) {
                fis.read(fileBuffer, 0, length);
                artwork = new RawType(fileBuffer, "image/jpeg");
            } catch (FileNotFoundException ex) {
                logger.debug("Could not find {} in cache. ", artworkFileInCache, ex);
            } catch (IOException ex) {
                logger.error("Could not read {} from cache. ", artworkFileInCache, ex);
            }
            return artwork;
        }
        String storeLocale = locale.getCountry() + "/" + locale.getLanguage();
        artwork = HttpUtil.downloadImage("https://store.playstation.com/store/api/chihiro/00_09_000/titlecontainer/"
                + storeLocale + "/999/" + titleid + "_00/image?w=" + size.toString() + "&h=" + size.toString(), 1000);
        if (artwork != null) {
            try (FileOutputStream fos = new FileOutputStream(artworkFileInCache)) {
                logger.debug("Caching artwork file {}", artworkFileInCache.getName());
                fos.write(artwork.getBytes(), 0, artwork.getBytes().length);
            } catch (FileNotFoundException ex) {
                logger.warn("Could not create {} in cache. ", artworkFileInCache, ex);
            } catch (IOException ex) {
                logger.error("Could not write {} to cache. ", artworkFileInCache, ex);
            }
        }
        return artwork;
    }

}
