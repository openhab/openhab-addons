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
package org.openhab.binding.jellyfin.internal.thirdparty.api.current;

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
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiException;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiResponse;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Configuration;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class HlsSegmentApi {
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
    private final Consumer<HttpResponse<InputStream>> memberVarAsyncResponseInterceptor;

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
        InputStream responseBody = ApiClient.getResponseBody(response);
        String body = null;
        try {
            body = responseBody == null ? null : new String(responseBody.readAllBytes());
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
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
    public File downloadFileFromResponse(HttpResponse<InputStream> response, InputStream responseBody)
            throws ApiException {
        if (responseBody == null) {
            throw new ApiException(new IOException("Response body is empty"));
        }
        try {
            File file = prepareDownloadFile(response);
            java.nio.file.Files.copy(responseBody, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsAudioSegmentLegacyAac(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId) throws ApiException {
        return getHlsAudioSegmentLegacyAac(itemId, segmentId, null);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsAudioSegmentLegacyAac(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsAudioSegmentLegacyAacWithHttpInfo(itemId, segmentId, headers);
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
    public ApiResponse<File> getHlsAudioSegmentLegacyAacWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId) throws ApiException {
        return getHlsAudioSegmentLegacyAacWithHttpInfo(itemId, segmentId, null);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsAudioSegmentLegacyAacWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsAudioSegmentLegacyAacRequestBuilder(itemId, segmentId,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsAudioSegmentLegacyAac", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getHlsAudioSegmentLegacyAacRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull String segmentId,
            Map<String, String> headers) throws ApiException {
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

        localVarRequestBuilder.header("Accept", "audio/*, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsAudioSegmentLegacyMp3(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId) throws ApiException {
        return getHlsAudioSegmentLegacyMp3(itemId, segmentId, null);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsAudioSegmentLegacyMp3(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsAudioSegmentLegacyMp3WithHttpInfo(itemId, segmentId, headers);
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
    public ApiResponse<File> getHlsAudioSegmentLegacyMp3WithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId) throws ApiException {
        return getHlsAudioSegmentLegacyMp3WithHttpInfo(itemId, segmentId, null);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsAudioSegmentLegacyMp3WithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String segmentId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsAudioSegmentLegacyMp3RequestBuilder(itemId, segmentId,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsAudioSegmentLegacyMp3", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getHlsAudioSegmentLegacyMp3RequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull String segmentId,
            Map<String, String> headers) throws ApiException {
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

        localVarRequestBuilder.header("Accept", "audio/*, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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
     * Gets a hls video playlist.
     * 
     * @param itemId The video id. (required)
     * @param playlistId The playlist id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsPlaylistLegacy(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId) throws ApiException {
        return getHlsPlaylistLegacy(itemId, playlistId, null);
    }

    /**
     * Gets a hls video playlist.
     * 
     * @param itemId The video id. (required)
     * @param playlistId The playlist id. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsPlaylistLegacy(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsPlaylistLegacyWithHttpInfo(itemId, playlistId, headers);
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
    public ApiResponse<File> getHlsPlaylistLegacyWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId) throws ApiException {
        return getHlsPlaylistLegacyWithHttpInfo(itemId, playlistId, null);
    }

    /**
     * Gets a hls video playlist.
     * 
     * @param itemId The video id. (required)
     * @param playlistId The playlist id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsPlaylistLegacyWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsPlaylistLegacyRequestBuilder(itemId, playlistId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsPlaylistLegacy", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getHlsPlaylistLegacyRequestBuilder(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId, Map<String, String> headers) throws ApiException {
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

        localVarRequestBuilder.header("Accept", "application/x-mpegURL, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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
     * Gets a hls video segment.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param segmentContainer The segment container. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsVideoSegmentLegacy(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId, @org.eclipse.jdt.annotation.NonNull String segmentId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer) throws ApiException {
        return getHlsVideoSegmentLegacy(itemId, playlistId, segmentId, segmentContainer, null);
    }

    /**
     * Gets a hls video segment.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param segmentContainer The segment container. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsVideoSegmentLegacy(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId, @org.eclipse.jdt.annotation.NonNull String segmentId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getHlsVideoSegmentLegacyWithHttpInfo(itemId, playlistId, segmentId,
                segmentContainer, headers);
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
    public ApiResponse<File> getHlsVideoSegmentLegacyWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId, @org.eclipse.jdt.annotation.NonNull String segmentId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer) throws ApiException {
        return getHlsVideoSegmentLegacyWithHttpInfo(itemId, playlistId, segmentId, segmentContainer, null);
    }

    /**
     * Gets a hls video segment.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param segmentContainer The segment container. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsVideoSegmentLegacyWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String itemId,
            @org.eclipse.jdt.annotation.NonNull String playlistId, @org.eclipse.jdt.annotation.NonNull String segmentId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsVideoSegmentLegacyRequestBuilder(itemId, playlistId,
                segmentId, segmentContainer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsVideoSegmentLegacy", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getHlsVideoSegmentLegacyRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.NonNull String segmentId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer, Map<String, String> headers)
            throws ApiException {
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
                "video/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
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
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
     * @throws ApiException if fails to make API call
     */
    public void stopEncodingProcess(@org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId) throws ApiException {
        stopEncodingProcess(deviceId, playSessionId, null);
    }

    /**
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void stopEncodingProcess(@org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId, Map<String, String> headers) throws ApiException {
        stopEncodingProcessWithHttpInfo(deviceId, playSessionId, headers);
    }

    /**
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> stopEncodingProcessWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId) throws ApiException {
        return stopEncodingProcessWithHttpInfo(deviceId, playSessionId, null);
    }

    /**
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> stopEncodingProcessWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = stopEncodingProcessRequestBuilder(deviceId, playSessionId,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("stopEncodingProcess", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder stopEncodingProcessRequestBuilder(@org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId, Map<String, String> headers) throws ApiException {
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
}
