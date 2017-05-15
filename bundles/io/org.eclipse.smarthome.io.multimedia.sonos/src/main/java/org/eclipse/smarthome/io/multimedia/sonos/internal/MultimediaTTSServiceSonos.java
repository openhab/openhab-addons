/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.multimedia.sonos.internal;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.io.multimedia.tts.TTSService;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechCache;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.TextToSpeechService;
import org.eclipse.smarthome.io.multimedia.tts.googletranslate.internal.TTSCacheImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jochen Hiller - Initial contribution and API
 */
public class MultimediaTTSServiceSonos implements TTSService {

    private static final Logger logger = LoggerFactory.getLogger(MultimediaTTSServiceSonos.class);

    private final static String DEFAULT_LANGUAGE = TextToSpeechService.LANGUAGE_ENGLISH;
    private final static String DEFAULT_ITEM = "Speaker_PlayURI";

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void say(final String text, final String voiceName, final String outputDevice) {
        logger.info("Say {} ({}) to {}", text, voiceName, outputDevice);
        String cacheFolderName = MultimediaSonosActivator.getCacheFolder();
        TextToSpeechCache cache = new TTSCacheImpl(new File(cacheFolderName));
        // TODO voice is language
        String language = voiceName;
        if (language == null) {
            language = DEFAULT_LANGUAGE;
        }
        File mp3;
        try {
            mp3 = cache.textToSpeech(text, language);
        } catch (IOException ex) {
            logger.error("Could not find the audio file for '" + text + "' (" + language + "): ", ex);
        }

        BundleContext bundleContext = MultimediaSonosActivator.getContext();
        ServiceReference<ItemRegistry> sref = bundleContext.getServiceReference(ItemRegistry.class);
        ItemRegistry itemRegistry = bundleContext.getService(sref);
        try {
            String itemName = outputDevice;
            if (itemName == null) {
                itemName = DEFAULT_ITEM;
            }
            StringItem item = (StringItem) itemRegistry.getItem(itemName);

            URL url = getHostedURLForText(cache, text, language);
            logger.trace("sendCommand ({}, {})", item.getName(), url.toString());
            item.send(StringType.valueOf(url.toString()));

        } catch (ItemNotFoundException e) {
            logger.error("An exception occurred while retrieving an Item : '{}'", e.getMessage());
        }

    }

    private URL getHostedURLForText(TextToSpeechCache cache, String text, String language) {
        String fileName = cache.getUniqueName(text, language);
        if (fileName == null) {
            return null;
        }

        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            // TODO where to get IP and port?
            String url = "http://" + ipAddress + ":8080" + "/audio/content/" + fileName + ".mp3";
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                logger.error("Could not create URL for text '" + text + "' (" + language + ")");
                return null;
            }
        } catch (UnknownHostException ex) {
            logger.error("Could not get IP address", ex);
        }
        return null;
    }
}
