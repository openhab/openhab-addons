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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.QuickConnectResult;

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
    private final Consumer<HttpResponse<InputStream>> memberVarAsyncResponseInterceptor;

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
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param userId The user the authorize. Access to the requested user is required. (optional)
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean authorizeQuickConnect(@org.eclipse.jdt.annotation.NonNull String code,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return authorizeQuickConnect(code, userId, null);
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param userId The user the authorize. Access to the requested user is required. (optional)
     * @param headers Optional headers to include in the request
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean authorizeQuickConnect(@org.eclipse.jdt.annotation.NonNull String code,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<Boolean> localVarResponse = authorizeQuickConnectWithHttpInfo(code, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param userId The user the authorize. Access to the requested user is required. (optional)
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> authorizeQuickConnectWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String code,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return authorizeQuickConnectWithHttpInfo(code, userId, null);
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param userId The user the authorize. Access to the requested user is required. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> authorizeQuickConnectWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String code,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = authorizeQuickConnectRequestBuilder(code, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("authorizeQuickConnect", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                Boolean responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<Boolean>() {
                        });

                return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder authorizeQuickConnectRequestBuilder(@org.eclipse.jdt.annotation.NonNull String code,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'code' is set
        if (code == null) {
            throw new ApiException(400, "Missing the required parameter 'code' when calling authorizeQuickConnect");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/QuickConnect/Authorize";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "code";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("code", code));
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
     * Gets the current quick connect state.
     * 
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean getQuickConnectEnabled() throws ApiException {
        return getQuickConnectEnabled(null);
    }

    /**
     * Gets the current quick connect state.
     * 
     * @param headers Optional headers to include in the request
     * @return Boolean
     * @throws ApiException if fails to make API call
     */
    public Boolean getQuickConnectEnabled(Map<String, String> headers) throws ApiException {
        ApiResponse<Boolean> localVarResponse = getQuickConnectEnabledWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the current quick connect state.
     * 
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> getQuickConnectEnabledWithHttpInfo() throws ApiException {
        return getQuickConnectEnabledWithHttpInfo(null);
    }

    /**
     * Gets the current quick connect state.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Boolean&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Boolean> getQuickConnectEnabledWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getQuickConnectEnabledRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getQuickConnectEnabled", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                Boolean responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<Boolean>() {
                        });

                return new ApiResponse<Boolean>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getQuickConnectEnabledRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/QuickConnect/Enabled";

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
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult getQuickConnectState(@org.eclipse.jdt.annotation.NonNull String secret)
            throws ApiException {
        return getQuickConnectState(secret, null);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @param headers Optional headers to include in the request
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult getQuickConnectState(@org.eclipse.jdt.annotation.NonNull String secret,
            Map<String, String> headers) throws ApiException {
        ApiResponse<QuickConnectResult> localVarResponse = getQuickConnectStateWithHttpInfo(secret, headers);
        return localVarResponse.getData();
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> getQuickConnectStateWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String secret) throws ApiException {
        return getQuickConnectStateWithHttpInfo(secret, null);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> getQuickConnectStateWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String secret, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getQuickConnectStateRequestBuilder(secret, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getQuickConnectState", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                QuickConnectResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<QuickConnectResult>() {
                        });

                return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getQuickConnectStateRequestBuilder(@org.eclipse.jdt.annotation.NonNull String secret,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'secret' is set
        if (secret == null) {
            throw new ApiException(400, "Missing the required parameter 'secret' when calling getQuickConnectState");
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
     * Initiate a new quick connect request.
     * 
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult initiateQuickConnect() throws ApiException {
        return initiateQuickConnect(null);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @param headers Optional headers to include in the request
     * @return QuickConnectResult
     * @throws ApiException if fails to make API call
     */
    public QuickConnectResult initiateQuickConnect(Map<String, String> headers) throws ApiException {
        ApiResponse<QuickConnectResult> localVarResponse = initiateQuickConnectWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> initiateQuickConnectWithHttpInfo() throws ApiException {
        return initiateQuickConnectWithHttpInfo(null);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;QuickConnectResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QuickConnectResult> initiateQuickConnectWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = initiateQuickConnectRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("initiateQuickConnect", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                QuickConnectResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<QuickConnectResult>() {
                        });

                return new ApiResponse<QuickConnectResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder initiateQuickConnectRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/QuickConnect/Initiate";

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
}
