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
package org.openhab.voice.googlestt.internal;

import static org.openhab.voice.googlestt.internal.GoogleSTTConstants.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.voice.RecognitionStartEvent;
import org.openhab.core.voice.RecognitionStopEvent;
import org.openhab.core.voice.STTListener;
import org.openhab.core.voice.STTService;
import org.openhab.core.voice.STTServiceHandle;
import org.openhab.core.voice.SpeechRecognitionErrorEvent;
import org.openhab.core.voice.SpeechRecognitionEvent;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;

/**
 * The {@link GoogleSTTService} class is a service implementation to use Google Cloud Speech-to-Text features.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Speech-to-Text", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class GoogleSTTService implements STTService {

    private static final String GCP_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    private static final String GCP_TOKEN_URI = "https://accounts.google.com/o/oauth2/token";
    private static final String GCP_REDIRECT_URI = "https://www.google.com";
    private static final String GCP_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private final Logger logger = LoggerFactory.getLogger(GoogleSTTService.class);
    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("OH-voice-googlestt");
    private final OAuthFactory oAuthFactory;
    private final ConfigurationAdmin configAdmin;

    private GoogleSTTConfiguration config = new GoogleSTTConfiguration();
    private @Nullable OAuthClientService oAuthService;

    @Activate
    public GoogleSTTService(final @Reference OAuthFactory oAuthFactory,
            final @Reference ConfigurationAdmin configAdmin) {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
        this.oAuthFactory = oAuthFactory;
        this.configAdmin = configAdmin;
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        this.config = new Configuration(config).as(GoogleSTTConfiguration.class);
        executor.submit(() -> GoogleSTTLocale.loadLocales(this.config.refreshSupportedLocales));
        updateConfig();
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(GoogleSTTConfiguration.class);
        updateConfig();
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
        return GoogleSTTLocale.getSupportedLocales();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Set.of(
                new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, null, 16000L),
                new AudioFormat(AudioFormat.CONTAINER_OGG, "OPUS", null, null, null, 8000L),
                new AudioFormat(AudioFormat.CONTAINER_OGG, "OPUS", null, null, null, 12000L),
                new AudioFormat(AudioFormat.CONTAINER_OGG, "OPUS", null, null, null, 16000L),
                new AudioFormat(AudioFormat.CONTAINER_OGG, "OPUS", null, null, null, 24000L),
                new AudioFormat(AudioFormat.CONTAINER_OGG, "OPUS", null, null, null, 48000L));
    }

    @Override
    public STTServiceHandle recognize(STTListener sttListener, AudioStream audioStream, Locale locale,
            Set<String> set) {
        AtomicBoolean aborted = new AtomicBoolean(false);
        backgroundRecognize(sttListener, audioStream, aborted, locale, set);
        return new STTServiceHandle() {
            @Override
            public void abort() {
                aborted.set(true);
            }
        };
    }

    private void updateConfig() {
        String clientId = this.config.clientId;
        String clientSecret = this.config.clientSecret;
        if (!clientId.isBlank() && !clientSecret.isBlank()) {
            var oAuthService = oAuthFactory.createOAuthClientService(SERVICE_PID, GCP_TOKEN_URI, GCP_AUTH_URI, clientId,
                    clientSecret, GCP_SCOPE, false);
            this.oAuthService = oAuthService;
            if (!this.config.oauthCode.isEmpty()) {
                getAccessToken(oAuthService, this.config.oauthCode);
                deleteAuthCode();
            }
        } else {
            logger.warn("Missing authentication configuration to access Google Cloud STT API.");
        }
    }

    private void getAccessToken(OAuthClientService oAuthService, String oauthCode) {
        logger.debug("Trying to get access and refresh tokens.");
        try {
            AccessTokenResponse response = oAuthService.getAccessTokenResponseByAuthorizationCode(oauthCode,
                    GCP_REDIRECT_URI);
            if (response.getRefreshToken() == null || response.getRefreshToken().isEmpty()) {
                logger.warn("Error fetching refresh token. Please try to reauthorize.");
            }
        } catch (OAuthException | OAuthResponseException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error fetching access token: {}", e.getMessage(), e);
            } else {
                logger.warn("Error fetching access token. Invalid oauth code? Please generate a new one.");
            }
        } catch (IOException e) {
            logger.warn("An unexpected IOException occurred when fetching access token: {}", e.getMessage());
        }
    }

    private void deleteAuthCode() {
        try {
            org.osgi.service.cm.Configuration serviceConfig = configAdmin.getConfiguration(SERVICE_PID);
            Dictionary<String, Object> configProperties = serviceConfig.getProperties();
            if (configProperties != null) {
                configProperties.put("oauthCode", "");
                serviceConfig.update(configProperties);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete current oauth code, please delete it manually.");
        }
    }

    private Future<?> backgroundRecognize(STTListener sttListener, AudioStream audioStream, AtomicBoolean aborted,
            Locale locale, Set<String> set) {
        Credentials credentials = getCredentials();
        return executor.submit(() -> {
            logger.debug("Background recognize starting");
            ClientStream<StreamingRecognizeRequest> clientStream = null;
            try (SpeechClient client = SpeechClient
                    .create(SpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build())) {
                TranscriptionListener responseObserver = new TranscriptionListener(sttListener, config, aborted);
                clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);
                streamAudio(clientStream, audioStream, responseObserver, aborted, locale);
                clientStream.closeSend();
                logger.debug("Background recognize done");
            } catch (IOException e) {
                if (clientStream != null && clientStream.isSendReady()) {
                    clientStream.closeSendWithError(e);
                } else if (!config.errorMessage.isBlank()) {
                    logger.warn("Error running speech to text: {}", e.getMessage());
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
                }
            }
        });
    }

    private void streamAudio(ClientStream<StreamingRecognizeRequest> clientStream, AudioStream audioStream,
            TranscriptionListener responseObserver, AtomicBoolean aborted, Locale locale) throws IOException {
        // Gather stream info and send config
        AudioFormat streamFormat = audioStream.getFormat();
        RecognitionConfig.AudioEncoding streamEncoding;
        if (AudioFormat.WAV.isCompatible(streamFormat)) {
            streamEncoding = RecognitionConfig.AudioEncoding.LINEAR16;
        } else if (AudioFormat.OGG.isCompatible(streamFormat)) {
            streamEncoding = RecognitionConfig.AudioEncoding.OGG_OPUS;
        } else {
            logger.debug("Unsupported format {}", streamFormat);
            return;
        }
        Integer channelsObject = streamFormat.getChannels();
        int channels = channelsObject != null ? channelsObject : 1;
        Long longFrequency = streamFormat.getFrequency();
        if (longFrequency == null) {
            logger.debug("Missing frequency info");
            return;
        }
        int frequency = Math.toIntExact(longFrequency);
        // First thing we need to send the stream config
        sendStreamConfig(clientStream, streamEncoding, frequency, channels, locale);
        // Loop sending audio data
        long startTime = System.currentTimeMillis();
        long maxTranscriptionMillis = (config.maxTranscriptionSeconds * 1000L);
        long maxSilenceMillis = (config.maxSilenceSeconds * 1000L);
        int readBytes = 6400;
        while (!aborted.get()) {
            byte[] data = new byte[readBytes];
            int dataN = audioStream.read(data);
            if (aborted.get()) {
                logger.debug("Stops listening, aborted");
                break;
            }
            if (isExpiredInterval(maxTranscriptionMillis, startTime)) {
                logger.debug("Stops listening, max transcription time reached");
                break;
            }
            if (!config.singleUtteranceMode
                    && isExpiredInterval(maxSilenceMillis, responseObserver.getLastInputTime())) {
                logger.debug("Stops listening, max silence time reached");
                break;
            }
            if (dataN != readBytes) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                continue;
            }
            StreamingRecognizeRequest dataRequest = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(data)).build();
            logger.debug("Sending audio data {}", dataN);
            clientStream.send(dataRequest);
        }
    }

    private void sendStreamConfig(ClientStream<StreamingRecognizeRequest> clientStream,
            RecognitionConfig.AudioEncoding encoding, int sampleRate, int channels, Locale locale) {
        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder().setEncoding(encoding)
                .setAudioChannelCount(channels).setLanguageCode(locale.toLanguageTag()).setSampleRateHertz(sampleRate)
                .build();

        StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(recognitionConfig).setInterimResults(false).setSingleUtterance(config.singleUtteranceMode)
                .build();

        clientStream
                .send(StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingRecognitionConfig).build());
    }

    private @Nullable Credentials getCredentials() {
        String accessToken = null;
        String refreshToken = null;
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService != null) {
                AccessTokenResponse response = oAuthService.getAccessTokenResponse();
                if (response != null) {
                    accessToken = response.getAccessToken();
                    refreshToken = response.getRefreshToken();
                }
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.warn("Access token error: {}", e.getMessage());
        }
        if (accessToken == null || refreshToken == null) {
            logger.warn("Missed google cloud access and/or refresh token");
            return null;
        }
        return OAuth2Credentials.create(new AccessToken(accessToken, null));
    }

    private boolean isExpiredInterval(long interval, long referenceTime) {
        return System.currentTimeMillis() - referenceTime > interval;
    }

    private static class TranscriptionListener implements ResponseObserver<StreamingRecognizeResponse> {
        private final Logger logger = LoggerFactory.getLogger(TranscriptionListener.class);
        private final StringBuilder transcriptBuilder = new StringBuilder();
        private final STTListener sttListener;
        GoogleSTTConfiguration config;
        private final AtomicBoolean aborted;
        private float confidenceSum = 0;
        private int responseCount = 0;
        private long lastInputTime = 0;

        public TranscriptionListener(STTListener sttListener, GoogleSTTConfiguration config, AtomicBoolean aborted) {
            this.sttListener = sttListener;
            this.config = config;
            this.aborted = aborted;
        }

        @Override
        public void onStart(@Nullable StreamController controller) {
            sttListener.sttEventReceived(new RecognitionStartEvent());
            lastInputTime = System.currentTimeMillis();
        }

        @Override
        public void onResponse(StreamingRecognizeResponse response) {
            lastInputTime = System.currentTimeMillis();
            List<StreamingRecognitionResult> results = response.getResultsList();
            logger.debug("Got {} results", response.getResultsList().size());
            if (results.isEmpty()) {
                logger.debug("No results");
                return;
            }
            results.forEach(result -> {
                List<SpeechRecognitionAlternative> alternatives = result.getAlternativesList();
                logger.debug("Got {} alternatives", alternatives.size());
                SpeechRecognitionAlternative alternative = alternatives.stream()
                        .max(Comparator.comparing(SpeechRecognitionAlternative::getConfidence)).orElse(null);
                if (alternative == null) {
                    return;
                }
                String transcript = alternative.getTranscript();
                logger.debug("Alternative transcript: {}", transcript);
                logger.debug("Alternative confidence: {}", alternative.getConfidence());
                if (result.getIsFinal()) {
                    transcriptBuilder.append(transcript);
                    confidenceSum += alternative.getConfidence();
                    responseCount++;
                    // when in single utterance mode we can just get one final result so complete
                    if (config.singleUtteranceMode) {
                        onComplete();
                    }
                }
            });
        }

        @Override
        public void onComplete() {
            if (!aborted.getAndSet(true)) {
                sttListener.sttEventReceived(new RecognitionStopEvent());
                float averageConfidence = confidenceSum / responseCount;
                String transcript = transcriptBuilder.toString();
                if (!transcript.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionEvent(transcript, averageConfidence));
                } else if (!config.noResultsMessage.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.noResultsMessage));
                } else {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent("No results"));
                }
            }
        }

        @Override
        public void onError(@Nullable Throwable t) {
            logger.warn("Recognition error: ", t);
            if (!aborted.getAndSet(true)) {
                sttListener.sttEventReceived(new RecognitionStopEvent());
                if (!config.errorMessage.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
                } else {
                    String errorMessage = t.getMessage();
                    sttListener.sttEventReceived(
                            new SpeechRecognitionErrorEvent(errorMessage != null ? errorMessage : "Unknown error"));
                }
            }
        }

        public long getLastInputTime() {
            return lastInputTime;
        }
    }
}
