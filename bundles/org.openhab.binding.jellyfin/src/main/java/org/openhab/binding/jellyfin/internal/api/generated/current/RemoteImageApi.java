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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageProviderInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RemoteImageResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RemoteImageApi {
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

    public RemoteImageApi() {
        this(Configuration.getDefaultApiClient());
    }

    public RemoteImageApi(ApiClient apiClient) {
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
     * Downloads a remote image for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (required)
     * @param imageUrl The image url. (optional)
     * @throws ApiException if fails to make API call
     */
    public void downloadRemoteImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType type, @org.eclipse.jdt.annotation.NonNull String imageUrl)
            throws ApiException {
        downloadRemoteImage(itemId, type, imageUrl, null);
    }

    /**
     * Downloads a remote image for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (required)
     * @param imageUrl The image url. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void downloadRemoteImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType type, @org.eclipse.jdt.annotation.NonNull String imageUrl,
            Map<String, String> headers) throws ApiException {
        downloadRemoteImageWithHttpInfo(itemId, type, imageUrl, headers);
    }

    /**
     * Downloads a remote image for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (required)
     * @param imageUrl The image url. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> downloadRemoteImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType type, @org.eclipse.jdt.annotation.NonNull String imageUrl)
            throws ApiException {
        return downloadRemoteImageWithHttpInfo(itemId, type, imageUrl, null);
    }

    /**
     * Downloads a remote image for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (required)
     * @param imageUrl The image url. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> downloadRemoteImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType type, @org.eclipse.jdt.annotation.NonNull String imageUrl,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = downloadRemoteImageRequestBuilder(itemId, type, imageUrl, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("downloadRemoteImage", localVarResponse);
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

    private HttpRequest.Builder downloadRemoteImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType type, @org.eclipse.jdt.annotation.NonNull String imageUrl,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling downloadRemoteImage");
        }
        // verify the required parameter 'type' is set
        if (type == null) {
            throw new ApiException(400, "Missing the required parameter 'type' when calling downloadRemoteImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/RemoteImages/Download".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "type";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("type", type));
        localVarQueryParameterBaseName = "imageUrl";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageUrl", imageUrl));

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
     * Gets available remote image providers for an item.
     * 
     * @param itemId Item Id. (required)
     * @return List&lt;ImageProviderInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageProviderInfo> getRemoteImageProviders(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        return getRemoteImageProviders(itemId, null);
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * @param itemId Item Id. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;ImageProviderInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageProviderInfo> getRemoteImageProviders(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<ImageProviderInfo>> localVarResponse = getRemoteImageProvidersWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * @param itemId Item Id. (required)
     * @return ApiResponse&lt;List&lt;ImageProviderInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageProviderInfo>> getRemoteImageProvidersWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        return getRemoteImageProvidersWithHttpInfo(itemId, null);
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * @param itemId Item Id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;ImageProviderInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageProviderInfo>> getRemoteImageProvidersWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRemoteImageProvidersRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRemoteImageProviders", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<ImageProviderInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<ImageProviderInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<ImageProviderInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<ImageProviderInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getRemoteImageProvidersRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getRemoteImageProviders");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/RemoteImages/Providers".replace("{itemId}",
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
     * Gets available remote images for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param providerName Optional. The image provider to use. (optional)
     * @param includeAllLanguages Optional. Include all languages. (optional, default to false)
     * @return RemoteImageResult
     * @throws ApiException if fails to make API call
     */
    public RemoteImageResult getRemoteImages(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType type, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull String providerName,
            @org.eclipse.jdt.annotation.NonNull Boolean includeAllLanguages) throws ApiException {
        return getRemoteImages(itemId, type, startIndex, limit, providerName, includeAllLanguages, null);
    }

    /**
     * Gets available remote images for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param providerName Optional. The image provider to use. (optional)
     * @param includeAllLanguages Optional. Include all languages. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return RemoteImageResult
     * @throws ApiException if fails to make API call
     */
    public RemoteImageResult getRemoteImages(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType type, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull String providerName,
            @org.eclipse.jdt.annotation.NonNull Boolean includeAllLanguages, Map<String, String> headers)
            throws ApiException {
        ApiResponse<RemoteImageResult> localVarResponse = getRemoteImagesWithHttpInfo(itemId, type, startIndex, limit,
                providerName, includeAllLanguages, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets available remote images for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param providerName Optional. The image provider to use. (optional)
     * @param includeAllLanguages Optional. Include all languages. (optional, default to false)
     * @return ApiResponse&lt;RemoteImageResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<RemoteImageResult> getRemoteImagesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType type, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull String providerName,
            @org.eclipse.jdt.annotation.NonNull Boolean includeAllLanguages) throws ApiException {
        return getRemoteImagesWithHttpInfo(itemId, type, startIndex, limit, providerName, includeAllLanguages, null);
    }

    /**
     * Gets available remote images for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param providerName Optional. The image provider to use. (optional)
     * @param includeAllLanguages Optional. Include all languages. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;RemoteImageResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<RemoteImageResult> getRemoteImagesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType type, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull String providerName,
            @org.eclipse.jdt.annotation.NonNull Boolean includeAllLanguages, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRemoteImagesRequestBuilder(itemId, type, startIndex, limit,
                providerName, includeAllLanguages, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRemoteImages", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<RemoteImageResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                RemoteImageResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<RemoteImageResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<RemoteImageResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getRemoteImagesRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType type, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull String providerName,
            @org.eclipse.jdt.annotation.NonNull Boolean includeAllLanguages, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getRemoteImages");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/RemoteImages".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "type";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("type", type));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "providerName";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("providerName", providerName));
        localVarQueryParameterBaseName = "includeAllLanguages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includeAllLanguages", includeAllLanguages));

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
}
