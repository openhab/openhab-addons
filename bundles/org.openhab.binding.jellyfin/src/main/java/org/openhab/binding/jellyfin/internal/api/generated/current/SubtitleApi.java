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
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.FontFile;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RemoteSubtitleInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UploadSubtitleDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SubtitleApi {
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

    public SubtitleApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SubtitleApi(ApiClient apiClient) {
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
     * Deletes an external subtitle file.
     * 
     * @param itemId The item id. (required)
     * @param index The index of the subtitle file. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteSubtitle(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        deleteSubtitle(itemId, index, null);
    }

    /**
     * Deletes an external subtitle file.
     * 
     * @param itemId The item id. (required)
     * @param index The index of the subtitle file. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteSubtitle(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index, Map<String, String> headers) throws ApiException {
        deleteSubtitleWithHttpInfo(itemId, index, headers);
    }

    /**
     * Deletes an external subtitle file.
     * 
     * @param itemId The item id. (required)
     * @param index The index of the subtitle file. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        return deleteSubtitleWithHttpInfo(itemId, index, null);
    }

    /**
     * Deletes an external subtitle file.
     * 
     * @param itemId The item id. (required)
     * @param index The index of the subtitle file. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteSubtitleRequestBuilder(itemId, index, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteSubtitle", localVarResponse);
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

    private HttpRequest.Builder deleteSubtitleRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteSubtitle");
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new ApiException(400, "Missing the required parameter 'index' when calling deleteSubtitle");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/Subtitles/{index}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{index}", ApiClient.urlEncode(index.toString()));

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
     * Downloads a remote subtitle.
     * 
     * @param itemId The item id. (required)
     * @param subtitleId The subtitle id. (required)
     * @throws ApiException if fails to make API call
     */
    public void downloadRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String subtitleId) throws ApiException {
        downloadRemoteSubtitles(itemId, subtitleId, null);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * @param itemId The item id. (required)
     * @param subtitleId The subtitle id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void downloadRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String subtitleId, Map<String, String> headers) throws ApiException {
        downloadRemoteSubtitlesWithHttpInfo(itemId, subtitleId, headers);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * @param itemId The item id. (required)
     * @param subtitleId The subtitle id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> downloadRemoteSubtitlesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String subtitleId) throws ApiException {
        return downloadRemoteSubtitlesWithHttpInfo(itemId, subtitleId, null);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * @param itemId The item id. (required)
     * @param subtitleId The subtitle id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> downloadRemoteSubtitlesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String subtitleId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = downloadRemoteSubtitlesRequestBuilder(itemId, subtitleId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("downloadRemoteSubtitles", localVarResponse);
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

    private HttpRequest.Builder downloadRemoteSubtitlesRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String subtitleId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling downloadRemoteSubtitles");
        }
        // verify the required parameter 'subtitleId' is set
        if (subtitleId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'subtitleId' when calling downloadRemoteSubtitles");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/RemoteSearch/Subtitles/{subtitleId}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{subtitleId}", ApiClient.urlEncode(subtitleId.toString()));

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
     * Gets a fallback font file.
     * 
     * @param name The name of the fallback font file to get. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getFallbackFont(@org.eclipse.jdt.annotation.Nullable String name) throws ApiException {
        return getFallbackFont(name, null);
    }

    /**
     * Gets a fallback font file.
     * 
     * @param name The name of the fallback font file to get. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getFallbackFont(@org.eclipse.jdt.annotation.Nullable String name, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getFallbackFontWithHttpInfo(name, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a fallback font file.
     * 
     * @param name The name of the fallback font file to get. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getFallbackFontWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name)
            throws ApiException {
        return getFallbackFontWithHttpInfo(name, null);
    }

    /**
     * Gets a fallback font file.
     * 
     * @param name The name of the fallback font file to get. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getFallbackFontWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getFallbackFontRequestBuilder(name, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getFallbackFont", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getFallbackFontRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getFallbackFont");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/FallbackFont/Fonts/{name}".replace("{name}", ApiClient.urlEncode(name.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "font/*, text/html");

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
     * Gets a list of available fallback font files.
     * 
     * @return List&lt;FontFile&gt;
     * @throws ApiException if fails to make API call
     */
    public List<FontFile> getFallbackFontList() throws ApiException {
        return getFallbackFontList(null);
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;FontFile&gt;
     * @throws ApiException if fails to make API call
     */
    public List<FontFile> getFallbackFontList(Map<String, String> headers) throws ApiException {
        ApiResponse<List<FontFile>> localVarResponse = getFallbackFontListWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * @return ApiResponse&lt;List&lt;FontFile&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<FontFile>> getFallbackFontListWithHttpInfo() throws ApiException {
        return getFallbackFontListWithHttpInfo(null);
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;FontFile&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<FontFile>> getFallbackFontListWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getFallbackFontListRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getFallbackFontList", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<FontFile>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<FontFile> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<FontFile>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<FontFile>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getFallbackFontListRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/FallbackFont/Fonts";

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
     * Gets the remote subtitles.
     * 
     * @param subtitleId The item id. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable String subtitleId) throws ApiException {
        return getRemoteSubtitles(subtitleId, null);
    }

    /**
     * Gets the remote subtitles.
     * 
     * @param subtitleId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable String subtitleId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getRemoteSubtitlesWithHttpInfo(subtitleId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the remote subtitles.
     * 
     * @param subtitleId The item id. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getRemoteSubtitlesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String subtitleId)
            throws ApiException {
        return getRemoteSubtitlesWithHttpInfo(subtitleId, null);
    }

    /**
     * Gets the remote subtitles.
     * 
     * @param subtitleId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getRemoteSubtitlesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String subtitleId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRemoteSubtitlesRequestBuilder(subtitleId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRemoteSubtitles", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getRemoteSubtitlesRequestBuilder(@org.eclipse.jdt.annotation.Nullable String subtitleId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'subtitleId' is set
        if (subtitleId == null) {
            throw new ApiException(400, "Missing the required parameter 'subtitleId' when calling getRemoteSubtitles");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Providers/Subtitles/Subtitles/{subtitleId}".replace("{subtitleId}",
                ApiClient.urlEncode(subtitleId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/*, text/html");

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
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional, default to 0)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSubtitle(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks) throws ApiException {
        return getSubtitle(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId, mediaSourceId, index,
                format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks, null);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional, default to 0)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSubtitle(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getSubtitleWithHttpInfo(routeItemId, routeMediaSourceId, routeIndex,
                routeFormat, itemId, mediaSourceId, index, format, endPositionTicks, copyTimestamps, addVttTimeMap,
                startPositionTicks, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional, default to 0)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks) throws ApiException {
        return getSubtitleWithHttpInfo(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId, mediaSourceId,
                index, format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks, null);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional, default to 0)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSubtitleRequestBuilder(routeItemId, routeMediaSourceId,
                routeIndex, routeFormat, itemId, mediaSourceId, index, format, endPositionTicks, copyTimestamps,
                addVttTimeMap, startPositionTicks, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSubtitle", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getSubtitleRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'routeItemId' is set
        if (routeItemId == null) {
            throw new ApiException(400, "Missing the required parameter 'routeItemId' when calling getSubtitle");
        }
        // verify the required parameter 'routeMediaSourceId' is set
        if (routeMediaSourceId == null) {
            throw new ApiException(400, "Missing the required parameter 'routeMediaSourceId' when calling getSubtitle");
        }
        // verify the required parameter 'routeIndex' is set
        if (routeIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'routeIndex' when calling getSubtitle");
        }
        // verify the required parameter 'routeFormat' is set
        if (routeFormat == null) {
            throw new ApiException(400, "Missing the required parameter 'routeFormat' when calling getSubtitle");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/Stream.{routeFormat}"
                .replace("{routeItemId}", ApiClient.urlEncode(routeItemId.toString()))
                .replace("{routeMediaSourceId}", ApiClient.urlEncode(routeMediaSourceId.toString()))
                .replace("{routeIndex}", ApiClient.urlEncode(routeIndex.toString()))
                .replace("{routeFormat}", ApiClient.urlEncode(routeFormat.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "itemId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemId", itemId));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "index";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("index", index));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "endPositionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("endPositionTicks", endPositionTicks));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "addVttTimeMap";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("addVttTimeMap", addVttTimeMap));
        localVarQueryParameterBaseName = "startPositionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startPositionTicks", startPositionTicks));

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

        localVarRequestBuilder.header("Accept", "text/*, text/html");

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
     * Gets an HLS subtitle playlist.
     * 
     * @param itemId The item id. (required)
     * @param index The subtitle stream index. (required)
     * @param mediaSourceId The media source id. (required)
     * @param segmentLength The subtitle segment length. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSubtitlePlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength) throws ApiException {
        return getSubtitlePlaylist(itemId, index, mediaSourceId, segmentLength, null);
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * @param itemId The item id. (required)
     * @param index The subtitle stream index. (required)
     * @param mediaSourceId The media source id. (required)
     * @param segmentLength The subtitle segment length. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSubtitlePlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getSubtitlePlaylistWithHttpInfo(itemId, index, mediaSourceId,
                segmentLength, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * @param itemId The item id. (required)
     * @param index The subtitle stream index. (required)
     * @param mediaSourceId The media source id. (required)
     * @param segmentLength The subtitle segment length. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSubtitlePlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength) throws ApiException {
        return getSubtitlePlaylistWithHttpInfo(itemId, index, mediaSourceId, segmentLength, null);
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * @param itemId The item id. (required)
     * @param index The subtitle stream index. (required)
     * @param mediaSourceId The media source id. (required)
     * @param segmentLength The subtitle segment length. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSubtitlePlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSubtitlePlaylistRequestBuilder(itemId, index, mediaSourceId,
                segmentLength, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSubtitlePlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getSubtitlePlaylistRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSubtitlePlaylist");
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new ApiException(400, "Missing the required parameter 'index' when calling getSubtitlePlaylist");
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaSourceId' when calling getSubtitlePlaylist");
        }
        // verify the required parameter 'segmentLength' is set
        if (segmentLength == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentLength' when calling getSubtitlePlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/{mediaSourceId}/Subtitles/{index}/subtitles.m3u8"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{index}", ApiClient.urlEncode(index.toString()))
                .replace("{mediaSourceId}", ApiClient.urlEncode(mediaSourceId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));

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
                "application/x-mpegURL, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSubtitleWithTicks(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable Long routeStartPositionTicks,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap) throws ApiException {
        return getSubtitleWithTicks(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks, routeFormat,
                itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps,
                addVttTimeMap, null);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSubtitleWithTicks(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable Long routeStartPositionTicks,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getSubtitleWithTicksWithHttpInfo(routeItemId, routeMediaSourceId,
                routeIndex, routeStartPositionTicks, routeFormat, itemId, mediaSourceId, index, startPositionTicks,
                format, endPositionTicks, copyTimestamps, addVttTimeMap, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSubtitleWithTicksWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable Long routeStartPositionTicks,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap) throws ApiException {
        return getSubtitleWithTicksWithHttpInfo(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks,
                routeFormat, itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps,
                addVttTimeMap, null);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSubtitleWithTicksWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable Long routeStartPositionTicks,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSubtitleWithTicksRequestBuilder(routeItemId, routeMediaSourceId,
                routeIndex, routeStartPositionTicks, routeFormat, itemId, mediaSourceId, index, startPositionTicks,
                format, endPositionTicks, copyTimestamps, addVttTimeMap, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSubtitleWithTicks", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse);

                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getSubtitleWithTicksRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable Long routeStartPositionTicks,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'routeItemId' is set
        if (routeItemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeItemId' when calling getSubtitleWithTicks");
        }
        // verify the required parameter 'routeMediaSourceId' is set
        if (routeMediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeMediaSourceId' when calling getSubtitleWithTicks");
        }
        // verify the required parameter 'routeIndex' is set
        if (routeIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeIndex' when calling getSubtitleWithTicks");
        }
        // verify the required parameter 'routeStartPositionTicks' is set
        if (routeStartPositionTicks == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeStartPositionTicks' when calling getSubtitleWithTicks");
        }
        // verify the required parameter 'routeFormat' is set
        if (routeFormat == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeFormat' when calling getSubtitleWithTicks");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/{routeStartPositionTicks}/Stream.{routeFormat}"
                .replace("{routeItemId}", ApiClient.urlEncode(routeItemId.toString()))
                .replace("{routeMediaSourceId}", ApiClient.urlEncode(routeMediaSourceId.toString()))
                .replace("{routeIndex}", ApiClient.urlEncode(routeIndex.toString()))
                .replace("{routeStartPositionTicks}", ApiClient.urlEncode(routeStartPositionTicks.toString()))
                .replace("{routeFormat}", ApiClient.urlEncode(routeFormat.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "itemId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemId", itemId));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "index";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("index", index));
        localVarQueryParameterBaseName = "startPositionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startPositionTicks", startPositionTicks));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "endPositionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("endPositionTicks", endPositionTicks));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "addVttTimeMap";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("addVttTimeMap", addVttTimeMap));

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

        localVarRequestBuilder.header("Accept", "text/*, text/html");

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
     * Search remote subtitles.
     * 
     * @param itemId The item id. (required)
     * @param language The language of the subtitles. (required)
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match. (optional)
     * @return List&lt;RemoteSubtitleInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSubtitleInfo> searchRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String language,
            @org.eclipse.jdt.annotation.NonNull Boolean isPerfectMatch) throws ApiException {
        return searchRemoteSubtitles(itemId, language, isPerfectMatch, null);
    }

    /**
     * Search remote subtitles.
     * 
     * @param itemId The item id. (required)
     * @param language The language of the subtitles. (required)
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match. (optional)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSubtitleInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSubtitleInfo> searchRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String language,
            @org.eclipse.jdt.annotation.NonNull Boolean isPerfectMatch, Map<String, String> headers)
            throws ApiException {
        ApiResponse<List<RemoteSubtitleInfo>> localVarResponse = searchRemoteSubtitlesWithHttpInfo(itemId, language,
                isPerfectMatch, headers);
        return localVarResponse.getData();
    }

    /**
     * Search remote subtitles.
     * 
     * @param itemId The item id. (required)
     * @param language The language of the subtitles. (required)
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match. (optional)
     * @return ApiResponse&lt;List&lt;RemoteSubtitleInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSubtitleInfo>> searchRemoteSubtitlesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.Nullable String language,
            @org.eclipse.jdt.annotation.NonNull Boolean isPerfectMatch) throws ApiException {
        return searchRemoteSubtitlesWithHttpInfo(itemId, language, isPerfectMatch, null);
    }

    /**
     * Search remote subtitles.
     * 
     * @param itemId The item id. (required)
     * @param language The language of the subtitles. (required)
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSubtitleInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSubtitleInfo>> searchRemoteSubtitlesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.Nullable String language,
            @org.eclipse.jdt.annotation.NonNull Boolean isPerfectMatch, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = searchRemoteSubtitlesRequestBuilder(itemId, language,
                isPerfectMatch, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("searchRemoteSubtitles", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSubtitleInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<RemoteSubtitleInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSubtitleInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSubtitleInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder searchRemoteSubtitlesRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String language,
            @org.eclipse.jdt.annotation.NonNull Boolean isPerfectMatch, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling searchRemoteSubtitles");
        }
        // verify the required parameter 'language' is set
        if (language == null) {
            throw new ApiException(400, "Missing the required parameter 'language' when calling searchRemoteSubtitles");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/RemoteSearch/Subtitles/{language}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{language}", ApiClient.urlEncode(language.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "isPerfectMatch";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isPerfectMatch", isPerfectMatch));

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
     * Upload an external subtitle file.
     * 
     * @param itemId The item the subtitle belongs to. (required)
     * @param uploadSubtitleDto The request body. (required)
     * @throws ApiException if fails to make API call
     */
    public void uploadSubtitle(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UploadSubtitleDto uploadSubtitleDto) throws ApiException {
        uploadSubtitle(itemId, uploadSubtitleDto, null);
    }

    /**
     * Upload an external subtitle file.
     * 
     * @param itemId The item the subtitle belongs to. (required)
     * @param uploadSubtitleDto The request body. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void uploadSubtitle(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UploadSubtitleDto uploadSubtitleDto, Map<String, String> headers)
            throws ApiException {
        uploadSubtitleWithHttpInfo(itemId, uploadSubtitleDto, headers);
    }

    /**
     * Upload an external subtitle file.
     * 
     * @param itemId The item the subtitle belongs to. (required)
     * @param uploadSubtitleDto The request body. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> uploadSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UploadSubtitleDto uploadSubtitleDto) throws ApiException {
        return uploadSubtitleWithHttpInfo(itemId, uploadSubtitleDto, null);
    }

    /**
     * Upload an external subtitle file.
     * 
     * @param itemId The item the subtitle belongs to. (required)
     * @param uploadSubtitleDto The request body. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> uploadSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UploadSubtitleDto uploadSubtitleDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = uploadSubtitleRequestBuilder(itemId, uploadSubtitleDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("uploadSubtitle", localVarResponse);
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

    private HttpRequest.Builder uploadSubtitleRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UploadSubtitleDto uploadSubtitleDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling uploadSubtitle");
        }
        // verify the required parameter 'uploadSubtitleDto' is set
        if (uploadSubtitleDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'uploadSubtitleDto' when calling uploadSubtitle");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/Subtitles".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(uploadSubtitleDto);
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
