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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.DisplayPreferencesDto;

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
    private final Consumer<HttpResponse<InputStream>> memberVarAsyncResponseInterceptor;

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
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @return DisplayPreferencesDto
     * @throws ApiException if fails to make API call
     */
    public DisplayPreferencesDto getDisplayPreferences(@org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return getDisplayPreferences(displayPreferencesId, client, userId, null);
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return DisplayPreferencesDto
     * @throws ApiException if fails to make API call
     */
    public DisplayPreferencesDto getDisplayPreferences(@org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<DisplayPreferencesDto> localVarResponse = getDisplayPreferencesWithHttpInfo(displayPreferencesId,
                client, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;DisplayPreferencesDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DisplayPreferencesDto> getDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return getDisplayPreferencesWithHttpInfo(displayPreferencesId, client, userId, null);
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;DisplayPreferencesDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<DisplayPreferencesDto> getDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getDisplayPreferencesRequestBuilder(displayPreferencesId, client,
                userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getDisplayPreferences", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<DisplayPreferencesDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                DisplayPreferencesDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<DisplayPreferencesDto>() {
                        });

                return new ApiResponse<DisplayPreferencesDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getDisplayPreferencesRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling getDisplayPreferences");
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
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateDisplayPreferences(@org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client,
            @org.eclipse.jdt.annotation.NonNull DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        updateDisplayPreferences(displayPreferencesId, client, displayPreferencesDto, userId, null);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateDisplayPreferences(@org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client,
            @org.eclipse.jdt.annotation.NonNull DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        updateDisplayPreferencesWithHttpInfo(displayPreferencesId, client, displayPreferencesDto, userId, headers);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client,
            @org.eclipse.jdt.annotation.NonNull DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return updateDisplayPreferencesWithHttpInfo(displayPreferencesId, client, displayPreferencesDto, userId, null);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client,
            @org.eclipse.jdt.annotation.NonNull DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateDisplayPreferencesRequestBuilder(displayPreferencesId,
                client, displayPreferencesDto, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateDisplayPreferences", localVarResponse);
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

    private HttpRequest.Builder updateDisplayPreferencesRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String displayPreferencesId,
            @org.eclipse.jdt.annotation.NonNull String client,
            @org.eclipse.jdt.annotation.NonNull DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling updateDisplayPreferences");
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
        localVarRequestBuilder.header("Accept", "text/html");

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
