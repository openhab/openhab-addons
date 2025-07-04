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
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class HlsSegmentApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public HlsSegmentApi() {
        this(Configuration.getDefaultApiClient());
    }

    public HlsSegmentApi(ApiClient apiClient) {
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
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsAudioSegmentLegacyAac(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsAudioSegmentLegacyAacWithHttpInfo(itemId, segmentId);
        return localVarResponse.getData();
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsAudioSegmentLegacyAacWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsAudioSegmentLegacyAacRequestBuilder(itemId, segmentId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsAudioSegmentLegacyAac", localVarResponse);
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

    private HttpRequest.Builder getHlsAudioSegmentLegacyAacRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable String segmentId)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getHlsAudioSegmentLegacyAac");
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentId' when calling getHlsAudioSegmentLegacyAac");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/hls/{segmentId}/stream.aac"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{segmentId}", ApiClient.urlEncode(segmentId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "audio/*");

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
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsAudioSegmentLegacyMp3(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsAudioSegmentLegacyMp3WithHttpInfo(itemId, segmentId);
        return localVarResponse.getData();
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsAudioSegmentLegacyMp3WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsAudioSegmentLegacyMp3RequestBuilder(itemId, segmentId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsAudioSegmentLegacyMp3", localVarResponse);
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

    private HttpRequest.Builder getHlsAudioSegmentLegacyMp3RequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable String segmentId)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getHlsAudioSegmentLegacyMp3");
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentId' when calling getHlsAudioSegmentLegacyMp3");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/hls/{segmentId}/stream.mp3"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{segmentId}", ApiClient.urlEncode(segmentId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "audio/*");

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
     * Gets a hls video playlist.
     * 
     * @param itemId The video id. (required)
     * @param playlistId The playlist id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsPlaylistLegacy(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsPlaylistLegacyWithHttpInfo(itemId, playlistId);
        return localVarResponse.getData();
    }

    /**
     * Gets a hls video playlist.
     * 
     * @param itemId The video id. (required)
     * @param playlistId The playlist id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsPlaylistLegacyWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsPlaylistLegacyRequestBuilder(itemId, playlistId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsPlaylistLegacy", localVarResponse);
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

    private HttpRequest.Builder getHlsPlaylistLegacyRequestBuilder(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getHlsPlaylistLegacy");
        }
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling getHlsPlaylistLegacy");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/hls/{playlistId}/stream.m3u8"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

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
     * Gets a hls video segment.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param segmentContainer The segment container. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsVideoSegmentLegacy(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String segmentId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsVideoSegmentLegacyWithHttpInfo(itemId, playlistId, segmentId,
                segmentContainer);
        return localVarResponse.getData();
    }

    /**
     * Gets a hls video segment.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param segmentContainer The segment container. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsVideoSegmentLegacyWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String segmentId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsVideoSegmentLegacyRequestBuilder(itemId, playlistId,
                segmentId, segmentContainer);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsVideoSegmentLegacy", localVarResponse);
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

    private HttpRequest.Builder getHlsVideoSegmentLegacyRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String segmentId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getHlsVideoSegmentLegacy");
        }
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling getHlsVideoSegmentLegacy");
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentId' when calling getHlsVideoSegmentLegacy");
        }
        // verify the required parameter 'segmentContainer' is set
        if (segmentContainer == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentContainer' when calling getHlsVideoSegmentLegacy");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/hls/{playlistId}/{segmentId}.{segmentContainer}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()))
                .replace("{segmentId}", ApiClient.urlEncode(segmentId.toString()))
                .replace("{segmentContainer}", ApiClient.urlEncode(segmentContainer.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "video/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
     * @throws ApiException if fails to make API call
     */
    public void stopEncodingProcess(@org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId) throws ApiException {
        stopEncodingProcessWithHttpInfo(deviceId, playSessionId);
    }

    /**
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> stopEncodingProcessWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = stopEncodingProcessRequestBuilder(deviceId, playSessionId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("stopEncodingProcess", localVarResponse);
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

    private HttpRequest.Builder stopEncodingProcessRequestBuilder(@org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId) throws ApiException {
        // verify the required parameter 'deviceId' is set
        if (deviceId == null) {
            throw new ApiException(400, "Missing the required parameter 'deviceId' when calling stopEncodingProcess");
        }
        // verify the required parameter 'playSessionId' is set
        if (playSessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playSessionId' when calling stopEncodingProcess");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/ActiveEncodings";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
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

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
