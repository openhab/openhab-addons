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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ImageFormat;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ImageInfo;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ImageType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageApi {
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

    public ImageApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ImageApi(ApiClient apiClient) {
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
     * Delete a custom splashscreen.
     * 
     * @throws ApiException if fails to make API call
     */
    public void deleteCustomSplashscreen() throws ApiException {
        deleteCustomSplashscreen(null);
    }

    /**
     * Delete a custom splashscreen.
     * 
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteCustomSplashscreen(Map<String, String> headers) throws ApiException {
        deleteCustomSplashscreenWithHttpInfo(headers);
    }

    /**
     * Delete a custom splashscreen.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteCustomSplashscreenWithHttpInfo() throws ApiException {
        return deleteCustomSplashscreenWithHttpInfo(null);
    }

    /**
     * Delete a custom splashscreen.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteCustomSplashscreenWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteCustomSplashscreenRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteCustomSplashscreen", localVarResponse);
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

    private HttpRequest.Builder deleteCustomSplashscreenRequestBuilder(Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Branding/Splashscreen";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (optional)
     * @throws ApiException if fails to make API call
     */
    public void deleteItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        deleteItemImage(itemId, imageType, imageIndex, null);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        deleteItemImageWithHttpInfo(itemId, imageType, imageIndex, headers);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return deleteItemImageWithHttpInfo(itemId, imageType, imageIndex, null);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteItemImageRequestBuilder(itemId, imageType, imageIndex,
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
                    throw getApiException("deleteItemImage", localVarResponse);
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

    private HttpRequest.Builder deleteItemImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteItemImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling deleteItemImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        deleteItemImageByIndex(itemId, imageType, imageIndex, null);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, Map<String, String> headers) throws ApiException {
        deleteItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, headers);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex) throws ApiException {
        return deleteItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, null);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex The image index. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteItemImageByIndexRequestBuilder(itemId, imageType, imageIndex,
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
                    throw getApiException("deleteItemImageByIndex", localVarResponse);
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

    private HttpRequest.Builder deleteItemImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteItemImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling deleteItemImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling deleteItemImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

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
     * Delete the user&#39;s image.
     * 
     * @param userId User Id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void deleteUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        deleteUserImage(userId, null);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * @param userId User Id. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers)
            throws ApiException {
        deleteUserImageWithHttpInfo(userId, headers);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * @param userId User Id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return deleteUserImageWithHttpInfo(userId, null);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * @param userId User Id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteUserImageRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteUserImage", localVarResponse);
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

    private HttpRequest.Builder deleteUserImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserImage";

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
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getArtistImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getArtistImage(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getArtistImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getArtistImageWithHttpInfo(name, imageType, imageIndex, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getArtistImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getArtistImageWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getArtistImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getArtistImageRequestBuilder(name, imageType, imageIndex, tag,
                format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getArtistImage", localVarResponse);
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

    private HttpRequest.Builder getArtistImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getArtistImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getArtistImage");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling getArtistImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Artists/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getGenreImageRequestBuilder(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getGenreImage", localVarResponse);
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

    private HttpRequest.Builder getGenreImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getGenreImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getGenreImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Genres/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getGenreImageByIndexRequestBuilder(name, imageType, imageIndex,
                tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getGenreImageByIndex", localVarResponse);
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

    private HttpRequest.Builder getGenreImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getGenreImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getGenreImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getGenreImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Genres/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getItemImage(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag,
                format, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getItemImageWithHttpInfo(itemId, imageType, maxWidth, maxHeight, width,
                height, quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getItemImageWithHttpInfo(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth,
                fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getItemImageRequestBuilder(itemId, imageType, maxWidth, maxHeight,
                width, height, quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getItemImage", localVarResponse);
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

    private HttpRequest.Builder getItemImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getItemImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getItemImage2(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getItemImage2(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount,
                imageIndex, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getItemImage2(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getItemImage2WithHttpInfo(itemId, imageType, maxWidth, maxHeight, tag,
                format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getItemImage2WithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getItemImage2WithHttpInfo(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed,
                unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getItemImage2WithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getItemImage2RequestBuilder(itemId, imageType, maxWidth, maxHeight,
                tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight,
                blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getItemImage2", localVarResponse);
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

    private HttpRequest.Builder getItemImage2RequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImage2");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getItemImage2");
        }
        // verify the required parameter 'maxWidth' is set
        if (maxWidth == null) {
            throw new ApiException(400, "Missing the required parameter 'maxWidth' when calling getItemImage2");
        }
        // verify the required parameter 'maxHeight' is set
        if (maxHeight == null) {
            throw new ApiException(400, "Missing the required parameter 'maxHeight' when calling getItemImage2");
        }
        // verify the required parameter 'tag' is set
        if (tag == null) {
            throw new ApiException(400, "Missing the required parameter 'tag' when calling getItemImage2");
        }
        // verify the required parameter 'format' is set
        if (format == null) {
            throw new ApiException(400, "Missing the required parameter 'format' when calling getItemImage2");
        }
        // verify the required parameter 'percentPlayed' is set
        if (percentPlayed == null) {
            throw new ApiException(400, "Missing the required parameter 'percentPlayed' when calling getItemImage2");
        }
        // verify the required parameter 'unplayedCount' is set
        if (unplayedCount == null) {
            throw new ApiException(400, "Missing the required parameter 'unplayedCount' when calling getItemImage2");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling getItemImage2");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{maxWidth}", ApiClient.urlEncode(maxWidth.toString()))
                .replace("{maxHeight}", ApiClient.urlEncode(maxHeight.toString()))
                .replace("{tag}", ApiClient.urlEncode(tag.toString()))
                .replace("{format}", ApiClient.urlEncode(format.toString()))
                .replace("{percentPlayed}", ApiClient.urlEncode(percentPlayed.toString()))
                .replace("{unplayedCount}", ApiClient.urlEncode(unplayedCount.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getItemImageByIndex(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality,
                fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, maxWidth,
                maxHeight, width, height, quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount,
                blur, backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height,
                quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getItemImageByIndexRequestBuilder(itemId, imageType, imageIndex,
                maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, format, percentPlayed,
                unplayedCount, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getItemImageByIndex", localVarResponse);
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

    private HttpRequest.Builder getItemImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getItemImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling getItemImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get item image infos.
     * 
     * @param itemId Item id. (required)
     * @return List&lt;ImageInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageInfo> getItemImageInfos(@org.eclipse.jdt.annotation.NonNull UUID itemId) throws ApiException {
        return getItemImageInfos(itemId, null);
    }

    /**
     * Get item image infos.
     * 
     * @param itemId Item id. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;ImageInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageInfo> getItemImageInfos(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<ImageInfo>> localVarResponse = getItemImageInfosWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get item image infos.
     * 
     * @param itemId Item id. (required)
     * @return ApiResponse&lt;List&lt;ImageInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageInfo>> getItemImageInfosWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId)
            throws ApiException {
        return getItemImageInfosWithHttpInfo(itemId, null);
    }

    /**
     * Get item image infos.
     * 
     * @param itemId Item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;ImageInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageInfo>> getItemImageInfosWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getItemImageInfosRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getItemImageInfos", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<ImageInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<ImageInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<ImageInfo>>() {
                        });

                return new ApiResponse<List<ImageInfo>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getItemImageInfosRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getItemImageInfos");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMusicGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getMusicGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount,
                width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex,
                null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMusicGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getMusicGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMusicGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getMusicGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMusicGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicGenreImageRequestBuilder(name, imageType, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicGenreImage", localVarResponse);
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

    private HttpRequest.Builder getMusicGenreImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getMusicGenreImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getMusicGenreImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/MusicGenres/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMusicGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getMusicGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMusicGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getMusicGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag,
                format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMusicGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getMusicGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMusicGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicGenreImageByIndexRequestBuilder(name, imageType,
                imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality,
                fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicGenreImageByIndex", localVarResponse);
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

    private HttpRequest.Builder getMusicGenreImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getMusicGenreImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling getMusicGenreImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getMusicGenreImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/MusicGenres/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getPersonImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getPersonImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getPersonImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getPersonImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getPersonImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getPersonImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getPersonImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPersonImageRequestBuilder(name, imageType, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPersonImage", localVarResponse);
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

    private HttpRequest.Builder getPersonImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getPersonImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getPersonImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Persons/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getPersonImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getPersonImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getPersonImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getPersonImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getPersonImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getPersonImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getPersonImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPersonImageByIndexRequestBuilder(name, imageType, imageIndex,
                tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPersonImageByIndex", localVarResponse);
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

    private HttpRequest.Builder getPersonImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getPersonImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling getPersonImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getPersonImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Persons/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Generates or gets the splashscreen.
     * 
     * @param tag Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSplashscreen(@org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format) throws ApiException {
        return getSplashscreen(tag, format, null);
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * @param tag Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getSplashscreen(@org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getSplashscreenWithHttpInfo(tag, format, headers);
        return localVarResponse.getData();
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * @param tag Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSplashscreenWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format) throws ApiException {
        return getSplashscreenWithHttpInfo(tag, format, null);
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * @param tag Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getSplashscreenWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSplashscreenRequestBuilder(tag, format, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSplashscreen", localVarResponse);
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

    private HttpRequest.Builder getSplashscreenRequestBuilder(@org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Branding/Splashscreen";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));

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

        localVarRequestBuilder.header("Accept", "image/*, text/html");

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
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getStudioImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getStudioImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getStudioImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getStudioImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getStudioImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return getStudioImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getStudioImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getStudioImageRequestBuilder(name, imageType, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getStudioImage", localVarResponse);
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

    private HttpRequest.Builder getStudioImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getStudioImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling getStudioImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Studios/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getStudioImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getStudioImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getStudioImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getStudioImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getStudioImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return getStudioImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getStudioImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getStudioImageByIndexRequestBuilder(name, imageType, imageIndex,
                tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getStudioImageByIndex", localVarResponse);
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

    private HttpRequest.Builder getStudioImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getStudioImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling getStudioImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling getStudioImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Studios/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format)
            throws ApiException {
        return getUserImage(userId, tag, format, null);
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getUserImageWithHttpInfo(userId, tag, format, headers);
        return localVarResponse.getData();
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format)
            throws ApiException {
        return getUserImageWithHttpInfo(userId, tag, format, null);
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getUserImageRequestBuilder(userId, tag, format, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getUserImage", localVarResponse);
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

    private HttpRequest.Builder getUserImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserImage";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headArtistImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headArtistImage(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headArtistImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headArtistImageWithHttpInfo(name, imageType, imageIndex, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headArtistImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headArtistImageWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get artist image by name.
     * 
     * @param name Artist name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headArtistImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headArtistImageRequestBuilder(name, imageType, imageIndex, tag,
                format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headArtistImage", localVarResponse);
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

    private HttpRequest.Builder headArtistImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headArtistImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headArtistImage");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling headArtistImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Artists/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = headGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headGenreImageRequestBuilder(name, imageType, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headGenreImage", localVarResponse);
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

    private HttpRequest.Builder headGenreImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headGenreImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headGenreImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Genres/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get genre image by name.
     * 
     * @param name Genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headGenreImageByIndexRequestBuilder(name, imageType, imageIndex,
                tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headGenreImageByIndex", localVarResponse);
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

    private HttpRequest.Builder headGenreImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headGenreImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headGenreImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headGenreImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Genres/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headItemImage(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag,
                format, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = headItemImageWithHttpInfo(itemId, imageType, maxWidth, maxHeight, width,
                height, quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headItemImageWithHttpInfo(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth,
                fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headItemImageRequestBuilder(itemId, imageType, maxWidth, maxHeight,
                width, height, quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headItemImage", localVarResponse);
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

    private HttpRequest.Builder headItemImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headItemImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headItemImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headItemImage2(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headItemImage2(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount,
                imageIndex, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headItemImage2(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headItemImage2WithHttpInfo(itemId, imageType, maxWidth, maxHeight, tag,
                format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headItemImage2WithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headItemImage2WithHttpInfo(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed,
                unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param maxWidth The maximum image width to return. (required)
     * @param maxHeight The maximum image height to return. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (required)
     * @param format Determines the output format of the image - original,gif,jpg,png. (required)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (required)
     * @param unplayedCount Optional. Unplayed count overlay to render. (required)
     * @param imageIndex Image index. (required)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headItemImage2WithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headItemImage2RequestBuilder(itemId, imageType, maxWidth,
                maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headItemImage2", localVarResponse);
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

    private HttpRequest.Builder headItemImage2RequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull ImageFormat format,
            @org.eclipse.jdt.annotation.NonNull Double percentPlayed,
            @org.eclipse.jdt.annotation.NonNull Integer unplayedCount,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headItemImage2");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headItemImage2");
        }
        // verify the required parameter 'maxWidth' is set
        if (maxWidth == null) {
            throw new ApiException(400, "Missing the required parameter 'maxWidth' when calling headItemImage2");
        }
        // verify the required parameter 'maxHeight' is set
        if (maxHeight == null) {
            throw new ApiException(400, "Missing the required parameter 'maxHeight' when calling headItemImage2");
        }
        // verify the required parameter 'tag' is set
        if (tag == null) {
            throw new ApiException(400, "Missing the required parameter 'tag' when calling headItemImage2");
        }
        // verify the required parameter 'format' is set
        if (format == null) {
            throw new ApiException(400, "Missing the required parameter 'format' when calling headItemImage2");
        }
        // verify the required parameter 'percentPlayed' is set
        if (percentPlayed == null) {
            throw new ApiException(400, "Missing the required parameter 'percentPlayed' when calling headItemImage2");
        }
        // verify the required parameter 'unplayedCount' is set
        if (unplayedCount == null) {
            throw new ApiException(400, "Missing the required parameter 'unplayedCount' when calling headItemImage2");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling headItemImage2");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{maxWidth}", ApiClient.urlEncode(maxWidth.toString()))
                .replace("{maxHeight}", ApiClient.urlEncode(maxHeight.toString()))
                .replace("{tag}", ApiClient.urlEncode(tag.toString()))
                .replace("{format}", ApiClient.urlEncode(format.toString()))
                .replace("{percentPlayed}", ApiClient.urlEncode(percentPlayed.toString()))
                .replace("{unplayedCount}", ApiClient.urlEncode(unplayedCount.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headItemImageByIndex(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality,
                fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, maxWidth,
                maxHeight, width, height, quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount,
                blur, backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height,
                quality, fillWidth, fillHeight, tag, format, percentPlayed, unplayedCount, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headItemImageByIndexRequestBuilder(itemId, imageType, imageIndex,
                maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, format, percentPlayed,
                unplayedCount, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headItemImageByIndex", localVarResponse);
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

    private HttpRequest.Builder headItemImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Integer width,
            @org.eclipse.jdt.annotation.Nullable Integer height, @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headItemImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headItemImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headItemImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headMusicGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headMusicGenreImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount,
                width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex,
                null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headMusicGenreImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = headMusicGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headMusicGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headMusicGenreImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headMusicGenreImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headMusicGenreImageRequestBuilder(name, imageType, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headMusicGenreImage", localVarResponse);
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

    private HttpRequest.Builder headMusicGenreImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headMusicGenreImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headMusicGenreImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/MusicGenres/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headMusicGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headMusicGenreImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headMusicGenreImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headMusicGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag,
                format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headMusicGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headMusicGenreImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get music genre image by name.
     * 
     * @param name Music genre name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headMusicGenreImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headMusicGenreImageByIndexRequestBuilder(name, imageType,
                imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality,
                fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headMusicGenreImageByIndex", localVarResponse);
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

    private HttpRequest.Builder headMusicGenreImageByIndexRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String name, @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'name' when calling headMusicGenreImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headMusicGenreImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headMusicGenreImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/MusicGenres/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headPersonImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headPersonImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headPersonImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = headPersonImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headPersonImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headPersonImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headPersonImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headPersonImageRequestBuilder(name, imageType, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headPersonImage", localVarResponse);
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

    private HttpRequest.Builder headPersonImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headPersonImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headPersonImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Persons/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headPersonImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headPersonImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headPersonImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headPersonImageByIndexWithHttpInfo(name, imageType, imageIndex, tag,
                format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headPersonImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headPersonImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get person image by name.
     * 
     * @param name Person name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headPersonImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headPersonImageByIndexRequestBuilder(name, imageType, imageIndex,
                tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headPersonImageByIndex", localVarResponse);
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

    private HttpRequest.Builder headPersonImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headPersonImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headPersonImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headPersonImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Persons/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headStudioImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headStudioImage(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width,
                height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, imageIndex, null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headStudioImage(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = headStudioImageWithHttpInfo(name, imageType, tag, format, maxWidth,
                maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        return localVarResponse.getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headStudioImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex) throws ApiException {
        return headStudioImageWithHttpInfo(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                imageIndex, null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param imageIndex Image index. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headStudioImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headStudioImageRequestBuilder(name, imageType, tag, format,
                maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur,
                backgroundColor, foregroundLayer, imageIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headStudioImage", localVarResponse);
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

    private HttpRequest.Builder headStudioImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer,
            @org.eclipse.jdt.annotation.Nullable Integer imageIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headStudioImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling headStudioImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Studios/{name}/Images/{imageType}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));
        localVarQueryParameterBaseName = "imageIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageIndex", imageIndex));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headStudioImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headStudioImageByIndex(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed,
                unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer,
                null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headStudioImageByIndex(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headStudioImageByIndexWithHttpInfo(name, imageType, imageIndex, tag,
                format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        return localVarResponse.getData();
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headStudioImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer) throws ApiException {
        return headStudioImageByIndexWithHttpInfo(name, imageType, imageIndex, tag, format, maxWidth, maxHeight,
                percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, blur, backgroundColor,
                foregroundLayer, null);
    }

    /**
     * Get studio image by name.
     * 
     * @param name Studio name. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Image index. (required)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param maxWidth The maximum image width to return. (optional)
     * @param maxHeight The maximum image height to return. (optional)
     * @param percentPlayed Optional. Percent to render for the percent played overlay. (optional)
     * @param unplayedCount Optional. Unplayed count overlay to render. (optional)
     * @param width The fixed image width to return. (optional)
     * @param height The fixed image height to return. (optional)
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases. (optional)
     * @param fillWidth Width of box to fill. (optional)
     * @param fillHeight Height of box to fill. (optional)
     * @param blur Optional. Blur image. (optional)
     * @param backgroundColor Optional. Apply a background color for transparent images. (optional)
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headStudioImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headStudioImageByIndexRequestBuilder(name, imageType, imageIndex,
                tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth,
                fillHeight, blur, backgroundColor, foregroundLayer, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headStudioImageByIndex", localVarResponse);
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

    private HttpRequest.Builder headStudioImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Double percentPlayed,
            @org.eclipse.jdt.annotation.Nullable Integer unplayedCount,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer quality,
            @org.eclipse.jdt.annotation.Nullable Integer fillWidth,
            @org.eclipse.jdt.annotation.Nullable Integer fillHeight, @org.eclipse.jdt.annotation.Nullable Integer blur,
            @org.eclipse.jdt.annotation.Nullable String backgroundColor,
            @org.eclipse.jdt.annotation.Nullable String foregroundLayer, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling headStudioImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageType' when calling headStudioImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling headStudioImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Studios/{name}/Images/{imageType}/{imageIndex}"
                .replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "percentPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("percentPlayed", percentPlayed));
        localVarQueryParameterBaseName = "unplayedCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("unplayedCount", unplayedCount));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "quality";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("quality", quality));
        localVarQueryParameterBaseName = "fillWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillWidth", fillWidth));
        localVarQueryParameterBaseName = "fillHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("fillHeight", fillHeight));
        localVarQueryParameterBaseName = "blur";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("blur", blur));
        localVarQueryParameterBaseName = "backgroundColor";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("backgroundColor", backgroundColor));
        localVarQueryParameterBaseName = "foregroundLayer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("foregroundLayer", foregroundLayer));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format)
            throws ApiException {
        return headUserImage(userId, tag, format, null);
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = headUserImageWithHttpInfo(userId, tag, format, headers);
        return localVarResponse.getData();
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format)
            throws ApiException {
        return headUserImageWithHttpInfo(userId, tag, format, null);
    }

    /**
     * Get user profile image.
     * 
     * @param userId User id. (optional)
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers. (optional)
     * @param format Determines the output format of the image - original,gif,jpg,png. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headUserImageRequestBuilder(userId, tag, format, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headUserImage", localVarResponse);
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

    private HttpRequest.Builder headUserImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String tag, @org.eclipse.jdt.annotation.Nullable ImageFormat format,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserImage";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "format";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));

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
                "image/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
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
     * Sets the user image.
     * 
     * @param userId User Id. (optional)
     * @param body (optional)
     * @throws ApiException if fails to make API call
     */
    public void postUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable File body) throws ApiException {
        postUserImage(userId, body, null);
    }

    /**
     * Sets the user image.
     * 
     * @param userId User Id. (optional)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void postUserImage(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable File body, Map<String, String> headers) throws ApiException {
        postUserImageWithHttpInfo(userId, body, headers);
    }

    /**
     * Sets the user image.
     * 
     * @param userId User Id. (optional)
     * @param body (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable File body) throws ApiException {
        return postUserImageWithHttpInfo(userId, body, null);
    }

    /**
     * Sets the user image.
     * 
     * @param userId User Id. (optional)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> postUserImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable File body, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postUserImageRequestBuilder(userId, body, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postUserImage", localVarResponse);
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

    private HttpRequest.Builder postUserImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable File body, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/UserImage";

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

        localVarRequestBuilder.header("Content-Type", "image/*");
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

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param body (optional)
     * @throws ApiException if fails to make API call
     */
    public void setItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable File body)
            throws ApiException {
        setItemImage(itemId, imageType, body, null);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void setItemImage(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        setItemImageWithHttpInfo(itemId, imageType, body, headers);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param body (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable File body)
            throws ApiException {
        return setItemImageWithHttpInfo(itemId, imageType, body, null);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setItemImageWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setItemImageRequestBuilder(itemId, imageType, body, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("setItemImage", localVarResponse);
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

    private HttpRequest.Builder setItemImageRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling setItemImage");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling setItemImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "image/*");
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

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex (Unused) Image index. (required)
     * @param body (optional)
     * @throws ApiException if fails to make API call
     */
    public void setItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable File body)
            throws ApiException {
        setItemImageByIndex(itemId, imageType, imageIndex, body, null);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex (Unused) Image index. (required)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void setItemImageByIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        setItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, body, headers);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex (Unused) Image index. (required)
     * @param body (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable File body)
            throws ApiException {
        return setItemImageByIndexWithHttpInfo(itemId, imageType, imageIndex, body, null);
    }

    /**
     * Set item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex (Unused) Image index. (required)
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setItemImageByIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setItemImageByIndexRequestBuilder(itemId, imageType, imageIndex,
                body, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("setItemImageByIndex", localVarResponse);
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

    private HttpRequest.Builder setItemImageByIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex, @org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling setItemImageByIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling setItemImageByIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'imageIndex' when calling setItemImageByIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "image/*");
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

    /**
     * Updates the index for an item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Old image index. (required)
     * @param newIndex New image index. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateItemImageIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer newIndex) throws ApiException {
        updateItemImageIndex(itemId, imageType, imageIndex, newIndex, null);
    }

    /**
     * Updates the index for an item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Old image index. (required)
     * @param newIndex New image index. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateItemImageIndex(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer newIndex, Map<String, String> headers) throws ApiException {
        updateItemImageIndexWithHttpInfo(itemId, imageType, imageIndex, newIndex, headers);
    }

    /**
     * Updates the index for an item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Old image index. (required)
     * @param newIndex New image index. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateItemImageIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer newIndex) throws ApiException {
        return updateItemImageIndexWithHttpInfo(itemId, imageType, imageIndex, newIndex, null);
    }

    /**
     * Updates the index for an item image.
     * 
     * @param itemId Item id. (required)
     * @param imageType Image type. (required)
     * @param imageIndex Old image index. (required)
     * @param newIndex New image index. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateItemImageIndexWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer newIndex, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateItemImageIndexRequestBuilder(itemId, imageType, imageIndex,
                newIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateItemImageIndex", localVarResponse);
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

    private HttpRequest.Builder updateItemImageIndexRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType imageType,
            @org.eclipse.jdt.annotation.NonNull Integer imageIndex,
            @org.eclipse.jdt.annotation.NonNull Integer newIndex, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling updateItemImageIndex");
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new ApiException(400, "Missing the required parameter 'imageType' when calling updateItemImageIndex");
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'imageIndex' when calling updateItemImageIndex");
        }
        // verify the required parameter 'newIndex' is set
        if (newIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'newIndex' when calling updateItemImageIndex");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Images/{imageType}/{imageIndex}/Index"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{imageType}", ApiClient.urlEncode(imageType.toString()))
                .replace("{imageIndex}", ApiClient.urlEncode(imageIndex.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "newIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("newIndex", newIndex));

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
     * Uploads a custom splashscreen. The body is expected to the image contents base64 encoded.
     * 
     * @param body (optional)
     * @throws ApiException if fails to make API call
     */
    public void uploadCustomSplashscreen(@org.eclipse.jdt.annotation.Nullable File body) throws ApiException {
        uploadCustomSplashscreen(body, null);
    }

    /**
     * Uploads a custom splashscreen. The body is expected to the image contents base64 encoded.
     * 
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void uploadCustomSplashscreen(@org.eclipse.jdt.annotation.Nullable File body, Map<String, String> headers)
            throws ApiException {
        uploadCustomSplashscreenWithHttpInfo(body, headers);
    }

    /**
     * Uploads a custom splashscreen. The body is expected to the image contents base64 encoded.
     * 
     * @param body (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> uploadCustomSplashscreenWithHttpInfo(@org.eclipse.jdt.annotation.Nullable File body)
            throws ApiException {
        return uploadCustomSplashscreenWithHttpInfo(body, null);
    }

    /**
     * Uploads a custom splashscreen. The body is expected to the image contents base64 encoded.
     * 
     * @param body (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> uploadCustomSplashscreenWithHttpInfo(@org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = uploadCustomSplashscreenRequestBuilder(body, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("uploadCustomSplashscreen", localVarResponse);
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

    private HttpRequest.Builder uploadCustomSplashscreenRequestBuilder(@org.eclipse.jdt.annotation.Nullable File body,
            Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Branding/Splashscreen";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "image/*");
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
