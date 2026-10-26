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
package org.openhab.voice.parakeetstt.internal;

import static org.openhab.voice.parakeetstt.internal.ParakeetSTTConstants.*;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.voice.stt.RecognitionStartEvent;
import org.openhab.core.voice.stt.RecognitionStopEvent;
import org.openhab.core.voice.stt.STTException;
import org.openhab.core.voice.stt.STTListener;
import org.openhab.core.voice.stt.STTService;
import org.openhab.core.voice.stt.STTServiceHandle;
import org.openhab.core.voice.stt.SpeechRecognitionErrorEvent;
import org.openhab.core.voice.stt.SpeechRecognitionEvent;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jvoiceproject.parakeet.BundleComponent;
import io.github.jvoiceproject.parakeet.ModelKind;
import io.github.jvoiceproject.parakeet.Parakeet;
import io.github.jvoiceproject.parakeet.ParakeetException;
import io.github.jvoiceproject.parakeet.ParakeetModel;
import io.github.jvoiceproject.parakeet.transcribe.TranscribeOptions;
import io.github.jvoiceproject.parakeet.transcribe.TranscribeStream;
import io.github.jvoiceproject.parakeet.transcribe.TranscribeStreamResult;
import io.github.jvoiceproject.parakeet.vad.VadEvent;
import io.github.jvoiceproject.parakeet.vad.VadOptions;
import io.github.jvoiceproject.parakeet.vad.VadStream;
import io.github.jvoiceproject.parakeet.vad.VadStreamResult;

/**
 * The {@link ParakeetSTTService} class is a service implementation to use NVIDIA Parakeet and NeMo models via
 * parakeet.cpp for Speech-to-Text.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, service = { STTService.class,
        ConfigOptionProvider.class }, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Speech-to-Text", description_uri = CONFIG_URI)
public class ParakeetSTTService implements STTService, ConfigOptionProvider {
    /**
     * Error message sent when no speech could be recognized in the audio stream.
     */
    private static final String NO_RESULTS_MESSAGE = "No speech recognized";

    /**
     * Generic error message sent when speech recognition encounters an unrecoverable failure.
     */
    private static final String ERROR_MESSAGE = "Speech recognition failed";

    private final Logger logger = LoggerFactory.getLogger(ParakeetSTTService.class);
    private final LocaleProvider localeProvider;
    private final Path parakeetFolder;

    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool(THREAD_POOL);

    /**
     * Synchronization lock coordinating thread-safe access to {@link #preloadedModels} and model lifecycle reference
     * counts.
     */
    private final Object modelLock = new Object();

    /**
     * Set of cancellation flags for all currently running recognition tasks, used to abort in-flight jobs on
     * deactivation.
     */
    private final Set<AtomicBoolean> activeRecognitions = ConcurrentHashMap.newKeySet();

    private volatile ParakeetSTTConfiguration config = new ParakeetSTTConfiguration();

    /**
     * Cached models kept in memory across recognition requests when model preloading is enabled.
     */
    private @Nullable LoadedModels preloadedModels;

    @Activate
    public ParakeetSTTService(final @Reference LocaleProvider localeProvider) {
        this(localeProvider, PARAKEET_FOLDER);
    }

    ParakeetSTTService(LocaleProvider localeProvider, Path parakeetFolder) {
        this.localeProvider = localeProvider;
        this.parakeetFolder = parakeetFolder;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        ensureModelDirectoryExists();
        try {
            logger.debug("Using parakeet.cpp version {} (ABI {})", Parakeet.version(), Parakeet.abiVersion());
        } catch (RuntimeException | LinkageError e) {
            logger.warn("Unable to initialize Parakeet native library: {}", e.getMessage());
        }
        configChange(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        configChange(config);
    }

    @Deactivate
    protected void deactivate() {
        for (AtomicBoolean aborted : activeRecognitions) {
            aborted.set(true);
        }
        activeRecognitions.clear();
        unloadPreloadedModels();
    }

    private void ensureModelDirectoryExists() {
        if (!Files.exists(parakeetFolder)) {
            try {
                Files.createDirectories(parakeetFolder);
                logger.info("Parakeet model directory created at {}", parakeetFolder);
            } catch (IOException e) {
                logger.warn("Unable to create Parakeet model directory at {}: {}", parakeetFolder, e.getMessage());
            }
        }
    }

    /**
     * Updates the active configuration and manages unloading and (if enabled) preloading models accordingly.
     *
     * @param newConfigMap the new configuration map
     */
    private void configChange(Map<String, Object> newConfigMap) {
        ParakeetSTTConfiguration newConfig = ConfigUtil.resolveVariables(new Configuration(newConfigMap))
                .as(ParakeetSTTConfiguration.class);
        this.config = newConfig;
        logger.debug("Parakeet configuration updated: {}", newConfigMap);

        unloadPreloadedModels();
        if (newConfig.preloadModel && !newConfig.modelName.isBlank()) {
            try {
                releaseModels(acquireModels(newConfig));
            } catch (IOException | ParakeetException e) {
                logger.warn("Unable to preload Parakeet model '{}': {}", newConfig.modelName, e.getMessage());
            } catch (LinkageError e) {
                logger.warn("Missing or incompatible Parakeet native library: {}", e.getMessage());
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
    public Set<Locale> getSupportedLocales() {
        // TODO: More sophisticated implementation based on model metadata
        String language = config.language;
        Locale modelLocale = localeProvider.getLocale();
        if (!language.isBlank()) {
            Locale parsed = Locale.forLanguageTag(language.trim());
            if (!parsed.getLanguage().isEmpty()) {
                modelLocale = parsed;
            } else {
                logger.warn("Invalid language '{}', defaulting to server locale", language);
            }
        }
        return Set.of(modelLocale);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(AUDIO_FORMAT_PCM, AUDIO_FORMAT_WAVE);
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (!CONFIG_URI.equals(uri.toString())) {
            return null;
        }
        if (!PARAM_MODEL_NAME.equals(param) && !PARAM_VAD_MODEL_NAME.equals(param)) {
            return null;
        }
        if (!Files.isDirectory(parakeetFolder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(parakeetFolder)) {
            // TODO: Extract metadata from GGUF files
            return files.filter(Files::isRegularFile).map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(MODEL_EXTENSION)).sorted(Comparator.naturalOrder())
                    .map(name -> new ParameterOption(name, name)).toList();
        } catch (IOException e) {
            logger.warn("Unable to list Parakeet models in {}: {}", parakeetFolder, e.getMessage());
            return List.of();
        }
    }

    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale)
            throws STTException {
        AudioFormat format = audioStream.getFormat();
        if (!isSupportedFormat(format)) {
            throw new STTException("Unsupported audio format: " + format);
        }
        ParakeetSTTConfiguration currentConfig = this.config;
        if (currentConfig.modelName.isBlank()) {
            throw new STTException("Model name configuration is missing");
        }

        AtomicBoolean aborted = new AtomicBoolean(false);
        activeRecognitions.add(aborted);
        sttListener.sttEventReceived(new RecognitionStartEvent());
        executor.submit(() -> runRecognition(currentConfig, sttListener, audioStream, locale, aborted));
        return () -> aborted.set(true);
    }

    private boolean isSupportedFormat(AudioFormat format) {
        return AUDIO_FORMAT_PCM.isCompatible(format) || AUDIO_FORMAT_WAVE.isCompatible(format);
    }

    /**
     * Executes the speech recognition workflow.
     *
     * @apiNote Execute this method asynchronously on a background thread with
     *          {@link java.util.concurrent.ExecutorService}.
     *
     * @param currentConfig configuration snapshot for this recognition task
     * @param sttListener listener to send recognition events to
     * @param audioStream incoming audio stream
     * @param locale target locale requested by the caller
     * @param aborted cancellation flag for this recognition task
     */
    private void runRecognition(final ParakeetSTTConfiguration currentConfig, final STTListener sttListener,
            final AudioStream audioStream, final Locale locale, final AtomicBoolean aborted) {
        LoadedModels models = null;
        try (audioStream) {
            if (AudioFormat.CONTAINER_WAVE.equals(audioStream.getFormat().getContainer())) {
                AudioWaveUtils.removeFMT(audioStream);
            }
            models = acquireModels(currentConfig);
            String transcription = switch (currentConfig.mode) {
                case STREAMING -> recognizeStreaming(currentConfig, models, locale.getLanguage(), audioStream, aborted);
                case OFFLINE -> recognizeOffline(currentConfig, models, locale.getLanguage(), audioStream, aborted);
                case AUTO -> recognizeAuto(currentConfig, models, locale.getLanguage(), audioStream, aborted);
            };
            if (!aborted.get()) {
                sttListener.sttEventReceived(new RecognitionStopEvent());
                String trimmed = transcription.trim();
                logger.debug("Final transcript: '{}'", trimmed);
                if (!trimmed.isEmpty()) {
                    sttListener.sttEventReceived(new SpeechRecognitionEvent(trimmed, 1.0f));
                } else {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(NO_RESULTS_MESSAGE));
                }
            }
        } catch (STTException | IOException | ParakeetException e) {
            logger.warn("Error running Parakeet speech-to-text: {}", e.getMessage());
            if (!aborted.get()) {
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(ERROR_MESSAGE));
            }
        } catch (LinkageError e) {
            logger.warn("Missing or incompatible Parakeet native library: {}", e.getMessage());
            if (!aborted.get()) {
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(ERROR_MESSAGE));
            }
        } finally {
            activeRecognitions.remove(aborted);
            if (models != null) {
                releaseModels(models);
            }
        }
    }

    /**
     * Attempts streaming recognition first and automatically falls back to offline recognition if streaming is
     * unsupported by the loaded model.
     *
     * @param currentConfig configuration snapshot
     * @param models loaded ASR and optional VAD models
     * @param targetLang target language code, or empty string
     * @param audioStream incoming audio stream
     * @param aborted cancellation flag
     * @return transcribed text
     * @throws IOException if reading audio fails
     * @throws STTException if offline fallback fails due to missing VAD
     */
    private String recognizeAuto(ParakeetSTTConfiguration currentConfig, LoadedModels models, String targetLang,
            AudioStream audioStream, AtomicBoolean aborted) throws IOException, STTException {
        TranscribeStream stream = null;
        try {
            stream = models.asrModel().startTranscribeStream(targetLang.isBlank() ? null : targetLang);
        } catch (ParakeetException e) {
            logger.debug("Failed to start streaming session for model '{}' ({}), falling back to offline mode",
                    currentConfig.modelName, e.getMessage());
        }
        if (stream != null) {
            try (TranscribeStream transcribeStream = stream) {
                return recognizeWithStream(currentConfig, models, transcribeStream, audioStream, aborted);
            }
        }
        return recognizeOffline(currentConfig, models, targetLang, audioStream, aborted);
    }

    /**
     * Transcribes an audio stream in real-time streaming mode using a streaming ASR session.
     *
     * @param currentConfig configuration snapshot
     * @param models loaded ASR and optional VAD models
     * @param targetLang target language code, or empty string
     * @param audioStream incoming audio stream
     * @param aborted cancellation flag
     * @return transcribed text
     * @throws IOException if reading audio fails
     */
    private String recognizeStreaming(ParakeetSTTConfiguration currentConfig, LoadedModels models, String targetLang,
            AudioStream audioStream, AtomicBoolean aborted) throws IOException {
        try (TranscribeStream stream = models.asrModel()
                .startTranscribeStream(targetLang.isBlank() ? null : targetLang)) {
            return recognizeWithStream(currentConfig, models, stream, audioStream, aborted);
        } catch (ParakeetException e) {
            logger.warn("Failed to start streaming session for model '{}': {}", currentConfig.modelName,
                    e.getMessage());
            return ERROR_MESSAGE;
        }
    }

    /**
     * Consumes audio chunks in steps and feeds them into the active transcription stream and optional VAD stream.
     *
     * @param currentConfig configuration snapshot
     * @param models loaded ASR and optional VAD models
     * @param transcribeStream active streaming transcription session
     * @param audioStream incoming audio stream
     * @param aborted cancellation flag
     * @return transcribed text
     * @throws IOException if reading audio fails
     */
    private String recognizeWithStream(ParakeetSTTConfiguration currentConfig, LoadedModels models,
            TranscribeStream transcribeStream, AudioStream audioStream, AtomicBoolean aborted) throws IOException {
        // TODO: Do we need to calculate and handle these ourself? Most of them are passed to Parakeet VAD model
        int stepSamples = Math.max(1, (int) Math.round(currentConfig.stepSeconds * SAMPLE_RATE));
        int maxSamples = Math.max(stepSamples, currentConfig.maxSeconds * SAMPLE_RATE);
        int initSilenceSamples = Math.max(stepSamples,
                (int) Math.round(currentConfig.initSilenceSeconds * SAMPLE_RATE));
        int maxSilenceSamples = Math.max(stepSamples, (int) Math.round(currentConfig.maxSilenceSeconds * SAMPLE_RATE));

        StringBuilder transcript = new StringBuilder();
        ParakeetModel vadModel = models.vadModel();

        // TODO: How does this handle models with integrated VAD support?
        try (VadStream vadStream = vadModel != null
                ? vadModel.startVadStream(SAMPLE_RATE, buildVadOptions(currentConfig))
                : null) {

            // ByteBuffer allocated once per recognition session, used to read o.o.c.a.AudioStream into short[]
            ByteBuffer captureBuffer = ByteBuffer.allocate(stepSamples * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
            short[] stepAudioSamples = new short[stepSamples];

            int totalSamples = 0;
            int silenceSamples = 0;

            boolean speechDetected = false;
            boolean vadSpeechActive = false;

            while (!aborted.get() && totalSamples < maxSamples) {
                int samplesRead = readSamples(audioStream, captureBuffer, stepAudioSamples, aborted);
                if (samplesRead <= 0 || aborted.get()) {
                    break;
                }
                short[] chunk = samplesRead == stepSamples ? stepAudioSamples
                        : Arrays.copyOf(stepAudioSamples, samplesRead);
                totalSamples += samplesRead;

                // TODO: Shouldn't we run VAD before?
                TranscribeStreamResult streamResult = transcribeStream.feed(chunk);
                boolean stepHadActivity = false;
                if (!streamResult.text().isEmpty()) {
                    appendTranscript(transcript, streamResult.text());
                    speechDetected = true;
                    stepHadActivity = true;
                }

                boolean vadEndedUtterance = false;
                if (vadStream != null) {
                    VadStreamResult vadResult = vadStream.feed(chunk, false);
                    for (VadEvent event : vadResult.events()) {
                        if ("start".equals(event.type())) {
                            speechDetected = true;
                            vadSpeechActive = true;
                            stepHadActivity = true;
                        } else if ("end".equals(event.type())) {
                            vadSpeechActive = false;
                            if (speechDetected && currentConfig.singleUtteranceMode) {
                                vadEndedUtterance = true;
                            }
                        }
                    }
                    if (vadSpeechActive) {
                        stepHadActivity = true;
                    }
                }

                if (streamResult.eou()) {
                    speechDetected = true;
                    if (currentConfig.singleUtteranceMode) {
                        logger.debug("Streaming EOU detected, stopping single-utterance recognition");
                        break;
                    }
                }

                if (vadEndedUtterance) {
                    logger.debug("VAD end-of-speech detected in streaming mode, stopping single-utterance recognition");
                    break;
                }

                if (stepHadActivity) {
                    silenceSamples = 0;
                } else {
                    silenceSamples += samplesRead;
                    int silenceLimit = speechDetected ? maxSilenceSamples : initSilenceSamples;
                    if (silenceSamples >= silenceLimit) {
                        logger.debug("Silence limit ({} samples) reached in streaming mode", silenceLimit);
                        break;
                    }
                }

                if (samplesRead < stepSamples) {
                    break;
                }
            }

            if (!aborted.get()) {
                TranscribeStreamResult finalResult = transcribeStream.finish();
                if (!finalResult.text().isEmpty()) {
                    appendTranscript(transcript, finalResult.text());
                }
            }
        }
        return transcript.toString();
    }

    /**
     * Transcribes an audio stream using VAD-segmented offline transcription.
     *
     * @param currentConfig configuration snapshot
     * @param models loaded ASR and VAD models
     * @param targetLang target language code, or empty string
     * @param audioStream incoming audio stream
     * @param aborted cancellation flag
     * @return transcribed text
     * @throws IOException if reading audio fails
     * @throws STTException if VAD model is missing or transcription fails
     */
    private String recognizeOffline(ParakeetSTTConfiguration currentConfig, LoadedModels models, String targetLang,
            AudioStream audioStream, AtomicBoolean aborted) throws IOException, STTException {
        ParakeetModel vadModel = models.vadModel();
        if (vadModel == null) {
            throw new STTException(
                    "Offline transcription mode requires a VAD model (configure vadModelName or use a bundle GGUF with a 'vad' component)");
        }
        // TODO: Do we need to calculate and handle these ourself? Most of them are passed to Parakeet VAD model
        int stepSamples = Math.max(1, (int) Math.round(currentConfig.stepSeconds * SAMPLE_RATE));
        int maxSamples = Math.max(stepSamples, currentConfig.maxSeconds * SAMPLE_RATE);
        int initSilenceSamples = Math.max(stepSamples,
                (int) Math.round(currentConfig.initSilenceSeconds * SAMPLE_RATE));
        int maxSilenceSamples = Math.max(stepSamples, (int) Math.round(currentConfig.maxSilenceSeconds * SAMPLE_RATE));

        TranscribeOptions transcribeOptions = buildTranscribeOptions(currentConfig, targetLang);
        StringBuilder transcript = new StringBuilder();

        // TODO: Review try_catch block
        try (VadStream vadStream = vadModel.startVadStream(SAMPLE_RATE, buildVadOptions(currentConfig))) {
            // ByteBuffer allocated once per recognition session, used to read o.o.c.a.AudioStream into short[]
            ByteBuffer captureBuffer = ByteBuffer.allocate(stepSamples * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
            short[] stepAudioSamples = new short[stepSamples];

            short[] audioBuffer = new short[maxSamples];

            int totalSamples = 0;
            int utteranceStartSample = 0;
            int silenceSamples = 0;

            boolean speechActive = false;
            boolean anySpeechDetected = false;
            boolean stopListening = false;

            while (!aborted.get() && !stopListening && totalSamples < maxSamples) {
                int maxRead = Math.min(stepSamples, maxSamples - totalSamples);
                int samplesRead = readSamples(audioStream, captureBuffer, stepAudioSamples, maxRead, aborted);
                if (samplesRead <= 0 || aborted.get()) {
                    break;
                }
                System.arraycopy(stepAudioSamples, 0, audioBuffer, totalSamples, samplesRead);
                int previousTotalSamples = totalSamples;
                totalSamples += samplesRead;

                short[] chunk = samplesRead == stepSamples ? stepAudioSamples
                        : Arrays.copyOf(stepAudioSamples, samplesRead);
                VadStreamResult vadResult = vadStream.feed(chunk, false);

                for (VadEvent event : vadResult.events()) {
                    if ("start".equals(event.type())) {
                        speechActive = true;
                        anySpeechDetected = true;
                        silenceSamples = 0;
                        int startSample = (int) Math.round(event.time() * SAMPLE_RATE);
                        utteranceStartSample = Math.max(utteranceStartSample,
                                Math.min(previousTotalSamples, startSample));
                        logger.debug("VAD speech start at {}s (sample {})", event.time(), utteranceStartSample);
                    } else if ("end".equals(event.type()) && speechActive) {
                        speechActive = false;
                        silenceSamples = 0;
                        int endSample = (int) Math.round(event.time() * SAMPLE_RATE);
                        int utteranceEndSample = Math.max(utteranceStartSample, Math.min(totalSamples, endSample));
                        logger.debug("VAD speech end at {}s (sample {})", event.time(), utteranceEndSample);
                        if (utteranceEndSample > utteranceStartSample) {
                            short[] segment = Arrays.copyOfRange(audioBuffer, utteranceStartSample, utteranceEndSample);
                            String segmentText = models.asrModel().transcribe(segment, SAMPLE_RATE, transcribeOptions);
                            appendTranscript(transcript, segmentText);
                        }
                        utteranceStartSample = utteranceEndSample;
                        if (currentConfig.singleUtteranceMode) {
                            stopListening = true;
                            break;
                        }
                    }
                }

                if (!speechActive && !stopListening) {
                    silenceSamples += samplesRead;
                    int silenceLimit = anySpeechDetected ? maxSilenceSamples : initSilenceSamples;
                    if (silenceSamples >= silenceLimit) {
                        logger.debug("Silence limit ({} samples) reached in offline mode", silenceLimit);
                        break;
                    }
                }

                if (samplesRead < maxRead) {
                    break;
                }
            }

            if (!aborted.get() && speechActive && totalSamples > utteranceStartSample) {
                vadStream.feed(new short[0], true);
                short[] trailingSegment = Arrays.copyOfRange(audioBuffer, utteranceStartSample, totalSamples);
                String segmentText = models.asrModel().transcribe(trailingSegment, SAMPLE_RATE, transcribeOptions);
                appendTranscript(transcript, segmentText);
            }
        }
        return transcript.toString();
    }

    /**
     * Reads up to {@code target.length} 16-bit PCM samples from the audio stream into the target array.
     *
     * @param audioStream source audio stream
     * @param captureBuffer byte buffer used as an intermediate read buffer: must be writeable, have at least
     *            {@code maxSamples * Short.BYTES} bytes capacity, and match endianness of the {@link AudioStream}'s
     *            {@link AudioFormat}
     * @param target array to receive the converted short samples
     * @param aborted cancellation flag
     * @return number of samples read, or -1 on end of stream
     * @throws IOException if an I/O error occurs
     */
    private static int readSamples(AudioStream audioStream, ByteBuffer captureBuffer, short[] target,
            AtomicBoolean aborted) throws IOException {
        return readSamples(audioStream, captureBuffer, target, target.length, aborted);
    }

    /**
     * Reads up to {@code maxSamples} 16-bit PCM samples from the audio stream into the target array.
     *
     * @param audioStream source audio stream
     * @param captureBuffer byte buffer used as an intermediate read buffer: must be writeable, have at least
     *            {@code maxSamples * Short.BYTES} bytes capacity, and match endianness of the {@link AudioStream}'s
     *            {@link AudioFormat}
     * @param target array to receive the converted short samples
     * @param maxSamples maximum number of samples to read
     * @param aborted cancellation flag
     * @return number of samples read, or -1 on end of stream
     * @throws IOException if an I/O error occurs
     */
    private static int readSamples(AudioStream audioStream, ByteBuffer captureBuffer, short[] target, int maxSamples,
            AtomicBoolean aborted) throws IOException {
        int bytesToRead = maxSamples * Short.BYTES;
        captureBuffer.clear();

        int offset = 0;
        byte[] bufferArray = captureBuffer.array();
        // Read loop (blocking I/O)
        while (offset < bytesToRead && !aborted.get()) {
            int read = audioStream.read(bufferArray, offset, bytesToRead - offset);
            if (read == -1) {
                break;
            }
            offset += read;
        }
        int samplesRead = offset / Short.BYTES;
        // Convert bytes of full samples to short[]
        if (samplesRead > 0) {
            captureBuffer.position(0);
            captureBuffer.limit(samplesRead * Short.BYTES);
            captureBuffer.asShortBuffer().get(target, 0, samplesRead);
        }
        return samplesRead;
    }

    /**
     * Appends a non-empty piece of transcribed text to the accumulator, separating with a space if needed.
     *
     * @param builder the transcription accumulator
     * @param piece the text segment to append
     */
    private static void appendTranscript(StringBuilder builder, String piece) {
        String trimmed = piece.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(trimmed);
    }

    /**
     * Constructs {@link VadOptions} from the given configuration.
     *
     * @param currentConfig configuration snapshot
     * @return configured {@link VadOptions}
     */
    private static VadOptions buildVadOptions(ParakeetSTTConfiguration currentConfig) {
        return VadOptions.builder() //
                .threshold(currentConfig.vadThreshold) //
                .minPause(currentConfig.maxSilenceSeconds) //
                .minSpeech(currentConfig.minSpeechSeconds) //
                .speechPad(currentConfig.speechPadSeconds) //
                .build();
    }

    /**
     * Constructs {@link TranscribeOptions} from the given configuration and target language.
     *
     * @param currentConfig configuration snapshot
     * @param targetLang target language code, or empty string
     * @return configured {@link TranscribeOptions}
     */
    private static TranscribeOptions buildTranscribeOptions(ParakeetSTTConfiguration currentConfig, String targetLang) {
        TranscribeOptions.Decoder decoder = switch (currentConfig.decoder) {
            case DEFAULT -> TranscribeOptions.Decoder.DEFAULT;
            case CTC -> TranscribeOptions.Decoder.CTC;
            case TRANSDUCER -> TranscribeOptions.Decoder.TRANSDUCER;
        };
        TranscribeOptions.Builder builder = TranscribeOptions.builder().decoder(decoder);
        if (!targetLang.isBlank()) {
            builder.language(targetLang);
        }
        return builder.build();
    }

    /**
     * Acquires model instances matching the configuration, reusing {@link #preloadedModels} if available or loading
     * from disk.
     *
     * @param currentConfig configuration snapshot specifying model files and concurrency parameters
     * @return the acquired {@link LoadedModels} instance with an incremented consumer count
     * @throws IOException if model files cannot be read or validated
     */
    private LoadedModels acquireModels(ParakeetSTTConfiguration currentConfig) throws IOException {
        synchronized (modelLock) {
            LoadedModels existing = this.preloadedModels;
            if (existing != null && existing.matches(currentConfig)) {
                existing.consumers().incrementAndGet();
                return existing;
            }
        }

        LoadedModels loaded = loadModelsFromDisk(currentConfig);
        if (currentConfig.preloadModel) {
            LoadedModels obsolete = null;
            synchronized (modelLock) {
                LoadedModels currentPreloaded = this.preloadedModels;
                if (this.config.equals(currentConfig) && currentPreloaded == null) {
                    loaded.consumers().incrementAndGet();
                    this.preloadedModels = loaded;
                } else if (currentPreloaded != null && !currentPreloaded.equals(loaded)
                        && !currentPreloaded.matches(currentConfig)) {
                    obsolete = currentPreloaded;
                    this.preloadedModels = null;
                }
            }
            if (obsolete != null) {
                releaseModels(obsolete);
            }
        }
        return loaded;
    }

    /**
     * Releases a consumer reference for the given models and closes them if no consumers remain and they are not
     * cached.
     *
     * @param models the models to release
     */
    private void releaseModels(LoadedModels models) {
        boolean shouldClose = false;
        synchronized (modelLock) {
            int remaining = models.consumers().decrementAndGet();
            if (remaining <= 0 && !models.equals(this.preloadedModels)) {
                shouldClose = true;
            }
        }
        if (shouldClose) {
            logger.debug("Unloading Parakeet models '{}'", models.modelName());
            models.close();
        }
    }

    /**
     * Removes the preloaded model reference and closes the models if no active recognition tasks are consuming them.
     */
    private void unloadPreloadedModels() {
        LoadedModels toClose = null;
        synchronized (modelLock) {
            LoadedModels existing = this.preloadedModels;
            if (existing != null) {
                this.preloadedModels = null;
                int remaining = existing.consumers().decrementAndGet();
                if (remaining <= 0) {
                    toClose = existing;
                }
            }
        }
        if (toClose != null) {
            logger.debug("Unloading preloaded Parakeet models '{}'", toClose.modelName());
            toClose.close();
        }
    }

    /**
     * Loads the ASR model and optional VAD model from disk and configures concurrency parameters.
     *
     * @param currentConfig configuration snapshot specifying model files and settings
     * @return new {@link LoadedModels} instance initialized with 1 consumer
     * @throws IOException if model loading or validation fails
     */
    private LoadedModels loadModelsFromDisk(ParakeetSTTConfiguration currentConfig) throws IOException {
        Path asrPath = resolveModelPath(currentConfig.modelName);
        logger.debug("Loading Parakeet ASR model from {}", asrPath);
        ParakeetModel asrModel = Parakeet.loadModel(asrPath);
        boolean success = false;
        ParakeetModel vadModel = null;
        try {
            if (asrModel.kind() != ModelKind.ASR) {
                throw new IOException("Expected ASR model but '" + asrPath + "' has kind " + asrModel.kind());
            }
            if (currentConfig.backends >= 0 && currentConfig.threads >= 0) {
                asrModel.setConcurrency(currentConfig.backends, currentConfig.threads);
            }
            if (!currentConfig.vadModelName.isBlank()) {
                Path vadPath = resolveModelPath(currentConfig.vadModelName);
                logger.debug("Loading Parakeet VAD model from {}", vadPath);
                vadModel = Parakeet.loadModel(vadPath);
                if (vadModel.kind() != ModelKind.VAD) {
                    throw new IOException("Expected VAD model but '" + vadPath + "' has kind " + vadModel.kind());
                }
            } else {
                vadModel = tryLoadBundleVadComponent(asrPath);
            }
            LoadedModels loaded = new LoadedModels(currentConfig.modelName, currentConfig.vadModelName,
                    currentConfig.backends, currentConfig.threads, asrModel, vadModel, new AtomicInteger(1));
            success = true;
            return loaded;
        } finally {
            if (!success) {
                if (vadModel != null) {
                    vadModel.close();
                }
                asrModel.close();
            }
        }
    }

    /**
     * Attempts to find and load a bundled VAD model component from a unified GGUF bundle file.
     *
     * @param bundlePath path to the GGUF model bundle
     * @return loaded VAD {@link ParakeetModel}, or null if the file is not a bundle or lacks a VAD component
     */
    private @Nullable ParakeetModel tryLoadBundleVadComponent(Path bundlePath) {
        try {
            List<BundleComponent> components = Parakeet.bundleComponents(bundlePath);
            for (BundleComponent component : components) {
                if (BUNDLE_VAD_COMPONENT.equalsIgnoreCase(component.name())
                        || BUNDLE_VAD_COMPONENT.equalsIgnoreCase(component.kind())) {
                    logger.debug("Loading bundled VAD component '{}' from {}", component.name(), bundlePath);
                    ParakeetModel vadModel = Parakeet.loadModel(bundlePath, component.name());
                    if (vadModel.kind() == ModelKind.VAD) {
                        return vadModel;
                    }
                    vadModel.close();
                }
            }
        } catch (ParakeetException e) {
            logger.debug("Model '{}' is not a bundle or has no VAD component: {}", bundlePath.getFileName(),
                    e.getMessage());
        }
        return null;
    }

    /**
     * Resolves and validates a model filename within the Parakeet model storage directory.
     *
     * @param modelName filename or relative name of the model
     * @return resolved absolute {@link Path} to the model file
     * @throws IOException if the model name is empty, escapes the base directory, or does not exist
     */
    private Path resolveModelPath(String modelName) throws IOException {
        String filename = modelName.trim();
        if (filename.isEmpty()) {
            throw new IOException("Model name must not be empty");
        }
        if (!filename.endsWith(MODEL_EXTENSION)) {
            filename = filename + MODEL_EXTENSION;
        }
        Path baseFolder = parakeetFolder.toAbsolutePath().normalize();
        Path modelPath = baseFolder.resolve(filename).normalize();
        if (!modelPath.startsWith(baseFolder)) {
            throw new IOException("Invalid model path outside Parakeet directory: " + modelName);
        }
        if (!Files.isRegularFile(modelPath)) {
            throw new IOException("Missing model file: " + modelPath);
        }
        return modelPath;
    }

    /**
     * Holds loaded native model instances, their configuration signature, and an active consumer count.
     *
     * @param modelName name of the loaded ASR model
     * @param vadModelName name of the loaded VAD model, if configured separately
     * @param backends number of execution backends configured
     * @param threads number of worker threads configured
     * @param asrModel active native ASR model instance
     * @param vadModel active native VAD model instance, or null if absent
     * @param consumers reference counter tracking active recognition consumers and preloaded cache retention
     */
    private record LoadedModels(String modelName, String vadModelName, int backends, int threads,
            ParakeetModel asrModel, @Nullable ParakeetModel vadModel,
            AtomicInteger consumers) implements AutoCloseable {

        /**
         * Checks whether these loaded models match the given configuration and remain open.
         *
         * @param cfg the configuration to compare against
         * @return true if this model instance matches the configuration and is open, false otherwise
         */
        boolean matches(ParakeetSTTConfiguration cfg) {
            ParakeetModel localVadModel = vadModel;
            return modelName.equals(cfg.modelName) && vadModelName.equals(cfg.vadModelName) && backends == cfg.backends
                    && threads == cfg.threads && !asrModel.isClosed()
                    && (localVadModel == null || !localVadModel.isClosed());
        }

        @Override
        public void close() {
            ParakeetModel localVadModel = vadModel;
            if (localVadModel != null) {
                localVadModel.close();
            }
            asrModel.close();
        }
    }
}
