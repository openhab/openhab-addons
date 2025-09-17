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
package org.openhab.binding.jellyfin.internal.api.generated.legacy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.ImageByNameInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageByNameApi {
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

    public ImageByNameApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ImageByNameApi(ApiClient apiClient) {
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
     * Get General Image.
     * 
     * @param name The name of the image. (required)
     * @param type Image Type (primary, backdrop, logo, etc). (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getGeneralImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String type) throws ApiException {
        return getGeneralImage(name, type, null);
    }

    /**
     * Get General Image.
     * 
     * @param name The name of the image. (required)
     * @param type Image Type (primary, backdrop, logo, etc). (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getGeneralImage(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String type, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getGeneralImageWithHttpInfo(name, type, headers);
        return localVarResponse.getData();
    }

    /**
     * Get General Image.
     * 
     * @param name The name of the image. (required)
     * @param type Image Type (primary, backdrop, logo, etc). (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getGeneralImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String type) throws ApiException {
        return getGeneralImageWithHttpInfo(name, type, null);
    }

    /**
     * Get General Image.
     * 
     * @param name The name of the image. (required)
     * @param type Image Type (primary, backdrop, logo, etc). (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getGeneralImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String type, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getGeneralImageRequestBuilder(name, type, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getGeneralImage", localVarResponse);
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

    private HttpRequest.Builder getGeneralImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable String type, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getGeneralImage");
        }
        // verify the required parameter 'type' is set
        if (type == null) {
            throw new ApiException(400, "Missing the required parameter 'type' when calling getGeneralImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Images/General/{name}/{type}".replace("{name}", ApiClient.urlEncode(name.toString()))
                .replace("{type}", ApiClient.urlEncode(type.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "image/*, application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Get all general images.
     * 
     * @return List&lt;ImageByNameInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageByNameInfo> getGeneralImages() throws ApiException {
        return getGeneralImages(null);
    }

    /**
     * Get all general images.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;ImageByNameInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageByNameInfo> getGeneralImages(Map<String, String> headers) throws ApiException {
        ApiResponse<List<ImageByNameInfo>> localVarResponse = getGeneralImagesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Get all general images.
     * 
     * @return ApiResponse&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageByNameInfo>> getGeneralImagesWithHttpInfo() throws ApiException {
        return getGeneralImagesWithHttpInfo(null);
    }

    /**
     * Get all general images.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageByNameInfo>> getGeneralImagesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getGeneralImagesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getGeneralImages", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<ImageByNameInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<ImageByNameInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<ImageByNameInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<ImageByNameInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getGeneralImagesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Images/General";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Get media info image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaInfoImage(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name) throws ApiException {
        return getMediaInfoImage(theme, name, null);
    }

    /**
     * Get media info image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMediaInfoImage(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getMediaInfoImageWithHttpInfo(theme, name, headers);
        return localVarResponse.getData();
    }

    /**
     * Get media info image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaInfoImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name) throws ApiException {
        return getMediaInfoImageWithHttpInfo(theme, name, null);
    }

    /**
     * Get media info image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMediaInfoImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMediaInfoImageRequestBuilder(theme, name, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMediaInfoImage", localVarResponse);
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

    private HttpRequest.Builder getMediaInfoImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'theme' is set
        if (theme == null) {
            throw new ApiException(400, "Missing the required parameter 'theme' when calling getMediaInfoImage");
        }
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getMediaInfoImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Images/MediaInfo/{theme}/{name}"
                .replace("{theme}", ApiClient.urlEncode(theme.toString()))
                .replace("{name}", ApiClient.urlEncode(name.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "image/*, application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Get all media info images.
     * 
     * @return List&lt;ImageByNameInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageByNameInfo> getMediaInfoImages() throws ApiException {
        return getMediaInfoImages(null);
    }

    /**
     * Get all media info images.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;ImageByNameInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageByNameInfo> getMediaInfoImages(Map<String, String> headers) throws ApiException {
        ApiResponse<List<ImageByNameInfo>> localVarResponse = getMediaInfoImagesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Get all media info images.
     * 
     * @return ApiResponse&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageByNameInfo>> getMediaInfoImagesWithHttpInfo() throws ApiException {
        return getMediaInfoImagesWithHttpInfo(null);
    }

    /**
     * Get all media info images.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageByNameInfo>> getMediaInfoImagesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMediaInfoImagesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMediaInfoImages", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<ImageByNameInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<ImageByNameInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<ImageByNameInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<ImageByNameInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getMediaInfoImagesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Images/MediaInfo";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Get rating image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getRatingImage(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name) throws ApiException {
        return getRatingImage(theme, name, null);
    }

    /**
     * Get rating image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getRatingImage(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name, Map<String, String> headers) throws ApiException {
        ApiResponse<File> localVarResponse = getRatingImageWithHttpInfo(theme, name, headers);
        return localVarResponse.getData();
    }

    /**
     * Get rating image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getRatingImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name) throws ApiException {
        return getRatingImageWithHttpInfo(theme, name, null);
    }

    /**
     * Get rating image.
     * 
     * @param theme The theme to get the image from. (required)
     * @param name The name of the image. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getRatingImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRatingImageRequestBuilder(theme, name, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRatingImage", localVarResponse);
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

    private HttpRequest.Builder getRatingImageRequestBuilder(@org.eclipse.jdt.annotation.Nullable String theme,
            @org.eclipse.jdt.annotation.Nullable String name, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'theme' is set
        if (theme == null) {
            throw new ApiException(400, "Missing the required parameter 'theme' when calling getRatingImage");
        }
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getRatingImage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Images/Ratings/{theme}/{name}".replace("{theme}", ApiClient.urlEncode(theme.toString()))
                .replace("{name}", ApiClient.urlEncode(name.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "image/*, application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Get all general images.
     * 
     * @return List&lt;ImageByNameInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageByNameInfo> getRatingImages() throws ApiException {
        return getRatingImages(null);
    }

    /**
     * Get all general images.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;ImageByNameInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ImageByNameInfo> getRatingImages(Map<String, String> headers) throws ApiException {
        ApiResponse<List<ImageByNameInfo>> localVarResponse = getRatingImagesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Get all general images.
     * 
     * @return ApiResponse&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageByNameInfo>> getRatingImagesWithHttpInfo() throws ApiException {
        return getRatingImagesWithHttpInfo(null);
    }

    /**
     * Get all general images.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ImageByNameInfo>> getRatingImagesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getRatingImagesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getRatingImages", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<ImageByNameInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<ImageByNameInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<ImageByNameInfo>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<ImageByNameInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getRatingImagesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Images/Ratings";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
