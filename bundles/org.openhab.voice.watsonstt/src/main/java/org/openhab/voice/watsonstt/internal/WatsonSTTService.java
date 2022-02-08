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
package org.openhab.voice.watsonstt.internal;

import static org.openhab.voice.watsonstt.internal.WatsonSTTConstants.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
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
    private final List<String> models = List.of("ar-AR_BroadbandModel", "de-DE_BroadbandModel", "en-AU_BroadbandModel",
            "en-GB_BroadbandModel", "en-US_BroadbandModel", "es-AR_BroadbandModel", "es-CL_BroadbandModel",
            "es-CO_BroadbandModel", "es-ES_BroadbandModel", "es-MX_BroadbandModel", "es-PE_BroadbandModel",
            "fr-CA_BroadbandModel", "fr-FR_BroadbandModel", "it-IT_BroadbandModel", "ja-JP_BroadbandModel",
            "ko-KR_BroadbandModel", "nl-NL_BroadbandModel", "pt-BR_BroadbandModel", "zh-CN_BroadbandModel");
    private final Set<Locale> supportedLocales = models.stream().map(name -> name.split("_")[0])
            .map(Locale::forLanguageTag).collect(Collectors.toSet());
    private WatsonSTTConfiguration config = new WatsonSTTConfiguration();

    @Activate
    protected void activate(Map<String, Object> config) {
        this.config = new Configuration(config).as(WatsonSTTConfiguration.class);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(WatsonSTTConfiguration.class);
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
        return Set.of(AudioFormat.WAV, AudioFormat.OGG, new AudioFormat("OGG", "OPUS", null, null, null, null),
                AudioFormat.MP3);
    }

    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale, Set<String> set)
            throws STTException {
        if (config.apiKey.isBlank() || config.instanceUrl.isBlank()) {
            throw new STTException("service is not correctly configured");
        }
        String contentType = getContentType(audioStream);
        if (contentType == null) {
            throw new STTException("Unsupported format, unable to resolve audio content type");
        }
        logger.debug("Content-Type: {}", contentType);
        var speechToText = new SpeechToText(new IamAuthenticator.Builder().apikey(config.apiKey).build());
        speechToText.setServiceUrl(config.instanceUrl);
        if (config.optOutLogging) {
            speechToText.setDefaultHeaders(Map.of("X-Watson-Learning-Opt-Out", "1"));
        }
        RecognizeWithWebsocketsOptions wsOptions = new RecognizeWithWebsocketsOptions.Builder().audio(audioStream)
                .contentType(contentType).redaction(config.redaction).smartFormatting(config.smartFormatting)
                .model(locale.toLanguageTag() + "_BroadbandModel").interimResults(true)
                .backgroundAudioSuppression(config.backgroundAudioSuppression)
                .speechDetectorSensitivity(config.speechDetectorSensitivity).inactivityTimeout(config.inactivityTimeout)
                .build();
        final AtomicReference<@Nullable WebSocket> socketRef = new AtomicReference<>();
        var task = executor.submit(() -> {
            int retries = 2;
            while (retries > 0) {
                try {
                    socketRef.set(speechToText.recognizeUsingWebSocket(wsOptions,
                            new TranscriptionListener(sttListener, config)));
                    break;
                } catch (RuntimeException e) {
                    var cause = e.getCause();
                    if (cause instanceof SSLPeerUnverifiedException) {
                        logger.debug("Retrying on error: {}", cause.getMessage());
                        retries--;
                    } else {
                        var errorMessage = e.getMessage();
                        logger.warn("Aborting on error: {}", errorMessage);
                        sttListener.sttEventReceived(
                                new SpeechRecognitionErrorEvent(errorMessage != null ? errorMessage : "Unknown error"));
                        break;
                    }
                }
            }
        });
        return new STTServiceHandle() {
            @Override
            public void abort() {
                var socket = socketRef.get();
                if (socket != null) {
                    socket.close(1000, null);
                    socket.cancel();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
                task.cancel(true);
            }
        };
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
            case AudioFormat.CONTAINER_NONE:
                if (AudioFormat.CODEC_MP3.equals(codec)) {
                    return "audio/mp3";
                }
                break;
        }
        return null;
    }

    private static class TranscriptionListener implements RecognizeCallback {
        private final Logger logger = LoggerFactory.getLogger(TranscriptionListener.class);
        private final StringBuilder transcriptBuilder = new StringBuilder();
        private final STTListener sttListener;
        private final WatsonSTTConfiguration config;
        private float confidenceSum = 0f;
        private int responseCount = 0;
        private boolean disconnected = false;

        public TranscriptionListener(STTListener sttListener, WatsonSTTConfiguration config) {
            this.sttListener = sttListener;
            this.config = config;
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
            sttListener.sttEventReceived(
                    new SpeechRecognitionErrorEvent(errorMessage != null ? errorMessage : "Unknown error"));
        }

        @Override
        public void onDisconnected() {
            logger.debug("onDisconnected");
            disconnected = true;
            sttListener.sttEventReceived(new RecognitionStopEvent());
            float averageConfidence = confidenceSum / (float) responseCount;
            String transcript = transcriptBuilder.toString();
            if (!transcript.isBlank()) {
                sttListener.sttEventReceived(new SpeechRecognitionEvent(transcript, averageConfidence));
            } else {
                if (!config.noResultsMessage.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.noResultsMessage));
                } else {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("No results"));
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
