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
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.AdminNotificationDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.NameIdPair;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.NotificationResultDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.NotificationTypeInfo;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.NotificationsSummaryDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NotificationsApi {
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

    public NotificationsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public NotificationsApi(ApiClient apiClient) {
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
     * Sends a notification to all admins.
     * 
     * @param adminNotificationDto The notification request. (required)
     * @throws ApiException if fails to make API call
     */
    public void createAdminNotification(@org.eclipse.jdt.annotation.Nullable AdminNotificationDto adminNotificationDto)
            throws ApiException {
        createAdminNotification(adminNotificationDto, null);
    }

    /**
     * Sends a notification to all admins.
     * 
     * @param adminNotificationDto The notification request. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void createAdminNotification(@org.eclipse.jdt.annotation.Nullable AdminNotificationDto adminNotificationDto,
            Map<String, String> headers) throws ApiException {
        createAdminNotificationWithHttpInfo(adminNotificationDto, headers);
    }

    /**
     * Sends a notification to all admins.
     * 
     * @param adminNotificationDto The notification request. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> createAdminNotificationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable AdminNotificationDto adminNotificationDto) throws ApiException {
        return createAdminNotificationWithHttpInfo(adminNotificationDto, null);
    }

    /**
     * Sends a notification to all admins.
     * 
     * @param adminNotificationDto The notification request. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> createAdminNotificationWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable AdminNotificationDto adminNotificationDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createAdminNotificationRequestBuilder(adminNotificationDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createAdminNotification", localVarResponse);
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

    private HttpRequest.Builder createAdminNotificationRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable AdminNotificationDto adminNotificationDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'adminNotificationDto' is set
        if (adminNotificationDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'adminNotificationDto' when calling createAdminNotification");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Notifications/Admin";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(adminNotificationDto);
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
     * Gets notification services.
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NameIdPair> getNotificationServices() throws ApiException {
        return getNotificationServices(null);
    }

    /**
     * Gets notification services.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NameIdPair> getNotificationServices(Map<String, String> headers) throws ApiException {
        ApiResponse<List<NameIdPair>> localVarResponse = getNotificationServicesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets notification services.
     * 
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NameIdPair>> getNotificationServicesWithHttpInfo() throws ApiException {
        return getNotificationServicesWithHttpInfo(null);
    }

    /**
     * Gets notification services.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NameIdPair>> getNotificationServicesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getNotificationServicesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getNotificationServices", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<NameIdPair> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<NameIdPair>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getNotificationServicesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Notifications/Services";

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
     * Gets notification types.
     * 
     * @return List&lt;NotificationTypeInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NotificationTypeInfo> getNotificationTypes() throws ApiException {
        return getNotificationTypes(null);
    }

    /**
     * Gets notification types.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;NotificationTypeInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NotificationTypeInfo> getNotificationTypes(Map<String, String> headers) throws ApiException {
        ApiResponse<List<NotificationTypeInfo>> localVarResponse = getNotificationTypesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets notification types.
     * 
     * @return ApiResponse&lt;List&lt;NotificationTypeInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NotificationTypeInfo>> getNotificationTypesWithHttpInfo() throws ApiException {
        return getNotificationTypesWithHttpInfo(null);
    }

    /**
     * Gets notification types.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;NotificationTypeInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NotificationTypeInfo>> getNotificationTypesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getNotificationTypesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getNotificationTypes", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<NotificationTypeInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<NotificationTypeInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody,
                                new TypeReference<List<NotificationTypeInfo>>() {
                                });

                localVarResponse.body().close();

                return new ApiResponse<List<NotificationTypeInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getNotificationTypesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Notifications/Types";

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
     * Gets a user&#39;s notifications.
     * 
     * @param userId (required)
     * @return NotificationResultDto
     * @throws ApiException if fails to make API call
     */
    public NotificationResultDto getNotifications(@org.eclipse.jdt.annotation.Nullable String userId)
            throws ApiException {
        return getNotifications(userId, null);
    }

    /**
     * Gets a user&#39;s notifications.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @return NotificationResultDto
     * @throws ApiException if fails to make API call
     */
    public NotificationResultDto getNotifications(@org.eclipse.jdt.annotation.Nullable String userId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<NotificationResultDto> localVarResponse = getNotificationsWithHttpInfo(userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a user&#39;s notifications.
     * 
     * @param userId (required)
     * @return ApiResponse&lt;NotificationResultDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<NotificationResultDto> getNotificationsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String userId) throws ApiException {
        return getNotificationsWithHttpInfo(userId, null);
    }

    /**
     * Gets a user&#39;s notifications.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;NotificationResultDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<NotificationResultDto> getNotificationsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getNotificationsRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getNotifications", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<NotificationResultDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                NotificationResultDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<NotificationResultDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<NotificationResultDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getNotificationsRequestBuilder(@org.eclipse.jdt.annotation.Nullable String userId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getNotifications");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Notifications/{userId}".replace("{userId}", ApiClient.urlEncode(userId.toString()));

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
     * Gets a user&#39;s notification summary.
     * 
     * @param userId (required)
     * @return NotificationsSummaryDto
     * @throws ApiException if fails to make API call
     */
    public NotificationsSummaryDto getNotificationsSummary(@org.eclipse.jdt.annotation.Nullable String userId)
            throws ApiException {
        return getNotificationsSummary(userId, null);
    }

    /**
     * Gets a user&#39;s notification summary.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @return NotificationsSummaryDto
     * @throws ApiException if fails to make API call
     */
    public NotificationsSummaryDto getNotificationsSummary(@org.eclipse.jdt.annotation.Nullable String userId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<NotificationsSummaryDto> localVarResponse = getNotificationsSummaryWithHttpInfo(userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a user&#39;s notification summary.
     * 
     * @param userId (required)
     * @return ApiResponse&lt;NotificationsSummaryDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<NotificationsSummaryDto> getNotificationsSummaryWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String userId) throws ApiException {
        return getNotificationsSummaryWithHttpInfo(userId, null);
    }

    /**
     * Gets a user&#39;s notification summary.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;NotificationsSummaryDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<NotificationsSummaryDto> getNotificationsSummaryWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getNotificationsSummaryRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getNotificationsSummary", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<NotificationsSummaryDto>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                NotificationsSummaryDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<NotificationsSummaryDto>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<NotificationsSummaryDto>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getNotificationsSummaryRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getNotificationsSummary");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Notifications/{userId}/Summary".replace("{userId}",
                ApiClient.urlEncode(userId.toString()));

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
     * Sets notifications as read.
     * 
     * @param userId (required)
     * @throws ApiException if fails to make API call
     */
    public void setRead(@org.eclipse.jdt.annotation.Nullable String userId) throws ApiException {
        setRead(userId, null);
    }

    /**
     * Sets notifications as read.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void setRead(@org.eclipse.jdt.annotation.Nullable String userId, Map<String, String> headers)
            throws ApiException {
        setReadWithHttpInfo(userId, headers);
    }

    /**
     * Sets notifications as read.
     * 
     * @param userId (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setReadWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String userId)
            throws ApiException {
        return setReadWithHttpInfo(userId, null);
    }

    /**
     * Sets notifications as read.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setReadWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setReadRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("setRead", localVarResponse);
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

    private HttpRequest.Builder setReadRequestBuilder(@org.eclipse.jdt.annotation.Nullable String userId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling setRead");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Notifications/{userId}/Read".replace("{userId}",
                ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

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
     * Sets notifications as unread.
     * 
     * @param userId (required)
     * @throws ApiException if fails to make API call
     */
    public void setUnread(@org.eclipse.jdt.annotation.Nullable String userId) throws ApiException {
        setUnread(userId, null);
    }

    /**
     * Sets notifications as unread.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void setUnread(@org.eclipse.jdt.annotation.Nullable String userId, Map<String, String> headers)
            throws ApiException {
        setUnreadWithHttpInfo(userId, headers);
    }

    /**
     * Sets notifications as unread.
     * 
     * @param userId (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setUnreadWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String userId)
            throws ApiException {
        return setUnreadWithHttpInfo(userId, null);
    }

    /**
     * Sets notifications as unread.
     * 
     * @param userId (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> setUnreadWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = setUnreadRequestBuilder(userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("setUnread", localVarResponse);
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

    private HttpRequest.Builder setUnreadRequestBuilder(@org.eclipse.jdt.annotation.Nullable String userId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling setUnread");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Notifications/{userId}/Unread".replace("{userId}",
                ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

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
