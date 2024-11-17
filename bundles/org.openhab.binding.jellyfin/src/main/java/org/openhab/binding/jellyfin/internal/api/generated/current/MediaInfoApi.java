/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LiveStreamResponse;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.OpenLiveStreamDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackInfoResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaInfoApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public MediaInfoApi() {
        this(Configuration.getDefaultApiClient());
    }

    public MediaInfoApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }

    protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
        String body = response.body() == null ? null : new String(response.body().readAllBytes());
        String message = formatExceptionMessage(operationId, response.statusCode(), body);
        return new ApiException(response.statusCode(), message, response.headers(), body);
    }

    private String formatExceptionMessage(String operationId, int statusCode, String body) {
        if (body == null || body.isEmpty()) {
            body = "[no body]";
        }
        return operationId + " call failed with: " + statusCode + " - " + body;
    }

    /**
     * Closes a media source.
     * 
     * @param liveStreamId The livestream id. (required)
     * @throws ApiException if fails to make API call
     */
    public void closeLiveStream(@org.eclipse.jdt.annotation.Nullable String liveStreamId) throws ApiException {
        closeLiveStreamWithHttpInfo(liveStreamId);
    }

    /**
     * Closes a media source.
     * 
     * @param liveStreamId The livestream id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> closeLiveStreamWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String liveStreamId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = closeLiveStreamRequestBuilder(liveStreamId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("closeLiveStream", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder closeLiveStreamRequestBuilder(@org.eclipse.jdt.annotation.Nullable String liveStreamId)
            throws ApiException {
        // verify the required parameter 'liveStreamId' is set
        if (liveStreamId == null) {
            throw new ApiException(400, "Missing the required parameter 'liveStreamId' when calling closeLiveStream");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveStreams/Close";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * @param size The bitrate. Defaults to 102400. (optional, default to 102400)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getBitrateTestBytes(@org.eclipse.jdt.annotation.NonNull Integer size) throws ApiException {
        ApiResponse<File> localVarResponse = getBitrateTestBytesWithHttpInfo(size);
        return localVarResponse.getData();
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * @param size The bitrate. Defaults to 102400. (optional, default to 102400)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getBitrateTestBytesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Integer size)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getBitrateTestBytesRequestBuilder(size);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getBitrateTestBytes", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getBitrateTestBytesRequestBuilder(@org.eclipse.jdt.annotation.NonNull Integer size)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playback/BitrateTest";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "size";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("size", size));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/octet-stream");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets live playback media info for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @return PlaybackInfoResponse
     * @throws ApiException if fails to make API call
     */
    public PlaybackInfoResponse getPlaybackInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        ApiResponse<PlaybackInfoResponse> localVarResponse = getPlaybackInfoWithHttpInfo(itemId, userId);
        return localVarResponse.getData();
    }

    /**
     * Gets live playback media info for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;PlaybackInfoResponse&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaybackInfoResponse> getPlaybackInfoWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaybackInfoRequestBuilder(itemId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaybackInfo", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<PlaybackInfoResponse>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<PlaybackInfoResponse>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<PlaybackInfoResponse>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getPlaybackInfoRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getPlaybackInfo");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/PlaybackInfo".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param liveStreamId The livestream id. (optional)
     * @param autoOpenLiveStream Whether to auto open the livestream. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param enableTranscoding Whether to enable transcoding. Default: true. (optional)
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true. (optional)
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true. (optional)
     * @param playbackInfoDto The playback info. (optional)
     * @return PlaybackInfoResponse
     * @throws ApiException if fails to make API call
     */
    public PlaybackInfoResponse getPostedPlaybackInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean autoOpenLiveStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTranscoding,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull PlaybackInfoDto playbackInfoDto) throws ApiException {
        ApiResponse<PlaybackInfoResponse> localVarResponse = getPostedPlaybackInfoWithHttpInfo(itemId, userId,
                maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels,
                mediaSourceId, liveStreamId, autoOpenLiveStream, enableDirectPlay, enableDirectStream,
                enableTranscoding, allowVideoStreamCopy, allowAudioStreamCopy, playbackInfoDto);
        return localVarResponse.getData();
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param liveStreamId The livestream id. (optional)
     * @param autoOpenLiveStream Whether to auto open the livestream. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param enableTranscoding Whether to enable transcoding. Default: true. (optional)
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true. (optional)
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true. (optional)
     * @param playbackInfoDto The playback info. (optional)
     * @return ApiResponse&lt;PlaybackInfoResponse&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaybackInfoResponse> getPostedPlaybackInfoWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean autoOpenLiveStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTranscoding,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull PlaybackInfoDto playbackInfoDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPostedPlaybackInfoRequestBuilder(itemId, userId,
                maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels,
                mediaSourceId, liveStreamId, autoOpenLiveStream, enableDirectPlay, enableDirectStream,
                enableTranscoding, allowVideoStreamCopy, allowAudioStreamCopy, playbackInfoDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPostedPlaybackInfo", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<PlaybackInfoResponse>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<PlaybackInfoResponse>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<PlaybackInfoResponse>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getPostedPlaybackInfoRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean autoOpenLiveStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTranscoding,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull PlaybackInfoDto playbackInfoDto) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getPostedPlaybackInfo");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/PlaybackInfo".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "autoOpenLiveStream";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("autoOpenLiveStream", autoOpenLiveStream));
        localVarQueryParameterBaseName = "enableDirectPlay";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableDirectPlay", enableDirectPlay));
        localVarQueryParameterBaseName = "enableDirectStream";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableDirectStream", enableDirectStream));
        localVarQueryParameterBaseName = "enableTranscoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTranscoding", enableTranscoding));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(playbackInfoDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Opens a media source.
     * 
     * @param openToken The open token. (optional)
     * @param userId The user id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param itemId The item id. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param alwaysBurnInSubtitleWhenTranscoding Always burn-in subtitle when transcoding. (optional)
     * @param openLiveStreamDto The open live stream dto. (optional)
     * @return LiveStreamResponse
     * @throws ApiException if fails to make API call
     */
    public LiveStreamResponse openLiveStream(@org.eclipse.jdt.annotation.NonNull String openToken,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding,
            @org.eclipse.jdt.annotation.NonNull OpenLiveStreamDto openLiveStreamDto) throws ApiException {
        ApiResponse<LiveStreamResponse> localVarResponse = openLiveStreamWithHttpInfo(openToken, userId, playSessionId,
                maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId,
                enableDirectPlay, enableDirectStream, alwaysBurnInSubtitleWhenTranscoding, openLiveStreamDto);
        return localVarResponse.getData();
    }

    /**
     * Opens a media source.
     * 
     * @param openToken The open token. (optional)
     * @param userId The user id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param itemId The item id. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param alwaysBurnInSubtitleWhenTranscoding Always burn-in subtitle when transcoding. (optional)
     * @param openLiveStreamDto The open live stream dto. (optional)
     * @return ApiResponse&lt;LiveStreamResponse&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LiveStreamResponse> openLiveStreamWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String openToken, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding,
            @org.eclipse.jdt.annotation.NonNull OpenLiveStreamDto openLiveStreamDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = openLiveStreamRequestBuilder(openToken, userId, playSessionId,
                maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId,
                enableDirectPlay, enableDirectStream, alwaysBurnInSubtitleWhenTranscoding, openLiveStreamDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("openLiveStream", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<LiveStreamResponse>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<LiveStreamResponse>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<LiveStreamResponse>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder openLiveStreamRequestBuilder(@org.eclipse.jdt.annotation.NonNull String openToken,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding,
            @org.eclipse.jdt.annotation.NonNull OpenLiveStreamDto openLiveStreamDto) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/LiveStreams/Open";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "openToken";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("openToken", openToken));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "itemId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemId", itemId));
        localVarQueryParameterBaseName = "enableDirectPlay";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableDirectPlay", enableDirectPlay));
        localVarQueryParameterBaseName = "enableDirectStream";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableDirectStream", enableDirectStream));
        localVarQueryParameterBaseName = "alwaysBurnInSubtitleWhenTranscoding";
        localVarQueryParams.addAll(
                ApiClient.parameterToPairs("alwaysBurnInSubtitleWhenTranscoding", alwaysBurnInSubtitleWhenTranscoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(openLiveStreamDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
