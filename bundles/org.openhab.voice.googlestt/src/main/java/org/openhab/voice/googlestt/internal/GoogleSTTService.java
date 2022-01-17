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
package org.openhab.voice.googlestt.internal;

import static org.openhab.voice.googlestt.internal.GoogleSTTBindingConstants.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.voice.STTListener;
import org.openhab.core.voice.STTService;
import org.openhab.core.voice.STTServiceHandle;
import org.openhab.core.voice.SpeechRecognitionErrorEvent;
import org.openhab.core.voice.SpeechRecognitionEvent;
import org.openhab.core.voice.SpeechStartEvent;
import org.openhab.core.voice.SpeechStopEvent;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

import io.grpc.LoadBalancerRegistry;
import io.grpc.internal.PickFirstLoadBalancerProvider;

/**
 * The {@link GoogleSTTService} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME, description_uri = SERVICE_CATEGORY + ":"
        + SERVICE_ID)
public class GoogleSTTService implements STTService {

    private static final String GCP_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    private static final String GCP_TOKEN_URI = "https://accounts.google.com/o/oauth2/token";
    private static final String GCP_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
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
        var serviceConfig = new org.openhab.core.config.core.Configuration(config).as(GoogleSTTConfiguration.class);
        this.config = serviceConfig;
        executor.submit(() -> GoogleSTTLocale.loadLocales(serviceConfig.refreshSupportedLocales));
        String clientId = serviceConfig.clientId;
        String clientSecret = serviceConfig.clientSecret;
        if (!clientId.isEmpty() && !clientSecret.isEmpty()) {
            var oAuthService = oAuthFactory.createOAuthClientService(SERVICE_PID, GCP_TOKEN_URI, GCP_AUTH_URI, clientId,
                    clientSecret, GCP_SCOPE, false);
            this.oAuthService = oAuthService;
            if (!serviceConfig.oauthCode.isEmpty()) {
                getAccessToken(oAuthService, serviceConfig.oauthCode);
                deleteAuthCode();
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
        var scheduledTask = backgroundRecognize(sttListener, audioStream, locale, set);
        return () -> scheduledTask.cancel(true);
    }

    private void getAccessToken(OAuthClientService oAuthService, String oauthCode) {
        logger.debug("Trying to get access and refresh tokens.");
        try {
            oAuthService.getAccessTokenResponseByAuthorizationCode(oauthCode, GCP_REDIRECT_URI);
        } catch (OAuthException | OAuthResponseException e) {
            logger.debug("Error fetching access token: {}", e.getMessage(), e);
            logger.error("Error fetching access token. Invalid oauth code? Please generate a new one.");
        } catch (IOException e) {
            logger.error("An unexpected IOException occurred: {}", e.getMessage());
        }
    }

    private void deleteAuthCode() {
        try {
            Configuration serviceConfig = configAdmin.getConfiguration(SERVICE_PID);
            var configProperties = serviceConfig.getProperties();
            if (configProperties != null) {
                configProperties.put("oauthCode", "");
                serviceConfig.update(configProperties);
            }
        } catch (IOException e) {
            logger.error("Failed to delete current oauth code, please delete it manually.");
        }
    }

    private Future<?> backgroundRecognize(STTListener sttListener, AudioStream audioStream, Locale locale,
            Set<String> set) {
        var credentials = getCredentials();
        return executor.submit(() -> {
            logger.debug("Background recognize starting");
            ClientStream<StreamingRecognizeRequest> clientStream = null;
            try (SpeechClient client = SpeechClient
                    .create(SpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build())) {
                AtomicBoolean keepStreaming = new AtomicBoolean(true);
                var responseObserver = new TranscriptionListener(sttListener, config, (t) -> keepStreaming.set(false));
                clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);
                streamAudio(clientStream, audioStream, responseObserver, keepStreaming, locale);
                clientStream.closeSend();
                logger.debug("Background recognize done");
            } catch (IOException e) {
                if (clientStream != null && clientStream.isSendReady()) {
                    clientStream.closeSendWithError(e);
                } else if (!config.errorMessage.isBlank()) {
                    logger.error("Error running speech to text: {}", e.getMessage());
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
                }
            }
        });
    }

    private void streamAudio(ClientStream<StreamingRecognizeRequest> clientStream, AudioStream audioStream,
            TranscriptionListener responseObserver, AtomicBoolean keepStreaming, Locale locale) throws IOException {
        // Gather stream info and send config
        var streamFormat = audioStream.getFormat();
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
        while (keepStreaming.get()) {
            byte[] data = new byte[6400];
            var dataN = audioStream.read(data);
            if (!keepStreaming.get() || isExpiredInterval(maxTranscriptionMillis, startTime)) { // 60 seconds
                logger.debug("Stops listening, max transcription time reached");
                break;
            }
            if (!config.singleUtteranceMode
                    && isExpiredInterval(maxSilenceMillis, responseObserver.getLastInputTime())) { // 60 seconds
                logger.debug("Stops listening, max silence time reached");
                break;
            }
            var dataRequest = StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build();
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
        try {
            var oAuthService = this.oAuthService;
            if (oAuthService != null) {
                var response = oAuthService.getAccessTokenResponse();
                if (response != null) {
                    accessToken = response.getAccessToken();
                }
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.error("Access token error: {}", e.getMessage());
        }
        if (accessToken == null) {
            logger.warn("Missed google cloud access token");
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
        private final Consumer<@Nullable Throwable> completeListener;
        private float confidenceSum = 0;
        private int responseCount = 0;
        private long lastInputTime = 0;

        public TranscriptionListener(STTListener sttListener, GoogleSTTConfiguration config,
                Consumer<@Nullable Throwable> completeListener) {
            this.sttListener = sttListener;
            this.config = config;
            this.completeListener = completeListener;
        }

        public void onStart(@Nullable StreamController controller) {
            sttListener.sttEventReceived(new SpeechStartEvent());
            lastInputTime = System.currentTimeMillis();
        }

        public void onResponse(StreamingRecognizeResponse response) {
            lastInputTime = System.currentTimeMillis();
            var results = response.getResultsList();
            logger.debug("Got {} results", response.getResultsList().size());
            if (results.isEmpty()) {
                logger.debug("No results");
                return;
            }
            results.forEach(result -> {
                var alternatives = result.getAlternativesList();
                logger.debug("Got {} alternatives", alternatives.size());
                SpeechRecognitionAlternative alternative = alternatives.stream()
                        .min(Comparator.comparing(SpeechRecognitionAlternative::getConfidence)).orElse(null);
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
                        completeListener.accept(null);
                    }
                }
            });
        }

        public void onComplete() {
            sttListener.sttEventReceived(new SpeechStopEvent());
            float averageConfidence = confidenceSum / (float) responseCount;
            var transcript = transcriptBuilder.toString();
            if (!transcript.isBlank()) {
                sttListener.sttEventReceived(new SpeechRecognitionEvent(transcript, averageConfidence));
            } else {
                if (!config.noResultsMessage.isBlank()) {
                    sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.noResultsMessage));
                }
            }
        }

        public void onError(@Nullable Throwable t) {
            logger.error("Recognition error: ", t);
            completeListener.accept(t);
            sttListener.sttEventReceived(new SpeechStopEvent());
            if (!config.errorMessage.isBlank()) {
                sttListener.sttEventReceived(new SpeechRecognitionErrorEvent(config.errorMessage));
            }
        }

        public long getLastInputTime() {
            return lastInputTime;
        }
    }
}
