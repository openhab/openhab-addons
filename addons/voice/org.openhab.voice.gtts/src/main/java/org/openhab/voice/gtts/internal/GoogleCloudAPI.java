/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.gtts.internal;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1beta1.*;
import com.google.protobuf.ByteString;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
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
     * Home folder.
     */
    private File homeFolder;

    /**
     * Status flag
     */
    private boolean initialized;

    /**
     * Constructor.
     *
     * @param homeFolder  Service home folder.
     * @param cacheFolder Service cache folder
     */
    GoogleCloudAPI(File homeFolder, File cacheFolder) {
        this.homeFolder = homeFolder;
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
            if (config.getServiceKeyFileName() != null) {
                File keyFile;
                if (config.getServiceKeyFileName().contains(File.separator)) {
                    keyFile = new File(config.getServiceKeyFileName());
                } else {

                    keyFile = new File(homeFolder, config.getServiceKeyFileName());
                }
                logger.debug("Loading service key file from {}", keyFile.getAbsoluteFile());
                try {
                    GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(keyFile));
                    FixedCredentialsProvider credentialProvider = FixedCredentialsProvider.create(credential);
                    TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialProvider)
                            .build();
                    googleClient = TextToSpeechClient.create(settings);
                    initialized = true;
                    initVoices();
                } catch (Exception e) {
                    logger.error("Error initializing the service using {}", keyFile.getAbsoluteFile(), e);
                    initialized = false;
                }
            } else {
                googleClient = null;
                voices.clear();
            }
        }
    }

    private boolean checkArch() {
        String arch = System.getProperty("os.arch");
        String normalizedArch = arch.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
        String commonArch;
        if (normalizedArch.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            commonArch = "x86_64";
        } else if (normalizedArch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            commonArch =  "x86_32";
        } else if (normalizedArch.matches("^(ia64|itanium64)$")) {
            commonArch =  "itanium_64";
        } else if (normalizedArch.matches("^(sparc|sparc32)$")) {
            commonArch =  "sparc_32";
        } else if (normalizedArch.matches("^(sparcv9|sparc64)$")) {
            commonArch =  "sparc_64";
        } else if (normalizedArch.matches("^(arm|arm32)$")) {
            commonArch =  "arm_32";
        } else if ("aarch64".equals(normalizedArch)) {
            commonArch =  "aarch_64";
        } else if (normalizedArch.matches("^(ppc|ppc32)$")) {
            commonArch =  "ppc_32";
        } else if ("ppc64".equals(normalizedArch)) {
            commonArch =  "ppc_64";
        } else if ("ppc64le".equals(normalizedArch)) {
            commonArch =  "ppcle_64";
        } else if ("s390".equals(normalizedArch)) {
            commonArch =  "s390_32";
        } else {
            commonArch =  "s390x".equals(normalizedArch) ? "s390_64" : "unknown";
        }

        if (!"x86_64".equals(commonArch)) {
            logger.error("The architecture underneath the OH is not x86_64 but {}. Only x86_64 platforms are supported. " +
                    "The os.arch value is {}.", commonArch, arch);
            return false;
        } else {
            logger.info("OH is running on architecture {} - supported by Google Cloud TTS API", commonArch);
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
        return set != null ? Collections.unmodifiableSet(set) : Collections.EMPTY_SET;
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

    File synthesizeSpeech(String text, GoogleTTSVoice voice, String codec) {
        String[] format = getFormatForCodec(codec);
        String fileNameInCache = getUniqueFilenameForText(text, voice.getLocale());
        // check if in cache
        File audioFileInCache = new File(cacheFolder, fileNameInCache + "." + format[1]);
        if (audioFileInCache.exists()) {
            logger.debug("Audio file {} was found in cache.", audioFileInCache.getName());
            return audioFileInCache;
        }

        // if not in cache, get audio data and put to cache
        try (InputStream is = synthesizeSpeechByGoogle(text, voice, format[0]);
             FileOutputStream fos = new FileOutputStream(audioFileInCache)) {
            logger.debug("Caching audio file {}", audioFileInCache.getName());
            copyStream(is, fos);
            // write text to file for transparency too
            // this allows to know which contents is in which audio file
            File txtFileInCache = new File(cacheFolder, fileNameInCache + ".txt");
            writeText(txtFileInCache, text);
            // return from cache
            return audioFileInCache;
        } catch (FileNotFoundException ex) {
            logger.warn("Could not write {} to cache, return null", audioFileInCache, ex);
            return null;
        } catch (IOException ex) {
            logger.error("Could not write {} to cache, return null", audioFileInCache, ex);
            return null;
        }
    }

    /**
     * Call Google service to synthesize the required text
     *
     * @param text  Text to synthesise
     * @param voice Voice parameter
     * @return Audio input stream
     */
    private InputStream synthesizeSpeechByGoogle(String text, GoogleTTSVoice voice, String audioFormat) {
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

        return new ByteArrayInputStream(audioContents.toByteArray());
    }

    /**
     * Gets a unique filename for a give text, by creating a MD5 hash of it. It
     * will be preceded by the locale.
     * <p>
     * Sample: "en-US_00a2653ac5f77063bc4ea2fee87318d3"
     */
    private String getUniqueFilenameForText(String text, Locale locale) {
        try {
            byte[] bytesOfMessage = text.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md.digest(bytesOfMessage);
            BigInteger bigInt = new BigInteger(1, md5Hash);
            StringBuilder hashText = new StringBuilder(bigInt.toString(16));
            // Now we need to zero pad it if you actually want the full 32
            // chars.
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return locale.getLanguage() + "_" + hashText;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            // should not happen
            logger.error("Could not create MD5 hash for '{}'", text, ex);
            return null;
        }
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[4096];
        int read = inputStream.read(bytes, 0, 4096);
        while (read > 0) {
            outputStream.write(bytes, 0, read);
            read = inputStream.read(bytes, 0, 4096);
        }
    }

    private void writeText(File file, String text) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(text.getBytes("UTF-8"));
        }
    }

    boolean isInitialized() {
        return initialized;
    }
}
