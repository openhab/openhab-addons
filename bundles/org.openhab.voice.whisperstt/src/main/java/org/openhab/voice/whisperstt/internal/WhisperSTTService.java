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
package org.openhab.voice.whisperstt.internal;

import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_CATEGORY;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_ID;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_NAME;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_PID;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
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
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.rest.LocaleService;
import org.openhab.core.voice.RecognitionStopEvent;
import org.openhab.core.voice.STTException;
import org.openhab.core.voice.STTListener;
import org.openhab.core.voice.STTService;
import org.openhab.core.voice.STTServiceHandle;
import org.openhab.core.voice.SpeechRecognitionErrorEvent;
import org.openhab.core.voice.SpeechRecognitionEvent;
import org.openhab.voice.whisperstt.internal.utils.VoiceActivityDetector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
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
    private static final int WHISPER_SAMPLE_RATE = 16000;
    private static final Path WHISPER_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "whisper");
    private static final Path SAMPLES_FOLDER = Path.of(WHISPER_FOLDER.toString(), "samples");

    static {
        Logger logger = LoggerFactory.getLogger(WhisperSTTService.class);
        File directory = WHISPER_FOLDER.toFile();
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.info("Whisper dir created {}", WHISPER_FOLDER);
            }
        }
    }

    private void createSamplesDir() {
        File samples = SAMPLES_FOLDER.toFile();
        if (!samples.exists()) {
            if (samples.mkdir()) {
                logger.info("Whisper samples dir created {}", WHISPER_FOLDER);
            } else {
                logger.warn("Unable to create whisper samples dir {}", WHISPER_FOLDER);
            }
        }
    }

    private final Logger logger = LoggerFactory.getLogger(WhisperSTTService.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-whisperstt");
    private final LocaleService localeService;
    private WhisperSTTConfiguration config = new WhisperSTTConfiguration();
    private @Nullable WhisperContext context;
    private @Nullable WhisperJNI whisper;

    @Activate
    public WhisperSTTService(@Reference LocaleService localeService) {
        this.localeService = localeService;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            WhisperJNI.loadLibrary();
            whisper = new WhisperJNI();
        } catch (IOException | RuntimeException e) {
            logger.warn("Unable to register native library: {}", e.getMessage());
        }
        configChange(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        configChange(config);
    }

    @Deactivate
    protected void deactivate(Map<String, Object> config) {
        try {
            unloadContext();
        } catch (IOException e) {
            logger.warn("IOException unloading model: {}", e.getMessage());
        }
    }

    private void configChange(Map<String, Object> config) {
        this.config = new Configuration(config).as(WhisperSTTConfiguration.class);
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
        return Set.of(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null,
                (long) WHISPER_SAMPLE_RATE, 1));
    }

    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale, Set<String> set)
            throws STTException {
        AtomicBoolean aborted = new AtomicBoolean(false);
        try {
            var whisper = getWhisper();
            WhisperContext ctx = getContext();
            logger.debug("Creating whisper state...");
            WhisperState state = whisper.initState(ctx);
            logger.debug("Whisper state created");
            backgroundRecognize(whisper, ctx, state, locale, sttListener, audioStream, aborted);
        } catch (IOException e) {
            throw new STTException("Unable to load model", e);
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
        if (this.config.modelName.isBlank()) {
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
        Path modelPath = Path.of(WHISPER_FOLDER.toString(), modelFilename).toAbsolutePath();
        File modelFile = modelPath.toFile();
        if (!modelFile.exists() || modelFile.isDirectory()) {
            throw new IOException("Missing model file: " + modelFile.getAbsolutePath());
        }
        logger.debug("Loading whisper context...");
        var context = getWhisper().initNoState(modelPath);
        logger.debug("Whisper context loaded");
        if (config.preloadModel) {
            this.context = context;
        }
        return context;
    }

    private void unloadContext() throws IOException {
        var context = this.context;
        if (context != null) {
            logger.debug("Unloading model");
            context.close();
            this.context = null;
        }
    }

    private Future<?> backgroundRecognize(WhisperJNI whisper, WhisperContext ctx, WhisperState state, Locale locale,
            STTListener sttListener, InputStream audioStream, AtomicBoolean aborted) {
        var releaseContext = !config.preloadModel;
        int stepMs;
        int keepMs;
        int lengthMs;
        var vadDetector = new VoiceActivityDetector(config.vadThreshold);
        if (config.useVAD) {
            stepMs = 1000;
            keepMs = 0;
        } else {
            stepMs = config.stepSeconds * 1000;
            keepMs = Integer.min(config.keepMs, stepMs);
        }
        var maxMs = config.maxSeconds * 1000;
        lengthMs = Integer.min(Integer.max(config.lengthSeconds * 1000, stepMs + keepMs), maxMs);
        int nSamplesStep = stepMs * WHISPER_SAMPLE_RATE / 1000;
        int nSamplesKeep = keepMs * WHISPER_SAMPLE_RATE / 1000;
        int nSamplesLength = lengthMs * WHISPER_SAMPLE_RATE / 1000;
        int nSamplesMax = maxMs * WHISPER_SAMPLE_RATE / 1000;
        logger.debug("Step samples {}", nSamplesStep);
        logger.debug("Keep samples {}", nSamplesKeep);
        logger.debug("Length samples {}", nSamplesLength);
        logger.debug("Max samples {}", nSamplesMax);
        ByteBuffer captureBuffer = ByteBuffer.allocate(nSamplesStep * 2).order(ByteOrder.LITTLE_ENDIAN);
        float[] audioSamples = new float[nSamplesLength];
        return executor.submit(() -> {
            int audioSamplesOffset = 0;
            int silenceDetections = 0;
            int nProcessedSamples = 0;
            int numBytesRead;
            int remaining = captureBuffer.capacity();
            WhisperFullParams params = getWhisperFullParams(ctx, locale);
            String transcription = "";
            String tempTranscription = "";
            try {
                try (state; audioStream) {
                    while (!aborted.get()) {
                        if (nProcessedSamples + nSamplesStep > nSamplesMax) {
                            logger.debug("Max transcription time reached");
                            break;
                        }
                        numBytesRead = audioStream.read(captureBuffer.array(), captureBuffer.capacity() - remaining,
                                remaining);
                        if (aborted.get() || numBytesRead == -1) {
                            break;
                        }
                        if (numBytesRead != remaining) {
                            remaining = remaining - numBytesRead;
                            continue;
                        }
                        remaining = captureBuffer.capacity();
                        if (audioSamplesOffset + nSamplesStep > nSamplesLength) {
                            logger.debug("Transcription segment: {}", tempTranscription);
                            transcription += tempTranscription;
                            tempTranscription = "";
                            params.initialPrompt = transcription;
                            int keepOffset = audioSamplesOffset - nSamplesKeep;
                            System.arraycopy(audioSamples, keepOffset, audioSamples, 0,
                                    audioSamplesOffset - keepOffset);
                            audioSamplesOffset = nSamplesKeep;
                        }
                        var shortBuffer = captureBuffer.asShortBuffer();
                        while (shortBuffer.hasRemaining()) {
                            float f32Sample = Float.min(1f,
                                    Float.max((float) shortBuffer.get() / ((float) Short.MAX_VALUE), -1f));
                            nProcessedSamples++;
                            audioSamples[audioSamplesOffset++] = f32Sample;
                        }
                        if (config.useVAD) {
                            if (audioSamplesOffset + nSamplesStep > nSamplesLength) {
                                logger.debug("VAD: Skipping, max length reached");
                            } else {
                                boolean isVoice = vadDetector.runDetection(audioSamples,
                                        audioSamplesOffset - nSamplesStep, audioSamplesOffset);
                                if (isVoice) {
                                    logger.debug("VAD: voice detected");
                                    silenceDetections = 0;
                                    continue;
                                } else {
                                    silenceDetections++;
                                    if (silenceDetections < config.vadMaxSilenceSeconds) {
                                        logger.debug("VAD: silence detected {}/{}", silenceDetections,
                                                config.vadMaxSilenceSeconds);
                                        continue;
                                    } else {
                                        logger.debug("VAD: silence detected");
                                        if (config.singleUtteranceMode) {
                                            // close the audio stream to avoid keep getting audio we don't need
                                            try {
                                                audioStream.close();
                                            } catch (IOException ignored) {
                                            }
                                        }
                                    }
                                }
                            }
                            logger.debug("removing end silence");
                            audioSamplesOffset -= nSamplesStep * silenceDetections;
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
                        logger.debug("running whisper...");
                        var result = whisper.fullWithState(ctx, state, params, audioSamples, audioSamplesOffset);
                        logger.debug("whisper result code {}", result);
                        if (result != 0) {
                            if (config.errorMessage.isBlank()) {
                                sttListener.sttEventReceived(
                                        new SpeechRecognitionErrorEvent("Unable to transcript audio"));
                            } else {
                                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
                            }
                            break;
                        }
                        int nSegments = whisper.fullNSegmentsFromState(state);
                        logger.debug("Available transcription segments {}", nSegments);
                        if (nSegments == 1) {
                            tempTranscription = whisper.fullGetSegmentTextFromState(state, 0);
                            if (config.createWAVFile) {
                                createAudioFile(audioSamples, audioSamplesOffset, tempTranscription);
                            }
                            if (config.useVAD) {
                                if (config.singleUtteranceMode) {
                                    logger.debug("VAD: single utterance mode, ending transcription");
                                    transcription = tempTranscription;
                                    break;
                                } else {
                                    // start a new transcription segment
                                    transcription += tempTranscription;
                                    tempTranscription = "";
                                    audioSamplesOffset = 0;
                                }
                            } else {
                                if (config.singleUtteranceMode && tempTranscription.contains(".")) {
                                    logger.debug("Single utterance mode, dot detected, ending transcription");
                                    transcription += tempTranscription.split("\\.")[0];
                                    break;
                                }
                            }
                        } else if (nSegments == 0 && config.singleUtteranceMode) {
                            logger.debug("Single utterance mode and no results, ending transcription");
                            break;
                        } else if (nSegments > 1) {
                            // non reachable
                            logger.error("Whisper should be configured in single segment mode {}", nSegments);
                            break;
                        }
                        logger.debug("Partial transcription: {}", tempTranscription);
                        logger.debug("Transcription: {}", transcription);
                    }
                } finally {
                    if (releaseContext) {
                        ctx.close();
                    }
                }
                if (!aborted.get()) {
                    sttListener.sttEventReceived(new RecognitionStopEvent());
                    String transcript = transcription.trim();
                    logger.debug("Final text: {}", transcript);
                    if (config.removeSpecials) {
                        transcript = transcript.replaceAll(", ", " ").replaceAll(",", " ").replaceAll("\\. ", " ")
                                .replaceAll("\\.", " ").replaceAll("¿|\\?", "").replaceAll("¡|!", "").trim();
                        logger.debug("Final text no specials: {}", transcript);
                    }
                    if (!transcript.isBlank()) {
                        sttListener.sttEventReceived(new SpeechRecognitionEvent(transcript, 1));
                    } else {
                        if (!config.noResultsMessage.isBlank()) {
                            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.noResultsMessage));
                        } else {
                            sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("No results"));
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("Error running speech to text: {}", e.getMessage());
                if (config.errorMessage.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Error"));
                } else {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
                }
            } catch (UnsatisfiedLinkError e) {
                logger.warn("Missing native dependency: {}", e.getMessage());
                if (config.errorMessage.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("Error"));
                } else {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
                }
            }
        });
    }

    private void createAudioFile(float[] samples, int size, String transcription) {
        createSamplesDir();
        var jAudioFormat = new javax.sound.sampled.AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT,
                WHISPER_SAMPLE_RATE, 32, 1, 4, WHISPER_SAMPLE_RATE, false);
        var buffer = ByteBuffer.allocate(size * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < size; i++) {
            buffer.putFloat(samples[i]);
        }
        AudioInputStream audioInputStreamTemp = new AudioInputStream(new ByteArrayInputStream(buffer.array()),
                jAudioFormat, samples.length);
        try {
            Path filePath = Path.of(SAMPLES_FOLDER.toString(),
                    new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SS").format(new Date()) + "(" + transcription + ").wav");
            logger.debug("Saving transcription audio file: {}", filePath);
            FileOutputStream outputStream = new FileOutputStream(filePath.toFile());
            AudioSystem.write(audioInputStreamTemp, AudioFileFormat.Type.WAVE, outputStream);
        } catch (IOException e) {
            logger.warn("Unable to store sample file");
        }
    }

    private WhisperFullParams getWhisperFullParams(WhisperContext context, Locale locale) {
        WhisperSamplingStrategy strategy = WhisperSamplingStrategy.valueOf(config.samplingStrategy);
        var params = new WhisperFullParams(strategy);
        params.temperature = config.temperature;
        params.nThreads = config.threads;
        params.audioCtx = config.audioContextSize;
        params.speedUp = config.speedUp;
        params.beamSearchBeamSize = config.beamSize;
        params.greedyBestOf = config.greedyBestOf;
        // there is no single language models other than the english ones
        params.language = whisper.isMultilingual(context) ? locale.getLanguage() : "en";
        params.translate = false;
        params.detectLanguage = false;
        // implementation assume this options
        params.printProgress = false;
        params.printRealtime = false;
        params.printSpecial = false;
        params.printTimestamps = false;
        params.suppressBlank = true;
        params.suppressNonSpeechTokens = true;
        params.singleSegment = true;
        params.noContext = true;
        return params;
    }
}
