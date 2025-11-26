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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlayMethod;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackProgressInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackStartInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackStopInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RepeatMode;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserItemDataDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaystateApi {
    /**
     * Utility class for extending HttpRequest.Builder functionality.
     */
    private static class HttpRequestBuilderExtensions {
        /**
         * Adds additional headers to the provided HttpRequest.Builder. Useful for adding method/endpoint specific
         * headers.
         *
         * @param builder the HttpRequest.Builder to which headers will be added
         * @param headers a map of header names and values to add; may be null
         * @return the same HttpRequest.Builder instance with the additional headers set
         */
        static HttpRequest.Builder withAdditionalHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }
            return builder;
        }
    }

    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public PlaystateApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PlaystateApi(ApiClient apiClient) {
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
     * Download file from the given response.
     *
     * @param response Response
     * @return File
     * @throws ApiException If fail to read file content from response and write to disk
     */
    public File downloadFileFromResponse(HttpResponse<InputStream> response) throws ApiException {
        try {
            File file = prepareDownloadFile(response);
            java.nio.file.Files.copy(response.body(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    /**
     * <p>
     * Prepare the file for download from the response.
     * </p>
     *
     * @param response a {@link java.net.http.HttpResponse} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    private File prepareDownloadFile(HttpResponse<InputStream> response) throws IOException {
        String filename = null;
        java.util.Optional<String> contentDisposition = response.headers().firstValue("Content-Disposition");
        if (contentDisposition.isPresent() && !"".equals(contentDisposition.get())) {
            // Get filename from the Content-Disposition header.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
            java.util.regex.Matcher matcher = pattern.matcher(contentDisposition.get());
            if (matcher.find())
                filename = matcher.group(1);
        }
        File file = null;
        if (filename != null) {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("swagger-gen-native");
            java.nio.file.Path filePath = java.nio.file.Files.createFile(tempDir.resolve(filename));
            file = filePath.toFile();
            tempDir.toFile().deleteOnExit(); // best effort cleanup
            file.deleteOnExit(); // best effort cleanup
        } else {
            file = java.nio.file.Files.createTempFile("download-", "").toFile();
            file.deleteOnExit(); // best effort cleanup
        }
        return file;
    }

    /**
     * Marks an item as played for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param datePlayed Optional. The date the item was played. (optional)
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto markPlayedItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime datePlayed) throws ApiException {
        return markPlayedItem(itemId, userId, datePlayed, null);
    }

    /**
     * Marks an item as played for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param datePlayed Optional. The date the item was played. (optional)
     * @param headers Optional headers to include in the request
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto markPlayedItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime datePlayed, Map<String, String> headers)
            throws ApiException {
        ApiResponse<UserItemDataDto> localVarResponse = markPlayedItemWithHttpInfo(itemId, userId, datePlayed, headers);
        return localVarResponse.getData();
    }

    /**
     * Marks an item as played for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param datePlayed Optional. The date the item was played. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> markPlayedItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime datePlayed) throws ApiException {
        return markPlayedItemWithHttpInfo(itemId, userId, datePlayed, null);
    }

    /**
     * Marks an item as played for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param datePlayed Optional. The date the item was played. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> markPlayedItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime datePlayed, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = markPlayedItemRequestBuilder(itemId, userId, datePlayed, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("markPlayedItem", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                UserItemDataDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserItemDataDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder markPlayedItemRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime datePlayed, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling markPlayedItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserPlayedItems/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "datePlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("datePlayed", datePlayed));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto markUnplayedItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return markUnplayedItem(itemId, userId, null);
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return UserItemDataDto
     * @throws ApiException if fails to make API call
     */
    public UserItemDataDto markUnplayedItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<UserItemDataDto> localVarResponse = markUnplayedItemWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> markUnplayedItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return markUnplayedItemWithHttpInfo(itemId, userId, null);
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserItemDataDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserItemDataDto> markUnplayedItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = markUnplayedItemRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("markUnplayedItem", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                UserItemDataDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserItemDataDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<UserItemDataDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder markUnplayedItemRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling markUnplayedItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserPlayedItems/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param volumeLevel Scale of 0-100. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param repeatMode The repeat mode. (optional)
     * @param isPaused Indicates if the player is paused. (optional, default to false)
     * @param isMuted Indicates if the player is muted. (optional, default to false)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void onPlaybackProgress(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer volumeLevel,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode,
            @org.eclipse.jdt.annotation.NonNull Boolean isPaused, @org.eclipse.jdt.annotation.NonNull Boolean isMuted)
            throws ApiException {
        onPlaybackProgress(itemId, mediaSourceId, positionTicks, audioStreamIndex, subtitleStreamIndex, volumeLevel,
                playMethod, liveStreamId, playSessionId, repeatMode, isPaused, isMuted, null);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param volumeLevel Scale of 0-100. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param repeatMode The repeat mode. (optional)
     * @param isPaused Indicates if the player is paused. (optional, default to false)
     * @param isMuted Indicates if the player is muted. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void onPlaybackProgress(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer volumeLevel,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode,
            @org.eclipse.jdt.annotation.NonNull Boolean isPaused, @org.eclipse.jdt.annotation.NonNull Boolean isMuted,
            Map<String, String> headers) throws ApiException {
        onPlaybackProgressWithHttpInfo(itemId, mediaSourceId, positionTicks, audioStreamIndex, subtitleStreamIndex,
                volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused, isMuted, headers);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param volumeLevel Scale of 0-100. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param repeatMode The repeat mode. (optional)
     * @param isPaused Indicates if the player is paused. (optional, default to false)
     * @param isMuted Indicates if the player is muted. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> onPlaybackProgressWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer volumeLevel,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode,
            @org.eclipse.jdt.annotation.NonNull Boolean isPaused, @org.eclipse.jdt.annotation.NonNull Boolean isMuted)
            throws ApiException {
        return onPlaybackProgressWithHttpInfo(itemId, mediaSourceId, positionTicks, audioStreamIndex,
                subtitleStreamIndex, volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused,
                isMuted, null);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param volumeLevel Scale of 0-100. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param repeatMode The repeat mode. (optional)
     * @param isPaused Indicates if the player is paused. (optional, default to false)
     * @param isMuted Indicates if the player is muted. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> onPlaybackProgressWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer volumeLevel,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode,
            @org.eclipse.jdt.annotation.NonNull Boolean isPaused, @org.eclipse.jdt.annotation.NonNull Boolean isMuted,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = onPlaybackProgressRequestBuilder(itemId, mediaSourceId,
                positionTicks, audioStreamIndex, subtitleStreamIndex, volumeLevel, playMethod, liveStreamId,
                playSessionId, repeatMode, isPaused, isMuted, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("onPlaybackProgress", localVarResponse);
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

    private HttpRequest.Builder onPlaybackProgressRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer volumeLevel,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode,
            @org.eclipse.jdt.annotation.NonNull Boolean isPaused, @org.eclipse.jdt.annotation.NonNull Boolean isMuted,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling onPlaybackProgress");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/PlayingItems/{itemId}/Progress".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "positionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("positionTicks", positionTicks));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "volumeLevel";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("volumeLevel", volumeLevel));
        localVarQueryParameterBaseName = "playMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playMethod", playMethod));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "repeatMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("repeatMode", repeatMode));
        localVarQueryParameterBaseName = "isPaused";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isPaused", isPaused));
        localVarQueryParameterBaseName = "isMuted";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMuted", isMuted));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param canSeek Indicates if the client can seek. (optional, default to false)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void onPlaybackStart(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Boolean canSeek) throws ApiException {
        onPlaybackStart(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod, liveStreamId,
                playSessionId, canSeek, null);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param canSeek Indicates if the client can seek. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void onPlaybackStart(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Boolean canSeek, Map<String, String> headers) throws ApiException {
        onPlaybackStartWithHttpInfo(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod,
                liveStreamId, playSessionId, canSeek, headers);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param canSeek Indicates if the client can seek. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> onPlaybackStartWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Boolean canSeek) throws ApiException {
        return onPlaybackStartWithHttpInfo(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod,
                liveStreamId, playSessionId, canSeek, null);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param canSeek Indicates if the client can seek. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> onPlaybackStartWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Boolean canSeek, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = onPlaybackStartRequestBuilder(itemId, mediaSourceId,
                audioStreamIndex, subtitleStreamIndex, playMethod, liveStreamId, playSessionId, canSeek, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("onPlaybackStart", localVarResponse);
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

    private HttpRequest.Builder onPlaybackStartRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Boolean canSeek, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling onPlaybackStart");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/PlayingItems/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "playMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playMethod", playMethod));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "canSeek";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("canSeek", canSeek));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param nextMediaType The next media type that will play. (optional)
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void onPlaybackStopped(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String nextMediaType,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId) throws ApiException {
        onPlaybackStopped(itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId, playSessionId, null);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param nextMediaType The next media type that will play. (optional)
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void onPlaybackStopped(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String nextMediaType,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId, Map<String, String> headers) throws ApiException {
        onPlaybackStoppedWithHttpInfo(itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId, playSessionId,
                headers);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param nextMediaType The next media type that will play. (optional)
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> onPlaybackStoppedWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String nextMediaType,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId) throws ApiException {
        return onPlaybackStoppedWithHttpInfo(itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId,
                playSessionId, null);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param nextMediaType The next media type that will play. (optional)
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> onPlaybackStoppedWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String nextMediaType,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = onPlaybackStoppedRequestBuilder(itemId, mediaSourceId,
                nextMediaType, positionTicks, liveStreamId, playSessionId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("onPlaybackStopped", localVarResponse);
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

    private HttpRequest.Builder onPlaybackStoppedRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String nextMediaType,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling onPlaybackStopped");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/PlayingItems/{itemId}".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "nextMediaType";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nextMediaType", nextMediaType));
        localVarQueryParameterBaseName = "positionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("positionTicks", positionTicks));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Pings a playback session.
     * 
     * @param playSessionId Playback session id. (required)
     * @throws ApiException if fails to make API call
     */
    public void pingPlaybackSession(@org.eclipse.jdt.annotation.Nullable String playSessionId) throws ApiException {
        pingPlaybackSession(playSessionId, null);
    }

    /**
     * Pings a playback session.
     * 
     * @param playSessionId Playback session id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void pingPlaybackSession(@org.eclipse.jdt.annotation.Nullable String playSessionId,
            Map<String, String> headers) throws ApiException {
        pingPlaybackSessionWithHttpInfo(playSessionId, headers);
    }

    /**
     * Pings a playback session.
     * 
     * @param playSessionId Playback session id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> pingPlaybackSessionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String playSessionId)
            throws ApiException {
        return pingPlaybackSessionWithHttpInfo(playSessionId, null);
    }

    /**
     * Pings a playback session.
     * 
     * @param playSessionId Playback session id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> pingPlaybackSessionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String playSessionId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = pingPlaybackSessionRequestBuilder(playSessionId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("pingPlaybackSession", localVarResponse);
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

    private HttpRequest.Builder pingPlaybackSessionRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String playSessionId, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'playSessionId' is set
        if (playSessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playSessionId' when calling pingPlaybackSession");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Playing/Ping";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));

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

        localVarRequestBuilder.header("Accept", "text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports playback progress within a session.
     * 
     * @param playbackProgressInfo The playback progress info. (optional)
     * @throws ApiException if fails to make API call
     */
    public void reportPlaybackProgress(@org.eclipse.jdt.annotation.NonNull PlaybackProgressInfo playbackProgressInfo)
            throws ApiException {
        reportPlaybackProgress(playbackProgressInfo, null);
    }

    /**
     * Reports playback progress within a session.
     * 
     * @param playbackProgressInfo The playback progress info. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void reportPlaybackProgress(@org.eclipse.jdt.annotation.NonNull PlaybackProgressInfo playbackProgressInfo,
            Map<String, String> headers) throws ApiException {
        reportPlaybackProgressWithHttpInfo(playbackProgressInfo, headers);
    }

    /**
     * Reports playback progress within a session.
     * 
     * @param playbackProgressInfo The playback progress info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> reportPlaybackProgressWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackProgressInfo playbackProgressInfo) throws ApiException {
        return reportPlaybackProgressWithHttpInfo(playbackProgressInfo, null);
    }

    /**
     * Reports playback progress within a session.
     * 
     * @param playbackProgressInfo The playback progress info. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> reportPlaybackProgressWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackProgressInfo playbackProgressInfo, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = reportPlaybackProgressRequestBuilder(playbackProgressInfo,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("reportPlaybackProgress", localVarResponse);
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

    private HttpRequest.Builder reportPlaybackProgressRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull PlaybackProgressInfo playbackProgressInfo, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Playing/Progress";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(playbackProgressInfo);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports playback has started within a session.
     * 
     * @param playbackStartInfo The playback start info. (optional)
     * @throws ApiException if fails to make API call
     */
    public void reportPlaybackStart(@org.eclipse.jdt.annotation.NonNull PlaybackStartInfo playbackStartInfo)
            throws ApiException {
        reportPlaybackStart(playbackStartInfo, null);
    }

    /**
     * Reports playback has started within a session.
     * 
     * @param playbackStartInfo The playback start info. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void reportPlaybackStart(@org.eclipse.jdt.annotation.NonNull PlaybackStartInfo playbackStartInfo,
            Map<String, String> headers) throws ApiException {
        reportPlaybackStartWithHttpInfo(playbackStartInfo, headers);
    }

    /**
     * Reports playback has started within a session.
     * 
     * @param playbackStartInfo The playback start info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> reportPlaybackStartWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackStartInfo playbackStartInfo) throws ApiException {
        return reportPlaybackStartWithHttpInfo(playbackStartInfo, null);
    }

    /**
     * Reports playback has started within a session.
     * 
     * @param playbackStartInfo The playback start info. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> reportPlaybackStartWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackStartInfo playbackStartInfo, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = reportPlaybackStartRequestBuilder(playbackStartInfo, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("reportPlaybackStart", localVarResponse);
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

    private HttpRequest.Builder reportPlaybackStartRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull PlaybackStartInfo playbackStartInfo, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Playing";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(playbackStartInfo);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * @param playbackStopInfo The playback stop info. (optional)
     * @throws ApiException if fails to make API call
     */
    public void reportPlaybackStopped(@org.eclipse.jdt.annotation.NonNull PlaybackStopInfo playbackStopInfo)
            throws ApiException {
        reportPlaybackStopped(playbackStopInfo, null);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * @param playbackStopInfo The playback stop info. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void reportPlaybackStopped(@org.eclipse.jdt.annotation.NonNull PlaybackStopInfo playbackStopInfo,
            Map<String, String> headers) throws ApiException {
        reportPlaybackStoppedWithHttpInfo(playbackStopInfo, headers);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * @param playbackStopInfo The playback stop info. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> reportPlaybackStoppedWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackStopInfo playbackStopInfo) throws ApiException {
        return reportPlaybackStoppedWithHttpInfo(playbackStopInfo, null);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * @param playbackStopInfo The playback stop info. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> reportPlaybackStoppedWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackStopInfo playbackStopInfo, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = reportPlaybackStoppedRequestBuilder(playbackStopInfo, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("reportPlaybackStopped", localVarResponse);
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

    private HttpRequest.Builder reportPlaybackStoppedRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull PlaybackStopInfo playbackStopInfo, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Playing/Stopped";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(playbackStopInfo);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
