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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AuthenticateUserByName;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AuthenticationResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CreateUserByName;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ForgotPasswordDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ForgotPasswordPinDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ForgotPasswordResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PinRedeemResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QuickConnectDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdateUserPassword;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserConfiguration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserPolicy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UserApi {
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
        ApiResponse<AuthenticationResult> localVarResponse = authenticateUserByNameWithHttpInfo(authenticateUserByName);
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
        HttpRequest.Builder localVarRequestBuilder = authenticateUserByNameRequestBuilder(authenticateUserByName);
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
                localVarResponse.body().close();

                return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<AuthenticationResult>() {
                                        }));
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
            @org.eclipse.jdt.annotation.Nullable AuthenticateUserByName authenticateUserByName) throws ApiException {
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
        ApiResponse<AuthenticationResult> localVarResponse = authenticateWithQuickConnectWithHttpInfo(quickConnectDto);
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
        HttpRequest.Builder localVarRequestBuilder = authenticateWithQuickConnectRequestBuilder(quickConnectDto);
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
                localVarResponse.body().close();

                return new ApiResponse<AuthenticationResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<AuthenticationResult>() {
                                        }));
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
            @org.eclipse.jdt.annotation.Nullable QuickConnectDto quickConnectDto) throws ApiException {
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
        ApiResponse<UserDto> localVarResponse = createUserByNameWithHttpInfo(createUserByName);
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
        HttpRequest.Builder localVarRequestBuilder = createUserByNameRequestBuilder(createUserByName);
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
                localVarResponse.body().close();

                return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserDto>() {
                                }));
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
            @org.eclipse.jdt.annotation.Nullable CreateUserByName createUserByName) throws ApiException {
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
        deleteUserWithHttpInfo(userId);
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
        HttpRequest.Builder localVarRequestBuilder = deleteUserRequestBuilder(userId);
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

    private HttpRequest.Builder deleteUserRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
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
        ApiResponse<ForgotPasswordResult> localVarResponse = forgotPasswordWithHttpInfo(forgotPasswordDto);
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
        HttpRequest.Builder localVarRequestBuilder = forgotPasswordRequestBuilder(forgotPasswordDto);
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
                localVarResponse.body().close();

                return new ApiResponse<ForgotPasswordResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<ForgotPasswordResult>() {
                                        }));
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
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordDto forgotPasswordDto) throws ApiException {
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
        ApiResponse<PinRedeemResult> localVarResponse = forgotPasswordPinWithHttpInfo(forgotPasswordPinDto);
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
        HttpRequest.Builder localVarRequestBuilder = forgotPasswordPinRequestBuilder(forgotPasswordPinDto);
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
                localVarResponse.body().close();

                return new ApiResponse<PinRedeemResult>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<PinRedeemResult>() {
                                }));
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
            @org.eclipse.jdt.annotation.Nullable ForgotPasswordPinDto forgotPasswordPinDto) throws ApiException {
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
        ApiResponse<UserDto> localVarResponse = getCurrentUserWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets the user based on auth token.
     * 
     * @return ApiResponse&lt;UserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<UserDto> getCurrentUserWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getCurrentUserRequestBuilder();
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
                localVarResponse.body().close();

                return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserDto>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getCurrentUserRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/Me";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
        ApiResponse<List<UserDto>> localVarResponse = getPublicUsersWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets a list of publicly visible users for display on a login screen.
     * 
     * @return ApiResponse&lt;List&lt;UserDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<UserDto>> getPublicUsersWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPublicUsersRequestBuilder();
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
                localVarResponse.body().close();

                return new ApiResponse<List<UserDto>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<UserDto>>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getPublicUsersRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/Public";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
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
        ApiResponse<UserDto> localVarResponse = getUserByIdWithHttpInfo(userId);
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
        HttpRequest.Builder localVarRequestBuilder = getUserByIdRequestBuilder(userId);
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
                localVarResponse.body().close();

                return new ApiResponse<UserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<UserDto>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getUserByIdRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
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
        ApiResponse<List<UserDto>> localVarResponse = getUsersWithHttpInfo(isHidden, isDisabled);
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
        HttpRequest.Builder localVarRequestBuilder = getUsersRequestBuilder(isHidden, isDisabled);
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
                localVarResponse.body().close();

                return new ApiResponse<List<UserDto>>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<UserDto>>() {
                                }));
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
            @org.eclipse.jdt.annotation.NonNull Boolean isDisabled) throws ApiException {

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
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Updates a user.
     * 
     * @param userDto The updated user model. (required)
     * @param userId The user id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateUser(@org.eclipse.jdt.annotation.Nullable UserDto userDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateUserWithHttpInfo(userDto, userId);
    }

    /**
     * Updates a user.
     * 
     * @param userDto The updated user model. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UserDto userDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserRequestBuilder(userDto, userId);
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

    private HttpRequest.Builder updateUserRequestBuilder(@org.eclipse.jdt.annotation.Nullable UserDto userDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'userDto' is set
        if (userDto == null) {
            throw new ApiException(400, "Missing the required parameter 'userDto' when calling updateUser");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users";

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
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Updates a user configuration.
     * 
     * @param userConfiguration The new user configuration. (required)
     * @param userId The user id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateUserConfiguration(@org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateUserConfigurationWithHttpInfo(userConfiguration, userId);
    }

    /**
     * Updates a user configuration.
     * 
     * @param userConfiguration The new user configuration. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserConfigurationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserConfigurationRequestBuilder(userConfiguration, userId);
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

    private HttpRequest.Builder updateUserConfigurationRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UserConfiguration userConfiguration,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'userConfiguration' is set
        if (userConfiguration == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'userConfiguration' when calling updateUserConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/Configuration";

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
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Updates a user&#39;s password.
     * 
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @param userId The user id. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateUserPassword(@org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateUserPasswordWithHttpInfo(updateUserPassword, userId);
    }

    /**
     * Updates a user&#39;s password.
     * 
     * @param updateUserPassword The
     *            M:Jellyfin.Api.Controllers.UserController.UpdateUserPassword(System.Nullable{System.Guid},Jellyfin.Api.Models.UserDtos.UpdateUserPassword)
     *            request. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateUserPasswordWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateUserPasswordRequestBuilder(updateUserPassword, userId);
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

    private HttpRequest.Builder updateUserPasswordRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UpdateUserPassword updateUserPassword,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // verify the required parameter 'updateUserPassword' is set
        if (updateUserPassword == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updateUserPassword' when calling updateUserPassword");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Users/Password";

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
        updateUserPolicyWithHttpInfo(userId, userPolicy);
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
        HttpRequest.Builder localVarRequestBuilder = updateUserPolicyRequestBuilder(userId, userPolicy);
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
            @org.eclipse.jdt.annotation.Nullable UserPolicy userPolicy) throws ApiException {
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
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
