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
package org.openhab.voice.watsonstt.internal;

import static org.openhab.voice.watsonstt.internal.WatsonSTTConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
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
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeWithWebsocketsOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.RecognizeCallback;

import okhttp3.WebSocket;

/**
 * The {@link WatsonSTTService} allows to use Watson as Speech-to-Text engine
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Speech-to-Text", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class WatsonSTTService implements STTService {
    private final Logger logger = LoggerFactory.getLogger(WatsonSTTService.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-watsonstt");
    private final List<String> telephonyModels = List.of("ar-MS_Telephony", "zh-CN_Telephony", "nl-BE_Telephony",
            "nl-NL_Telephony", "en-AU_Telephony", "en-IN_Telephony", "en-GB_Telephony", "en-US_Telephony",
            "fr-CA_Telephony", "fr-FR_Telephony", "hi-IN_Telephony", "pt-BR_Telephony", "es-ES_Telephony");
    private final List<String> multimediaModels = List.of("en-AU_Multimedia", "en-GB_Multimedia", "en-US_Multimedia",
            "fr-FR_Multimedia", "de-DE_Multimedia", "it-IT_Multimedia", "ja-JP_Multimedia", "ko-KR_Multimedia",
            "pt-BR_Multimedia", "es-ES_Multimedia");
    // model 'en-WW_Medical_Telephony' and 'es-LA_Telephony' will be used as fallbacks for es and en
    private final List<Locale> fallbackLocales = List.of(Locale.forLanguageTag("es"), Locale.ENGLISH);
    private final Set<Locale> supportedLocales = Stream
            .concat(Stream.concat(telephonyModels.stream(), multimediaModels.stream()).map(name -> name.split("_")[0])
                    .distinct().map(Locale::forLanguageTag), fallbackLocales.stream())
            .collect(Collectors.toSet());
    private WatsonSTTConfiguration config = new WatsonSTTConfiguration();
    private @Nullable SpeechToText speechToText = null;

    @Activate
    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(WatsonSTTConfiguration.class);
        if (this.config.apiKey.isBlank() || this.config.instanceUrl.isBlank()) {
            this.speechToText = null;
        } else {
            var speechToText = new SpeechToText(new IamAuthenticator.Builder().apikey(this.config.apiKey).build());
            speechToText.setServiceUrl(this.config.instanceUrl);
            if (this.config.optOutLogging) {
                speechToText.setDefaultHeaders(Map.of("X-Watson-Learning-Opt-Out", "1"));
            }
            this.speechToText = speechToText;
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
        return supportedLocales;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(AudioFormat.PCM_SIGNED, AudioFormat.WAV);
    }

    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale, Set<String> set)
            throws STTException {
        var stt = this.speechToText;
        if (stt == null) {
            throw new STTException("service is not correctly configured");
        }
        String contentType = getContentType(audioStream);
        if (contentType == null) {
            throw new STTException("Unsupported format, unable to resolve audio content type");
        }
        logger.debug("Content-Type: {}", contentType);
        RecognizeWithWebsocketsOptions wsOptions = new RecognizeWithWebsocketsOptions.Builder().audio(audioStream)
                .contentType(contentType).redaction(config.redaction).smartFormatting(config.smartFormatting)
                .model(getModel(locale)).interimResults(true)
                .backgroundAudioSuppression(config.backgroundAudioSuppression)
                .speechDetectorSensitivity(config.speechDetectorSensitivity).inactivityTimeout(config.maxSilenceSeconds)
                .build();
        final AtomicReference<@Nullable WebSocket> socketRef = new AtomicReference<>();
        final AtomicBoolean aborted = new AtomicBoolean(false);
        executor.submit(() -> {
            if (AudioFormat.CONTAINER_WAVE.equals(audioStream.getFormat().getContainer())) {
                try {
                    AudioWaveUtils.removeFMT(audioStream);
                } catch (IOException e) {
                    logger.warn("Error removing format header: {}", e.getMessage());
                }
            }
            socketRef.set(stt.recognizeUsingWebSocket(wsOptions,
                    new TranscriptionListener(socketRef, sttListener, config, aborted)));
        });
        return new STTServiceHandle() {
            @Override
            public void abort() {
                if (!aborted.getAndSet(true)) {
                    var socket = socketRef.get();
                    if (socket != null) {
                        sendStopMessage(socket);
                    }
                }
            }
        };
    }

    private String getModel(Locale locale) throws STTException {
        String languageTag = locale.toLanguageTag();
        Stream<String> allModels;
        if (config.preferMultimediaModel) {
            allModels = Stream.concat(multimediaModels.stream(), telephonyModels.stream());
        } else {
            allModels = Stream.concat(telephonyModels.stream(), multimediaModels.stream());
        }
        var modelOption = allModels.filter(model -> model.startsWith(languageTag)).findFirst();
        if (modelOption.isEmpty()) {
            if ("es".equals(locale.getLanguage())) {
                // fallback for latin american spanish languages
                var model = "es-LA_Telephony";
                logger.debug("Falling back to model: {}", model);
            }
            if ("en".equals(locale.getLanguage())) {
                // fallback english dialects
                var model = "en-WW_Medical_Telephony";
                logger.debug("Falling back to model: {}", model);
            }
            throw new STTException("No compatible model for language " + languageTag);
        }
        var model = modelOption.get();
        logger.debug("Using model: {}", model);
        return model;
    }

    private @Nullable String getContentType(AudioStream audioStream) throws STTException {
        AudioFormat format = audioStream.getFormat();
        String container = format.getContainer();
        String codec = format.getCodec();
        if (container == null || codec == null) {
            throw new STTException("Missing audio stream info");
        }
        Long frequency = format.getFrequency();
        Integer bitDepth = format.getBitDepth();
        switch (container) {
            case AudioFormat.CONTAINER_NONE:
                if (AudioFormat.CODEC_MP3.equals(codec)) {
                    return "audio/mp3";
                }
            case AudioFormat.CONTAINER_WAVE:
                if (AudioFormat.CODEC_PCM_SIGNED.equals(codec)) {
                    if (bitDepth == null || bitDepth != 16) {
                        return "audio/wav";
                    }
                    // rate is a required parameter for this type
                    if (frequency == null) {
                        return null;
                    }
                    StringBuilder contentTypeL16 = new StringBuilder(HttpMediaType.AUDIO_PCM).append(";rate=")
                            .append(frequency);
                    // // those are optional
                    Integer channels = format.getChannels();
                    if (channels != null) {
                        contentTypeL16.append(";channels=").append(channels);
                    }
                    Boolean bigEndian = format.isBigEndian();
                    if (bigEndian != null) {
                        contentTypeL16.append(";")
                                .append(bigEndian ? "endianness=big-endian" : "endianness=little-endian");
                    }
                    return contentTypeL16.toString();
                }
            case AudioFormat.CONTAINER_OGG:
                switch (codec) {
                    case AudioFormat.CODEC_VORBIS:
                        return "audio/ogg;codecs=vorbis";
                    case "OPUS":
                        return "audio/ogg;codecs=opus";
                }
                break;
        }
        return null;
    }

    private static void sendStopMessage(WebSocket ws) {
        JsonObject stopMessage = new JsonObject();
        stopMessage.addProperty("action", "stop");
        ws.send(stopMessage.toString());
    }

    private static class TranscriptionListener implements RecognizeCallback {
        private final Logger logger = LoggerFactory.getLogger(TranscriptionListener.class);
        private final StringBuilder transcriptBuilder = new StringBuilder();
        private final STTListener sttListener;
        private final WatsonSTTConfiguration config;
        private final AtomicBoolean aborted;
        private final AtomicReference<@Nullable WebSocket> socketRef;
        private float confidenceSum = 0f;
        private int responseCount = 0;
        private boolean disconnected = false;

        public TranscriptionListener(AtomicReference<@Nullable WebSocket> socketRef, STTListener sttListener,
                WatsonSTTConfiguration config, AtomicBoolean aborted) {
            this.socketRef = socketRef;
            this.sttListener = sttListener;
            this.config = config;
            this.aborted = aborted;
        }

        @Override
        public void onTranscription(@Nullable SpeechRecognitionResults speechRecognitionResults) {
            logger.debug("onTranscription");
            if (speechRecognitionResults == null) {
                return;
            }
            speechRecognitionResults.getResults().stream().filter(SpeechRecognitionResult::isXFinal).forEach(result -> {
                SpeechRecognitionAlternative alternative = result.getAlternatives().stream().findFirst().orElse(null);
                if (alternative == null) {
                    return;
                }
                logger.debug("onTranscription Final");
                Double confidence = alternative.getConfidence();
                transcriptBuilder.append(alternative.getTranscript());
                confidenceSum += confidence != null ? confidence.floatValue() : 0f;
                responseCount++;
                if (config.singleUtteranceMode) {
                    var socket = socketRef.get();
                    if (socket != null) {
                        sendStopMessage(socket);
                    }
                }
            });
        }

        @Override
        public void onConnected() {
            logger.debug("onConnected");
        }

        @Override
        public void onError(@Nullable Exception e) {
            var errorMessage = e != null ? e.getMessage() : null;
            if (errorMessage != null && disconnected && errorMessage.contains("Socket closed")) {
                logger.debug("Error ignored: {}", errorMessage);
                return;
            }
            logger.warn("TranscriptionError: {}", errorMessage);
            if (!aborted.getAndSet(true)) {
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
            }
        }

        @Override
        public void onDisconnected() {
            logger.debug("onDisconnected");
            disconnected = true;
            if (!aborted.getAndSet(true)) {
                sttListener.sttEventReceived(new RecognitionStopEvent());
                float averageConfidence = confidenceSum / (float) responseCount;
                String transcript = transcriptBuilder.toString().trim();
                if (!transcript.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionEvent(transcript, averageConfidence));
                } else {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.noResultsMessage));
                }
            }
        }

        @Override
        public void onInactivityTimeout(@Nullable RuntimeException e) {
            if (e != null) {
                logger.debug("InactivityTimeout: {}", e.getMessage());
            }
        }

        @Override
        public void onListening() {
            logger.debug("onListening");
            sttListener.sttEventReceived(new RecognitionStartEvent());
        }

        @Override
        public void onTranscriptionComplete() {
            logger.debug("onTranscriptionComplete");
        }
    }
}
