/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.voice.voicerss.internal.cloudapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Cloud service from VoiceRSS. For more information,
 * see API documentation at http://www.voicerss.org/api .
 *
 * Current state of implementation:
 * <ul>
 * <li>All API languages supported</li>
 * <li>Only default voice supported with good audio quality</li>
 * <li>MP3, OGG, AAC and WAV audio formats supported</li>
 * <li>It uses HTTP and not HTTPS (for performance reasons)</li>
 * </ul>
 *
 * @author Jochen Hiller - Initial contribution
 * @author Laurent Garnier - add support for all API languages
 * @author Laurent Garnier - add support for OGG and AAC audio formats
 * @author Andreas Brenk - add support for WAV audio format
 */
@NonNullByDefault
public class VoiceRSSCloudImpl implements VoiceRSSCloudAPI {

    public static final String DEFAULT_VOICE = "default";

    public static final String API_URL = "https://api.voicerss.org/?key=%s&hl=%s&c=%s&f=%s&src=%s";
    public static final String API_URL_WITH_VOICE = API_URL + "&v=%s";

    private static final Set<String> SUPPORTED_AUDIO_CODECS = Set.of("MP3", "OGG", "AAC", "WAV", "CAF");

    private static final Set<Locale> SUPPORTED_LOCALES = new HashSet<>();
    static {
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ar-eg"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ar-sa"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("bg-bg"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ca-es"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("cs-cz"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("da-dk"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("de-at"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("de-de"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("de-ch"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("el-gr"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-au"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-ca"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-gb"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-ie"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-in"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("en-us"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("es-es"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("es-mx"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("fi-fi"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("fr-ca"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("fr-fr"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("fr-ch"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("he-il"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("hi-in"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("hr-hr"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("hu-hu"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("id-id"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("it-it"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ja-jp"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ko-kr"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ms-my"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("nb-no"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("nl-be"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("nl-nl"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("pl-pl"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("pt-br"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("pt-pt"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ro-ro"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ru-ru"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("sk-sk"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("sl-si"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("sv-se"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("ta-in"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("th-th"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("tr-tr"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("vi-vn"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("zh-cn"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("zh-hk"));
        SUPPORTED_LOCALES.add(Locale.forLanguageTag("zh-tw"));
    }

    private static final Map<String, Set<String>> SUPPORTED_VOICES = new HashMap<>();
    static {
        SUPPORTED_VOICES.put("ar-eg", Set.of("Oda"));
        SUPPORTED_VOICES.put("ar-sa", Set.of("Salim"));
        SUPPORTED_VOICES.put("bg-bg", Set.of("Dimo"));
        SUPPORTED_VOICES.put("ca-es", Set.of("Rut"));
        SUPPORTED_VOICES.put("cs-cz", Set.of("Josef"));
        SUPPORTED_VOICES.put("da-dk", Set.of("Freja"));
        SUPPORTED_VOICES.put("de-at", Set.of("Lukas"));
        SUPPORTED_VOICES.put("de-de", Set.of("Hanna", "Lina", "Jonas"));
        SUPPORTED_VOICES.put("de-ch", Set.of("Tim"));
        SUPPORTED_VOICES.put("el-gr", Set.of("Neo"));
        SUPPORTED_VOICES.put("en-au", Set.of("Zoe", "Isla", "Evie", "Jack"));
        SUPPORTED_VOICES.put("en-ca", Set.of("Rose", "Clara", "Emma", "Mason"));
        SUPPORTED_VOICES.put("en-gb", Set.of("Alice", "Nancy", "Lily", "Harry"));
        SUPPORTED_VOICES.put("en-ie", Set.of("Oran"));
        SUPPORTED_VOICES.put("en-in", Set.of("Eka", "Jai", "Ajit"));
        SUPPORTED_VOICES.put("en-us", Set.of("Linda", "Amy", "Mary", "John", "Mike"));
        SUPPORTED_VOICES.put("es-es", Set.of("Camila", "Sofia", "Luna", "Diego"));
        SUPPORTED_VOICES.put("es-mx", Set.of("Juana", "Silvia", "Teresa", "Jose"));
        SUPPORTED_VOICES.put("fi-fi", Set.of("Aada"));
        SUPPORTED_VOICES.put("fr-ca", Set.of("Emile", "Olivia", "Logan", "Felix"));
        SUPPORTED_VOICES.put("fr-fr", Set.of("Bette", "Iva", "Zola", "Axel"));
        SUPPORTED_VOICES.put("fr-ch", Set.of("Theo"));
        SUPPORTED_VOICES.put("he-il", Set.of("Rami"));
        SUPPORTED_VOICES.put("hi-in", Set.of("Puja", "Kabir"));
        SUPPORTED_VOICES.put("hr-hr", Set.of("Nikola"));
        SUPPORTED_VOICES.put("hu-hu", Set.of("Mate"));
        SUPPORTED_VOICES.put("id-id", Set.of("Intan"));
        SUPPORTED_VOICES.put("it-it", Set.of("Bria", "Mia", "Pietro"));
        SUPPORTED_VOICES.put("ja-jp", Set.of("Hina", "Airi", "Fumi", "Akira"));
        SUPPORTED_VOICES.put("ko-kr", Set.of("Nari"));
        SUPPORTED_VOICES.put("ms-my", Set.of("Aqil"));
        SUPPORTED_VOICES.put("nb-no", Set.of("Marte", "Erik"));
        SUPPORTED_VOICES.put("nl-be", Set.of("Daan"));
        SUPPORTED_VOICES.put("nl-nl", Set.of("Lotte", "Bram"));
        SUPPORTED_VOICES.put("pl-pl", Set.of("Julia", "Jan"));
        SUPPORTED_VOICES.put("pt-br", Set.of("Marcia", "Ligia", "Yara", "Dinis"));
        SUPPORTED_VOICES.put("pt-pt", Set.of("Leonor"));
        SUPPORTED_VOICES.put("ro-ro", Set.of("Doru"));
        SUPPORTED_VOICES.put("ru-ru", Set.of("Olga", "Marina", "Peter"));
        SUPPORTED_VOICES.put("sk-sk", Set.of("Beda"));
        SUPPORTED_VOICES.put("sl-si", Set.of("Vid"));
        SUPPORTED_VOICES.put("sv-se", Set.of("Molly", "Hugo"));
        SUPPORTED_VOICES.put("ta-in", Set.of("Sai"));
        SUPPORTED_VOICES.put("th-th", Set.of("Ukrit"));
        SUPPORTED_VOICES.put("tr-tr", Set.of("Omer"));
        SUPPORTED_VOICES.put("vi-vn", Set.of("Chi"));
        SUPPORTED_VOICES.put("zh-cn", Set.of("Luli", "Shu", "Chow", "Wang"));
        SUPPORTED_VOICES.put("zh-hk", Set.of("Jia", "Xia", "Chen"));
        SUPPORTED_VOICES.put("zh-tw", Set.of("Akemi", "Lin", "Lee"));
    }

    protected boolean logging;

    public VoiceRSSCloudImpl(boolean logging) {
        this.logging = logging;
    }

    @Override
    public Set<String> getAvailableAudioCodecs() {
        return SUPPORTED_AUDIO_CODECS;
    }

    @Override
    public Set<Locale> getAvailableLocales() {
        return SUPPORTED_LOCALES;
    }

    @Override
    public Set<String> getAvailableVoices() {
        // different locales support different voices, so let's list all here in one big set when no locale is provided
        Set<String> allvoxes = new HashSet<>();
        allvoxes.add(DEFAULT_VOICE);
        for (Set<String> langvoxes : SUPPORTED_VOICES.values()) {
            for (String langvox : langvoxes) {
                allvoxes.add(langvox);
            }
        }
        return allvoxes;
    }

    @Override
    public Set<String> getAvailableVoices(Locale locale) {
        Set<String> allvoxes = new HashSet<>();
        allvoxes.add(DEFAULT_VOICE);
        // all maps must be defined with key in lowercase
        String langtag = locale.toLanguageTag().toLowerCase();
        if (SUPPORTED_VOICES.containsKey(langtag)) {
            for (String langvox : SUPPORTED_VOICES.get(langtag)) {
                allvoxes.add(langvox);
            }
        }
        return allvoxes;
    }

    /*
     * This method will return an input stream to an audio stream for the given
     * parameters.
     *
     * It will do that using a plain URL connection to avoid any external
     * dependencies.
     */
    @Override
    public InputStream getTextToSpeech(String apiKey, String text, String locale, String voice, String audioCodec,
            String audioFormat) throws IOException {
        String url = createURL(apiKey, text, locale, voice, audioCodec, audioFormat);
        if (logging) {
            LoggerFactory.getLogger(VoiceRSSCloudImpl.class).debug("Call {}", url.replace(apiKey, "***"));
        }
        URLConnection connection = new URL(url).openConnection();

        // we will check return codes. The service will ALWAYS return a HTTP
        // 200, but for error messages, it will return a text/plain format and
        // the error message in body
        int status = ((HttpURLConnection) connection).getResponseCode();
        if (HttpURLConnection.HTTP_OK != status) {
            if (logging) {
                LoggerFactory.getLogger(VoiceRSSCloudImpl.class).warn("Call {} returned HTTP {}",
                        url.replace(apiKey, "***"), status);
            }
            throw new IOException("Could not read from service: HTTP code " + status);
        }
        if (logging) {
            Logger logger = LoggerFactory.getLogger(VoiceRSSCloudImpl.class);
            if (logger.isTraceEnabled()) {
                for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                    logger.trace("Response.header: {}={}", header.getKey(), header.getValue());
                }
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
                if (logging) {
                    LoggerFactory.getLogger(VoiceRSSCloudImpl.class).debug("Failed to close inputstream", ex);
                }
            }
            throw new IOException(
                    "Could not read audio content, service returned an error: " + new String(bytes, "UTF-8"));
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
    private String createURL(String apiKey, String text, String locale, String voice, String audioCodec,
            String audioFormat) {
        String encodedMsg = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url;
        if (!DEFAULT_VOICE.equals(voice)) {
            url = String.format(API_URL_WITH_VOICE, apiKey, locale, audioCodec, audioFormat, encodedMsg, voice);
        } else {
            url = String.format(API_URL, apiKey, locale, audioCodec, audioFormat, encodedMsg);
        }
        return url;
    }
}
