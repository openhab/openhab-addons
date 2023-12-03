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
package org.openhab.voice.pipertts.internal;

import org.openhab.core.voice.AbstractCachedTTSService;
import org.openhab.core.voice.TTSCache;
import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_CATEGORY;
import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_ID;
import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_NAME;
import static org.openhab.voice.pipertts.internal.PiperTTSConstants.SERVICE_PID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.Voice;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.givimad.piperjni.PiperConfig;
import io.github.givimad.piperjni.PiperJNI;
import io.github.givimad.piperjni.PiperVoice;

/**
 * The {@link PiperSTTService} class is a service implementation to use Piper for Text-to-Speech.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Text-to-Speech", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class PiperSTTService extends AbstractCachedTTSService {
    private static final Path PIPER_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "piper");
    private final Logger logger = LoggerFactory.getLogger(PiperSTTService.class);
    private PiperTTSConfiguration config = new PiperTTSConfiguration();
    private @Nullable VoiceModel preloadedModel;
    private @Nullable PiperJNI piper;
    private HashMap<String, List<Voice>> cachedVoicesByModel = new HashMap<>();

    public PiperSTTService(TTSCache ttsCache) {
        super(ttsCache);
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            this.piper = new PiperJNI();
            logger.debug("Piper library loaded");
            logger.info("Using piper version: {}", this.piper.getPiperVersion());
        } catch (Exception e) {
            logger.warn("Library registration failed, the add-on will not work");
        }
        tryCreatePiperDirectory();
        configChange(config);
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
            logger.warn("IOException unloading model: {}", e.getMessage());
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

    public PiperJNI getPiper() throws IOException {
        PiperJNI piper = this.piper;
        if (piper == null) {
            throw new IOException("Library not loaded");
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
                        List<Voice> modelVoices = this.getVoice(filePath);
                        newCachedVoices.put(filePath.toString(), modelVoices);
                        return modelVoices;
                    }) //
                    .flatMap(List::stream) //
                    .collect(Collectors.toSet());
            cachedVoicesByModel = newCachedVoices;
            logger.debug("Available voice number: {}", voices.size());
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
            List<Voice> cachedVoices = this.cachedVoicesByModel.get(modelPath.toString());
            if(cachedVoices != null) {
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
                ArrayList<Voice> voices = new ArrayList<>();
                speakersIdsJsonNode.fieldNames().forEachRemaining(field -> {
                    JsonNode fieldNode = speakersIdsJsonNode.get(field);
                    voices.add(new PiperSTTVoice( //
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
            return List.of(new PiperSTTVoice(voiceUID, capitalize(voiceName), languageFamily, languageRegion, modelPath,
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
    public AudioStream synthesize(String text, Voice voice, AudioFormat audioFormat) throws TTSException {
        if (!(voice instanceof PiperSTTVoice voiceData)) {
            throw new TTSException("Not piper voice provided");
        }
        PiperJNI piper;
        try {
            piper = getPiper();
        } catch (IOException e) {
            throw new TTSException("Piper now ready");
        }
        VoiceModel voiceModel = null;
        boolean usingPreloadedModel = false;
        short[] buffer;
        final VoiceModel preloadedModel = this.preloadedModel;
        try {
            try {
                if (preloadedModel != null && preloadedModel.voiceData.getUID().equals(voiceData.getUID())) {
                    logger.debug("Using preloaded voice model");
                    preloadedModel.consumers.incrementAndGet();
                    voiceModel = preloadedModel;
                    usingPreloadedModel = true;
                } else {
                    logger.debug("Loading voice model...");
                    voiceModel = loadModel(voiceData);
                    synchronized (this) {
                        usingPreloadedModel = voiceModel.equals(this.preloadedModel);
                    }
                }
            } catch (IOException e) {
                throw new TTSException("Unable to load voice model: " + e.getMessage());
            }
            try {
                logger.debug("Generating audio for: '{}'", text);
                buffer = piper.textToAudio(voiceModel.config, voiceModel.voice, text);
                logger.debug("Generated {} samples of audio", buffer.length);
            } catch (Exception e) {
                throw new TTSException("Voice generation failed: " + e.getMessage());
            }
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
            logger.debug("Initializing audio stream...");
            return getAudioStream(buffer, voiceModel.sampleRate, audioFormat);
        } catch (IOException e) {
            throw new TTSException("Error while creating audio stream: " + e.getMessage());
        }
    }

    private VoiceModel loadModel(PiperSTTVoice voice) throws IOException, UnsatisfiedLinkError {
        if (!Files.exists(voice.voiceModelPath()) || !Files.exists(voice.voiceModelConfigPath())) {
            throw new IOException("Missing voice files");
        }
        PiperJNI piper = getPiper();
        logger.debug("loading voice model");
        PiperConfig piperConfig = null;
        PiperVoice piperVoice = null;
        VoiceModel voiceModel;
        try {
            piperConfig = piper.createConfig();
            piperVoice = piper.loadVoice(piperConfig, voice.voiceModelPath(), voice.voiceModelConfigPath(),
                    voice.speakerId.orElse(-1L));
            piperConfig.initialize(piperVoice);
            voiceModel = new VoiceModel(voice, piperConfig, piperVoice, piperVoice.getSampleRate(),
                    new AtomicInteger(1), logger);
        } catch (Exception e) {
            if (piperConfig != null) {
                try {
                    piperConfig.close();
                } catch (Exception ignored) {
                }
            }
            if (piperVoice != null) {
                try {
                    piperVoice.close();
                } catch (Exception ignored) {
                }
            }
            throw new IOException("Voice load failed");
        }

        if (config.preloadModel) {
            synchronized (this) {
                if (this.preloadedModel == null) {
                    logger.debug("Voice model will be kept preloaded");
                    this.preloadedModel = voiceModel;
                } else {
                    logger.debug("Another voice model already preloaded");
                }
            }
        }
        return voiceModel;
    }

    private void unloadModel() throws IOException {
        var model = this.preloadedModel;
        if (model != null) {
            synchronized (this) {
                this.preloadedModel = null;
                if (model.consumers.get() == 0) {
                    // Do not release the model memory if it's been used, it should be released by the consumer
                    // when there is no other consumers and is not a ref of the preloaded model object.
                    logger.debug("Unloading preloaded model");
                    model.close();
                } else {
                    logger.debug("Preloaded model is use, skip memory release");
                }
            }
        }
    }

    private ByteArrayAudioStream getAudioStream(short[] samples, long sampleRate, AudioFormat targetFormat)
            throws IOException {
        ByteBuffer byteBuffer;
        int numSamples = samples.length;
        byteBuffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (var sample : samples) {
            byteBuffer.putShort(sample);
        }
        byte[] bytes = byteBuffer.array();
        javax.sound.sampled.AudioFormat jAudioFormat = new javax.sound.sampled.AudioFormat(sampleRate, 16, 1, true,
                false);
        long audioDuration = (long) Math.ceil(((double) bytes.length) / jAudioFormat.getFrameSize());
        AudioInputStream audioInputStreamTemp = new AudioInputStream(new ByteArrayInputStream(bytes), jAudioFormat,
                audioDuration);
        // Move the audio to another audio stream in target format so the Java AudioSystem encoded it as needed.
        javax.sound.sampled.AudioFormat jTargetFormat = new javax.sound.sampled.AudioFormat(
                Objects.requireNonNull(targetFormat.getFrequency()), Objects.requireNonNull(targetFormat.getBitDepth()),
                Objects.requireNonNull(targetFormat.getChannels()), true, false);
        AudioInputStream convertedInputStream = AudioSystem.getAudioInputStream(jTargetFormat, audioInputStreamTemp);
        // It's required to add the wav header to the byte array stream returned for it to work with all the sink
        // implementations.
        // It can not be done with the AudioInputStream returned by AudioSystem::getAudioInputStream because it missed
        // the length property.
        // So the following call creates another AudioInputStream instance and uses the Java AudioSystem to write the
        // header.
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

    @Override
    public AudioStream synthesizeForCache(String text, Voice voice, AudioFormat audioFormat) throws TTSException {
        return synthesize(text, voice, audioFormat);
    }

    private record PiperSTTVoice(String voiceId, String voiceName, String languageFamily, String languageRegion,
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

    ;

    private record VoiceModel(PiperSTTVoice voiceData, PiperConfig config, PiperVoice voice, int sampleRate,
            AtomicInteger consumers, Logger logger) implements AutoCloseable {

        @Override
        public void close() {
            try {
                this.config.close();
            } catch (Exception e) {
                logger.warn("Error releasing config native memory", e);
            }
            try {
                this.voice.close();
            } catch (Exception e) {
                logger.warn("Error releasing voice native memory", e);
            }
        }
    }
}
