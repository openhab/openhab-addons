/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal.cloudapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jetty.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Cloud service from Google Translate TTS.
 *
 * @author Jochen Hiller - Initial contribution
 * @author Laurent Garnier - add support for all API languages
 * @author Laurent Garnier - add support for OGG and AAC audio formats
 * @author André Duffeck - Port to the Google Translate TTS service API
 */
public class GoogleTTSCloudImplementation implements GoogleTTSCloudAPI {

    private static final int MAX_CHARACTERS = 200;

    private final Logger logger = LoggerFactory.getLogger(GoogleTTSCloudImplementation.class);

    private static Set<String> supportedAudioFormats = new HashSet<String>();
    static {
        supportedAudioFormats.add("MP3");
    }
    private static Set<Locale> supportedLocales = new HashSet<Locale>();
    static {
        supportedLocales.add(Locale.forLanguageTag("af"));
        supportedLocales.add(Locale.forLanguageTag("ar"));
        supportedLocales.add(Locale.forLanguageTag("bn"));
        supportedLocales.add(Locale.forLanguageTag("ca"));
        supportedLocales.add(Locale.forLanguageTag("cs"));
        supportedLocales.add(Locale.forLanguageTag("cy"));
        supportedLocales.add(Locale.forLanguageTag("da"));
        supportedLocales.add(Locale.forLanguageTag("de"));
        supportedLocales.add(Locale.forLanguageTag("el"));
        supportedLocales.add(Locale.forLanguageTag("en"));
        supportedLocales.add(Locale.forLanguageTag("en-au"));
        supportedLocales.add(Locale.forLanguageTag("en-uk"));
        supportedLocales.add(Locale.forLanguageTag("en-us"));
        supportedLocales.add(Locale.forLanguageTag("eo"));
        supportedLocales.add(Locale.forLanguageTag("es"));
        supportedLocales.add(Locale.forLanguageTag("es-es"));
        supportedLocales.add(Locale.forLanguageTag("es-us"));
        supportedLocales.add(Locale.forLanguageTag("fi"));
        supportedLocales.add(Locale.forLanguageTag("fr"));
        supportedLocales.add(Locale.forLanguageTag("hi"));
        supportedLocales.add(Locale.forLanguageTag("hr"));
        supportedLocales.add(Locale.forLanguageTag("hu"));
        supportedLocales.add(Locale.forLanguageTag("hy"));
        supportedLocales.add(Locale.forLanguageTag("id"));
        supportedLocales.add(Locale.forLanguageTag("is"));
        supportedLocales.add(Locale.forLanguageTag("it"));
        supportedLocales.add(Locale.forLanguageTag("ja"));
        supportedLocales.add(Locale.forLanguageTag("ko"));
        supportedLocales.add(Locale.forLanguageTag("la"));
        supportedLocales.add(Locale.forLanguageTag("lv"));
        supportedLocales.add(Locale.forLanguageTag("mk"));
        supportedLocales.add(Locale.forLanguageTag("nl"));
        supportedLocales.add(Locale.forLanguageTag("no"));
        supportedLocales.add(Locale.forLanguageTag("pl"));
        supportedLocales.add(Locale.forLanguageTag("pt"));
        supportedLocales.add(Locale.forLanguageTag("pt-br"));
        supportedLocales.add(Locale.forLanguageTag("ro"));
        supportedLocales.add(Locale.forLanguageTag("ru"));
        supportedLocales.add(Locale.forLanguageTag("sk"));
        supportedLocales.add(Locale.forLanguageTag("sq"));
        supportedLocales.add(Locale.forLanguageTag("sr"));
        supportedLocales.add(Locale.forLanguageTag("sv"));
        supportedLocales.add(Locale.forLanguageTag("sw"));
        supportedLocales.add(Locale.forLanguageTag("ta"));
        supportedLocales.add(Locale.forLanguageTag("th"));
        supportedLocales.add(Locale.forLanguageTag("tr"));
        supportedLocales.add(Locale.forLanguageTag("uk"));
        supportedLocales.add(Locale.forLanguageTag("vi"));
        supportedLocales.add(Locale.forLanguageTag("zh"));
        supportedLocales.add(Locale.forLanguageTag("zh-cn"));
        supportedLocales.add(Locale.forLanguageTag("zh-tw"));
    }
    private static Set<String> supportedVoices = Collections.singleton("GoogleTTS");

    @Override
    public Set<String> getAvailableAudioFormats() {
        return supportedAudioFormats;
    }

    @Override
    public Set<Locale> getAvailableLocales() {
        return supportedLocales;
    }

    @Override
    public Set<String> getAvailableVoices() {
        return supportedVoices;
    }

    @Override
    public Set<String> getAvailableVoices(Locale locale) {
        for (Locale voiceLocale : supportedLocales) {
            if (voiceLocale.toLanguageTag().equalsIgnoreCase(locale.toLanguageTag())) {
                return supportedVoices;
            }
        }
        return new HashSet<String>();
    }

    /**
     * This method will return an input stream to an audio stream for the given
     * parameters.
     *
     * It will do that using a plain URL connection to avoid any external
     * dependencies.
     *
     * @throws URISyntaxException
     */
    @Override
    public InputStream getTextToSpeech(String text, String locale) throws IOException, URISyntaxException {
        List<String> chunks = splitText(text);
        InputStream is = null;
        for (int i = 0; i < chunks.size(); i++) {
            String sentence = chunks.get(i);
            String token = GoogleTTSToken.calculateToken(sentence);
            String url = createURL(sentence, locale, token, i, chunks.size());
            logger.debug("Call {}", url);
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");

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

            if (is == null) {
                is = connection.getInputStream();
            } else {
                is = new SequenceInputStream(is, connection.getInputStream());
            }
        }
        return is;
    }

    /**
     * Splits the text into multiple chunks that are processable by the Google Translate TTS API.
     * It tries to split the text at sentence borders in order to improve intonation.
     *
     * @param text
     *            The text to split
     * @return The list of chunks that can be given to the Google Translate TTS API
     */
    private List<String> splitText(String text) {
        String[] sentences = text.split("(?<=[.!?:;])");
        List<String> chunks = new ArrayList<String>();
        String currentChunk = "";

        for (String sentence : sentences) {
            if (!currentChunk.isEmpty() && (currentChunk.length() + sentence.length()) > MAX_CHARACTERS) {
                chunks.add(currentChunk.trim());
                currentChunk = "";
            }
            if (sentence.length() > MAX_CHARACTERS) {
                chunks.addAll(splitSentence(sentence));
            } else {
                currentChunk += sentence;
            }
        }
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.trim());
        }

        return chunks;
    }

    /**
     * Splits a sentence into multiple chunks that are processable by the Google Translate TTS API.
     *
     * @param text
     *            The sentence to split
     * @return The list of chunks that can be given to the Google Translate TTS API
     */
    private List<String> splitSentence(String sentence) {
        String[] words = sentence.trim().split("(?<=[^\\w])");
        List<String> chunks = new ArrayList<String>();
        String currentChunk = "";

        for (String word : words) {
            if (!currentChunk.isEmpty() && currentChunk.length() + word.length() > MAX_CHARACTERS) {
                chunks.add(currentChunk.trim());
                currentChunk = "";
            }
            if (word.length() > MAX_CHARACTERS) {
                logger.warn("Can not say '{}' because it exceeds the character limit of {} characters.", word,
                        Integer.toString(MAX_CHARACTERS));
            } else {
                currentChunk += word;
            }
        }
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.trim());
        }

        return chunks;
    }

    /**
     * This method will create the URL for the cloud service. The text will be
     * URI encoded as it is part of the URL.
     *
     * It is in package scope to be accessed by tests.
     *
     * @throws URISyntaxException
     */
    private String createURL(String text, String locale, String token, int index, int total) throws URISyntaxException {
        String encodedText;
        try {
            encodedText = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error("UnsupportedEncodingException for UTF-8 MUST NEVER HAPPEN! Check your JVM configuration!", ex);
            // fall through and use msg un-encoded
            encodedText = text;
        }
        String base = "https://translate.google.com";
        String requestPath = "translate_tts?ie=UTF-8&tl=" + locale + "&q=" + encodedText + "&tk=" + token + "&total="
                + Integer.toString(total) + "&idx=" + Integer.toString(index) + "&client=tw-ob&textlen="
                + Integer.toString(text.length());
        String url = URIUtil.addPaths(base, requestPath);
        return url;
    }
}
