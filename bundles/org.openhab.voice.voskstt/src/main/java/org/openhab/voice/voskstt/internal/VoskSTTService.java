/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.voice.voskstt.internal;

import static org.openhab.voice.voskstt.internal.VoskSTTConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
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
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vosk.Model;
import org.vosk.Recognizer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link VoskSTTService} class is a service implementation to use Vosk-API for Speech-to-Text.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Speech-to-Text", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class VoskSTTService implements STTService {
    private static final String VOSK_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "vosk").toString();
    private static final String MODEL_PATH = Path.of(VOSK_FOLDER, "model").toString();
    static {
        Logger logger = LoggerFactory.getLogger(VoskSTTService.class);
        File directory = new File(VOSK_FOLDER);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.info("vosk dir created {}", VOSK_FOLDER);
            }
        }
    }
    private final Logger logger = LoggerFactory.getLogger(VoskSTTService.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-voskstt");
    private final LocaleService localeService;
    private VoskSTTConfiguration config = new VoskSTTConfiguration();
    private @Nullable Model model;

    @Activate
    public VoskSTTService(@Reference LocaleService localeService) {
        this.localeService = localeService;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
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
        this.config = new Configuration(config).as(VoskSTTConfiguration.class);
        if (this.config.preloadModel) {
            try {
                loadModel();
            } catch (IOException e) {
                logger.warn("IOException loading model: {}", e.getMessage());
            } catch (UnsatisfiedLinkError e) {
                logger.warn("Missing native dependency: {}", e.getMessage());
            }
        } else {
            try {
                unloadModel();
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
        return Set.of(
                new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, null, null, 16000L));
    }

    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale, Set<String> set)
            throws STTException {
        AtomicBoolean aborted = new AtomicBoolean(false);
        try {
            var frequency = audioStream.getFormat().getFrequency();
            if (frequency == null) {
                throw new IOException("missing audio stream frequency");
            }
            backgroundRecognize(sttListener, audioStream, frequency, aborted);
        } catch (IOException e) {
            throw new STTException(e);
        }
        return () -> {
            aborted.set(true);
        };
    }

    private Model getModel() throws IOException, UnsatisfiedLinkError {
        var model = this.model;
        if (model != null) {
            return model;
        }
        return loadModel();
    }

    private Model loadModel() throws IOException, UnsatisfiedLinkError {
        unloadModel();
        var modelFile = new File(MODEL_PATH);
        if (!modelFile.exists() || !modelFile.isDirectory()) {
            throw new IOException("missing model dir: " + MODEL_PATH);
        }
        logger.debug("loading model");
        var model = new Model(MODEL_PATH);
        if (config.preloadModel) {
            this.model = model;
        }
        return model;
    }

    private void unloadModel() throws IOException {
        var model = this.model;
        if (model != null) {
            logger.debug("unloading model");
            model.close();
            this.model = null;
        }
    }

    private Future<?> backgroundRecognize(STTListener sttListener, InputStream audioStream, long frequency,
            AtomicBoolean aborted) {
        StringBuilder transcriptBuilder = new StringBuilder();
        long maxTranscriptionMillis = (config.maxTranscriptionSeconds * 1000L);
        long maxSilenceMillis = (config.maxSilenceSeconds * 1000L);
        long startTime = System.currentTimeMillis();
        return executor.submit(() -> {
            Recognizer recognizer = null;
            Model model = null;
            try {
                model = getModel();
                recognizer = new Recognizer(model, frequency);
                long lastInputTime = System.currentTimeMillis();
                int nbytes;
                byte[] b = new byte[4096];
                sttListener.sttEventReceived(new RecognitionStartEvent());
                while (!aborted.get()) {
                    nbytes = audioStream.read(b);
                    if (aborted.get()) {
                        break;
                    }
                    if (isExpiredInterval(maxTranscriptionMillis, startTime)) {
                        logger.debug("Stops listening, max transcription time reached");
                        break;
                    }
                    if (!config.singleUtteranceMode && isExpiredInterval(maxSilenceMillis, lastInputTime)) {
                        logger.debug("Stops listening, max silence time reached");
                        break;
                    }
                    if (nbytes == 0) {
                        trySleep(100);
                        continue;
                    }
                    if (recognizer.acceptWaveForm(b, nbytes)) {
                        lastInputTime = System.currentTimeMillis();
                        var result = recognizer.getResult();
                        logger.debug("Result: {}", result);
                        ObjectMapper mapper = new ObjectMapper();
                        var json = mapper.readTree(result);
                        transcriptBuilder.append(json.get("text").asText()).append(" ");
                        if (config.singleUtteranceMode) {
                            break;
                        }
                    } else {
                        logger.debug("Partial: {}", recognizer.getPartialResult());
                    }
                }
                if (!aborted.get()) {
                    sttListener.sttEventReceived(new RecognitionStopEvent());
                    var transcript = transcriptBuilder.toString().trim();
                    logger.debug("Final: {}", transcript);
                    if (!transcript.isBlank()) {
                        sttListener.sttEventReceived(new SpeechRecognitionEvent(transcript, 1F));
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
            } finally {
                if (recognizer != null) {
                    recognizer.close();
                }
                if (!config.preloadModel && model != null) {
                    model.close();
                }
            }
            try {
                audioStream.close();
            } catch (IOException e) {
                logger.warn("IOException on close: {}", e.getMessage());
            }
        });
    }

    private void trySleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private boolean isExpiredInterval(long interval, long referenceTime) {
        return System.currentTimeMillis() - referenceTime > interval;
    }
}
