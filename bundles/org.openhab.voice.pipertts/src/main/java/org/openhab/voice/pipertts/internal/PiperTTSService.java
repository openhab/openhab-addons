/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.net.URI;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import org.openhab.core.voice.tts.AbstractCachedTTSService;
import org.openhab.core.voice.tts.TTSCache;
import org.openhab.core.voice.tts.TTSException;
import org.openhab.core.voice.tts.TTSService;
import org.openhab.core.voice.tts.Voice;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jvoiceproject.piperjni.PiperJNI;
import io.github.jvoiceproject.piperjni.PiperVoice;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@link PiperTTSService} class is a service implementation to use Piper for Text-to-Speech.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Florian Hotze - Add support for raw PCM audio format
 */
@NonNullByDefault
@Component(service = TTSService.class, configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "="
        + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Text-to-Speech", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class PiperTTSService extends AbstractCachedTTSService {
    // piper-jni version from pom.xml
    private static final String PIPER_VERSION = "1.8.0.1";
    private static final Path PIPER_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "piper");
    private static final Path LIB_FOLDER = PIPER_FOLDER.resolve("lib-" + PIPER_VERSION);
    private static final Path ESPEAK_DATA_FOLDER = LIB_FOLDER.resolve("espeak-ng-data");
    private static final Path JAR_FILE = PIPER_FOLDER.resolve("piper-jni-" + PIPER_VERSION + ".jar");
    private static final String JAR_URL = "https://openhab.jfrog.io/artifactory/libs-online-deps/io/github/jvoice-project/piper-jni/"
            + PIPER_VERSION + "/piper-jni-" + PIPER_VERSION + ".jar";
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().configureForJackson2().build();

    private final Logger logger = LoggerFactory.getLogger(PiperTTSService.class);
    private final Object modelLock = new Object();
    private final ExecutorService executor = ThreadPoolManager.getPool("voice-pipertts");

    private PiperTTSConfiguration config = new PiperTTSConfiguration();
    private volatile boolean ready = false;
    private final Future<?> activateTask;

    private Map<String, List<Voice>> cachedVoicesByModel = new HashMap<>();
    private @Nullable VoiceModel preloadedModel;

    @Activate
    public PiperTTSService(final @Reference TTSCache ttsCache, Map<String, Object> config) {
        super(ttsCache);

        tryCreatePiperDirectory();
        activateTask = executor.submit(() -> {
            try {
                logger.debug("Setting Piper native library path to: {}", LIB_FOLDER);
                System.setProperty("piper.library.path", LIB_FOLDER.toAbsolutePath().toString());
                logger.debug("Setting Piper eSpeak NG data path to: {}", ESPEAK_DATA_FOLDER);
                System.setProperty("piper.espeak.data.path", ESPEAK_DATA_FOLDER.toAbsolutePath().toString());
                logger.debug("Setting up Piper native dependencies...");
                setupNativeDependencies();
                logger.debug("Initializing Piper...");
                PiperJNI.loadLibrary();
                logger.debug("Using Piper version {}", PiperJNI.getPiperVersion());
                ready = true;
            } catch (Throwable t) {
                logger.error("Piper registration failed, the add-on will not work: {}", t.getMessage(), t);
            }
        });
        configChange(config);
    }

    @Deactivate
    private void deactivate() {
        Future<?> activateTask = this.activateTask;
        if (!activateTask.isDone()) {
            activateTask.cancel(true);
        }
    }

    private void setupNativeDependencies() throws IOException {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String osArch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        String os;
        if (osName.contains("win")) {
            os = "windows";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            os = "linux";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            os = "macos";
        } else {
            throw new IOException("Incompatible platform, unable to setup add-on");
        }
        String arch;
        if (osArch.contains("amd64") || osArch.contains("x86_64") || osArch.contains("x86-64")) {
            arch = "x86-64";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            arch = "aarch64";
        } else {
            throw new IOException("Incompatible platform, unable to setup add-on");
        }
        String nativePrefix = "native/" + os + "-" + arch + "/";

        if (!Files.exists(LIB_FOLDER)) {
            Files.createDirectory(LIB_FOLDER);
        }
        if (!Files.exists(JAR_FILE)) {
            logger.debug("Downloading file: {}", JAR_URL);
            try (InputStream in = URI.create(JAR_URL).toURL().openStream()) {
                Files.copy(in, JAR_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Path normalizedLibFolder = LIB_FOLDER.toAbsolutePath().normalize();
        try (JarFile jar = new JarFile(JAR_FILE.toFile())) {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = enumEntries.nextElement();
                String filename = file.getName();
                if ("espeak-ng-data.zip".equals(filename)) {
                    if (Files.isDirectory(ESPEAK_DATA_FOLDER)) {
                        logger.debug("Found piper eSpeak NG data: {}", ESPEAK_DATA_FOLDER);
                    } else {
                        logger.debug("Extracting piper eSpeak NG data to: {}", ESPEAK_DATA_FOLDER);
                        try (ZipInputStream zis = new ZipInputStream(jar.getInputStream(file))) {
                            extractZipStream(zis, ESPEAK_DATA_FOLDER);
                        }
                    }
                    continue;
                }
                if (!filename.startsWith(nativePrefix) || file.isDirectory()) {
                    continue;
                }
                String relativeName = filename.substring(nativePrefix.length());
                if (relativeName.isEmpty()) {
                    continue;
                }
                Path targetPath = normalizedLibFolder.resolve(relativeName).normalize();
                if (!targetPath.startsWith(normalizedLibFolder)) {
                    throw new IOException("Bad jar entry: " + filename);
                }
                if (Files.exists(targetPath)) {
                    logger.debug("Found piper native dependency: {}", relativeName);
                    continue;
                }
                logger.debug("Extracting piper native dependency: {}", relativeName);
                Files.createDirectories(targetPath.getParent());
                try (var is = jar.getInputStream(file)) {
                    Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void extractZipStream(ZipInputStream zis, Path destDir) throws IOException {
        Files.createDirectories(destDir);
        Path normalizedDest = destDir.toAbsolutePath().normalize();
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            Path entryDest = normalizedDest.resolve(entry.getName()).normalize();
            if (!entryDest.startsWith(normalizedDest)) {
                throw new IOException("Bad zip entry: " + entry.getName());
            }
            if (entry.isDirectory()) {
                Files.createDirectories(entryDest);
            } else {
                Files.createDirectories(entryDest.getParent());
                Files.copy(zis, entryDest, StandardCopyOption.REPLACE_EXISTING);
            }
            zis.closeEntry();
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
        } catch (IOException e) {
            logger.warn("Exception unloading model: {}", e.getMessage());
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
            PiperVoiceConfig voiceConfig = OBJECT_MAPPER.readValue(voiceData, PiperVoiceConfig.class);

            String voiceName = voiceConfig.dataset;
            String voiceUID = voiceName.replace(" ", "_");
            String quality = voiceConfig.audio.quality;
            String languageFamily = voiceConfig.language.family;
            String languageRegion = voiceConfig.language.region;
            int numSpeakers = voiceConfig.numSpeakers;
            Map<String, Integer> speakerIdMap = voiceConfig.speakerIdMap;

            if (voiceName.isBlank() || quality.isBlank() || languageFamily.isBlank() || languageRegion.isBlank()) {
                throw new IOException("Unknown voice config structure");
            }

            if (numSpeakers != 1 && speakerIdMap != null) {
                List<Voice> voices = new ArrayList<>();
                speakerIdMap.forEach((field, id) -> {
                    voices.add(new PiperTTSVoice( //
                            voiceUID + "_" + field, //
                            capitalize(voiceName + " " + field), //
                            quality, //
                            languageFamily, //
                            languageRegion, //
                            modelPath, //
                            configFile, //
                            Optional.of(id)));
                });
                return voices;
            }
            return List.of(new PiperTTSVoice(voiceUID, capitalize(voiceName), quality, languageFamily, languageRegion,
                    modelPath, configFile, Optional.empty()));
        } catch (IOException | JacksonException e) {
            logger.warn("Exception reading voice info: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(
                new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, null, null, null,
                        null),
                new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, null, null, null,
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
        if (!isSupportedFormat(audioFormat)) {
            throw new TTSException("The requested AudioFormat is unsupported: " + audioFormat);
        }
        VoiceModel voiceModel = null;
        boolean usingPreloadedModel = false;
        short[] buffer;
        int sampleRate;
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
                buffer = voiceModel.piperVoice.textToAudio(text);
                sampleRate = voiceModel.sampleRate;
                logger.debug("Generated {} samples of audio", buffer.length);
            } catch (IOException e) {
                throw new TTSException("Voice generation failed: " + e.getMessage());
            }
        } catch (IllegalStateException e) {
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
            return getAudioStream(buffer, sampleRate, audioFormat);
        } catch (IOException e) {
            throw new TTSException("Error while creating audio stream: " + e.getMessage());
        }
    }

    private VoiceModel loadModel(PiperTTSVoice voice) throws IllegalStateException, IOException {
        if (!Files.exists(voice.voiceModelPath()) || !Files.exists(voice.voiceModelConfigPath())) {
            throw new IOException("Missing voice files");
        }
        PiperVoice piperVoice;
        VoiceModel voiceModel;
        piperVoice = PiperJNI.loadVoice(voice.voiceModelPath(), voice.voiceModelConfigPath(),
                voice.speakerId.orElse(-1));
        voiceModel = new VoiceModel(voice, piperVoice, piperVoice.getSampleRate(), new AtomicInteger(1));
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
        // Convert the 16-bit signed PCM samples from Piper JNI to a little-endian byte array
        ByteBuffer byteBuffer = ByteBuffer.allocate(samples.length * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asShortBuffer().put(samples);
        byte[] bytes = byteBuffer.array();

        // Resolve target audio parameters, falling back to Piper voice model defaults (native rate, 16-bit, mono).
        Long formatFrequency = targetFormat.getFrequency();
        long targetFrequency = formatFrequency != null ? formatFrequency : sampleRate;
        Integer formatBitDepth = targetFormat.getBitDepth();
        int targetBitDepth = formatBitDepth != null ? formatBitDepth : 16;
        Integer formatChannels = targetFormat.getChannels();
        int targetChannels = formatChannels != null ? formatChannels : 1;

        // Piper's native output format
        javax.sound.sampled.AudioFormat jAudioFormat = new javax.sound.sampled.AudioFormat(sampleRate, 16, 1, true,
                false);
        // Requested output format
        javax.sound.sampled.AudioFormat jTargetFormat = new javax.sound.sampled.AudioFormat(targetFrequency,
                targetBitDepth, targetChannels, true, false);

        // Resample/convert via Java AudioSystem only when the target format differs from Piper's native output
        byte[] audioBytes;
        if (jAudioFormat.matches(jTargetFormat)) {
            audioBytes = bytes;
        } else {
            AudioInputStream sourceStream = new AudioInputStream(new ByteArrayInputStream(bytes), jAudioFormat,
                    samples.length);
            audioBytes = AudioSystem.getAudioInputStream(jTargetFormat, sourceStream).readAllBytes();
        }

        AudioFormat streamFormat = new AudioFormat(targetFormat.getContainer(), targetFormat.getCodec(), false,
                targetBitDepth, targetFormat.getBitRate(), targetFrequency, targetChannels);

        // Prepend a RIFF/WAVE header if requested, otherwise return raw PCM
        if (AudioFormat.CONTAINER_WAVE.equals(targetFormat.getContainer())) {
            return getAudioStreamWithRIFFHeader(audioBytes, jTargetFormat, streamFormat);
        }
        return new ByteArrayAudioStream(audioBytes, streamFormat);
    }

    private boolean isSupportedFormat(AudioFormat audioFormat) {
        return (AudioFormat.CONTAINER_WAVE.equals(audioFormat.getContainer())
                || AudioFormat.CONTAINER_NONE.equals(audioFormat.getContainer()))
                && AudioFormat.CODEC_PCM_SIGNED.equals(audioFormat.getCodec())
                && !Boolean.TRUE.equals(audioFormat.isBigEndian());
    }

    private String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private ByteArrayAudioStream getAudioStreamWithRIFFHeader(byte[] audioBytes,
            javax.sound.sampled.AudioFormat jAudioFormat, AudioFormat audioFormat) throws IOException {
        AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioBytes), jAudioFormat,
                (long) Math.ceil((double) audioBytes.length / jAudioFormat.getFrameSize()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputStream);
        return new ByteArrayAudioStream(outputStream.toByteArray(), audioFormat);
    }

    private record PiperTTSVoice(String voiceId, String voiceName, String quality, String languageFamily,
            String languageRegion, Path voiceModelPath, Path voiceModelConfigPath,
            Optional<Integer> speakerId) implements Voice {
        @Override
        public String getUID() {
            // Voice uid should be prefixed by service id to be listed properly on the UI.
            return SERVICE_ID + ":" + voiceId + "-" + quality + "-" + languageFamily + "_" + languageRegion;
        }

        @Override
        public String getLabel() {
            return voiceName + " (" + quality + ")";
        }

        @Override
        public Locale getLocale() {
            return Locale.of(languageFamily, languageRegion);
        }
    }

    private record VoiceModel(PiperTTSVoice ttsVoice, PiperVoice piperVoice, int sampleRate,
            AtomicInteger consumers) implements AutoCloseable {

        @Override
        public void close() {
            piperVoice.close();
        }
    }
}
