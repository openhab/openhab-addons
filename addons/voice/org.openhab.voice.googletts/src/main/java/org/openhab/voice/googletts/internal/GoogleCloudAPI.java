/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1beta1.*;
import com.google.protobuf.ByteString;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(GoogleCloudAPI.class);

    /**
     * Supported voices and locales
     */
    private final Map<Locale, Set<GoogleTTSVoice>> voices = new HashMap<>();

    /**
     * Google client
     */
    private TextToSpeechClient googleClient;

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
        if (checkArch()) {
            String serviceAccountKey = config.getServiceAccountKey();
            if (serviceAccountKey != null) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(serviceAccountKey.getBytes());
                    GoogleCredentials credential = GoogleCredentials.fromStream(bis);
                    FixedCredentialsProvider credentialProvider = FixedCredentialsProvider.create(credential);
                    TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialProvider)
                            .build();
                    googleClient = TextToSpeechClient.create(settings);
                    initialized = true;
                    initVoices();
                } catch (IOException e) {
                    logger.error("Error initializing the service", e);
                    initialized = false;
                }
            } else {
                googleClient = null;
                voices.clear();
            }
        }

        //maintain cache
        if (config.getPurgeCache() != null && config.getPurgeCache()) {
            File[] files = cacheFolder.listFiles();
            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(File::delete);
            }
            logger.debug("Cache purged.");
        }
    }

    private boolean checkArch() {
        PlatformUtil.Architecture architecture = PlatformUtil.checkArchitecture();

        if (PlatformUtil.Architecture.X86_64 != architecture) {
            logger.error("The architecture is not x86_64 but {}. Only x86_64 platforms are supported. ", architecture);
            return false;
        } else {
            logger.debug("openHAB is running on architecture {} - supported by Google Cloud TTS API", architecture);
            return true;
        }
    }

    /**
     * Loads supported audio formats
     *
     * @return Set of audio formats
     */
    Set<String> getSupportedAudioFormats() {
        Set<String> formats = new HashSet<>();
        AudioEncoding[] values = AudioEncoding.values();
        for (AudioEncoding c : values) {
            if (c != AudioEncoding.AUDIO_ENCODING_UNSPECIFIED &&
                    c != AudioEncoding.UNRECOGNIZED) {
                formats.add(c.toString());
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
        return Collections.unmodifiableSet(voices.keySet());
    }

    /**
     * Supported voices for locale.
     *
     * @param l Locale
     * @return Set of voices
     */
    Set<GoogleTTSVoice> getVoicesForLocale(Locale l) {
        Set<GoogleTTSVoice> set = voices.get(l);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    /**
     * Google API call to load locales and voices.
     */
    private void initVoices() {
        if (googleClient != null) {
            voices.clear();
            ListVoicesResponse resp = googleClient.listVoices(""); //without language code to get all the voices
            for (Voice v : resp.getVoicesList()) {
                for (int i = 0; i < v.getLanguageCodesCount(); i++) {
                    String languageCode = v.getLanguageCodes(i);
                    Locale l = Locale.forLanguageTag(languageCode);
                    Set<GoogleTTSVoice> localVoices;
                    if (!voices.containsKey(l)) {
                        localVoices = new HashSet<>();
                        voices.put(l, localVoices);
                    } else {
                        localVoices = voices.get(l);
                    }
                    localVoices.add(new GoogleTTSVoice(l, v.getName(), v.getSsmlGenderValue()));
                }
            }
        } else {
            logger.error("Google client is not initialized!");
        }
    }

    /**
     * Converts openHAB audio format to Google parameters.
     *
     * @param codec Requested codec
     * @return String array of Google audio format and the file extension to use.
     */
    private String[] getFormatForCodec(String codec) {
        switch (codec) {
            case AudioFormat.CODEC_MP3:
                return new String[]{AudioEncoding.MP3.toString(), "mp3"};
            case AudioFormat.CODEC_PCM_SIGNED:
                return new String[]{AudioEncoding.LINEAR16.toString(), "wav"};
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
            saveAudioAndTextToFile(text, audioFileInCache, audio, voice.getTechnicalName());
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
    private void saveAudioAndTextToFile(String text, File cacheFile, byte[] audio, String voiceName) throws IOException {
        FileOutputStream fos = new FileOutputStream(cacheFile);
        fos.write(audio);
        fos.close();
        logger.debug("Caching audio file {}", cacheFile.getName());

        // write text to file for transparency too
        // this allows to know which contents is in which audio file
        String txtFileName = FilenameUtils.removeExtension(cacheFile.getName()) + ".txt";
        FileOutputStream txtFos = new FileOutputStream(new File(cacheFolder, txtFileName));
        StringBuilder sb = new StringBuilder("Config: ").append(config.toConfigString())
                .append(",voice=").append(voiceName).append("\n")
                .append("Text: ").append(text).append("\n");
        txtFos.write(sb.toString().getBytes(UTF_8));
        txtFos.close();
        logger.debug("Caching text file {}", txtFileName);
    }

    /**
     * Call Google service to synthesize the required text
     *
     * @param text Text to synthesise
     * @param voice Voice parameter
     * @return Audio input stream
     */
    private byte[] synthesizeSpeechByGoogle(String text, GoogleTTSVoice voice, String audioFormat) {
        // Set the text input to be synthesized
        SynthesisInput.Builder builder = SynthesisInput.newBuilder();
        if (text.startsWith("<speak>")) {
            builder.setSsml(text);
        } else {
            builder.setText(text);
        }
        SynthesisInput input = builder.build();

        // Build the voice request
        VoiceSelectionParams voiceParam = VoiceSelectionParams.newBuilder()
                .setLanguageCode(voice.getLocale().getLanguage())
                .setName(voice.getLabel())
                .setSsmlGender(SsmlVoiceGender.forNumber(voice.getSsmlGender()))
                .build();

        // Select the type of audio file you want returned
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.valueOf(audioFormat)) // MP3 or LINEAR16 audio.
                .setPitch(config.getPitch())
                .setSpeakingRate(config.getSpeakingRate())
                .setVolumeGainDb(config.getVolumeGainDb())
                .build();

        // Perform the text-to-speech request
        SynthesizeSpeechResponse response = googleClient.synthesizeSpeech(input, voiceParam,
                audioConfig);

        // Get the audio contents from the response
        ByteString audioContents = response.getAudioContent();

        return audioContents.toByteArray();
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
            // Now we need to zero pad it if you actually want the full 32
            // chars.
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
