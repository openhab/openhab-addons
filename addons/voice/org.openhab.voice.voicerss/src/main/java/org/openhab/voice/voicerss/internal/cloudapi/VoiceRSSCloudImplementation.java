/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.voicerss.internal.cloudapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Cloud service from VoiceRSS. For more information,
 * see API documentation at http://www.voicerss.org/api/documentation.aspx.
 *
 * Current state of implementation:
 * <ul>
 * <li>Only EN and DE languages supported</li>
 * <li>Only default voice supported with good audio quality</li>
 * <li>Only MP3 audio format supported</li>
 * <li>It uses HTTP and not HTTPS (for performance reasons)</li>
 * </ul>
 *
 * @author Jochen Hiller - Initial contribution
 */
public class VoiceRSSCloudImplementation implements VoiceRSSCloudAPI {

    private final Logger logger = LoggerFactory.getLogger(VoiceRSSCloudImplementation.class);

    private static Set<String> supportedAudioFormats = getSupportedAudioFormats();
    private static Set<Locale> supportedLocales = getSupportedLocales();
    private static Set<String> supportedVoices = getSupportedVoices();

    /**
     * Will support only "MP3" for the moment.
     */
    private static Set<String> getSupportedAudioFormats() {
        Set<String> formats = new HashSet<String>();
        formats.add(AudioFormat.CODEC_MP3);
        return formats;
    }

    @Override
    public Set<String> getAvailableAudioFormats() {
        return supportedAudioFormats;
    }

    /**
     * Will support only 3 locales for the moment.
     */
    private static Set<Locale> getSupportedLocales() {
        Set<Locale> locales = new HashSet<Locale>();
        locales.add(Locale.forLanguageTag("ca-es"));
        locales.add(Locale.forLanguageTag("da-dk"));
        locales.add(Locale.forLanguageTag("de-de"));
        locales.add(Locale.forLanguageTag("en-au"));
        locales.add(Locale.forLanguageTag("en-ca"));
        locales.add(Locale.forLanguageTag("en-gb"));
        locales.add(Locale.forLanguageTag("en-in"));
        locales.add(Locale.forLanguageTag("en-us"));
        locales.add(Locale.forLanguageTag("es-es"));
        locales.add(Locale.forLanguageTag("es-mx"));
        locales.add(Locale.forLanguageTag("fi-fi"));
        locales.add(Locale.forLanguageTag("fr-ca"));
        locales.add(Locale.forLanguageTag("fr-fr"));
        locales.add(Locale.forLanguageTag("it-it"));
        locales.add(Locale.forLanguageTag("ja-jp"));
        locales.add(Locale.forLanguageTag("ko-kr"));
        locales.add(Locale.forLanguageTag("nb-no"));
        locales.add(Locale.forLanguageTag("nl-nl"));
        locales.add(Locale.forLanguageTag("pl-pl"));
        locales.add(Locale.forLanguageTag("pt-br"));
        locales.add(Locale.forLanguageTag("pt-pt"));
        locales.add(Locale.forLanguageTag("ru-ru"));
        locales.add(Locale.forLanguageTag("sv-se"));
        locales.add(Locale.forLanguageTag("zh-cn"));
        locales.add(Locale.forLanguageTag("zh-hk"));
        locales.add(Locale.forLanguageTag("zh-tw"));
        return locales;
    }

    @Override
    public Set<Locale> getAvailableLocales() {
        return supportedLocales;
    }

    /**
     * Will support only a default voice with good quality for each locale.
     */
    private static Set<String> getSupportedVoices() {
        // one default voice for every locale
        Set<String> voices = new HashSet<String>();
        for (int i = 1; i <= getSupportedLocales().size(); i++) {
            voices.add("VoiceRSS");
        }
        return voices;
    }

    @Override
    public Set<String> getAvailableVoices() {
        return supportedVoices;
    }

    @Override
    public Set<String> getAvailableVoices(Locale locale) {
        Set<String> voices = new HashSet<String>();
        for (Locale voiceLocale : supportedLocales) {
            if (voiceLocale.toLanguageTag().equalsIgnoreCase(locale.toLanguageTag())) {
                voices.add("VoiceRSS");
                break;
            }
        }
        return voices;
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
            throw new IOException("Could not read from service: HTTP code" + status);
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
                // ignore
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
    String createURL(String apiKey, String text, String locale, String audioFormat) {
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
