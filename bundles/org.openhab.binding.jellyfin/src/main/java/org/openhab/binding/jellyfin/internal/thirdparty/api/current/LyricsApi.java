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
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiException;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiResponse;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Configuration;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Pair;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.LyricDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.RemoteLyricInfoDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LyricsApi {
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

    public LyricsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LyricsApi(ApiClient apiClient) {
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
     * Deletes an external lyric file.
     * 
     * @param itemId The item id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId) throws ApiException {
        deleteLyrics(itemId, null);
    }

    /**
     * Deletes an external lyric file.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId, Map<String, String> headers)
            throws ApiException {
        deleteLyricsWithHttpInfo(itemId, headers);
    }

    /**
     * Deletes an external lyric file.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId)
            throws ApiException {
        return deleteLyricsWithHttpInfo(itemId, null);
    }

    /**
     * Deletes an external lyric file.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteLyricsRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteLyrics", localVarResponse);
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

    private HttpRequest.Builder deleteLyricsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/Lyrics".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Downloads a remote lyric.
     * 
     * @param itemId The item id. (required)
     * @param lyricId The lyric id. (required)
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto downloadRemoteLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String lyricId) throws ApiException {
        return downloadRemoteLyrics(itemId, lyricId, null);
    }

    /**
     * Downloads a remote lyric.
     * 
     * @param itemId The item id. (required)
     * @param lyricId The lyric id. (required)
     * @param headers Optional headers to include in the request
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto downloadRemoteLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String lyricId, Map<String, String> headers) throws ApiException {
        ApiResponse<LyricDto> localVarResponse = downloadRemoteLyricsWithHttpInfo(itemId, lyricId, headers);
        return localVarResponse.getData();
    }

    /**
     * Downloads a remote lyric.
     * 
     * @param itemId The item id. (required)
     * @param lyricId The lyric id. (required)
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> downloadRemoteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String lyricId) throws ApiException {
        return downloadRemoteLyricsWithHttpInfo(itemId, lyricId, null);
    }

    /**
     * Downloads a remote lyric.
     * 
     * @param itemId The item id. (required)
     * @param lyricId The lyric id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> downloadRemoteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String lyricId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = downloadRemoteLyricsRequestBuilder(itemId, lyricId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("downloadRemoteLyrics", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                LyricDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
                        });

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder downloadRemoteLyricsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String lyricId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling downloadRemoteLyrics");
        }
        // verify the required parameter 'lyricId' is set
        if (lyricId == null) {
            throw new ApiException(400, "Missing the required parameter 'lyricId' when calling downloadRemoteLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/RemoteSearch/Lyrics/{lyricId}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{lyricId}", ApiClient.urlEncode(lyricId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Gets an item&#39;s lyrics.
     * 
     * @param itemId Item id. (required)
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto getLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId) throws ApiException {
        return getLyrics(itemId, null);
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * @param itemId Item id. (required)
     * @param headers Optional headers to include in the request
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto getLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<LyricDto> localVarResponse = getLyricsWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * @param itemId Item id. (required)
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> getLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId)
            throws ApiException {
        return getLyricsWithHttpInfo(itemId, null);
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * @param itemId Item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> getLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLyricsRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLyrics", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                LyricDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
                        });

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getLyricsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/Lyrics".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Gets the remote lyrics.
     * 
     * @param lyricId The remote provider item id. (required)
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto getRemoteLyrics(@org.eclipse.jdt.annotation.NonNull String lyricId) throws ApiException {
        return getRemoteLyrics(lyricId, null);
    }

    /**
     * Gets the remote lyrics.
     * 
     * @param lyricId The remote provider item id. (required)
     * @param headers Optional headers to include in the request
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto getRemoteLyrics(@org.eclipse.jdt.annotation.NonNull String lyricId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<LyricDto> localVarResponse = getRemoteLyricsWithHttpInfo(lyricId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the remote lyrics.
     * 
     * @param lyricId The remote provider item id. (required)
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> getRemoteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String lyricId)
            throws ApiException {
        return getRemoteLyricsWithHttpInfo(lyricId, null);
    }

    /**
     * Gets the remote lyrics.
     * 
     * @param lyricId The remote provider item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> getRemoteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String lyricId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRemoteLyricsRequestBuilder(lyricId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRemoteLyrics", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                LyricDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
                        });

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getRemoteLyricsRequestBuilder(@org.eclipse.jdt.annotation.NonNull String lyricId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'lyricId' is set
        if (lyricId == null) {
            throw new ApiException(400, "Missing the required parameter 'lyricId' when calling getRemoteLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Providers/Lyrics/{lyricId}".replace("{lyricId}",
                ApiClient.urlEncode(lyricId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Search remote lyrics.
     * 
     * @param itemId The item id. (required)
     * @return List&lt;RemoteLyricInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteLyricInfoDto> searchRemoteLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId)
            throws ApiException {
        return searchRemoteLyrics(itemId, null);
    }

    /**
     * Search remote lyrics.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteLyricInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteLyricInfoDto> searchRemoteLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteLyricInfoDto>> localVarResponse = searchRemoteLyricsWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Search remote lyrics.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;List&lt;RemoteLyricInfoDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteLyricInfoDto>> searchRemoteLyricsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId) throws ApiException {
        return searchRemoteLyricsWithHttpInfo(itemId, null);
    }

    /**
     * Search remote lyrics.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteLyricInfoDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteLyricInfoDto>> searchRemoteLyricsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = searchRemoteLyricsRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("searchRemoteLyrics", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteLyricInfoDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteLyricInfoDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteLyricInfoDto>>() {
                        });

                return new ApiResponse<List<RemoteLyricInfoDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
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

    private HttpRequest.Builder searchRemoteLyricsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling searchRemoteLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/RemoteSearch/Lyrics".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Upload an external lyric file.
     * 
     * @param itemId The item the lyric belongs to. (required)
     * @param fileName Name of the file being uploaded. (required)
     * @param body (optional)
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto uploadLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String fileName, @org.eclipse.jdt.annotation.Nullable File body)
            throws ApiException {
        return uploadLyrics(itemId, fileName, body, null);
    }

    /**
     * Upload an external lyric file.
     * 
     * @param itemId The item the lyric belongs to. (required)
     * @param fileName Name of the file being uploaded. (required)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto uploadLyrics(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String fileName, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        ApiResponse<LyricDto> localVarResponse = uploadLyricsWithHttpInfo(itemId, fileName, body, headers);
        return localVarResponse.getData();
    }

    /**
     * Upload an external lyric file.
     * 
     * @param itemId The item the lyric belongs to. (required)
     * @param fileName Name of the file being uploaded. (required)
     * @param body (optional)
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> uploadLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String fileName, @org.eclipse.jdt.annotation.Nullable File body)
            throws ApiException {
        return uploadLyricsWithHttpInfo(itemId, fileName, body, null);
    }

    /**
     * Upload an external lyric file.
     * 
     * @param itemId The item the lyric belongs to. (required)
     * @param fileName Name of the file being uploaded. (required)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> uploadLyricsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String fileName, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = uploadLyricsRequestBuilder(itemId, fileName, body, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("uploadLyrics", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                LyricDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
                        });

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder uploadLyricsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String fileName, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling uploadLyrics");
        }
        // verify the required parameter 'fileName' is set
        if (fileName == null) {
            throw new ApiException(400, "Missing the required parameter 'fileName' when calling uploadLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/Lyrics".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "fileName";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fileName", fileName));

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

        localVarRequestBuilder.header("Content-Type", "text/plain");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(body);
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
