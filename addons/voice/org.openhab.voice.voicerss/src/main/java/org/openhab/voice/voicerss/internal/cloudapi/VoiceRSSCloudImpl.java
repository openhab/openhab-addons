/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.voicerss.internal.cloudapi;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Cloud service from VoiceRSS. For more information,
 * see API documentation at http://www.voicerss.org/api/documentation.aspx.
 *
 * Current state of implementation:
 * <ul>
 * <li>All API languages supported</li>
 * <li>Only default voice supported with good audio quality</li>
 * <li>Only MP3, OGG and AAC audio formats supported</li>
 * <li>It uses HTTP and not HTTPS (for performance reasons)</li>
 * </ul>
 *
 * @author Jochen Hiller - Initial contribution
 * @author Laurent Garnier - add support for all API languages
 * @author Laurent Garnier - add support for OGG and AAC audio formats
 */
public class VoiceRSSCloudImpl implements VoiceRSSCloudAPI {

    private final Logger logger = LoggerFactory.getLogger(VoiceRSSCloudImpl.class);

    private static final Set<String> SUPPORTED_AUDIO_FORMATS = Stream.of("MP3", "OGG", "AAC").collect(toSet());

    private static final Set<Locale> SUPPORTED_LOCALES = new HashSet<>();
    static {
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ca-es"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("da-dk"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("de-de"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-au"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-ca"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-gb"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-in"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-us"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("es-es"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("es-mx"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("fi-fi"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("fr-ca"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("fr-fr"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("it-it"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ja-jp"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ko-kr"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("nb-no"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("nl-nl"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("pl-pl"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("pt-br"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("pt-pt"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ru-ru"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("sv-se"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("zh-cn"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("zh-hk"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("zh-tw"));
    }

    private static final Set<String> SUPPORTED_VOICES = Collections.singleton("VoiceRSS");

    @Override
    public Set<String> getAvailableAudioFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    @Override
    public Set<Locale> getAvailableLocales() {
        return SUPPORTED_LOCALES;
    }

    @Override
    public Set<String> getAvailableVoices() {
        return SUPPORTED_VOICES;
    }

    @Override
    public Set<String> getAvailableVoices(Locale locale) {
        for (Locale voiceLocale : SUPPORTED_LOCALES) {
            if (voiceLocale.toLanguageTag().equalsIgnoreCase(locale.toLanguageTag())) {
                return SUPPORTED_VOICES;
            }
        }
        return new HashSet<>();
    }

    /**
     * This method will return an input stream to an audio stream for the given
     * parameters.
     *
     * It will do that using a plain URL connection to avoid any external
     * dependencies.
     */
    @Override
    public InputStream getTextToSpeech(String apiKey, String text, String locale, String audioFormat)
            throws IOException {
        String url = createURL(apiKey, text, locale, audioFormat);
        logger.debug("Call {}", url);
        URLConnection connection = new URL(url).openConnection();

        // we will check return codes. The service will ALWAYS return a HTTP
        // 200, but for error messages, it will return a text/plain format and
        // the error message in body
        int status = ((HttpURLConnection) connection).getResponseCode();
        if (HttpURLConnection.HTTP_OK != status) {
            logger.error("Call {} returned HTTP {}", url, status);
            throw new IOException("Could not read from service: HTTP code " + status);
        }
        if (logger.isTraceEnabled()) {
            for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                logger.trace("Response.header: {}={}", header.getKey(), header.getValue());
            }
        }
        String contentType = connection.getHeaderField("Content-Type");
        InputStream is = connection.getInputStream();
        // check if content type is text/plain, then we have an error
        if (contentType.contains("text/plain")) {
            byte[] bytes = new byte[256];
            is.read(bytes, 0, 256);
            // close before throwing an exception
            try {
                is.close();
            } catch (IOException ex) {
                logger.debug("Failed to close inputstream", ex);
            }
            throw new IOException(
                    "Could not read audio content, service return an error: " + new String(bytes, "UTF-8"));
        } else {
            return is;
        }
    }

    // internal

    /**
     * This method will create the URL for the cloud service. The text will be
     * URI encoded as it is part of the URL.
     *
     * It is in package scope to be accessed by tests.
     */
    private String createURL(String apiKey, String text, String locale, String audioFormat) {
        String encodedMsg;
        try {
            encodedMsg = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error("UnsupportedEncodingException for UTF-8 MUST NEVER HAPPEN! Check your JVM configuration!", ex);
            // fall through and use msg un-encoded
            encodedMsg = text;
        }
        return "http://api.voicerss.org/?key=" + apiKey + "&hl=" + locale + "&c=" + audioFormat
                + "&f=44khz_16bit_mono&src=" + encodedMsg;
    }

}
