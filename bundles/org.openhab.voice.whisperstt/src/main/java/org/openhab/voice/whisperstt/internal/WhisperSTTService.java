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
package org.openhab.voice.whisperstt.internal;

import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_CATEGORY;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_ID;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_NAME;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_PID;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.rest.LocaleService;
import org.openhab.core.voice.RecognitionStartEvent;
import org.openhab.core.voice.RecognitionStopEvent;
import org.openhab.core.voice.STTException;
import org.openhab.core.voice.STTListener;
import org.openhab.core.voice.STTService;
import org.openhab.core.voice.STTServiceHandle;
import org.openhab.core.voice.SpeechRecognitionErrorEvent;
import org.openhab.core.voice.SpeechRecognitionEvent;
import org.openhab.voice.whisperstt.internal.utils.VAD;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.givimad.libfvadjni.VoiceActivityDetector;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperContextParams;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperGrammar;
import io.github.givimad.whisperjni.WhisperJNI;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;
import io.github.givimad.whisperjni.WhisperState;

/**
 * The {@link WhisperSTTService} class is a service implementation to use whisper.cpp for Speech-to-Text.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Speech-to-Text", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class WhisperSTTService implements STTService {
    protected static final Path WHISPER_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "whisper");
    private static final Path SAMPLES_FOLDER = Path.of(WHISPER_FOLDER.toString(), "samples");
    private static final int WHISPER_SAMPLE_RATE = 16000;
    private final Logger logger = LoggerFactory.getLogger(WhisperSTTService.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-whisperstt");
    private final LocaleService localeService;
    private WhisperSTTConfiguration config = new WhisperSTTConfiguration();
    private @Nullable WhisperContext context;
    private @Nullable WhisperGrammar grammar;
    private @Nullable WhisperJNI whisper;

    @Activate
    public WhisperSTTService(@Reference LocaleService localeService) {
        this.localeService = localeService;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            if (!Files.exists(WHISPER_FOLDER)) {
                Files.createDirectory(WHISPER_FOLDER);
            }
            WhisperJNI.loadLibrary(getLoadOptions());
            VoiceActivityDetector.loadLibrary();
            whisper = new WhisperJNI();
        } catch (IOException | RuntimeException e) {
            logger.warn("Unable to register native library: {}", e.getMessage());
        }
        configChange(config);
    }

    private WhisperJNI.LoadOptions getLoadOptions() {
        Path libFolder = Paths.get("/usr/local/lib");
        Path libFolderWin = Paths.get("/Windows/System32");
        var options = new WhisperJNI.LoadOptions();
        // Overwrite whisper jni shared library
        Path whisperJNILinuxLibrary = libFolder.resolve("libwhisperjni.so");
        Path whisperJNIMacLibrary = libFolder.resolve("libwhisperjni.dylib");
        Path whisperJNIWinLibrary = libFolderWin.resolve("libwhisperjni.dll");
        if (Files.exists(whisperJNILinuxLibrary)) {
            options.whisperJNILib = whisperJNILinuxLibrary;
        } else if (Files.exists(whisperJNIMacLibrary)) {
            options.whisperJNILib = whisperJNIMacLibrary;
        } else if (Files.exists(whisperJNIWinLibrary)) {
            options.whisperJNILib = whisperJNIWinLibrary;
        }
        // Overwrite whisper shared library, Windows searches library in $env:PATH
        Path whisperLinuxLibrary = libFolder.resolve("libwhisper.so");
        Path whisperMacLibrary = libFolder.resolve("libwhisper.dylib");
        if (Files.exists(whisperLinuxLibrary)) {
            options.whisperLib = whisperLinuxLibrary;
        } else if (Files.exists(whisperMacLibrary)) {
            options.whisperLib = whisperMacLibrary;
        }
        // Log library registration
        options.logger = (msg) -> logger.debug("Library load: {}", msg);
        return options;
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        configChange(config);
    }

    @Deactivate
    protected void deactivate(Map<String, Object> config) {
        try {
            WhisperGrammar grammar = this.grammar;
            if (grammar != null) {
                grammar.close();
                this.grammar = null;
            }
            unloadContext();
        } catch (IOException e) {
            logger.warn("IOException unloading model: {}", e.getMessage());
        }
        WhisperJNI.setLibraryLogger(null);
    }

    private void configChange(Map<String, Object> config) {
        this.config = new Configuration(config).as(WhisperSTTConfiguration.class);
        WhisperJNI.setLibraryLogger(this.config.enableWhisperLog ? this::onWhisperLog : null);
        WhisperGrammar grammar = this.grammar;
        if (grammar != null) {
            grammar.close();
            this.grammar = null;
        }
        WhisperJNI whisper;
        try {
            whisper = getWhisper();
        } catch (IOException ignored) {
            logger.warn("library not loaded, the add-on will not work");
            return;
        }
        String grammarText = String.join("\n", this.config.grammarLines);
        if (this.config.useGrammar && isValidGrammar(grammarText)) {
            try {
                logger.debug("Parsing GBNF grammar...");
                this.grammar = whisper.parseGrammar(grammarText);
            } catch (IOException e) {
                logger.warn("Error parsing grammar: {}", e.getMessage());
            }
        }
        if (this.config.preloadModel) {
            try {
                loadContext();
            } catch (IOException e) {
                logger.warn("IOException loading model: {}", e.getMessage());
            } catch (UnsatisfiedLinkError e) {
                logger.warn("Missing native dependency: {}", e.getMessage());
            }
        } else {
            try {
                unloadContext();
            } catch (IOException e) {
                logger.warn("IOException unloading model: {}", e.getMessage());
            }
        }
    }

    private boolean isValidGrammar(String grammarText) {
        try {
            WhisperGrammar.assertValidGrammar(grammarText);
        } catch (IllegalArgumentException | ParseException e) {
            logger.warn("Invalid grammar: {}", e.getMessage());
            return false;
        }
        return true;
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
        // as it is not possible to determine the language of the model that was downloaded and setup by the user, it is
        // assumed the language of the model is matching the locale of the openHAB server
        return Set.of(localeService.getLocale(null));
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(
                new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null,
                        (long) WHISPER_SAMPLE_RATE, 1),
                new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null,
                        (long) WHISPER_SAMPLE_RATE, 1));
    }

    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale, Set<String> set)
            throws STTException {
        AtomicBoolean aborted = new AtomicBoolean(false);
        WhisperContext ctx = null;
        WhisperState state = null;
        try {
            var whisper = getWhisper();
            ctx = getContext();
            logger.debug("Creating whisper state...");
            state = whisper.initState(ctx);
            logger.debug("Whisper state created");
            logger.debug("Creating VAD instance...");
            final int nSamplesStep = (int) (config.stepSeconds * (float) WHISPER_SAMPLE_RATE);
            VAD vad = new VAD(VoiceActivityDetector.Mode.valueOf(config.vadMode), WHISPER_SAMPLE_RATE, nSamplesStep,
                    config.vadStep, config.vadSensitivity);
            logger.debug("VAD instance created");
            sttListener.sttEventReceived(new RecognitionStartEvent());
            backgroundRecognize(whisper, ctx, state, nSamplesStep, locale, sttListener, audioStream, vad, aborted);
        } catch (IOException e) {
            if (ctx != null && !config.preloadModel) {
                ctx.close();
            }
            if (state != null) {
                state.close();
            }
            throw new STTException("Exception during initialization", e);
        }
        return () -> {
            aborted.set(true);
        };
    }

    private WhisperJNI getWhisper() throws IOException {
        var whisper = this.whisper;
        if (whisper == null) {
            throw new IOException("Library not loaded");
        }
        return whisper;
    }

    private WhisperContext getContext() throws IOException, UnsatisfiedLinkError {
        var context = this.context;
        if (context != null) {
            return context;
        }
        return loadContext();
    }

    private synchronized WhisperContext loadContext() throws IOException {
        unloadContext();
        String modelFilename = this.config.modelName;
        if (modelFilename.isBlank()) {
            throw new IOException("The modelName configuration is missing");
        }
        String modelPrefix = "ggml-";
        String modelExtension = ".bin";
        if (!modelFilename.startsWith(modelPrefix)) {
            modelFilename = modelPrefix + modelFilename;
        }
        if (!modelFilename.endsWith(modelExtension)) {
            modelFilename = modelFilename + modelExtension;
        }
        Path modelPath = WHISPER_FOLDER.resolve(modelFilename);
        if (!Files.exists(modelPath) || Files.isDirectory(modelPath)) {
            throw new IOException("Missing model file: " + modelPath);
        }
        logger.debug("Loading whisper context...");
        WhisperJNI whisper = getWhisper();
        var context = whisper.initNoState(modelPath, getWhisperContextParams());
        logger.debug("Whisper context loaded");
        if (config.preloadModel) {
            this.context = context;
        }
        if (!config.openvinoDevice.isBlank()) {
            // has no effect if OpenVINO is not enabled in whisper.cpp library.
            logger.debug("Init OpenVINO device");
            whisper.initOpenVINO(context, config.openvinoDevice);
        }
        return context;
    }

    private WhisperContextParams getWhisperContextParams() {
        var params = new WhisperContextParams();
        params.useGPU = config.useGPU;
        return params;
    }

    private void unloadContext() throws IOException {
        var context = this.context;
        if (context != null) {
            logger.debug("Unloading model");
            context.close();
            this.context = null;
        }
    }

    private void backgroundRecognize(WhisperJNI whisper, WhisperContext ctx, WhisperState state, final int nSamplesStep,
            Locale locale, STTListener sttListener, AudioStream audioStream, VAD vad, AtomicBoolean aborted) {
        var releaseContext = !config.preloadModel;
        final int nSamplesMax = config.maxSeconds * WHISPER_SAMPLE_RATE;
        final int nSamplesMin = (int) (config.minSeconds * (float) WHISPER_SAMPLE_RATE);
        final int nInitSilenceSamples = (int) (config.initSilenceSeconds * (float) WHISPER_SAMPLE_RATE);
        final int nMaxSilenceSamples = (int) (config.maxSilenceSeconds * (float) WHISPER_SAMPLE_RATE);
        logger.debug("Samples per step {}", nSamplesStep);
        logger.debug("Min transcription samples {}", nSamplesMin);
        logger.debug("Max transcription samples {}", nSamplesMax);
        logger.debug("Max init silence samples {}", nInitSilenceSamples);
        logger.debug("Max silence samples {}", nMaxSilenceSamples);
        // used to store the step samples in libfvad wanted format 16-bit int
        final short[] stepAudioSamples = new short[nSamplesStep];
        // used to store the full samples in whisper wanted format 32-bit float
        final float[] audioSamples = new float[nSamplesMax];
        executor.submit(() -> {
            int audioSamplesOffset = 0;
            int silenceSamplesCounter = 0;
            int nProcessedSamples = 0;
            int numBytesRead;
            boolean voiceDetected = false;
            String transcription = "";
            String tempTranscription = "";
            VAD.@Nullable VADResult lastVADResult;
            VAD.@Nullable VADResult firstConsecutiveSilenceVADResult = null;
            try {
                try (state; //
                        audioStream; //
                        vad) {
                    if (AudioFormat.CONTAINER_WAVE.equals(audioStream.getFormat().getContainer())) {
                        AudioWaveUtils.removeFMT(audioStream);
                    }
                    final ByteBuffer captureBuffer = ByteBuffer.allocate(nSamplesStep * 2)
                            .order(ByteOrder.LITTLE_ENDIAN);
                    // init remaining to full capacity
                    int remaining = captureBuffer.capacity();
                    WhisperFullParams params = getWhisperFullParams(ctx, locale);
                    while (!aborted.get()) {
                        // read until no remaining so we get the complete step samples
                        numBytesRead = audioStream.read(captureBuffer.array(), captureBuffer.capacity() - remaining,
                                remaining);
                        if (aborted.get() || numBytesRead == -1) {
                            break;
                        }
                        if (numBytesRead != remaining) {
                            remaining = remaining - numBytesRead;
                            continue;
                        }
                        // reset remaining to full capacity
                        remaining = captureBuffer.capacity();
                        // encode step samples and copy them to the audio buffers
                        var shortBuffer = captureBuffer.asShortBuffer();
                        while (shortBuffer.hasRemaining()) {
                            var position = shortBuffer.position();
                            short i16BitSample = shortBuffer.get();
                            float f32BitSample = Float.min(1f,
                                    Float.max((float) i16BitSample / ((float) Short.MAX_VALUE), -1f));
                            stepAudioSamples[position] = i16BitSample;
                            audioSamples[audioSamplesOffset++] = f32BitSample;
                            nProcessedSamples++;
                        }
                        // run vad
                        if (nProcessedSamples + nSamplesStep > nSamplesMax - nSamplesStep) {
                            logger.debug("VAD: Skipping, max length reached");
                        } else {
                            lastVADResult = vad.analyze(stepAudioSamples);
                            if (lastVADResult.isVoice()) {
                                voiceDetected = true;
                                logger.debug("VAD: voice detected");
                                silenceSamplesCounter = 0;
                                firstConsecutiveSilenceVADResult = null;
                                continue;
                            } else {
                                if (firstConsecutiveSilenceVADResult == null) {
                                    firstConsecutiveSilenceVADResult = lastVADResult;
                                }
                                silenceSamplesCounter += nSamplesStep;
                                int maxSilenceSamples = voiceDetected ? nMaxSilenceSamples : nInitSilenceSamples;
                                if (silenceSamplesCounter < maxSilenceSamples) {
                                    if (logger.isDebugEnabled()) {
                                        int totalSteps = maxSilenceSamples / nSamplesStep;
                                        int currentSteps = totalSteps
                                                - ((maxSilenceSamples - silenceSamplesCounter) / nSamplesStep);
                                        logger.debug("VAD: silence detected {}/{}", currentSteps, totalSteps);
                                    }
                                    if (!voiceDetected && config.removeSilence) {
                                        logger.debug("removing start silence");
                                        int samplesToKeep = lastVADResult.voiceSamplesInTail();
                                        if (samplesToKeep > 0) {
                                            for (int i = 0; i < samplesToKeep; i++) {
                                                audioSamples[i] = audioSamples[audioSamplesOffset
                                                        - (samplesToKeep - i)];
                                            }
                                            audioSamplesOffset = samplesToKeep;
                                            logger.debug("some audio was kept");
                                        } else {
                                            audioSamplesOffset = 0;
                                        }
                                    }
                                    continue;
                                } else {
                                    logger.debug("VAD: silence detected");
                                    if (audioSamplesOffset < nSamplesMin) {
                                        logger.debug("Not enough samples, continue");
                                        continue;
                                    }
                                    if (config.singleUtteranceMode) {
                                        // close the audio stream to avoid keep getting audio we don't need
                                        try {
                                            audioStream.close();
                                        } catch (IOException ignored) {
                                        }
                                    }
                                }
                            }
                            if (config.removeSilence) {
                                if (voiceDetected) {
                                    logger.debug("removing end silence");
                                    int samplesToKeep = firstConsecutiveSilenceVADResult.voiceSamplesInHead();
                                    if (samplesToKeep > 0) {
                                        logger.debug("some audio was kept");
                                    }
                                    var samplesToRemove = silenceSamplesCounter - samplesToKeep;
                                    if (audioSamplesOffset - samplesToRemove < nSamplesMin) {
                                        logger.debug("avoid removing under min audio seconds");
                                        samplesToRemove = audioSamplesOffset - nSamplesMin;
                                    }
                                    if (samplesToRemove > 0) {
                                        audioSamplesOffset -= samplesToRemove;
                                    }
                                } else {
                                    audioSamplesOffset = 0;
                                }
                            }
                            if (audioSamplesOffset == 0) {
                                if (config.singleUtteranceMode) {
                                    logger.debug("no audio to transcribe, ending");
                                    break;
                                } else {
                                    logger.debug("no audio to transcribe, continue listening");
                                    continue;
                                }
                            }
                        }
                        // run whisper
                        logger.debug("running whisper with {} seconds of audio...",
                                Math.round((((float) audioSamplesOffset) / (float) WHISPER_SAMPLE_RATE) * 100f) / 100f);
                        long execStartTime = System.currentTimeMillis();
                        var result = whisper.fullWithState(ctx, state, params, audioSamples, audioSamplesOffset);
                        logger.debug("whisper ended in {}ms with result code {}",
                                System.currentTimeMillis() - execStartTime, result);
                        // process result
                        if (result != 0) {
                            emitSpeechRecognitionError(sttListener);
                            break;
                        }
                        int nSegments = whisper.fullNSegmentsFromState(state);
                        logger.debug("Available transcription segments {}", nSegments);
                        if (nSegments == 1) {
                            tempTranscription = whisper.fullGetSegmentTextFromState(state, 0);
                            if (config.createWAVRecord) {
                                createAudioFile(audioSamples, audioSamplesOffset, tempTranscription,
                                        locale.getLanguage());
                            }
                            if (config.singleUtteranceMode) {
                                logger.debug("single utterance mode, ending transcription");
                                transcription = tempTranscription;
                                break;
                            } else {
                                // start a new transcription segment
                                transcription += tempTranscription;
                                tempTranscription = "";
                            }
                        } else if (nSegments == 0 && config.singleUtteranceMode) {
                            logger.debug("Single utterance mode and no results, ending transcription");
                            break;
                        } else if (nSegments > 1) {
                            // non reachable
                            logger.warn("Whisper should be configured in single segment mode {}", nSegments);
                            break;
                        }
                        // reset state to start with next segment
                        voiceDetected = false;
                        silenceSamplesCounter = 0;
                        audioSamplesOffset = 0;
                        logger.debug("Partial transcription: {}", tempTranscription);
                        logger.debug("Transcription: {}", transcription);
                    }
                } finally {
                    if (releaseContext) {
                        ctx.close();
                    }
                }
                // emit result
                if (!aborted.get()) {
                    sttListener.sttEventReceived(new RecognitionStopEvent());
                    logger.debug("Final transcription: '{}'", transcription);
                    if (!transcription.isBlank()) {
                        sttListener.sttEventReceived(new SpeechRecognitionEvent(transcription.trim(), 1));
                    } else {
                        emitSpeechRecognitionNoResultsError(sttListener);
                    }
                }
            } catch (IOException e) {
                logger.warn("Error running speech to text: {}", e.getMessage());
                emitSpeechRecognitionError(sttListener);
            } catch (UnsatisfiedLinkError e) {
                logger.warn("Missing native dependency: {}", e.getMessage());
                emitSpeechRecognitionError(sttListener);
            }
        });
    }

    private WhisperFullParams getWhisperFullParams(WhisperContext context, Locale locale) throws IOException {
        WhisperSamplingStrategy strategy = WhisperSamplingStrategy.valueOf(config.samplingStrategy);
        var params = new WhisperFullParams(strategy);
        params.temperature = config.temperature;
        params.nThreads = config.threads;
        params.audioCtx = config.audioContext;
        params.speedUp = config.speedUp;
        params.beamSearchBeamSize = config.beamSize;
        params.greedyBestOf = config.greedyBestOf;
        if (!config.initialPrompt.isBlank()) {
            params.initialPrompt = config.initialPrompt;
        }
        if (grammar != null) {
            params.grammar = grammar;
            params.grammarPenalty = config.grammarPenalty;
        }
        // there is no single language models other than the english ones
        params.language = getWhisper().isMultilingual(context) ? locale.getLanguage() : "en";
        // implementation assumes this options
        params.translate = false;
        params.detectLanguage = false;
        params.printProgress = false;
        params.noTimestamps = true;
        params.printRealtime = false;
        params.printSpecial = false;
        params.printTimestamps = false;
        params.suppressBlank = true;
        params.suppressNonSpeechTokens = true;
        params.singleSegment = true;
        params.noContext = true;
        return params;
    }

    private void emitSpeechRecognitionNoResultsError(STTListener sttListener) {
        sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.noResultsMessage));
    }

    private void emitSpeechRecognitionError(STTListener sttListener) {
        sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
    }

    private void createSamplesDir() {
        if (!Files.exists(SAMPLES_FOLDER)) {
            try {
                Files.createDirectory(SAMPLES_FOLDER);
                logger.info("Whisper samples dir created {}", SAMPLES_FOLDER);
            } catch (IOException ignored) {
                logger.warn("Unable to create whisper samples dir {}", SAMPLES_FOLDER);
            }
        }
    }

    private void createAudioFile(float[] samples, int size, String transcription, String language) {
        createSamplesDir();
        javax.sound.sampled.AudioFormat jAudioFormat;
        ByteBuffer byteBuffer;
        if ("i16".equals(config.recordSampleFormat)) {
            logger.debug("Saving audio file with sample format i16");
            jAudioFormat = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                    WHISPER_SAMPLE_RATE, 16, 1, 2, WHISPER_SAMPLE_RATE, false);
            byteBuffer = ByteBuffer.allocate(size * 2).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < size; i++) {
                byteBuffer.putShort((short) (samples[i] * (float) Short.MAX_VALUE));
            }
        } else {
            logger.debug("Saving audio file with sample format f32");
            jAudioFormat = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT,
                    WHISPER_SAMPLE_RATE, 32, 1, 4, WHISPER_SAMPLE_RATE, false);
            byteBuffer = ByteBuffer.allocate(size * 4).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < size; i++) {
                byteBuffer.putFloat(samples[i]);
            }
        }
        AudioInputStream audioInputStreamTemp = new AudioInputStream(new ByteArrayInputStream(byteBuffer.array()),
                jAudioFormat, samples.length);
        try {
            var scapedTranscription = transcription.replaceAll("[^a-zA-ZÀ-ú0-9.-]", "_");
            if (scapedTranscription.length() > 60) {
                scapedTranscription = scapedTranscription.substring(0, 60);
            }
            String fileName = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SS").format(new Date()) + "("
                    + scapedTranscription + ")";
            Path audioPath = Path.of(SAMPLES_FOLDER.toString(), fileName + ".wav");
            Path propertiesPath = Path.of(SAMPLES_FOLDER.toString(), fileName + ".props");
            logger.debug("Saving audio file: {}", audioPath);
            FileOutputStream audioFileOutputStream = new FileOutputStream(audioPath.toFile());
            AudioSystem.write(audioInputStreamTemp, AudioFileFormat.Type.WAVE, audioFileOutputStream);
            audioFileOutputStream.close();
            String properties = "transcription=" + transcription + "\nlanguage=" + language + "\n";
            logger.debug("Saving properties file: {}", propertiesPath);
            FileOutputStream propertiesFileOutputStream = new FileOutputStream(propertiesPath.toFile());
            propertiesFileOutputStream.write(properties.getBytes(StandardCharsets.UTF_8));
            propertiesFileOutputStream.close();
        } catch (IOException e) {
            logger.warn("Unable to store sample.", e);
        }
    }

    private void onWhisperLog(String text) {
        logger.debug("[whisper.cpp] {}", text);
    }
}
