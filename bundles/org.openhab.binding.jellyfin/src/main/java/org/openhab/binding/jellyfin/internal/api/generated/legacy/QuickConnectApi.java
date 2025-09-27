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
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.QuickConnectResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QuickConnectApi {
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

    public QuickConnectApi() {
        this(Configuration.getDefaultApiClient());
    }

    public QuickConnectApi(ApiClient apiClient) {
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
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean authorize(@org.eclipse.jdt.annotation.Nullable String code) throws ApiException {
        return authorize(code, null);
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param headers Optional headers to include in the request
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean authorize(@org.eclipse.jdt.annotation.Nullable String code, Map<String, String> headers)
            throws ApiException {
        ApiResponse<Boolean> localVarResponse = authorizeWithHttpInfo(code, headers);
        return localVarResponse.getData();
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> authorizeWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String code)
            throws ApiException {
        return authorizeWithHttpInfo(code, null);
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> authorizeWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String code,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = authorizeRequestBuilder(code, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("authorize", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                Boolean responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<Boolean>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder authorizeRequestBuilder(@org.eclipse.jdt.annotation.Nullable String code,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'code' is set
        if (code == null) {
            throw new ApiException(400, "Missing the required parameter 'code' when calling authorize");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/QuickConnect/Authorize";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "code";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("code", code));

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
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult connect(@org.eclipse.jdt.annotation.Nullable String secret) throws ApiException {
        return connect(secret, null);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @param headers Optional headers to include in the request
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult connect(@org.eclipse.jdt.annotation.Nullable String secret, Map<String, String> headers)
            throws ApiException {
        ApiResponse<QuickConnectResult> localVarResponse = connectWithHttpInfo(secret, headers);
        return localVarResponse.getData();
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> connectWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String secret)
            throws ApiException {
        return connectWithHttpInfo(secret, null);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> connectWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String secret,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = connectRequestBuilder(secret, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("connect", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                QuickConnectResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<QuickConnectResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder connectRequestBuilder(@org.eclipse.jdt.annotation.Nullable String secret,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'secret' is set
        if (secret == null) {
            throw new ApiException(400, "Missing the required parameter 'secret' when calling connect");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/QuickConnect/Connect";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "secret";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("secret", secret));

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
     * Gets the current quick connect state.
     * 
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean getEnabled() throws ApiException {
        return getEnabled(null);
    }

    /**
     * Gets the current quick connect state.
     * 
     * @param headers Optional headers to include in the request
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean getEnabled(Map<String, String> headers) throws ApiException {
        ApiResponse<Boolean> localVarResponse = getEnabledWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the current quick connect state.
     * 
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> getEnabledWithHttpInfo() throws ApiException {
        return getEnabledWithHttpInfo(null);
    }

    /**
     * Gets the current quick connect state.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> getEnabledWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getEnabledRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getEnabled", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                Boolean responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<Boolean>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getEnabledRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/QuickConnect/Enabled";

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
     * Initiate a new quick connect request.
     * 
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult initiate() throws ApiException {
        return initiate(null);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @param headers Optional headers to include in the request
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult initiate(Map<String, String> headers) throws ApiException {
        ApiResponse<QuickConnectResult> localVarResponse = initiateWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> initiateWithHttpInfo() throws ApiException {
        return initiateWithHttpInfo(null);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> initiateWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = initiateRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("initiate", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                QuickConnectResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<QuickConnectResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder initiateRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/QuickConnect/Initiate";

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
