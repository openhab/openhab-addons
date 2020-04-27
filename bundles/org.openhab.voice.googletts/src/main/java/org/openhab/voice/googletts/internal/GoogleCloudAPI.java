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
package org.openhab.voice.googletts.internal;

import static java.util.Collections.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.io.net.http.HttpRequestBuilder;
import org.openhab.voice.googletts.internal.protocol.AudioConfig;
import org.openhab.voice.googletts.internal.protocol.AudioEncoding;
import org.openhab.voice.googletts.internal.protocol.ListVoicesResponse;
import org.openhab.voice.googletts.internal.protocol.SsmlVoiceGender;
import org.openhab.voice.googletts.internal.protocol.SynthesisInput;
import org.openhab.voice.googletts.internal.protocol.SynthesizeSpeechRequest;
import org.openhab.voice.googletts.internal.protocol.SynthesizeSpeechResponse;
import org.openhab.voice.googletts.internal.protocol.Voice;
import org.openhab.voice.googletts.internal.protocol.VoiceSelectionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Google Cloud TTS API call implementation.
 *
 * @author Gabor Bicskei - Initial contribution and API
 */
class GoogleCloudAPI {
    /**
     * Default encoding
     */
    private static final String UTF_8 = "UTF-8";

    /**
     * JSON content type
     */
    private static final String APPLICATION_JSON = "application/json";

    /**
     * Authorization header
     */
    private static final String AUTH_HEADER_NAME = "Authorization";

    /**
     * Google Cloud Platform authorization scope
     */
    private static final String GCP_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    /**
     * URL used for retrieving the list of available voices
     */
    private static final String LIST_VOICES_URL = "https://texttospeech.googleapis.com/v1/voices";

    /**
     * URL used for synthesizing text to speech
     */
    private static final String SYTNHESIZE_SPEECH_URL = "https://texttospeech.googleapis.com/v1/text:synthesize";

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(GoogleCloudAPI.class);

    /**
     * Supported voices and locales
     */
    private final Map<Locale, Set<GoogleTTSVoice>> voices = new HashMap<>();

    /**
     * Cache folder
     */
    private File cacheFolder;

    /**
     * Configuration
     */
    private GoogleTTSConfig config;

    /**
     * Status flag
     */
    private boolean initialized;

    private Credentials credentials;

    private final Gson gson = new GsonBuilder().create();

    /**
     * Constructor.
     *
     * @param cacheFolder Service cache folder
     */
    GoogleCloudAPI(File cacheFolder) {
        this.cacheFolder = cacheFolder;
    }

    /**
     * Configuration update.
     *
     * @param config New configuration.
     */
    void setConfig(GoogleTTSConfig config) {
        this.config = config;
        String serviceAccountKey = config.getServiceAccountKey();
        if (serviceAccountKey != null && !serviceAccountKey.isEmpty()) {
            try {
                credentials = createCredentials(serviceAccountKey);
                initialized = true;
                initVoices();
            } catch (IOException e) {
                logger.error("Error initializing the service", e);
                initialized = false;
            }
        } else {
            credentials = null;
            initialized = false;
            voices.clear();
        }

        // maintain cache
        if (config.getPurgeCache() != null && config.getPurgeCache()) {
            File[] files = cacheFolder.listFiles();
            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(File::delete);
            }
            logger.debug("Cache purged.");
        }
    }

    private Credentials createCredentials(String serviceAccountKey) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(serviceAccountKey.getBytes())) {
            GoogleCredentials credential = GoogleCredentials.fromStream(bis)
                    .createScoped(Collections.singleton(GCP_SCOPE));
            return FixedCredentialsProvider.create(credential).getCredentials();
        }
    }

    private String getAuthorization() throws IOException {
        Map<String, List<String>> metadata = credentials.getRequestMetadata();
        return metadata.get(AUTH_HEADER_NAME).get(0);
    }

    /**
     * Loads supported audio formats
     *
     * @return Set of audio formats
     */
    Set<String> getSupportedAudioFormats() {
        Set<String> formats = new HashSet<>();
        for (AudioEncoding audioEncoding : AudioEncoding.values()) {
            if (audioEncoding != AudioEncoding.AUDIO_ENCODING_UNSPECIFIED) {
                formats.add(audioEncoding.toString());
            }
        }
        return formats;
    }

    /**
     * Supported locales.
     *
     * @return Set of locales
     */
    Set<Locale> getSupportedLocales() {
        return unmodifiableSet(voices.keySet());
    }

    /**
     * Supported voices for locale.
     *
     * @param locale Locale
     * @return Set of voices
     */
    Set<GoogleTTSVoice> getVoicesForLocale(Locale locale) {
        Set<GoogleTTSVoice> localeVoices = voices.get(locale);
        return localeVoices != null ? unmodifiableSet(localeVoices) : emptySet();
    }

    /**
     * Google API call to load locales and voices.
     */
    private void initVoices() throws IOException {
        if (credentials != null) {
            voices.clear();

            for (GoogleTTSVoice voice : listVoices()) {
                Locale locale = voice.getLocale();
                Set<GoogleTTSVoice> localeVoices;
                if (!voices.containsKey(locale)) {
                    localeVoices = new HashSet<>();
                    voices.put(locale, localeVoices);
                } else {
                    localeVoices = voices.get(locale);
                }
                localeVoices.add(voice);
            }
        } else {
            logger.error("Google client is not initialized!");
        }
    }

    @SuppressWarnings({ "unused", "null" })
    private List<GoogleTTSVoice> listVoices() throws IOException {
        HttpRequestBuilder builder = HttpRequestBuilder.getFrom(LIST_VOICES_URL).withHeader(AUTH_HEADER_NAME,
                getAuthorization());

        ListVoicesResponse listVoicesResponse = gson.fromJson(builder.getContentAsString(), ListVoicesResponse.class);

        if (listVoicesResponse == null || listVoicesResponse.getVoices() == null) {
            return emptyList();
        }

        List<GoogleTTSVoice> result = new ArrayList<>();
        for (Voice voice : listVoicesResponse.getVoices()) {
            for (String languageCode : voice.getLanguageCodes()) {
                result.add(new GoogleTTSVoice(Locale.forLanguageTag(languageCode), voice.getName(),
                        voice.getSsmlGender().name()));
            }
        }

        return result;
    }

    /**
     * Converts ESH audio format to Google parameters.
     *
     * @param codec Requested codec
     * @return String array of Google audio format and the file extension to use.
     */
    private String[] getFormatForCodec(String codec) {
        switch (codec) {
            case AudioFormat.CODEC_MP3:
                return new String[] { AudioEncoding.MP3.toString(), "mp3" };
            case AudioFormat.CODEC_PCM_SIGNED:
                return new String[] { AudioEncoding.LINEAR16.toString(), "wav" };
            default:
                throw new IllegalArgumentException("Audio format " + codec + " is not yet supported");
        }
    }

    byte[] synthesizeSpeech(String text, GoogleTTSVoice voice, String codec) {
        String[] format = getFormatForCodec(codec);
        String fileNameInCache = getUniqueFilenameForText(text, voice.getTechnicalName());
        File audioFileInCache = new File(cacheFolder, fileNameInCache + "." + format[1]);
        try {
            // check if in cache
            if (audioFileInCache.exists()) {
                logger.debug("Audio file {} was found in cache.", audioFileInCache.getName());
                return Files.readAllBytes(audioFileInCache.toPath());
            }

            // if not in cache, get audio data and put to cache
            byte[] audio = synthesizeSpeechByGoogle(text, voice, format[0]);
            if (audio != null) {
                saveAudioAndTextToFile(text, audioFileInCache, audio, voice.getTechnicalName());
            }
            return audio;
        } catch (FileNotFoundException ex) {
            logger.warn("Could not write {} to cache", audioFileInCache, ex);
            return null;
        } catch (IOException ex) {
            logger.error("Could not write {} to cache", audioFileInCache, ex);
            return null;
        }
    }

    /**
     * Create cache entry.
     *
     * @param text Converted text.
     * @param cacheFile Cache entry file.
     * @param audio Byte array of the audio.
     * @param voiceName Used voice
     * @throws IOException in case of file handling exceptions
     */
    private void saveAudioAndTextToFile(String text, File cacheFile, byte[] audio, String voiceName)
            throws IOException {
        logger.debug("Caching audio file {}", cacheFile.getName());
        try (FileOutputStream audioFileOutputStream = new FileOutputStream(cacheFile)) {
            audioFileOutputStream.write(audio);
        }

        // write text to file for transparency too
        // this allows to know which contents is in which audio file
        String textFileName = FilenameUtils.removeExtension(cacheFile.getName()) + ".txt";
        logger.debug("Caching text file {}", textFileName);
        try (FileOutputStream textFileOutputStream = new FileOutputStream(new File(cacheFolder, textFileName))) {
            // @formatter:off
            StringBuilder sb = new StringBuilder("Config: ")
                    .append(config.toConfigString())
                    .append(",voice=")
                    .append(voiceName)
                    .append(System.lineSeparator())
                    .append("Text: ")
                    .append(text)
                    .append(System.lineSeparator());
            // @formatter:on
            textFileOutputStream.write(sb.toString().getBytes(UTF_8));
        }
    }

    /**
     * Call Google service to synthesize the required text
     *
     * @param text Text to synthesize
     * @param voice Voice parameter
     * @param audioFormat Audio encoding format
     * @return Audio input stream or {@code null} when encoding exceptions occur
     */
    @SuppressWarnings({ "unused", "null" })
    private byte[] synthesizeSpeechByGoogle(String text, GoogleTTSVoice voice, String audioFormat) throws IOException {
        AudioConfig audioConfig = new AudioConfig(AudioEncoding.valueOf(audioFormat), config.getPitch(),
                config.getSpeakingRate(), config.getVolumeGainDb());
        SynthesisInput synthesisInput = new SynthesisInput(text);
        VoiceSelectionParams voiceSelectionParams = new VoiceSelectionParams(voice.getLocale().getLanguage(),
                voice.getLabel(), SsmlVoiceGender.valueOf(voice.getSsmlGender()));

        SynthesizeSpeechRequest request = new SynthesizeSpeechRequest(audioConfig, synthesisInput,
                voiceSelectionParams);

        HttpRequestBuilder builder = HttpRequestBuilder.postTo(SYTNHESIZE_SPEECH_URL)
                .withHeader(AUTH_HEADER_NAME, getAuthorization()).withContent(gson.toJson(request), APPLICATION_JSON);

        SynthesizeSpeechResponse synthesizeSpeechResponse = gson.fromJson(builder.getContentAsString(),
                SynthesizeSpeechResponse.class);

        if (synthesizeSpeechResponse == null) {
            return null;
        }

        byte[] encodedBytes = synthesizeSpeechResponse.getAudioContent().getBytes(StandardCharsets.UTF_8);
        return Base64.getDecoder().decode(encodedBytes);
    }

    /**
     * Gets a unique filename for a give text, by creating a MD5 hash of it. It
     * will be preceded by the locale.
     * <p>
     * Sample: "en-US_00a2653ac5f77063bc4ea2fee87318d3"
     */
    private String getUniqueFilenameForText(String text, String voiceName) {
        try {
            byte[] bytesOfMessage = (config.toConfigString() + text).getBytes(UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md.digest(bytesOfMessage);
            BigInteger bigInt = new BigInteger(1, md5Hash);
            StringBuilder hashText = new StringBuilder(bigInt.toString(16));
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return voiceName + "_" + hashText;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            // should not happen
            logger.error("Could not create MD5 hash for '{}'", text, ex);
            return null;
        }
    }

    boolean isInitialized() {
        return initialized;
    }
}
