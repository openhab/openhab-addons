/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.voice.pipertts.internal;

import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_CATEGORY;
import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_ID;
import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_NAME;
import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_PID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.voice.AbstractCachedTTSService;
import org.openhab.core.voice.TTSCache;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.givimad.piperjni.PiperJNI;
import io.github.givimad.piperjni.PiperVoice;

/**
 * The {@link PiperTTSService} class is a service implementation to use Piper for Text-to-Speech.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(service = TTSService.class, configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "="
        + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Text-to-Speech", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class PiperTTSService extends AbstractCachedTTSService {
    // piper-jni version from pom.xml
    private static final String PIPER_VERSION = "1.2.0-a0f09cd";
    private static final Path PIPER_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "piper");
    private static final Path LIB_FOLDER = PIPER_FOLDER.resolve("lib-" + PIPER_VERSION);
    private static final Path JAR_FILE = PIPER_FOLDER.resolve("piper-jni-" + PIPER_VERSION + ".jar");
    private static final String JAR_URL = "https://repo1.maven.org/maven2/io/github/givimad/piper-jni/" + PIPER_VERSION
            + "/piper-jni-" + PIPER_VERSION + ".jar";
    private final Logger logger = LoggerFactory.getLogger(PiperTTSService.class);
    private final Object modelLock = new Object();
    private final ExecutorService executor = ThreadPoolManager.getPool("voice-pipertts");
    private PiperTTSConfiguration config = new PiperTTSConfiguration();
    private Map<String, List<Voice>> cachedVoicesByModel = new HashMap<>();
    private boolean ready = false;
    private @Nullable VoiceModel preloadedModel;
    private @Nullable PiperJNI piper;
    private @Nullable Future<?> activateTask;
    static {
        System.setProperty("io.github.givimad.piperjni.libdir", LIB_FOLDER.toAbsolutePath().toString());
    }

    @Activate
    public PiperTTSService(final @Reference TTSCache ttsCache) {
        super(ttsCache);
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        tryCreatePiperDirectory();
        activateTask = executor.submit(() -> {
            try {
                setupNativeDependencies();
                piper = new PiperJNI();
                piper.initialize(true, false);
                logger.debug("Using Piper version {}", piper.getPiperVersion());
                ready = true;
            } catch (IOException e) {
                logger.warn("Piper registration failed, the add-on will not work: {}", e.getMessage());
            }
        });
        configChange(config);
    }

    @Deactivate
    private void deactivate() {
        if (activateTask != null && !activateTask.isDone()) {
            activateTask.cancel(true);
        }
    }

    private void setupNativeDependencies() throws IOException {
        String folderName = "";
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        if (osName.contains("win")) {
            if (osArch.contains("amd64") || osArch.contains("x86_64")) {
                folderName = "win-amd64";
            }
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            if (osArch.contains("amd64") || osArch.contains("x86_64")) {
                folderName = "debian-amd64";
            } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
                folderName = "debian-arm64";
            } else if (osArch.contains("armv7") || osArch.contains("arm")) {
                folderName = "debian-armv7l";
            }
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            if (osArch.contains("amd64") || osArch.contains("x86_64")) {
                folderName = "macos-amd64";
            } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
                folderName = "macos-arm64";
            }
        }
        if (folderName.isBlank()) {
            throw new IOException("Incompatible platform, unable to setup add-on");
        }
        if (!Files.exists(LIB_FOLDER)) {
            Files.createDirectory(LIB_FOLDER);
        }
        if (!Files.exists(JAR_FILE)) {
            logger.debug("Downloading file: {}", JAR_URL);
            InputStream in = new URL(JAR_URL).openStream();
            Files.copy(in, JAR_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(JAR_FILE.toFile())) {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                java.util.jar.JarEntry file = enumEntries.nextElement();
                String filename = file.getName();
                if (!filename.startsWith(folderName) && !"espeak-ng-data.zip".equals(filename)
                        && !"libtashkeel_model.ort".equals(filename)) {
                    continue;
                }
                Path targetPath = LIB_FOLDER.resolve(file.getName());
                if (Files.exists(targetPath)) {
                    logger.debug("Found piper native dependency: {}", file.getName());
                    continue;
                }
                if (file.isDirectory()) {
                    logger.debug("Creating dir: {}", targetPath);
                    Files.createDirectory(targetPath);
                    continue;
                }
                logger.debug("Extracting piper native dependency: {}", file.getName());
                try (var is = jar.getInputStream(file)) {
                    Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        configChange(config);
    }

    @Deactivate
    protected void deactivate(Map<String, Object> config) {
        try {
            unloadModel();
            getPiper().close();
            piper = null;
        } catch (IOException e) {
            logger.warn("Exception unloading model: {}", e.getMessage());
        } catch (LibraryNotLoaded ignored) {
        }
    }

    private void configChange(Map<String, Object> config) {
        this.config = new Configuration(config).as(PiperTTSConfiguration.class);
        try {
            unloadModel();
        } catch (IOException e) {
            logger.warn("IOException unloading model: {}", e.getMessage());
        }
    }

    private PiperJNI getPiper() throws LibraryNotLoaded {
        PiperJNI piper = this.piper;
        if (piper == null) {
            throw new LibraryNotLoaded();
        }
        return piper;
    }

    private void tryCreatePiperDirectory() {
        if (!Files.exists(PIPER_FOLDER)) {
            try {
                Files.createDirectory(PIPER_FOLDER);
                logger.info("Piper directory created at: {}", PIPER_FOLDER);
            } catch (IOException e) {
                logger.warn("Unable to create piper directory at {}", PIPER_FOLDER);
            }
        }
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        try (var filesStream = Files.list(PIPER_FOLDER)) {
            HashMap<String, List<Voice>> newCachedVoices = new HashMap<>();
            Set<Voice> voices = filesStream //
                    .filter(filePath -> filePath.getFileName().toString().endsWith(".onnx")) //
                    .map(filePath -> {
                        List<Voice> modelVoices = getVoice(filePath);
                        newCachedVoices.put(filePath.toString(), modelVoices);
                        return modelVoices;
                    }) //
                    .flatMap(List::stream) //
                    .collect(Collectors.toSet());
            cachedVoicesByModel = newCachedVoices;
            logger.debug("Available number of piper voices: {}", voices.size());
            return voices;
        } catch (IOException e) {
            logger.warn("IOException getting piper voices: {}", e.getMessage());
        }
        return Set.of();
    }

    private List<Voice> getVoice(Path modelPath) {
        try {
            Path configFile = modelPath.getParent().resolve(modelPath.getFileName() + ".json");
            if (!Files.exists(configFile) || Files.isDirectory(configFile)) {
                throw new IOException("Missed config file: " + configFile.toAbsolutePath());
            }
            List<Voice> cachedVoices = cachedVoicesByModel.get(modelPath.toString());
            if (cachedVoices != null) {
                return cachedVoices;
            }
            String voiceData = Files.readString(configFile);
            JsonNode voiceJsonRoot = new ObjectMapper().readTree(voiceData);
            JsonNode datasetJsonNode = voiceJsonRoot.get("dataset");
            JsonNode languageJsonNode = voiceJsonRoot.get("language");
            JsonNode numSpeakersJsonNode = voiceJsonRoot.get("num_speakers");
            if (datasetJsonNode == null || languageJsonNode == null) {
                throw new IOException("Unknown voice config structure");
            }
            JsonNode languageFamilyJsonNode = languageJsonNode.get("family");
            JsonNode languageRegionJsonNode = languageJsonNode.get("region");
            if (languageFamilyJsonNode == null || languageRegionJsonNode == null) {
                throw new IOException("Unknown voice config structure");
            }
            String voiceName = datasetJsonNode.textValue();
            String voiceUID = voiceName.replace(" ", "_");
            String languageFamily = languageFamilyJsonNode.textValue();
            String languageRegion = languageRegionJsonNode.textValue();
            int numSpeakers = numSpeakersJsonNode != null ? numSpeakersJsonNode.intValue() : 1;
            JsonNode speakersIdsJsonNode = voiceJsonRoot.get("speaker_id_map");
            if (numSpeakers != 1 && speakersIdsJsonNode != null) {
                List<Voice> voices = new ArrayList<>();
                speakersIdsJsonNode.fieldNames().forEachRemaining(field -> {
                    JsonNode fieldNode = speakersIdsJsonNode.get(field);
                    voices.add(new PiperTTSVoice( //
                            voiceUID + "_" + field, //
                            capitalize(voiceName + " " + field), //
                            languageFamily, //
                            languageRegion, //
                            modelPath, //
                            configFile, //
                            Optional.of(fieldNode.longValue())));
                });
                return voices;
            }
            return List.of(new PiperTTSVoice(voiceUID, capitalize(voiceName), languageFamily, languageRegion, modelPath,
                    configFile, Optional.empty()));
        } catch (IOException e) {
            logger.warn("IOException reading voice info: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, null, null, null,
                null));
    }

    @Override
    public AudioStream synthesizeForCache(String text, Voice voice, AudioFormat audioFormat) throws TTSException {
        if (!ready) {
            throw new TTSException("Add-on is not loaded");
        }
        if (!(voice instanceof PiperTTSVoice ttsVoice)) {
            throw new TTSException("No piper voice provided");
        }
        VoiceModel voiceModel = null;
        boolean usingPreloadedModel = false;
        short[] buffer;
        final VoiceModel preloadedModel = this.preloadedModel;
        try {
            try {
                if (preloadedModel != null && preloadedModel.ttsVoice.getUID().equals(ttsVoice.getUID())) {
                    logger.debug("Using preloaded voice model");
                    preloadedModel.consumers.incrementAndGet();
                    voiceModel = preloadedModel;
                    usingPreloadedModel = true;
                } else {
                    unloadModel();
                    logger.debug("Loading voice model...");
                    voiceModel = loadModel(ttsVoice);
                    synchronized (modelLock) {
                        usingPreloadedModel = voiceModel.equals(this.preloadedModel);
                    }
                }
            } catch (IOException e) {
                throw new TTSException("Unable to load voice model: " + e.getMessage());
            }
            try {
                logger.debug("Generating audio for: '{}'", text);
                buffer = getPiper().textToAudio(voiceModel.piperVoice, text);
                logger.debug("Generated {} samples of audio", buffer.length);
            } catch (IOException e) {
                throw new TTSException("Voice generation failed: " + e.getMessage());
            }
        } catch (PiperJNI.NotInitialized | LibraryNotLoaded e) {
            throw new TTSException("Piper not initialized, try restarting the add-on.");
        } catch (RuntimeException e) {
            logger.warn("RuntimeException running text to audio: {}", e.getMessage());
            throw new TTSException("There was an error running Piper");
        } finally {
            if (voiceModel != null) {
                if (!usingPreloadedModel
                        || voiceModel.consumers.decrementAndGet() == 0 && !voiceModel.equals(this.preloadedModel)) {
                    logger.debug("Unloading voice model");
                    voiceModel.close();
                } else {
                    logger.debug("Skipping voice model unload");
                }
            }
        }
        try {
            logger.debug("Return re-encoded audio stream");
            return getAudioStream(buffer, voiceModel.sampleRate, audioFormat);
        } catch (IOException e) {
            throw new TTSException("Error while creating audio stream: " + e.getMessage());
        }
    }

    private VoiceModel loadModel(PiperTTSVoice voice) throws IOException, PiperJNI.NotInitialized, LibraryNotLoaded {
        if (!Files.exists(voice.voiceModelPath()) || !Files.exists(voice.voiceModelConfigPath())) {
            throw new IOException("Missing voice files");
        }
        PiperJNI piper = getPiper();
        PiperVoice piperVoice;
        VoiceModel voiceModel;
        piperVoice = piper.loadVoice(voice.voiceModelPath(), voice.voiceModelConfigPath(), voice.speakerId.orElse(-1L));
        voiceModel = new VoiceModel(voice, piperVoice, piperVoice.getSampleRate(), new AtomicInteger(1), logger);
        if (config.preloadModel) {
            synchronized (modelLock) {
                if (preloadedModel == null) {
                    logger.debug("Voice model will be kept preloaded");
                    preloadedModel = voiceModel;
                } else {
                    logger.debug("Another voice model already preloaded");
                }
            }
        }
        return voiceModel;
    }

    private void unloadModel() throws IOException {
        var model = preloadedModel;
        if (model != null) {
            synchronized (modelLock) {
                preloadedModel = null;
                if (model.consumers.get() == 0) {
                    // Do not release the model memory if it's been used, it should be released by the consumer
                    // when there is no other consumers and is not a ref of the preloaded model object.
                    logger.debug("Unloading preloaded model");
                    model.close();
                } else {
                    logger.debug("Preloaded model in use, skip memory release");
                }
            }
        }
    }

    private ByteArrayAudioStream getAudioStream(short[] samples, long sampleRate, AudioFormat targetFormat)
            throws IOException {
        // Convert the i16 samples returned by piper to a byte buffer
        ByteBuffer byteBuffer;
        int numSamples = samples.length;
        byteBuffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (var sample : samples) {
            byteBuffer.putShort(sample);
        }
        // Initialize a Java audio stream using the Piper output format with the byte buffer created.
        byte[] bytes = byteBuffer.array();
        javax.sound.sampled.AudioFormat jAudioFormat = new javax.sound.sampled.AudioFormat(sampleRate, 16, 1, true,
                false);
        long audioLength = (long) Math.ceil(((double) bytes.length) / jAudioFormat.getFrameSize());
        AudioInputStream audioInputStreamTemp = new AudioInputStream(new ByteArrayInputStream(bytes), jAudioFormat,
                audioLength);
        // Move the audio data to another Java audio stream in the target format so the Java AudioSystem encoded it as
        // needed.
        javax.sound.sampled.AudioFormat jTargetFormat = new javax.sound.sampled.AudioFormat(
                Objects.requireNonNull(targetFormat.getFrequency()), Objects.requireNonNull(targetFormat.getBitDepth()),
                Objects.requireNonNull(targetFormat.getChannels()), true, false);
        AudioInputStream convertedInputStream = AudioSystem.getAudioInputStream(jTargetFormat, audioInputStreamTemp);
        // It's required to add the wav header to the byte array stream returned for it to work with all the sink
        // implementations.
        // It can not be done with the AudioInputStream returned by AudioSystem::getAudioInputStream because it missed
        // the length property.
        // Therefore, the following method creates another AudioInputStream instance and uses the Java AudioSystem to
        // prepend
        // the wav header bytes,
        // and finally initializes an OpenHAB audio stream.
        return getAudioStreamWithRIFFHeader(convertedInputStream.readAllBytes(), jTargetFormat, targetFormat);
    }

    private String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private ByteArrayAudioStream getAudioStreamWithRIFFHeader(byte[] audioBytes,
            javax.sound.sampled.AudioFormat jAudioFormat, AudioFormat audioFormat) throws IOException {
        AudioInputStream audioInputStreamTemp = new AudioInputStream(new ByteArrayInputStream(audioBytes), jAudioFormat,
                (long) Math.ceil(((double) audioBytes.length) / jAudioFormat.getFrameSize()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AudioSystem.write(audioInputStreamTemp, AudioFileFormat.Type.WAVE, outputStream);
        return new ByteArrayAudioStream(outputStream.toByteArray(), audioFormat);
    }

    private record PiperTTSVoice(String voiceId, String voiceName, String languageFamily, String languageRegion,
            Path voiceModelPath, Path voiceModelConfigPath, Optional<Long> speakerId) implements Voice {
        @Override
        public String getUID() {
            // Voice uid should be prefixed by service id to be listed properly on the UI.
            return SERVICE_ID + ":" + voiceId + "-" + languageFamily + "_" + languageRegion;
        }

        @Override
        public String getLabel() {
            return voiceName;
        }

        @Override
        public Locale getLocale() {
            return new Locale(languageFamily, languageRegion);
        }
    }

    private static class LibraryNotLoaded extends Exception {
        private LibraryNotLoaded() {
            super("Library not loaded");
        }
    }

    private record VoiceModel(PiperTTSVoice ttsVoice, PiperVoice piperVoice, int sampleRate, AtomicInteger consumers,
            Logger logger) implements AutoCloseable {

        @Override
        public void close() {
            piperVoice.close();
        }
    }
}
