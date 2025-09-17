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
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.DisplayPreferencesDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DisplayPreferencesApi {
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

    public DisplayPreferencesApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DisplayPreferencesApi(ApiClient apiClient) {
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
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User id. (required)
     * @param client Client. (required)
     * @return DisplayPreferencesDto
     * @throws ApiException if fails to make API call
     */
    public DisplayPreferencesDto getDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client)
            throws ApiException {
        return getDisplayPreferences(displayPreferencesId, userId, client, null);
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User id. (required)
     * @param client Client. (required)
     * @param headers Optional headers to include in the request
     * @return DisplayPreferencesDto
     * @throws ApiException if fails to make API call
     */
    public DisplayPreferencesDto getDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            Map<String, String> headers) throws ApiException {
        ApiResponse<DisplayPreferencesDto> localVarResponse = getDisplayPreferencesWithHttpInfo(displayPreferencesId,
                userId, client, headers);
        return localVarResponse.getData();
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User id. (required)
     * @param client Client. (required)
     * @return ApiResponse&lt;DisplayPreferencesDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DisplayPreferencesDto> getDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client)
            throws ApiException {
        return getDisplayPreferencesWithHttpInfo(displayPreferencesId, userId, client, null);
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User id. (required)
     * @param client Client. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DisplayPreferencesDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DisplayPreferencesDto> getDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDisplayPreferencesRequestBuilder(displayPreferencesId, userId,
                client, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDisplayPreferences", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<DisplayPreferencesDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                DisplayPreferencesDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DisplayPreferencesDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<DisplayPreferencesDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDisplayPreferencesRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling getDisplayPreferences");
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getDisplayPreferences");
        }
        // verify the required parameter 'client' is set
        if (client == null) {
            throw new ApiException(400, "Missing the required parameter 'client' when calling getDisplayPreferences");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/DisplayPreferences/{displayPreferencesId}".replace("{displayPreferencesId}",
                ApiClient.urlEncode(displayPreferencesId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "client";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("client", client));

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
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User Id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto) throws ApiException {
        updateDisplayPreferences(displayPreferencesId, userId, client, displayPreferencesDto, null);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User Id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            Map<String, String> headers) throws ApiException {
        updateDisplayPreferencesWithHttpInfo(displayPreferencesId, userId, client, displayPreferencesDto, headers);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User Id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto) throws ApiException {
        return updateDisplayPreferencesWithHttpInfo(displayPreferencesId, userId, client, displayPreferencesDto, null);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param userId User Id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateDisplayPreferencesRequestBuilder(displayPreferencesId,
                userId, client, displayPreferencesDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateDisplayPreferences", localVarResponse);
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

    private HttpRequest.Builder updateDisplayPreferencesRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling updateDisplayPreferences");
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'userId' when calling updateDisplayPreferences");
        }
        // verify the required parameter 'client' is set
        if (client == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'client' when calling updateDisplayPreferences");
        }
        // verify the required parameter 'displayPreferencesDto' is set
        if (displayPreferencesDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesDto' when calling updateDisplayPreferences");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/DisplayPreferences/{displayPreferencesId}".replace("{displayPreferencesId}",
                ApiClient.urlEncode(displayPreferencesId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "client";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("client", client));

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

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(displayPreferencesDto);
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
