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
package org.openhab.voice.googletts.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.auth.AuthenticationException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.voice.googletts.internal.dto.AudioConfig;
import org.openhab.voice.googletts.internal.dto.AudioEncoding;
import org.openhab.voice.googletts.internal.dto.ListVoicesResponse;
import org.openhab.voice.googletts.internal.dto.SsmlVoiceGender;
import org.openhab.voice.googletts.internal.dto.SynthesisInput;
import org.openhab.voice.googletts.internal.dto.SynthesizeSpeechRequest;
import org.openhab.voice.googletts.internal.dto.SynthesizeSpeechResponse;
import org.openhab.voice.googletts.internal.dto.Voice;
import org.openhab.voice.googletts.internal.dto.VoiceSelectionParams;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Google Cloud TTS API call implementation.
 *
 * @author Gabor Bicskei - Initial contribution and API
 */
class GoogleCloudAPI {

    private static final String BEARER = "Bearer ";

    private static final String GCP_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    private static final String GCP_TOKEN_URI = "https://accounts.google.com/o/oauth2/token";
    private static final String GCP_REDIRECT_URI = "https://www.google.com";
    /**
     * Google Cloud Platform authorization scope
     */
    private static final String GCP_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    /**
     * URL used for retrieving the list of available voices
     */
    private static final String LIST_VOICES_URL = "https://texttospeech.googleapis.com/v1/voices";

    /**
     * URL used for synthesizing text to speech
     */
    private static final String SYTNHESIZE_SPEECH_URL = "https://texttospeech.googleapis.com/v1/text:synthesize";

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(GoogleCloudAPI.class);

    /**
     * Supported voices and locales
     */
    private final Map<Locale, Set<GoogleTTSVoice>> voices = new HashMap<>();

    /**
     * Configuration
     */
    private @Nullable GoogleTTSConfig config;

    private final Gson gson = new GsonBuilder().create();
    private final ConfigurationAdmin configAdmin;
    private final OAuthFactory oAuthFactory;

    private @Nullable OAuthClientService oAuthService;

    /**
     * Constructor.
     *
     */
    GoogleCloudAPI(ConfigurationAdmin configAdmin, OAuthFactory oAuthFactory) {
        this.configAdmin = configAdmin;
        this.oAuthFactory = oAuthFactory;
    }

    /**
     * Configuration update.
     *
     * @param config New configuration.
     */
    void setConfig(GoogleTTSConfig config) {
        this.config = config;

        if (oAuthService != null) {
            oAuthFactory.ungetOAuthService(GoogleTTSService.SERVICE_PID);
            oAuthService = null;
        }

        String clientId = config.clientId;
        String clientSecret = config.clientSecret;
        if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
            final OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(GoogleTTSService.SERVICE_PID,
                    GCP_TOKEN_URI, GCP_AUTH_URI, clientId, clientSecret, GCP_SCOPE, false);
            this.oAuthService = oAuthService;
            try {
                getAccessToken();
                initVoices();
            } catch (AuthenticationException | CommunicationException e) {
                logger.warn("Error initializing Google Cloud TTS service: {}", e.getMessage());
                oAuthFactory.ungetOAuthService(GoogleTTSService.SERVICE_PID);
                this.oAuthService = null;
                voices.clear();
            }
        } else {
            voices.clear();
        }
    }

    public void dispose() {
        if (oAuthService != null) {
            oAuthFactory.ungetOAuthService(GoogleTTSService.SERVICE_PID);
            oAuthService = null;
        }
        voices.clear();
    }

    /**
     * Fetches the OAuth2 tokens from Google Cloud Platform if the auth-code is set in the configuration. If successful
     * the auth-code will be removed from the configuration.
     *
     * @throws AuthenticationException
     * @throws CommunicationException
     */
    @SuppressWarnings("null")
    private void getAccessToken() throws AuthenticationException, CommunicationException {
        String authcode = config.authcode;
        if (authcode != null && !authcode.isEmpty()) {
            logger.debug("Trying to get access and refresh tokens.");
            try {
                AccessTokenResponse response = oAuthService.getAccessTokenResponseByAuthorizationCode(authcode,
                        GCP_REDIRECT_URI);
                if (response.getRefreshToken() == null || response.getRefreshToken().isEmpty()) {
                    throw new AuthenticationException("Error fetching refresh token. Please reauthorize");
                }
            } catch (OAuthException | OAuthResponseException e) {
                logger.debug("Error fetching access token: {}", e.getMessage(), e);
                throw new AuthenticationException(
                        "Error fetching access token. Invalid authcode? Please generate a new one.");
            } catch (IOException e) {
                throw new CommunicationException(
                        String.format("An unexpected IOException occurred: %s", e.getMessage()));
            }

            config.authcode = null;

            try {
                Configuration serviceConfig = configAdmin.getConfiguration(GoogleTTSService.SERVICE_PID);
                Dictionary<String, Object> configProperties = serviceConfig.getProperties();
                if (configProperties != null) {
                    configProperties.put(GoogleTTSService.PARAM_AUTHCODE, "");
                    serviceConfig.update(configProperties);
                }
            } catch (IOException e) {
                // should not happen
                logger.warn(
                        "Failed to update configuration for Google Cloud TTS service. Please clear the 'authcode' configuration parameter manualy.");
            }
        }
    }

    @SuppressWarnings("null")
    private String getAuthorizationHeader() throws AuthenticationException, CommunicationException {
        final AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = oAuthService.getAccessTokenResponse();
        } catch (OAuthException | OAuthResponseException e) {
            logger.debug("Error fetching access token: {}", e.getMessage(), e);
            throw new AuthenticationException(
                    "Error fetching access token. Invalid authcode? Please generate a new one.");
        } catch (IOException e) {
            throw new CommunicationException(String.format("An unexpected IOException occurred: %s", e.getMessage()));
        }
        if (accessTokenResponse == null || accessTokenResponse.getAccessToken() == null
                || accessTokenResponse.getAccessToken().isEmpty()) {
            throw new AuthenticationException("No access token. Is this thing authorized?");
        }
        if (accessTokenResponse.getRefreshToken() == null || accessTokenResponse.getRefreshToken().isEmpty()) {
            throw new AuthenticationException("No refresh token. Please reauthorize");
        }
        return BEARER + accessTokenResponse.getAccessToken();
    }

    /**
     * Loads supported audio formats
     *
     * @return Set of audio formats
     */
    Set<String> getSupportedAudioFormats() {
        Set<String> formats = new HashSet<>();
        for (AudioEncoding audioEncoding : AudioEncoding.values()) {
            if (audioEncoding != AudioEncoding.AUDIO_ENCODING_UNSPECIFIED) {
                formats.add(audioEncoding.toString());
            }
        }
        return formats;
    }

    /**
     * Supported locales.
     *
     * @return Set of locales
     */
    Set<Locale> getSupportedLocales() {
        return voices.keySet();
    }

    /**
     * Supported voices for locale.
     *
     * @param locale Locale
     * @return Set of voices
     */
    Set<GoogleTTSVoice> getVoicesForLocale(Locale locale) {
        Set<GoogleTTSVoice> localeVoices = voices.get(locale);
        return localeVoices != null ? localeVoices : Set.of();
    }

    /**
     * Google API call to load locales and voices.
     *
     * @throws AuthenticationException
     * @throws CommunicationException
     */
    private void initVoices() throws AuthenticationException, CommunicationException {
        if (oAuthService != null) {
            voices.clear();
            for (GoogleTTSVoice voice : listVoices()) {
                Locale locale = voice.getLocale();
                Set<GoogleTTSVoice> localeVoices;
                if (!voices.containsKey(locale)) {
                    localeVoices = new HashSet<>();
                    voices.put(locale, localeVoices);
                } else {
                    localeVoices = voices.get(locale);
                }
                localeVoices.add(voice);
            }
        } else {
            logger.error("Google client is not initialized!");
        }
    }

    @SuppressWarnings("null")
    private List<GoogleTTSVoice> listVoices() throws AuthenticationException, CommunicationException {
        HttpRequestBuilder builder = HttpRequestBuilder.getFrom(LIST_VOICES_URL)
                .withHeader(HttpHeader.AUTHORIZATION.name(), getAuthorizationHeader());

        try {
            ListVoicesResponse listVoicesResponse = gson.fromJson(builder.getContentAsString(),
                    ListVoicesResponse.class);

            if (listVoicesResponse == null || listVoicesResponse.getVoices() == null) {
                return List.of();
            }

            List<GoogleTTSVoice> result = new ArrayList<>();
            for (Voice voice : listVoicesResponse.getVoices()) {
                for (String languageCode : voice.getLanguageCodes()) {
                    result.add(new GoogleTTSVoice(Locale.forLanguageTag(languageCode), voice.getName(),
                            voice.getSsmlGender().name()));
                }
            }
            return result;
        } catch (JsonSyntaxException e) {
            // do nothing
        } catch (IOException e) {
            throw new CommunicationException(String.format("An unexpected IOException occurred: %s", e.getMessage()));
        }
        return List.of();
    }

    /**
     * Converts audio format to Google parameters.
     *
     * @param codec Requested codec
     * @return String array of Google audio format and the file extension to use.
     */
    private String getFormatForCodec(String codec) {
        switch (codec) {
            case AudioFormat.CODEC_MP3:
                return AudioEncoding.MP3.toString();
            case AudioFormat.CODEC_PCM_SIGNED:
                return AudioEncoding.LINEAR16.toString();
            default:
                throw new IllegalArgumentException("Audio format " + codec + " is not yet supported");
        }
    }

    public byte[] synthesizeSpeech(String text, GoogleTTSVoice voice, String codec) {
        String format = getFormatForCodec(codec);
        try {
            return synthesizeSpeechByGoogle(text, voice, format);
        } catch (AuthenticationException e) {
            logger.warn("Error authenticating Google Cloud TTS service: {}", e.getMessage());
            if (oAuthService != null) {
                oAuthFactory.ungetOAuthService(GoogleTTSService.SERVICE_PID);
                oAuthService = null;
            }
        } catch (CommunicationException e) {
            logger.warn("Error initializing Google Cloud TTS service: {}", e.getMessage());
        }
        voices.clear();
        return null;
    }

    /**
     * Call Google service to synthesize the required text
     *
     * @param text Text to synthesize
     * @param voice Voice parameter
     * @param audioFormat Audio encoding format
     * @return Audio input stream or {@code null} when encoding exceptions occur
     * @throws AuthenticationException
     * @throws CommunicationException
     */
    @SuppressWarnings("null")
    private byte[] synthesizeSpeechByGoogle(String text, GoogleTTSVoice voice, String audioFormat)
            throws AuthenticationException, CommunicationException {
        AudioConfig audioConfig = new AudioConfig(AudioEncoding.valueOf(audioFormat), config.pitch, config.speakingRate,
                config.volumeGainDb);
        SynthesisInput synthesisInput = new SynthesisInput(text);
        VoiceSelectionParams voiceSelectionParams = new VoiceSelectionParams(voice.getLocale().getLanguage(),
                voice.getLabel(), SsmlVoiceGender.valueOf(voice.getSsmlGender()));

        SynthesizeSpeechRequest request = new SynthesizeSpeechRequest(audioConfig, synthesisInput,
                voiceSelectionParams);

        HttpRequestBuilder builder = HttpRequestBuilder.postTo(SYTNHESIZE_SPEECH_URL)
                .withHeader(HttpHeader.AUTHORIZATION.name(), getAuthorizationHeader())
                .withContent(gson.toJson(request), MimeTypes.Type.APPLICATION_JSON.name());

        try {
            SynthesizeSpeechResponse synthesizeSpeechResponse = gson.fromJson(builder.getContentAsString(),
                    SynthesizeSpeechResponse.class);

            if (synthesizeSpeechResponse == null) {
                return null;
            }

            byte[] encodedBytes = synthesizeSpeechResponse.getAudioContent().getBytes(StandardCharsets.UTF_8);
            return Base64.getDecoder().decode(encodedBytes);
        } catch (JsonSyntaxException e) {
            // do nothing
        } catch (IOException e) {
            throw new CommunicationException(String.format("An unexpected IOException occurred: %s", e.getMessage()));
        }
        return null;
    }

    boolean isInitialized() {
        return oAuthService != null;
    }
}
