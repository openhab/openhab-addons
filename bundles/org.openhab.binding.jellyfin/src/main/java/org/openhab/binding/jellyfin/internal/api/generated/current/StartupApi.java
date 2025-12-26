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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.StartupConfigurationDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.StartupRemoteAccessDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.StartupUserDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StartupApi {
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
     * Completes the startup wizard.
     * 
     * @throws ApiException if fails to make API call
     */
    public void completeWizard() throws ApiException {
        completeWizard(null);
    }

    /**
     * Completes the startup wizard.
     * 
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void completeWizard(Map<String, String> headers) throws ApiException {
        completeWizardWithHttpInfo(headers);
    }

    /**
     * Completes the startup wizard.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> completeWizardWithHttpInfo() throws ApiException {
        return completeWizardWithHttpInfo(null);
    }

    /**
     * Completes the startup wizard.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> completeWizardWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = completeWizardRequestBuilder(headers);
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

    private HttpRequest.Builder completeWizardRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/Complete";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "text/html");

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
     * Gets the first user.
     * 
     * @return StartupUserDto
     * @throws ApiException if fails to make API call
     */
    public StartupUserDto getFirstUser() throws ApiException {
        return getFirstUser(null);
    }

    /**
     * Gets the first user.
     * 
     * @param headers Optional headers to include in the request
     * @return StartupUserDto
     * @throws ApiException if fails to make API call
     */
    public StartupUserDto getFirstUser(Map<String, String> headers) throws ApiException {
        ApiResponse<StartupUserDto> localVarResponse = getFirstUserWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the first user.
     * 
     * @return ApiResponse&lt;StartupUserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupUserDto> getFirstUserWithHttpInfo() throws ApiException {
        return getFirstUserWithHttpInfo(null);
    }

    /**
     * Gets the first user.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;StartupUserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupUserDto> getFirstUserWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getFirstUserRequestBuilder(headers);
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
                StartupUserDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<StartupUserDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<StartupUserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getFirstUserRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/User";

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
     * Gets the first user.
     * 
     * @return StartupUserDto
     * @throws ApiException if fails to make API call
     */
    public StartupUserDto getFirstUser2() throws ApiException {
        return getFirstUser2(null);
    }

    /**
     * Gets the first user.
     * 
     * @param headers Optional headers to include in the request
     * @return StartupUserDto
     * @throws ApiException if fails to make API call
     */
    public StartupUserDto getFirstUser2(Map<String, String> headers) throws ApiException {
        ApiResponse<StartupUserDto> localVarResponse = getFirstUser2WithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the first user.
     * 
     * @return ApiResponse&lt;StartupUserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupUserDto> getFirstUser2WithHttpInfo() throws ApiException {
        return getFirstUser2WithHttpInfo(null);
    }

    /**
     * Gets the first user.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;StartupUserDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupUserDto> getFirstUser2WithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getFirstUser2RequestBuilder(headers);
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
                StartupUserDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<StartupUserDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<StartupUserDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getFirstUser2RequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/FirstUser";

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
     * Gets the initial startup wizard configuration.
     * 
     * @return StartupConfigurationDto
     * @throws ApiException if fails to make API call
     */
    public StartupConfigurationDto getStartupConfiguration() throws ApiException {
        return getStartupConfiguration(null);
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * @param headers Optional headers to include in the request
     * @return StartupConfigurationDto
     * @throws ApiException if fails to make API call
     */
    public StartupConfigurationDto getStartupConfiguration(Map<String, String> headers) throws ApiException {
        ApiResponse<StartupConfigurationDto> localVarResponse = getStartupConfigurationWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * @return ApiResponse&lt;StartupConfigurationDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupConfigurationDto> getStartupConfigurationWithHttpInfo() throws ApiException {
        return getStartupConfigurationWithHttpInfo(null);
    }

    /**
     * Gets the initial startup wizard configuration.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;StartupConfigurationDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<StartupConfigurationDto> getStartupConfigurationWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getStartupConfigurationRequestBuilder(headers);
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
                StartupConfigurationDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<StartupConfigurationDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<StartupConfigurationDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getStartupConfigurationRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/Configuration";

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
     * Sets remote access and UPnP.
     * 
     * @param startupRemoteAccessDto The startup remote access dto. (required)
     * @throws ApiException if fails to make API call
     */
    public void setRemoteAccess(@org.eclipse.jdt.annotation.Nullable StartupRemoteAccessDto startupRemoteAccessDto)
            throws ApiException {
        setRemoteAccess(startupRemoteAccessDto, null);
    }

    /**
     * Sets remote access and UPnP.
     * 
     * @param startupRemoteAccessDto The startup remote access dto. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void setRemoteAccess(@org.eclipse.jdt.annotation.Nullable StartupRemoteAccessDto startupRemoteAccessDto,
            Map<String, String> headers) throws ApiException {
        setRemoteAccessWithHttpInfo(startupRemoteAccessDto, headers);
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
        return setRemoteAccessWithHttpInfo(startupRemoteAccessDto, null);
    }

    /**
     * Sets remote access and UPnP.
     * 
     * @param startupRemoteAccessDto The startup remote access dto. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setRemoteAccessWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable StartupRemoteAccessDto startupRemoteAccessDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setRemoteAccessRequestBuilder(startupRemoteAccessDto, headers);
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
            @org.eclipse.jdt.annotation.Nullable StartupRemoteAccessDto startupRemoteAccessDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'startupRemoteAccessDto' is set
        if (startupRemoteAccessDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'startupRemoteAccessDto' when calling setRemoteAccess");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/RemoteAccess";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(startupRemoteAccessDto);
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
     * Sets the initial startup wizard configuration.
     * 
     * @param startupConfigurationDto The updated startup configuration. (required)
     * @throws ApiException if fails to make API call
     */
    public void updateInitialConfiguration(
            @org.eclipse.jdt.annotation.Nullable StartupConfigurationDto startupConfigurationDto) throws ApiException {
        updateInitialConfiguration(startupConfigurationDto, null);
    }

    /**
     * Sets the initial startup wizard configuration.
     * 
     * @param startupConfigurationDto The updated startup configuration. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateInitialConfiguration(
            @org.eclipse.jdt.annotation.Nullable StartupConfigurationDto startupConfigurationDto,
            Map<String, String> headers) throws ApiException {
        updateInitialConfigurationWithHttpInfo(startupConfigurationDto, headers);
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
        return updateInitialConfigurationWithHttpInfo(startupConfigurationDto, null);
    }

    /**
     * Sets the initial startup wizard configuration.
     * 
     * @param startupConfigurationDto The updated startup configuration. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateInitialConfigurationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable StartupConfigurationDto startupConfigurationDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateInitialConfigurationRequestBuilder(startupConfigurationDto,
                headers);
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
            @org.eclipse.jdt.annotation.Nullable StartupConfigurationDto startupConfigurationDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'startupConfigurationDto' is set
        if (startupConfigurationDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'startupConfigurationDto' when calling updateInitialConfiguration");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/Configuration";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(startupConfigurationDto);
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
     * Sets the user name and password.
     * 
     * @param startupUserDto The DTO containing username and password. (optional)
     * @throws ApiException if fails to make API call
     */
    public void updateStartupUser(@org.eclipse.jdt.annotation.NonNull StartupUserDto startupUserDto)
            throws ApiException {
        updateStartupUser(startupUserDto, null);
    }

    /**
     * Sets the user name and password.
     * 
     * @param startupUserDto The DTO containing username and password. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updateStartupUser(@org.eclipse.jdt.annotation.NonNull StartupUserDto startupUserDto,
            Map<String, String> headers) throws ApiException {
        updateStartupUserWithHttpInfo(startupUserDto, headers);
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
        return updateStartupUserWithHttpInfo(startupUserDto, null);
    }

    /**
     * Sets the user name and password.
     * 
     * @param startupUserDto The DTO containing username and password. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updateStartupUserWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull StartupUserDto startupUserDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updateStartupUserRequestBuilder(startupUserDto, headers);
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
            @org.eclipse.jdt.annotation.NonNull StartupUserDto startupUserDto, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Startup/User";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(startupUserDto);
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
