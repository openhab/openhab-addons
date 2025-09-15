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
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.StartupConfigurationDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.StartupRemoteAccessDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.StartupUserDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StartupApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public StartupApi() {
        this(Configuration.getDefaultApiClient());
    }

    public StartupApi(ApiClient apiClient) {
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
     * Completes the startup wizard.
     * 
     * @throws ApiException if fails to make API call
     */
    public void completeWizard() throws ApiException {
        completeWizardWithHttpInfo();
    }

    /**
     * Completes the startup wizard.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> completeWizardWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = completeWizardRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("completeWizard", localVarResponse);
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

    private HttpRequest.Builder completeWizardRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/Complete";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets the first user.
     * 
     * @return StartupUserDto
     * @throws ApiException if fails to make API call
     */
    public StartupUserDto getFirstUser() throws ApiException {
        ApiResponse<StartupUserDto> localVarResponse = getFirstUserWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets the first user.
     * 
     * @return ApiResponse&lt;StartupUserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupUserDto> getFirstUserWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getFirstUserRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getFirstUser", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<StartupUserDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<StartupUserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<StartupUserDto>() {
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

    private HttpRequest.Builder getFirstUserRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/User";

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
     * Gets the first user.
     * 
     * @return StartupUserDto
     * @throws ApiException if fails to make API call
     */
    public StartupUserDto getFirstUser2() throws ApiException {
        ApiResponse<StartupUserDto> localVarResponse = getFirstUser2WithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets the first user.
     * 
     * @return ApiResponse&lt;StartupUserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupUserDto> getFirstUser2WithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getFirstUser2RequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getFirstUser2", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<StartupUserDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<StartupUserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<StartupUserDto>() {
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

    private HttpRequest.Builder getFirstUser2RequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/FirstUser";

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
     * Gets the initial startup wizard configuration.
     * 
     * @return StartupConfigurationDto
     * @throws ApiException if fails to make API call
     */
    public StartupConfigurationDto getStartupConfiguration() throws ApiException {
        ApiResponse<StartupConfigurationDto> localVarResponse = getStartupConfigurationWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * @return ApiResponse&lt;StartupConfigurationDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupConfigurationDto> getStartupConfigurationWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getStartupConfigurationRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getStartupConfiguration", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<StartupConfigurationDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<StartupConfigurationDto>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<StartupConfigurationDto>() {
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

    private HttpRequest.Builder getStartupConfigurationRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/Configuration";

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
     * Sets remote access and UPnP.
     * 
     * @param startupRemoteAccessDto The startup remote access dto. (required)
     * @throws ApiException if fails to make API call
     */
    public void setRemoteAccess(@org.eclipse.jdt.annotation.Nullable StartupRemoteAccessDto startupRemoteAccessDto)
            throws ApiException {
        setRemoteAccessWithHttpInfo(startupRemoteAccessDto);
    }

    /**
     * Sets remote access and UPnP.
     * 
     * @param startupRemoteAccessDto The startup remote access dto. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setRemoteAccessWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable StartupRemoteAccessDto startupRemoteAccessDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setRemoteAccessRequestBuilder(startupRemoteAccessDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("setRemoteAccess", localVarResponse);
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

    private HttpRequest.Builder setRemoteAccessRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable StartupRemoteAccessDto startupRemoteAccessDto) throws ApiException {
        // verify the required parameter 'startupRemoteAccessDto' is set
        if (startupRemoteAccessDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'startupRemoteAccessDto' when calling setRemoteAccess");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/RemoteAccess";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(startupRemoteAccessDto);
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
     * Sets the initial startup wizard configuration.
     * 
     * @param startupConfigurationDto The updated startup configuration. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateInitialConfiguration(
            @org.eclipse.jdt.annotation.Nullable StartupConfigurationDto startupConfigurationDto) throws ApiException {
        updateInitialConfigurationWithHttpInfo(startupConfigurationDto);
    }

    /**
     * Sets the initial startup wizard configuration.
     * 
     * @param startupConfigurationDto The updated startup configuration. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateInitialConfigurationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable StartupConfigurationDto startupConfigurationDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateInitialConfigurationRequestBuilder(startupConfigurationDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateInitialConfiguration", localVarResponse);
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

    private HttpRequest.Builder updateInitialConfigurationRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable StartupConfigurationDto startupConfigurationDto) throws ApiException {
        // verify the required parameter 'startupConfigurationDto' is set
        if (startupConfigurationDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'startupConfigurationDto' when calling updateInitialConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/Configuration";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(startupConfigurationDto);
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
     * Sets the user name and password.
     * 
     * @param startupUserDto The DTO containing username and password. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateStartupUser(@org.eclipse.jdt.annotation.NonNull StartupUserDto startupUserDto)
            throws ApiException {
        updateStartupUserWithHttpInfo(startupUserDto);
    }

    /**
     * Sets the user name and password.
     * 
     * @param startupUserDto The DTO containing username and password. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateStartupUserWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull StartupUserDto startupUserDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateStartupUserRequestBuilder(startupUserDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updateStartupUser", localVarResponse);
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

    private HttpRequest.Builder updateStartupUserRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull StartupUserDto startupUserDto) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/User";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(startupUserDto);
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
