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
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.BufferRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.GroupInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.IgnoreWaitRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.JoinGroupRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.MovePlaylistItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.NewGroupRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.NextItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.PingRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.PlayRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.PreviousItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.QueueRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.ReadyRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.RemoveFromPlaylistRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.SeekRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.SetPlaylistItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.SetRepeatModeRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.legacy.model.SetShuffleModeRequestDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SyncPlayApi {
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

    public SyncPlayApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SyncPlayApi(ApiClient apiClient) {
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
     * Notify SyncPlay group that member is buffering.
     * 
     * @param bufferRequestDto The player status. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayBuffering(@org.eclipse.jdt.annotation.Nullable BufferRequestDto bufferRequestDto)
            throws ApiException {
        syncPlayBuffering(bufferRequestDto, null);
    }

    /**
     * Notify SyncPlay group that member is buffering.
     * 
     * @param bufferRequestDto The player status. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayBuffering(@org.eclipse.jdt.annotation.Nullable BufferRequestDto bufferRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayBufferingWithHttpInfo(bufferRequestDto, headers);
    }

    /**
     * Notify SyncPlay group that member is buffering.
     * 
     * @param bufferRequestDto The player status. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayBufferingWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable BufferRequestDto bufferRequestDto) throws ApiException {
        return syncPlayBufferingWithHttpInfo(bufferRequestDto, null);
    }

    /**
     * Notify SyncPlay group that member is buffering.
     * 
     * @param bufferRequestDto The player status. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayBufferingWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable BufferRequestDto bufferRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayBufferingRequestBuilder(bufferRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayBuffering", localVarResponse);
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

    private HttpRequest.Builder syncPlayBufferingRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable BufferRequestDto bufferRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'bufferRequestDto' is set
        if (bufferRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'bufferRequestDto' when calling syncPlayBuffering");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Buffering";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(bufferRequestDto);
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
     * Create a new SyncPlay group.
     * 
     * @param newGroupRequestDto The settings of the new group. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayCreateGroup(@org.eclipse.jdt.annotation.Nullable NewGroupRequestDto newGroupRequestDto)
            throws ApiException {
        syncPlayCreateGroup(newGroupRequestDto, null);
    }

    /**
     * Create a new SyncPlay group.
     * 
     * @param newGroupRequestDto The settings of the new group. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayCreateGroup(@org.eclipse.jdt.annotation.Nullable NewGroupRequestDto newGroupRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayCreateGroupWithHttpInfo(newGroupRequestDto, headers);
    }

    /**
     * Create a new SyncPlay group.
     * 
     * @param newGroupRequestDto The settings of the new group. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayCreateGroupWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable NewGroupRequestDto newGroupRequestDto) throws ApiException {
        return syncPlayCreateGroupWithHttpInfo(newGroupRequestDto, null);
    }

    /**
     * Create a new SyncPlay group.
     * 
     * @param newGroupRequestDto The settings of the new group. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayCreateGroupWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable NewGroupRequestDto newGroupRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayCreateGroupRequestBuilder(newGroupRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayCreateGroup", localVarResponse);
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

    private HttpRequest.Builder syncPlayCreateGroupRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable NewGroupRequestDto newGroupRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'newGroupRequestDto' is set
        if (newGroupRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'newGroupRequestDto' when calling syncPlayCreateGroup");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/New";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(newGroupRequestDto);
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
     * Gets all SyncPlay groups.
     * 
     * @return List&lt;GroupInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<GroupInfoDto> syncPlayGetGroups() throws ApiException {
        return syncPlayGetGroups(null);
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;GroupInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<GroupInfoDto> syncPlayGetGroups(Map<String, String> headers) throws ApiException {
        ApiResponse<List<GroupInfoDto>> localVarResponse = syncPlayGetGroupsWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * @return ApiResponse&lt;List&lt;GroupInfoDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<GroupInfoDto>> syncPlayGetGroupsWithHttpInfo() throws ApiException {
        return syncPlayGetGroupsWithHttpInfo(null);
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;GroupInfoDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<GroupInfoDto>> syncPlayGetGroupsWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayGetGroupsRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayGetGroups", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<GroupInfoDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<GroupInfoDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<GroupInfoDto>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<GroupInfoDto>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder syncPlayGetGroupsRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/List";

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
     * Join an existing SyncPlay group.
     * 
     * @param joinGroupRequestDto The group to join. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayJoinGroup(@org.eclipse.jdt.annotation.Nullable JoinGroupRequestDto joinGroupRequestDto)
            throws ApiException {
        syncPlayJoinGroup(joinGroupRequestDto, null);
    }

    /**
     * Join an existing SyncPlay group.
     * 
     * @param joinGroupRequestDto The group to join. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayJoinGroup(@org.eclipse.jdt.annotation.Nullable JoinGroupRequestDto joinGroupRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayJoinGroupWithHttpInfo(joinGroupRequestDto, headers);
    }

    /**
     * Join an existing SyncPlay group.
     * 
     * @param joinGroupRequestDto The group to join. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayJoinGroupWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable JoinGroupRequestDto joinGroupRequestDto) throws ApiException {
        return syncPlayJoinGroupWithHttpInfo(joinGroupRequestDto, null);
    }

    /**
     * Join an existing SyncPlay group.
     * 
     * @param joinGroupRequestDto The group to join. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayJoinGroupWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable JoinGroupRequestDto joinGroupRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayJoinGroupRequestBuilder(joinGroupRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayJoinGroup", localVarResponse);
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

    private HttpRequest.Builder syncPlayJoinGroupRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable JoinGroupRequestDto joinGroupRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'joinGroupRequestDto' is set
        if (joinGroupRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'joinGroupRequestDto' when calling syncPlayJoinGroup");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Join";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(joinGroupRequestDto);
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
     * Leave the joined SyncPlay group.
     * 
     * @throws ApiException if fails to make API call
     */
    public void syncPlayLeaveGroup() throws ApiException {
        syncPlayLeaveGroup(null);
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayLeaveGroup(Map<String, String> headers) throws ApiException {
        syncPlayLeaveGroupWithHttpInfo(headers);
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayLeaveGroupWithHttpInfo() throws ApiException {
        return syncPlayLeaveGroupWithHttpInfo(null);
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayLeaveGroupWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayLeaveGroupRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayLeaveGroup", localVarResponse);
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

    private HttpRequest.Builder syncPlayLeaveGroupRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Leave";

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
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * @param movePlaylistItemRequestDto The new position for the item. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayMovePlaylistItem(
            @org.eclipse.jdt.annotation.Nullable MovePlaylistItemRequestDto movePlaylistItemRequestDto)
            throws ApiException {
        syncPlayMovePlaylistItem(movePlaylistItemRequestDto, null);
    }

    /**
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * @param movePlaylistItemRequestDto The new position for the item. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayMovePlaylistItem(
            @org.eclipse.jdt.annotation.Nullable MovePlaylistItemRequestDto movePlaylistItemRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayMovePlaylistItemWithHttpInfo(movePlaylistItemRequestDto, headers);
    }

    /**
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * @param movePlaylistItemRequestDto The new position for the item. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayMovePlaylistItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MovePlaylistItemRequestDto movePlaylistItemRequestDto)
            throws ApiException {
        return syncPlayMovePlaylistItemWithHttpInfo(movePlaylistItemRequestDto, null);
    }

    /**
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * @param movePlaylistItemRequestDto The new position for the item. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayMovePlaylistItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MovePlaylistItemRequestDto movePlaylistItemRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayMovePlaylistItemRequestBuilder(movePlaylistItemRequestDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayMovePlaylistItem", localVarResponse);
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

    private HttpRequest.Builder syncPlayMovePlaylistItemRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable MovePlaylistItemRequestDto movePlaylistItemRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'movePlaylistItemRequestDto' is set
        if (movePlaylistItemRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'movePlaylistItemRequestDto' when calling syncPlayMovePlaylistItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/MovePlaylistItem";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(movePlaylistItemRequestDto);
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
     * Request next item in SyncPlay group.
     * 
     * @param nextItemRequestDto The current item information. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayNextItem(@org.eclipse.jdt.annotation.Nullable NextItemRequestDto nextItemRequestDto)
            throws ApiException {
        syncPlayNextItem(nextItemRequestDto, null);
    }

    /**
     * Request next item in SyncPlay group.
     * 
     * @param nextItemRequestDto The current item information. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayNextItem(@org.eclipse.jdt.annotation.Nullable NextItemRequestDto nextItemRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayNextItemWithHttpInfo(nextItemRequestDto, headers);
    }

    /**
     * Request next item in SyncPlay group.
     * 
     * @param nextItemRequestDto The current item information. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayNextItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable NextItemRequestDto nextItemRequestDto) throws ApiException {
        return syncPlayNextItemWithHttpInfo(nextItemRequestDto, null);
    }

    /**
     * Request next item in SyncPlay group.
     * 
     * @param nextItemRequestDto The current item information. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayNextItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable NextItemRequestDto nextItemRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayNextItemRequestBuilder(nextItemRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayNextItem", localVarResponse);
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

    private HttpRequest.Builder syncPlayNextItemRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable NextItemRequestDto nextItemRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'nextItemRequestDto' is set
        if (nextItemRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'nextItemRequestDto' when calling syncPlayNextItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/NextItem";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(nextItemRequestDto);
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
     * Request pause in SyncPlay group.
     * 
     * @throws ApiException if fails to make API call
     */
    public void syncPlayPause() throws ApiException {
        syncPlayPause(null);
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayPause(Map<String, String> headers) throws ApiException {
        syncPlayPauseWithHttpInfo(headers);
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayPauseWithHttpInfo() throws ApiException {
        return syncPlayPauseWithHttpInfo(null);
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayPauseWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayPauseRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayPause", localVarResponse);
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

    private HttpRequest.Builder syncPlayPauseRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Pause";

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
     * Update session ping.
     * 
     * @param pingRequestDto The new ping. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayPing(@org.eclipse.jdt.annotation.Nullable PingRequestDto pingRequestDto) throws ApiException {
        syncPlayPing(pingRequestDto, null);
    }

    /**
     * Update session ping.
     * 
     * @param pingRequestDto The new ping. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayPing(@org.eclipse.jdt.annotation.Nullable PingRequestDto pingRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayPingWithHttpInfo(pingRequestDto, headers);
    }

    /**
     * Update session ping.
     * 
     * @param pingRequestDto The new ping. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayPingWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable PingRequestDto pingRequestDto) throws ApiException {
        return syncPlayPingWithHttpInfo(pingRequestDto, null);
    }

    /**
     * Update session ping.
     * 
     * @param pingRequestDto The new ping. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayPingWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable PingRequestDto pingRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayPingRequestBuilder(pingRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayPing", localVarResponse);
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

    private HttpRequest.Builder syncPlayPingRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable PingRequestDto pingRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'pingRequestDto' is set
        if (pingRequestDto == null) {
            throw new ApiException(400, "Missing the required parameter 'pingRequestDto' when calling syncPlayPing");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Ping";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(pingRequestDto);
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
     * Request previous item in SyncPlay group.
     * 
     * @param previousItemRequestDto The current item information. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayPreviousItem(@org.eclipse.jdt.annotation.Nullable PreviousItemRequestDto previousItemRequestDto)
            throws ApiException {
        syncPlayPreviousItem(previousItemRequestDto, null);
    }

    /**
     * Request previous item in SyncPlay group.
     * 
     * @param previousItemRequestDto The current item information. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayPreviousItem(@org.eclipse.jdt.annotation.Nullable PreviousItemRequestDto previousItemRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayPreviousItemWithHttpInfo(previousItemRequestDto, headers);
    }

    /**
     * Request previous item in SyncPlay group.
     * 
     * @param previousItemRequestDto The current item information. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayPreviousItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable PreviousItemRequestDto previousItemRequestDto) throws ApiException {
        return syncPlayPreviousItemWithHttpInfo(previousItemRequestDto, null);
    }

    /**
     * Request previous item in SyncPlay group.
     * 
     * @param previousItemRequestDto The current item information. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayPreviousItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable PreviousItemRequestDto previousItemRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayPreviousItemRequestBuilder(previousItemRequestDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayPreviousItem", localVarResponse);
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

    private HttpRequest.Builder syncPlayPreviousItemRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable PreviousItemRequestDto previousItemRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'previousItemRequestDto' is set
        if (previousItemRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'previousItemRequestDto' when calling syncPlayPreviousItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/PreviousItem";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(previousItemRequestDto);
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
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * @param queueRequestDto The items to add. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayQueue(@org.eclipse.jdt.annotation.Nullable QueueRequestDto queueRequestDto)
            throws ApiException {
        syncPlayQueue(queueRequestDto, null);
    }

    /**
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * @param queueRequestDto The items to add. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayQueue(@org.eclipse.jdt.annotation.Nullable QueueRequestDto queueRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayQueueWithHttpInfo(queueRequestDto, headers);
    }

    /**
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * @param queueRequestDto The items to add. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayQueueWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable QueueRequestDto queueRequestDto) throws ApiException {
        return syncPlayQueueWithHttpInfo(queueRequestDto, null);
    }

    /**
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * @param queueRequestDto The items to add. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayQueueWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable QueueRequestDto queueRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayQueueRequestBuilder(queueRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayQueue", localVarResponse);
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

    private HttpRequest.Builder syncPlayQueueRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable QueueRequestDto queueRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'queueRequestDto' is set
        if (queueRequestDto == null) {
            throw new ApiException(400, "Missing the required parameter 'queueRequestDto' when calling syncPlayQueue");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Queue";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(queueRequestDto);
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
     * Notify SyncPlay group that member is ready for playback.
     * 
     * @param readyRequestDto The player status. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayReady(@org.eclipse.jdt.annotation.Nullable ReadyRequestDto readyRequestDto)
            throws ApiException {
        syncPlayReady(readyRequestDto, null);
    }

    /**
     * Notify SyncPlay group that member is ready for playback.
     * 
     * @param readyRequestDto The player status. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayReady(@org.eclipse.jdt.annotation.Nullable ReadyRequestDto readyRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayReadyWithHttpInfo(readyRequestDto, headers);
    }

    /**
     * Notify SyncPlay group that member is ready for playback.
     * 
     * @param readyRequestDto The player status. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayReadyWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ReadyRequestDto readyRequestDto) throws ApiException {
        return syncPlayReadyWithHttpInfo(readyRequestDto, null);
    }

    /**
     * Notify SyncPlay group that member is ready for playback.
     * 
     * @param readyRequestDto The player status. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayReadyWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ReadyRequestDto readyRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayReadyRequestBuilder(readyRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayReady", localVarResponse);
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

    private HttpRequest.Builder syncPlayReadyRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable ReadyRequestDto readyRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'readyRequestDto' is set
        if (readyRequestDto == null) {
            throw new ApiException(400, "Missing the required parameter 'readyRequestDto' when calling syncPlayReady");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Ready";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(readyRequestDto);
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
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * @param removeFromPlaylistRequestDto The items to remove. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayRemoveFromPlaylist(
            @org.eclipse.jdt.annotation.Nullable RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto)
            throws ApiException {
        syncPlayRemoveFromPlaylist(removeFromPlaylistRequestDto, null);
    }

    /**
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * @param removeFromPlaylistRequestDto The items to remove. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayRemoveFromPlaylist(
            @org.eclipse.jdt.annotation.Nullable RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlayRemoveFromPlaylistWithHttpInfo(removeFromPlaylistRequestDto, headers);
    }

    /**
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * @param removeFromPlaylistRequestDto The items to remove. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayRemoveFromPlaylistWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto)
            throws ApiException {
        return syncPlayRemoveFromPlaylistWithHttpInfo(removeFromPlaylistRequestDto, null);
    }

    /**
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * @param removeFromPlaylistRequestDto The items to remove. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayRemoveFromPlaylistWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayRemoveFromPlaylistRequestBuilder(
                removeFromPlaylistRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayRemoveFromPlaylist", localVarResponse);
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

    private HttpRequest.Builder syncPlayRemoveFromPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'removeFromPlaylistRequestDto' is set
        if (removeFromPlaylistRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'removeFromPlaylistRequestDto' when calling syncPlayRemoveFromPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/RemoveFromPlaylist";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(removeFromPlaylistRequestDto);
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
     * Request seek in SyncPlay group.
     * 
     * @param seekRequestDto The new playback position. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySeek(@org.eclipse.jdt.annotation.Nullable SeekRequestDto seekRequestDto) throws ApiException {
        syncPlaySeek(seekRequestDto, null);
    }

    /**
     * Request seek in SyncPlay group.
     * 
     * @param seekRequestDto The new playback position. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySeek(@org.eclipse.jdt.annotation.Nullable SeekRequestDto seekRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlaySeekWithHttpInfo(seekRequestDto, headers);
    }

    /**
     * Request seek in SyncPlay group.
     * 
     * @param seekRequestDto The new playback position. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySeekWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SeekRequestDto seekRequestDto) throws ApiException {
        return syncPlaySeekWithHttpInfo(seekRequestDto, null);
    }

    /**
     * Request seek in SyncPlay group.
     * 
     * @param seekRequestDto The new playback position. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySeekWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SeekRequestDto seekRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlaySeekRequestBuilder(seekRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlaySeek", localVarResponse);
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

    private HttpRequest.Builder syncPlaySeekRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable SeekRequestDto seekRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'seekRequestDto' is set
        if (seekRequestDto == null) {
            throw new ApiException(400, "Missing the required parameter 'seekRequestDto' when calling syncPlaySeek");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Seek";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(seekRequestDto);
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
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * @param ignoreWaitRequestDto The settings to set. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetIgnoreWait(@org.eclipse.jdt.annotation.Nullable IgnoreWaitRequestDto ignoreWaitRequestDto)
            throws ApiException {
        syncPlaySetIgnoreWait(ignoreWaitRequestDto, null);
    }

    /**
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * @param ignoreWaitRequestDto The settings to set. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetIgnoreWait(@org.eclipse.jdt.annotation.Nullable IgnoreWaitRequestDto ignoreWaitRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlaySetIgnoreWaitWithHttpInfo(ignoreWaitRequestDto, headers);
    }

    /**
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * @param ignoreWaitRequestDto The settings to set. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetIgnoreWaitWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable IgnoreWaitRequestDto ignoreWaitRequestDto) throws ApiException {
        return syncPlaySetIgnoreWaitWithHttpInfo(ignoreWaitRequestDto, null);
    }

    /**
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * @param ignoreWaitRequestDto The settings to set. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetIgnoreWaitWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable IgnoreWaitRequestDto ignoreWaitRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetIgnoreWaitRequestBuilder(ignoreWaitRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlaySetIgnoreWait", localVarResponse);
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

    private HttpRequest.Builder syncPlaySetIgnoreWaitRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable IgnoreWaitRequestDto ignoreWaitRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'ignoreWaitRequestDto' is set
        if (ignoreWaitRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'ignoreWaitRequestDto' when calling syncPlaySetIgnoreWait");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/SetIgnoreWait";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(ignoreWaitRequestDto);
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
     * Request to set new playlist in SyncPlay group.
     * 
     * @param playRequestDto The new playlist to play in the group. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetNewQueue(@org.eclipse.jdt.annotation.Nullable PlayRequestDto playRequestDto)
            throws ApiException {
        syncPlaySetNewQueue(playRequestDto, null);
    }

    /**
     * Request to set new playlist in SyncPlay group.
     * 
     * @param playRequestDto The new playlist to play in the group. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetNewQueue(@org.eclipse.jdt.annotation.Nullable PlayRequestDto playRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlaySetNewQueueWithHttpInfo(playRequestDto, headers);
    }

    /**
     * Request to set new playlist in SyncPlay group.
     * 
     * @param playRequestDto The new playlist to play in the group. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetNewQueueWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable PlayRequestDto playRequestDto) throws ApiException {
        return syncPlaySetNewQueueWithHttpInfo(playRequestDto, null);
    }

    /**
     * Request to set new playlist in SyncPlay group.
     * 
     * @param playRequestDto The new playlist to play in the group. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetNewQueueWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable PlayRequestDto playRequestDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetNewQueueRequestBuilder(playRequestDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlaySetNewQueue", localVarResponse);
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

    private HttpRequest.Builder syncPlaySetNewQueueRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable PlayRequestDto playRequestDto, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'playRequestDto' is set
        if (playRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playRequestDto' when calling syncPlaySetNewQueue");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/SetNewQueue";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(playRequestDto);
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
     * Request to change playlist item in SyncPlay group.
     * 
     * @param setPlaylistItemRequestDto The new item to play. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetPlaylistItem(
            @org.eclipse.jdt.annotation.Nullable SetPlaylistItemRequestDto setPlaylistItemRequestDto)
            throws ApiException {
        syncPlaySetPlaylistItem(setPlaylistItemRequestDto, null);
    }

    /**
     * Request to change playlist item in SyncPlay group.
     * 
     * @param setPlaylistItemRequestDto The new item to play. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetPlaylistItem(
            @org.eclipse.jdt.annotation.Nullable SetPlaylistItemRequestDto setPlaylistItemRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlaySetPlaylistItemWithHttpInfo(setPlaylistItemRequestDto, headers);
    }

    /**
     * Request to change playlist item in SyncPlay group.
     * 
     * @param setPlaylistItemRequestDto The new item to play. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetPlaylistItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetPlaylistItemRequestDto setPlaylistItemRequestDto)
            throws ApiException {
        return syncPlaySetPlaylistItemWithHttpInfo(setPlaylistItemRequestDto, null);
    }

    /**
     * Request to change playlist item in SyncPlay group.
     * 
     * @param setPlaylistItemRequestDto The new item to play. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetPlaylistItemWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetPlaylistItemRequestDto setPlaylistItemRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetPlaylistItemRequestBuilder(setPlaylistItemRequestDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlaySetPlaylistItem", localVarResponse);
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

    private HttpRequest.Builder syncPlaySetPlaylistItemRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable SetPlaylistItemRequestDto setPlaylistItemRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'setPlaylistItemRequestDto' is set
        if (setPlaylistItemRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'setPlaylistItemRequestDto' when calling syncPlaySetPlaylistItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/SetPlaylistItem";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(setPlaylistItemRequestDto);
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
     * Request to set repeat mode in SyncPlay group.
     * 
     * @param setRepeatModeRequestDto The new repeat mode. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetRepeatMode(
            @org.eclipse.jdt.annotation.Nullable SetRepeatModeRequestDto setRepeatModeRequestDto) throws ApiException {
        syncPlaySetRepeatMode(setRepeatModeRequestDto, null);
    }

    /**
     * Request to set repeat mode in SyncPlay group.
     * 
     * @param setRepeatModeRequestDto The new repeat mode. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetRepeatMode(
            @org.eclipse.jdt.annotation.Nullable SetRepeatModeRequestDto setRepeatModeRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlaySetRepeatModeWithHttpInfo(setRepeatModeRequestDto, headers);
    }

    /**
     * Request to set repeat mode in SyncPlay group.
     * 
     * @param setRepeatModeRequestDto The new repeat mode. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetRepeatModeWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetRepeatModeRequestDto setRepeatModeRequestDto) throws ApiException {
        return syncPlaySetRepeatModeWithHttpInfo(setRepeatModeRequestDto, null);
    }

    /**
     * Request to set repeat mode in SyncPlay group.
     * 
     * @param setRepeatModeRequestDto The new repeat mode. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetRepeatModeWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetRepeatModeRequestDto setRepeatModeRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetRepeatModeRequestBuilder(setRepeatModeRequestDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlaySetRepeatMode", localVarResponse);
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

    private HttpRequest.Builder syncPlaySetRepeatModeRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable SetRepeatModeRequestDto setRepeatModeRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'setRepeatModeRequestDto' is set
        if (setRepeatModeRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'setRepeatModeRequestDto' when calling syncPlaySetRepeatMode");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/SetRepeatMode";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(setRepeatModeRequestDto);
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
     * Request to set shuffle mode in SyncPlay group.
     * 
     * @param setShuffleModeRequestDto The new shuffle mode. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetShuffleMode(
            @org.eclipse.jdt.annotation.Nullable SetShuffleModeRequestDto setShuffleModeRequestDto)
            throws ApiException {
        syncPlaySetShuffleMode(setShuffleModeRequestDto, null);
    }

    /**
     * Request to set shuffle mode in SyncPlay group.
     * 
     * @param setShuffleModeRequestDto The new shuffle mode. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlaySetShuffleMode(
            @org.eclipse.jdt.annotation.Nullable SetShuffleModeRequestDto setShuffleModeRequestDto,
            Map<String, String> headers) throws ApiException {
        syncPlaySetShuffleModeWithHttpInfo(setShuffleModeRequestDto, headers);
    }

    /**
     * Request to set shuffle mode in SyncPlay group.
     * 
     * @param setShuffleModeRequestDto The new shuffle mode. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetShuffleModeWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetShuffleModeRequestDto setShuffleModeRequestDto)
            throws ApiException {
        return syncPlaySetShuffleModeWithHttpInfo(setShuffleModeRequestDto, null);
    }

    /**
     * Request to set shuffle mode in SyncPlay group.
     * 
     * @param setShuffleModeRequestDto The new shuffle mode. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlaySetShuffleModeWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SetShuffleModeRequestDto setShuffleModeRequestDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetShuffleModeRequestBuilder(setShuffleModeRequestDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlaySetShuffleMode", localVarResponse);
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

    private HttpRequest.Builder syncPlaySetShuffleModeRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable SetShuffleModeRequestDto setShuffleModeRequestDto,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'setShuffleModeRequestDto' is set
        if (setShuffleModeRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'setShuffleModeRequestDto' when calling syncPlaySetShuffleMode");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/SetShuffleMode";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(setShuffleModeRequestDto);
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
     * Request stop in SyncPlay group.
     * 
     * @throws ApiException if fails to make API call
     */
    public void syncPlayStop() throws ApiException {
        syncPlayStop(null);
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayStop(Map<String, String> headers) throws ApiException {
        syncPlayStopWithHttpInfo(headers);
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayStopWithHttpInfo() throws ApiException {
        return syncPlayStopWithHttpInfo(null);
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayStopWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayStopRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayStop", localVarResponse);
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

    private HttpRequest.Builder syncPlayStopRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Stop";

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
     * Request unpause in SyncPlay group.
     * 
     * @throws ApiException if fails to make API call
     */
    public void syncPlayUnpause() throws ApiException {
        syncPlayUnpause(null);
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void syncPlayUnpause(Map<String, String> headers) throws ApiException {
        syncPlayUnpauseWithHttpInfo(headers);
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayUnpauseWithHttpInfo() throws ApiException {
        return syncPlayUnpauseWithHttpInfo(null);
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayUnpauseWithHttpInfo(Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayUnpauseRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("syncPlayUnpause", localVarResponse);
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

    private HttpRequest.Builder syncPlayUnpauseRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Unpause";

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
