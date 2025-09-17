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
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.AuthenticateUserByName;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.AuthenticationResult;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.CreateUserByName;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.ForgotPasswordDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.ForgotPasswordPinDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.ForgotPasswordResult;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.PinRedeemResult;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.QuickConnectDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.UpdateUserEasyPassword;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.UpdateUserPassword;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.UserConfiguration;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.UserDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.UserPolicy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UserApi {
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

    public UserApi() {
        this(Configuration.getDefaultApiClient());
    }

    public UserApi(ApiClient apiClient) {
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
     * Authenticates a user.
     * 
     * @param userId The user id. (required)
     * @param pw The password as plain text. (required)
     * @param password The password sha1-hash. (optional)
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     */
    public AuthenticationResult authenticateUser(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String pw, @org.eclipse.jdt.annotation.NonNull String password)
            throws ApiException {
        return authenticateUser(userId, pw, password, null);
    }

    /**
     * Authenticates a user.
     * 
     * @param userId The user id. (required)
     * @param pw The password as plain text. (required)
     * @param password The password sha1-hash. (optional)
     * @param headers Optional headers to include in the request
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     */
    public AuthenticationResult authenticateUser(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String pw, @org.eclipse.jdt.annotation.NonNull String password,
            Map<String, String> headers) throws ApiException {
        ApiResponse<AuthenticationResult> localVarResponse = authenticateUserWithHttpInfo(userId, pw, password,
                headers);
        return localVarResponse.getData();
    }

    /**
     * Authenticates a user.
     * 
     * @param userId The user id. (required)
     * @param pw The password as plain text. (required)
     * @param password The password sha1-hash. (optional)
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthenticationResult> authenticateUserWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String pw,
            @org.eclipse.jdt.annotation.NonNull String password) throws ApiException {
        return authenticateUserWithHttpInfo(userId, pw, password, null);
    }

    /**
     * Authenticates a user.
     * 
     * @param userId The user id. (required)
     * @param pw The password as plain text. (required)
     * @param password The password sha1-hash. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthenticationResult> authenticateUserWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable String pw,
            @org.eclipse.jdt.annotation.NonNull String password, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = authenticateUserRequestBuilder(userId, pw, password, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("authenticateUser", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                AuthenticationResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<AuthenticationResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder authenticateUserRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String pw, @org.eclipse.jdt.annotation.NonNull String password,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling authenticateUser");
        }
        // verify the required parameter 'pw' is set
        if (pw == null) {
            throw new ApiException(400, "Missing the required parameter 'pw' when calling authenticateUser");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}/Authenticate".replace("{userId}",
                ApiClient.urlEncode(userId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "pw";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("pw", pw));
        localVarQueryParameterBaseName = "password";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("password", password));

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
     * Authenticates a user by name.
     * 
     * @param authenticateUserByName The
     *            M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName)
     *            request. (required)
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     */
    public AuthenticationResult authenticateUserByName(
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName) throws ApiException {
        return authenticateUserByName(authenticateUserByName, null);
    }

    /**
     * Authenticates a user by name.
     * 
     * @param authenticateUserByName The
     *            M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName)
     *            request. (required)
     * @param headers Optional headers to include in the request
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     */
    public AuthenticationResult authenticateUserByName(
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName,
            Map<String, String> headers) throws ApiException {
        ApiResponse<AuthenticationResult> localVarResponse = authenticateUserByNameWithHttpInfo(authenticateUserByName,
                headers);
        return localVarResponse.getData();
    }

    /**
     * Authenticates a user by name.
     * 
     * @param authenticateUserByName The
     *            M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName)
     *            request. (required)
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthenticationResult> authenticateUserByNameWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName) throws ApiException {
        return authenticateUserByNameWithHttpInfo(authenticateUserByName, null);
    }

    /**
     * Authenticates a user by name.
     * 
     * @param authenticateUserByName The
     *            M:Jellyfin.Api.Controllers.UserController.AuthenticateUserByName(Jellyfin.Api.Models.UserDtos.AuthenticateUserByName)
     *            request. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthenticationResult> authenticateUserByNameWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = authenticateUserByNameRequestBuilder(authenticateUserByName,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("authenticateUserByName", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                AuthenticationResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<AuthenticationResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder authenticateUserByNameRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'authenticateUserByName' is set
        if (authenticateUserByName == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'authenticateUserByName' when calling authenticateUserByName");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/AuthenticateByName";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(authenticateUserByName);
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
     * Authenticates a user with quick connect.
     * 
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request. (required)
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     */
    public AuthenticationResult authenticateWithQuickConnect(
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto) throws ApiException {
        return authenticateWithQuickConnect(quickConnectDto, null);
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request. (required)
     * @param headers Optional headers to include in the request
     * @return AuthenticationResult
     * @throws ApiException if fails to make API call
     */
    public AuthenticationResult authenticateWithQuickConnect(
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto, Map<String, String> headers)
            throws ApiException {
        ApiResponse<AuthenticationResult> localVarResponse = authenticateWithQuickConnectWithHttpInfo(quickConnectDto,
                headers);
        return localVarResponse.getData();
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request. (required)
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthenticationResult> authenticateWithQuickConnectWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto) throws ApiException {
        return authenticateWithQuickConnectWithHttpInfo(quickConnectDto, null);
    }

    /**
     * Authenticates a user with quick connect.
     * 
     * @param quickConnectDto The Jellyfin.Api.Models.UserDtos.QuickConnectDto request. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;AuthenticationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AuthenticationResult> authenticateWithQuickConnectWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = authenticateWithQuickConnectRequestBuilder(quickConnectDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("authenticateWithQuickConnect", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                AuthenticationResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<AuthenticationResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder authenticateWithQuickConnectRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'quickConnectDto' is set
        if (quickConnectDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'quickConnectDto' when calling authenticateWithQuickConnect");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/AuthenticateWithQuickConnect";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(quickConnectDto);
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
     * Creates a user.
     * 
     * @param createUserByName The create user by name request body. (required)
     * @return UserDto
     * @throws ApiException if fails to make API call
     */
    public UserDto createUserByName(@org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName)
            throws ApiException {
        return createUserByName(createUserByName, null);
    }

    /**
     * Creates a user.
     * 
     * @param createUserByName The create user by name request body. (required)
     * @param headers Optional headers to include in the request
     * @return UserDto
     * @throws ApiException if fails to make API call
     */
    public UserDto createUserByName(@org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName,
            Map<String, String> headers) throws ApiException {
        ApiResponse<UserDto> localVarResponse = createUserByNameWithHttpInfo(createUserByName, headers);
        return localVarResponse.getData();
    }

    /**
     * Creates a user.
     * 
     * @param createUserByName The create user by name request body. (required)
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserDto> createUserByNameWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName) throws ApiException {
        return createUserByNameWithHttpInfo(createUserByName, null);
    }

    /**
     * Creates a user.
     * 
     * @param createUserByName The create user by name request body. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserDto> createUserByNameWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createUserByNameRequestBuilder(createUserByName, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createUserByName", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                UserDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder createUserByNameRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'createUserByName' is set
        if (createUserByName == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'createUserByName' when calling createUserByName");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/New";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(createUserByName);
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
     * Deletes a user.
     * 
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteUser(@org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        deleteUser(userId, null);
    }

    /**
     * Deletes a user.
     * 
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteUser(@org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers)
            throws ApiException {
        deleteUserWithHttpInfo(userId, headers);
    }

    /**
     * Deletes a user.
     * 
     * @param userId The user id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return deleteUserWithHttpInfo(userId, null);
    }

    /**
     * Deletes a user.
     * 
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteUserRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteUser", localVarResponse);
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

    private HttpRequest.Builder deleteUserRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling deleteUser");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}".replace("{userId}", ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Initiates the forgot password process for a local user.
     * 
     * @param forgotPasswordDto The forgot password request containing the entered username. (required)
     * @return ForgotPasswordResult
     * @throws ApiException if fails to make API call
     */
    public ForgotPasswordResult forgotPassword(@org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto)
            throws ApiException {
        return forgotPassword(forgotPasswordDto, null);
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * @param forgotPasswordDto The forgot password request containing the entered username. (required)
     * @param headers Optional headers to include in the request
     * @return ForgotPasswordResult
     * @throws ApiException if fails to make API call
     */
    public ForgotPasswordResult forgotPassword(@org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto,
            Map<String, String> headers) throws ApiException {
        ApiResponse<ForgotPasswordResult> localVarResponse = forgotPasswordWithHttpInfo(forgotPasswordDto, headers);
        return localVarResponse.getData();
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * @param forgotPasswordDto The forgot password request containing the entered username. (required)
     * @return ApiResponse&lt;ForgotPasswordResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ForgotPasswordResult> forgotPasswordWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto) throws ApiException {
        return forgotPasswordWithHttpInfo(forgotPasswordDto, null);
    }

    /**
     * Initiates the forgot password process for a local user.
     * 
     * @param forgotPasswordDto The forgot password request containing the entered username. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;ForgotPasswordResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ForgotPasswordResult> forgotPasswordWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = forgotPasswordRequestBuilder(forgotPasswordDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("forgotPassword", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ForgotPasswordResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                ForgotPasswordResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<ForgotPasswordResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<ForgotPasswordResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder forgotPasswordRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'forgotPasswordDto' is set
        if (forgotPasswordDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'forgotPasswordDto' when calling forgotPassword");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/ForgotPassword";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(forgotPasswordDto);
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
     * Redeems a forgot password pin.
     * 
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin. (required)
     * @return PinRedeemResult
     * @throws ApiException if fails to make API call
     */
    public PinRedeemResult forgotPasswordPin(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto) throws ApiException {
        return forgotPasswordPin(forgotPasswordPinDto, null);
    }

    /**
     * Redeems a forgot password pin.
     * 
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin. (required)
     * @param headers Optional headers to include in the request
     * @return PinRedeemResult
     * @throws ApiException if fails to make API call
     */
    public PinRedeemResult forgotPasswordPin(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto, Map<String, String> headers)
            throws ApiException {
        ApiResponse<PinRedeemResult> localVarResponse = forgotPasswordPinWithHttpInfo(forgotPasswordPinDto, headers);
        return localVarResponse.getData();
    }

    /**
     * Redeems a forgot password pin.
     * 
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin. (required)
     * @return ApiResponse&lt;PinRedeemResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PinRedeemResult> forgotPasswordPinWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto) throws ApiException {
        return forgotPasswordPinWithHttpInfo(forgotPasswordPinDto, null);
    }

    /**
     * Redeems a forgot password pin.
     * 
     * @param forgotPasswordPinDto The forgot password pin request containing the entered pin. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;PinRedeemResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PinRedeemResult> forgotPasswordPinWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = forgotPasswordPinRequestBuilder(forgotPasswordPinDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("forgotPasswordPin", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<PinRedeemResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                PinRedeemResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<PinRedeemResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<PinRedeemResult>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder forgotPasswordPinRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'forgotPasswordPinDto' is set
        if (forgotPasswordPinDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'forgotPasswordPinDto' when calling forgotPasswordPin");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/ForgotPassword/Pin";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(forgotPasswordPinDto);
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
     * Gets the user based on auth token.
     * 
     * @return UserDto
     * @throws ApiException if fails to make API call
     */
    public UserDto getCurrentUser() throws ApiException {
        return getCurrentUser(null);
    }

    /**
     * Gets the user based on auth token.
     * 
     * @param headers Optional headers to include in the request
     * @return UserDto
     * @throws ApiException if fails to make API call
     */
    public UserDto getCurrentUser(Map<String, String> headers) throws ApiException {
        ApiResponse<UserDto> localVarResponse = getCurrentUserWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the user based on auth token.
     * 
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserDto> getCurrentUserWithHttpInfo() throws ApiException {
        return getCurrentUserWithHttpInfo(null);
    }

    /**
     * Gets the user based on auth token.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserDto> getCurrentUserWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getCurrentUserRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getCurrentUser", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                UserDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getCurrentUserRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/Me";

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
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * @return List&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<UserDto> getPublicUsers() throws ApiException {
        return getPublicUsers(null);
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<UserDto> getPublicUsers(Map<String, String> headers) throws ApiException {
        ApiResponse<List<UserDto>> localVarResponse = getPublicUsersWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * @return ApiResponse&lt;List&lt;UserDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<UserDto>> getPublicUsersWithHttpInfo() throws ApiException {
        return getPublicUsersWithHttpInfo(null);
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;UserDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<UserDto>> getPublicUsersWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPublicUsersRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPublicUsers", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<UserDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<UserDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<UserDto>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<UserDto>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getPublicUsersRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/Public";

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
     * Gets a user by Id.
     * 
     * @param userId The user id. (required)
     * @return UserDto
     * @throws ApiException if fails to make API call
     */
    public UserDto getUserById(@org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getUserById(userId, null);
    }

    /**
     * Gets a user by Id.
     * 
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @return UserDto
     * @throws ApiException if fails to make API call
     */
    public UserDto getUserById(@org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<UserDto> localVarResponse = getUserByIdWithHttpInfo(userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a user by Id.
     * 
     * @param userId The user id. (required)
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserDto> getUserByIdWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return getUserByIdWithHttpInfo(userId, null);
    }

    /**
     * Gets a user by Id.
     * 
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserDto> getUserByIdWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getUserByIdRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getUserById", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                UserDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getUserByIdRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getUserById");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}".replace("{userId}", ApiClient.urlEncode(userId.toString()));

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
     * Gets a list of users.
     * 
     * @param isHidden Optional filter by IsHidden&#x3D;true or false. (optional)
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false. (optional)
     * @return List&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<UserDto> getUsers(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled) throws ApiException {
        return getUsers(isHidden, isDisabled, null);
    }

    /**
     * Gets a list of users.
     * 
     * @param isHidden Optional filter by IsHidden&#x3D;true or false. (optional)
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false. (optional)
     * @param headers Optional headers to include in the request
     * @return List&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<UserDto> getUsers(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled, Map<String, String> headers) throws ApiException {
        ApiResponse<List<UserDto>> localVarResponse = getUsersWithHttpInfo(isHidden, isDisabled, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of users.
     * 
     * @param isHidden Optional filter by IsHidden&#x3D;true or false. (optional)
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false. (optional)
     * @return ApiResponse&lt;List&lt;UserDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<UserDto>> getUsersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled) throws ApiException {
        return getUsersWithHttpInfo(isHidden, isDisabled, null);
    }

    /**
     * Gets a list of users.
     * 
     * @param isHidden Optional filter by IsHidden&#x3D;true or false. (optional)
     * @param isDisabled Optional filter by IsDisabled&#x3D;true or false. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;UserDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<UserDto>> getUsersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getUsersRequestBuilder(isHidden, isDisabled, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getUsers", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<UserDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<UserDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<UserDto>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<UserDto>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getUsersRequestBuilder(@org.eclipse.jdt.annotation.NonNull Boolean isHidden,
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "isHidden";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isHidden", isHidden));
        localVarQueryParameterBaseName = "isDisabled";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isDisabled", isDisabled));

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
     * Updates a user.
     * 
     * @param userId The user id. (required)
     * @param userDto The updated user model. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateUser(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserDto userDto) throws ApiException {
        updateUser(userId, userDto, null);
    }

    /**
     * Updates a user.
     * 
     * @param userId The user id. (required)
     * @param userDto The updated user model. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateUser(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserDto userDto, Map<String, String> headers) throws ApiException {
        updateUserWithHttpInfo(userId, userDto, headers);
    }

    /**
     * Updates a user.
     * 
     * @param userId The user id. (required)
     * @param userDto The updated user model. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserDto userDto) throws ApiException {
        return updateUserWithHttpInfo(userId, userDto, null);
    }

    /**
     * Updates a user.
     * 
     * @param userId The user id. (required)
     * @param userDto The updated user model. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserDto userDto, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserRequestBuilder(userId, userDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateUser", localVarResponse);
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

    private HttpRequest.Builder updateUserRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserDto userDto, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updateUser");
        }
        // verify the required parameter 'userDto' is set
        if (userDto == null) {
            throw new ApiException(400, "Missing the required parameter 'userDto' when calling updateUser");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}".replace("{userId}", ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(userDto);
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
     * Updates a user configuration.
     * 
     * @param userId The user id. (required)
     * @param userConfiguration The new user configuration. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateUserConfiguration(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration) throws ApiException {
        updateUserConfiguration(userId, userConfiguration, null);
    }

    /**
     * Updates a user configuration.
     * 
     * @param userId The user id. (required)
     * @param userConfiguration The new user configuration. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateUserConfiguration(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration, Map<String, String> headers)
            throws ApiException {
        updateUserConfigurationWithHttpInfo(userId, userConfiguration, headers);
    }

    /**
     * Updates a user configuration.
     * 
     * @param userId The user id. (required)
     * @param userConfiguration The new user configuration. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration) throws ApiException {
        return updateUserConfigurationWithHttpInfo(userId, userConfiguration, null);
    }

    /**
     * Updates a user configuration.
     * 
     * @param userId The user id. (required)
     * @param userConfiguration The new user configuration. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserConfigurationWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserConfigurationRequestBuilder(userId, userConfiguration,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateUserConfiguration", localVarResponse);
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

    private HttpRequest.Builder updateUserConfigurationRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updateUserConfiguration");
        }
        // verify the required parameter 'userConfiguration' is set
        if (userConfiguration == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'userConfiguration' when calling updateUserConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}/Configuration".replace("{userId}",
                ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(userConfiguration);
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
     * Updates a user&#39;s easy password.
     * 
     * @param userId The user id. (required)
     * @param updateUserEasyPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserEasyPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserEasyPassword)
     *            request. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateUserEasyPassword(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserEasyPassword updateUserEasyPassword) throws ApiException {
        updateUserEasyPassword(userId, updateUserEasyPassword, null);
    }

    /**
     * Updates a user&#39;s easy password.
     * 
     * @param userId The user id. (required)
     * @param updateUserEasyPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserEasyPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserEasyPassword)
     *            request. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateUserEasyPassword(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserEasyPassword updateUserEasyPassword,
            Map<String, String> headers) throws ApiException {
        updateUserEasyPasswordWithHttpInfo(userId, updateUserEasyPassword, headers);
    }

    /**
     * Updates a user&#39;s easy password.
     * 
     * @param userId The user id. (required)
     * @param updateUserEasyPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserEasyPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserEasyPassword)
     *            request. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserEasyPasswordWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserEasyPassword updateUserEasyPassword) throws ApiException {
        return updateUserEasyPasswordWithHttpInfo(userId, updateUserEasyPassword, null);
    }

    /**
     * Updates a user&#39;s easy password.
     * 
     * @param userId The user id. (required)
     * @param updateUserEasyPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserEasyPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserEasyPassword)
     *            request. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserEasyPasswordWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserEasyPassword updateUserEasyPassword,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserEasyPasswordRequestBuilder(userId,
                updateUserEasyPassword, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateUserEasyPassword", localVarResponse);
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

    private HttpRequest.Builder updateUserEasyPasswordRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserEasyPassword updateUserEasyPassword,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updateUserEasyPassword");
        }
        // verify the required parameter 'updateUserEasyPassword' is set
        if (updateUserEasyPassword == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updateUserEasyPassword' when calling updateUserEasyPassword");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}/EasyPassword".replace("{userId}",
                ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updateUserEasyPassword);
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
     * Updates a user&#39;s password.
     * 
     * @param userId The user id. (required)
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateUserPassword(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword) throws ApiException {
        updateUserPassword(userId, updateUserPassword, null);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * @param userId The user id. (required)
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateUserPassword(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword, Map<String, String> headers)
            throws ApiException {
        updateUserPasswordWithHttpInfo(userId, updateUserPassword, headers);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * @param userId The user id. (required)
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserPasswordWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword) throws ApiException {
        return updateUserPasswordWithHttpInfo(userId, updateUserPassword, null);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * @param userId The user id. (required)
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Guid,Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserPasswordWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserPasswordRequestBuilder(userId, updateUserPassword,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateUserPassword", localVarResponse);
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

    private HttpRequest.Builder updateUserPasswordRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updateUserPassword");
        }
        // verify the required parameter 'updateUserPassword' is set
        if (updateUserPassword == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updateUserPassword' when calling updateUserPassword");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}/Password".replace("{userId}", ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updateUserPassword);
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
     * Updates a user policy.
     * 
     * @param userId The user id. (required)
     * @param userPolicy The new user policy. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateUserPolicy(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy) throws ApiException {
        updateUserPolicy(userId, userPolicy, null);
    }

    /**
     * Updates a user policy.
     * 
     * @param userId The user id. (required)
     * @param userPolicy The new user policy. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateUserPolicy(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy, Map<String, String> headers)
            throws ApiException {
        updateUserPolicyWithHttpInfo(userId, userPolicy, headers);
    }

    /**
     * Updates a user policy.
     * 
     * @param userId The user id. (required)
     * @param userPolicy The new user policy. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserPolicyWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy) throws ApiException {
        return updateUserPolicyWithHttpInfo(userId, userPolicy, null);
    }

    /**
     * Updates a user policy.
     * 
     * @param userId The user id. (required)
     * @param userPolicy The new user policy. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserPolicyWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserPolicyRequestBuilder(userId, userPolicy, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateUserPolicy", localVarResponse);
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

    private HttpRequest.Builder updateUserPolicyRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updateUserPolicy");
        }
        // verify the required parameter 'userPolicy' is set
        if (userPolicy == null) {
            throw new ApiException(400, "Missing the required parameter 'userPolicy' when calling updateUserPolicy");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/{userId}/Policy".replace("{userId}", ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(userPolicy);
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
