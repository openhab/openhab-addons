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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LyricDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RemoteLyricInfoDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LyricsApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

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
     * Deletes an external lyric file.
     * 
     * @param itemId The item id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteLyrics(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        deleteLyricsWithHttpInfo(itemId);
    }

    /**
     * Deletes an external lyric file.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteLyricsRequestBuilder(itemId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteLyrics", localVarResponse);
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

    private HttpRequest.Builder deleteLyricsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/Lyrics".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
    public LyricDto downloadRemoteLyrics(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String lyricId) throws ApiException {
        ApiResponse<LyricDto> localVarResponse = downloadRemoteLyricsWithHttpInfo(itemId, lyricId);
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
    public ApiResponse<LyricDto> downloadRemoteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String lyricId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = downloadRemoteLyricsRequestBuilder(itemId, lyricId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("downloadRemoteLyrics", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
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

    private HttpRequest.Builder downloadRemoteLyricsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String lyricId) throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Gets an item&#39;s lyrics.
     * 
     * @param itemId Item id. (required)
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto getLyrics(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        ApiResponse<LyricDto> localVarResponse = getLyricsWithHttpInfo(itemId);
        return localVarResponse.getData();
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * @param itemId Item id. (required)
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> getLyricsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLyricsRequestBuilder(itemId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLyrics", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
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

    private HttpRequest.Builder getLyricsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/Lyrics".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Gets the remote lyrics.
     * 
     * @param lyricId The remote provider item id. (required)
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto getRemoteLyrics(@org.eclipse.jdt.annotation.Nullable String lyricId) throws ApiException {
        ApiResponse<LyricDto> localVarResponse = getRemoteLyricsWithHttpInfo(lyricId);
        return localVarResponse.getData();
    }

    /**
     * Gets the remote lyrics.
     * 
     * @param lyricId The remote provider item id. (required)
     * @return ApiResponse&lt;LyricDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<LyricDto> getRemoteLyricsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String lyricId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRemoteLyricsRequestBuilder(lyricId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRemoteLyrics", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
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

    private HttpRequest.Builder getRemoteLyricsRequestBuilder(@org.eclipse.jdt.annotation.Nullable String lyricId)
            throws ApiException {
        // verify the required parameter 'lyricId' is set
        if (lyricId == null) {
            throw new ApiException(400, "Missing the required parameter 'lyricId' when calling getRemoteLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Providers/Lyrics/{lyricId}".replace("{lyricId}",
                ApiClient.urlEncode(lyricId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Search remote lyrics.
     * 
     * @param itemId The item id. (required)
     * @return List&lt;RemoteLyricInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteLyricInfoDto> searchRemoteLyrics(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        ApiResponse<List<RemoteLyricInfoDto>> localVarResponse = searchRemoteLyricsWithHttpInfo(itemId);
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
            @org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = searchRemoteLyricsRequestBuilder(itemId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("searchRemoteLyrics", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteLyricInfoDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteLyricInfoDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteLyricInfoDto>>() {
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

    private HttpRequest.Builder searchRemoteLyricsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling searchRemoteLyrics");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/RemoteSearch/Lyrics".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Upload an external lyric file.
     * 
     * @param itemId The item the lyric belongs to. (required)
     * @param fileName Name of the file being uploaded. (required)
     * @param body (optional)
     * @return LyricDto
     * @throws ApiException if fails to make API call
     */
    public LyricDto uploadLyrics(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String fileName, @org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        ApiResponse<LyricDto> localVarResponse = uploadLyricsWithHttpInfo(itemId, fileName, body);
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
    public ApiResponse<LyricDto> uploadLyricsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String fileName, @org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = uploadLyricsRequestBuilder(itemId, fileName, body);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("uploadLyrics", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<LyricDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<LyricDto>() {
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

    private HttpRequest.Builder uploadLyricsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String fileName, @org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(body);
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
