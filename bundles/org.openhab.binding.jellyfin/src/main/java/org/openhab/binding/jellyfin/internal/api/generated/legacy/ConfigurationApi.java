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
import java.util.Map;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.MediaEncoderPathDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.MetadataOptions;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.ServerConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ConfigurationApi {
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

    public ConfigurationApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ConfigurationApi(ApiClient apiClient) {
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
     * Gets application configuration.
     * 
     * @return ServerConfiguration
     * @throws ApiException if fails to make API call
     */
    public ServerConfiguration getConfiguration() throws ApiException {
        return getConfiguration(null);
    }

    /**
     * Gets application configuration.
     * 
     * @param headers Optional headers to include in the request
     * @return ServerConfiguration
     * @throws ApiException if fails to make API call
     */
    public ServerConfiguration getConfiguration(Map<String, String> headers) throws ApiException {
        ApiResponse<ServerConfiguration> localVarResponse = getConfigurationWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets application configuration.
     * 
     * @return ApiResponse&lt;ServerConfiguration&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ServerConfiguration> getConfigurationWithHttpInfo() throws ApiException {
        return getConfigurationWithHttpInfo(null);
    }

    /**
     * Gets application configuration.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;ServerConfiguration&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ServerConfiguration> getConfigurationWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getConfigurationRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getConfiguration", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ServerConfiguration>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                ServerConfiguration responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<ServerConfiguration>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<ServerConfiguration>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getConfigurationRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/System/Configuration";

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
     * Gets a default MetadataOptions object.
     * 
     * @return MetadataOptions
     * @throws ApiException if fails to make API call
     */
    public MetadataOptions getDefaultMetadataOptions() throws ApiException {
        return getDefaultMetadataOptions(null);
    }

    /**
     * Gets a default MetadataOptions object.
     * 
     * @param headers Optional headers to include in the request
     * @return MetadataOptions
     * @throws ApiException if fails to make API call
     */
    public MetadataOptions getDefaultMetadataOptions(Map<String, String> headers) throws ApiException {
        ApiResponse<MetadataOptions> localVarResponse = getDefaultMetadataOptionsWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a default MetadataOptions object.
     * 
     * @return ApiResponse&lt;MetadataOptions&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<MetadataOptions> getDefaultMetadataOptionsWithHttpInfo() throws ApiException {
        return getDefaultMetadataOptionsWithHttpInfo(null);
    }

    /**
     * Gets a default MetadataOptions object.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;MetadataOptions&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<MetadataOptions> getDefaultMetadataOptionsWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDefaultMetadataOptionsRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDefaultMetadataOptions", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<MetadataOptions>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                MetadataOptions responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<MetadataOptions>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<MetadataOptions>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getDefaultMetadataOptionsRequestBuilder(Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/System/Configuration/MetadataOptions/Default";

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
     * Gets a named configuration.
     * 
     * @param key Configuration key. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getNamedConfiguration(@org.eclipse.jdt.annotation.Nullable String key) throws ApiException {
        return getNamedConfiguration(key, null);
    }

    /**
     * Gets a named configuration.
     * 
     * @param key Configuration key. (required)
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getNamedConfiguration(@org.eclipse.jdt.annotation.Nullable String key, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getNamedConfigurationWithHttpInfo(key, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a named configuration.
     * 
     * @param key Configuration key. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getNamedConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String key)
            throws ApiException {
        return getNamedConfigurationWithHttpInfo(key, null);
    }

    /**
     * Gets a named configuration.
     * 
     * @param key Configuration key. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getNamedConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String key,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getNamedConfigurationRequestBuilder(key, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getNamedConfiguration", localVarResponse);
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

    private HttpRequest.Builder getNamedConfigurationRequestBuilder(@org.eclipse.jdt.annotation.Nullable String key,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'key' is set
        if (key == null) {
            throw new ApiException(400, "Missing the required parameter 'key' when calling getNamedConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/System/Configuration/{key}".replace("{key}", ApiClient.urlEncode(key.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

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
     * Updates application configuration.
     * 
     * @param serverConfiguration Configuration. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateConfiguration(@org.eclipse.jdt.annotation.Nullable ServerConfiguration serverConfiguration)
            throws ApiException {
        updateConfiguration(serverConfiguration, null);
    }

    /**
     * Updates application configuration.
     * 
     * @param serverConfiguration Configuration. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateConfiguration(@org.eclipse.jdt.annotation.Nullable ServerConfiguration serverConfiguration,
            Map<String, String> headers) throws ApiException {
        updateConfigurationWithHttpInfo(serverConfiguration, headers);
    }

    /**
     * Updates application configuration.
     * 
     * @param serverConfiguration Configuration. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateConfigurationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ServerConfiguration serverConfiguration) throws ApiException {
        return updateConfigurationWithHttpInfo(serverConfiguration, null);
    }

    /**
     * Updates application configuration.
     * 
     * @param serverConfiguration Configuration. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateConfigurationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ServerConfiguration serverConfiguration, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateConfigurationRequestBuilder(serverConfiguration, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateConfiguration", localVarResponse);
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

    private HttpRequest.Builder updateConfigurationRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable ServerConfiguration serverConfiguration, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'serverConfiguration' is set
        if (serverConfiguration == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'serverConfiguration' when calling updateConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/System/Configuration";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(serverConfiguration);
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
     * Updates the path to the media encoder.
     * 
     * @param mediaEncoderPathDto Media encoder path form body. (required)
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void updateMediaEncoderPath(@org.eclipse.jdt.annotation.Nullable MediaEncoderPathDto mediaEncoderPathDto)
            throws ApiException {
        updateMediaEncoderPath(mediaEncoderPathDto, null);
    }

    /**
     * Updates the path to the media encoder.
     * 
     * @param mediaEncoderPathDto Media encoder path form body. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public void updateMediaEncoderPath(@org.eclipse.jdt.annotation.Nullable MediaEncoderPathDto mediaEncoderPathDto,
            Map<String, String> headers) throws ApiException {
        updateMediaEncoderPathWithHttpInfo(mediaEncoderPathDto, headers);
    }

    /**
     * Updates the path to the media encoder.
     * 
     * @param mediaEncoderPathDto Media encoder path form body. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> updateMediaEncoderPathWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MediaEncoderPathDto mediaEncoderPathDto) throws ApiException {
        return updateMediaEncoderPathWithHttpInfo(mediaEncoderPathDto, null);
    }

    /**
     * Updates the path to the media encoder.
     * 
     * @param mediaEncoderPathDto Media encoder path form body. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @deprecated
     */
    @Deprecated
    public ApiResponse<Void> updateMediaEncoderPathWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MediaEncoderPathDto mediaEncoderPathDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateMediaEncoderPathRequestBuilder(mediaEncoderPathDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateMediaEncoderPath", localVarResponse);
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

    private HttpRequest.Builder updateMediaEncoderPathRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable MediaEncoderPathDto mediaEncoderPathDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'mediaEncoderPathDto' is set
        if (mediaEncoderPathDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaEncoderPathDto' when calling updateMediaEncoderPath");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/System/MediaEncoder/Path";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(mediaEncoderPathDto);
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
     * Updates named configuration.
     * 
     * @param key Configuration key. (required)
     * @param body Configuration. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateNamedConfiguration(@org.eclipse.jdt.annotation.Nullable String key,
            @org.eclipse.jdt.annotation.Nullable Object body) throws ApiException {
        updateNamedConfiguration(key, body, null);
    }

    /**
     * Updates named configuration.
     * 
     * @param key Configuration key. (required)
     * @param body Configuration. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateNamedConfiguration(@org.eclipse.jdt.annotation.Nullable String key,
            @org.eclipse.jdt.annotation.Nullable Object body, Map<String, String> headers) throws ApiException {
        updateNamedConfigurationWithHttpInfo(key, body, headers);
    }

    /**
     * Updates named configuration.
     * 
     * @param key Configuration key. (required)
     * @param body Configuration. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateNamedConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String key,
            @org.eclipse.jdt.annotation.Nullable Object body) throws ApiException {
        return updateNamedConfigurationWithHttpInfo(key, body, null);
    }

    /**
     * Updates named configuration.
     * 
     * @param key Configuration key. (required)
     * @param body Configuration. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateNamedConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String key,
            @org.eclipse.jdt.annotation.Nullable Object body, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateNamedConfigurationRequestBuilder(key, body, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateNamedConfiguration", localVarResponse);
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

    private HttpRequest.Builder updateNamedConfigurationRequestBuilder(@org.eclipse.jdt.annotation.Nullable String key,
            @org.eclipse.jdt.annotation.Nullable Object body, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'key' is set
        if (key == null) {
            throw new ApiException(400, "Missing the required parameter 'key' when calling updateNamedConfiguration");
        }
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling updateNamedConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/System/Configuration/{key}".replace("{key}", ApiClient.urlEncode(key.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

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
