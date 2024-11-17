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
import java.util.List;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BufferRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.GroupInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.IgnoreWaitRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.JoinGroupRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MovePlaylistItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.NewGroupRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.NextItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PingRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlayRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PreviousItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QueueRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ReadyRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RemoveFromPlaylistRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SeekRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SetPlaylistItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SetRepeatModeRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SetShuffleModeRequestDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SyncPlayApi {
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
     * Notify SyncPlay group that member is buffering.
     * 
     * @param bufferRequestDto The player status. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayBuffering(@org.eclipse.jdt.annotation.Nullable BufferRequestDto bufferRequestDto)
            throws ApiException {
        syncPlayBufferingWithHttpInfo(bufferRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayBufferingRequestBuilder(bufferRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable BufferRequestDto bufferRequestDto) throws ApiException {
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
        syncPlayCreateGroupWithHttpInfo(newGroupRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayCreateGroupRequestBuilder(newGroupRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable NewGroupRequestDto newGroupRequestDto) throws ApiException {
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
        ApiResponse<List<GroupInfoDto>> localVarResponse = syncPlayGetGroupsWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * @return ApiResponse&lt;List&lt;GroupInfoDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<GroupInfoDto>> syncPlayGetGroupsWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayGetGroupsRequestBuilder();
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
                localVarResponse.body().close();

                return new ApiResponse<List<GroupInfoDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<GroupInfoDto>>() {
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

    private HttpRequest.Builder syncPlayGetGroupsRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/List";

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
     * Join an existing SyncPlay group.
     * 
     * @param joinGroupRequestDto The group to join. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayJoinGroup(@org.eclipse.jdt.annotation.Nullable JoinGroupRequestDto joinGroupRequestDto)
            throws ApiException {
        syncPlayJoinGroupWithHttpInfo(joinGroupRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayJoinGroupRequestBuilder(joinGroupRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable JoinGroupRequestDto joinGroupRequestDto) throws ApiException {
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
        syncPlayLeaveGroupWithHttpInfo();
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayLeaveGroupWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayLeaveGroupRequestBuilder();
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

    private HttpRequest.Builder syncPlayLeaveGroupRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Leave";

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
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * @param movePlaylistItemRequestDto The new position for the item. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayMovePlaylistItem(
            @org.eclipse.jdt.annotation.Nullable MovePlaylistItemRequestDto movePlaylistItemRequestDto)
            throws ApiException {
        syncPlayMovePlaylistItemWithHttpInfo(movePlaylistItemRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayMovePlaylistItemRequestBuilder(movePlaylistItemRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable MovePlaylistItemRequestDto movePlaylistItemRequestDto)
            throws ApiException {
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
        syncPlayNextItemWithHttpInfo(nextItemRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayNextItemRequestBuilder(nextItemRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable NextItemRequestDto nextItemRequestDto) throws ApiException {
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
        syncPlayPauseWithHttpInfo();
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayPauseWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayPauseRequestBuilder();
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

    private HttpRequest.Builder syncPlayPauseRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Pause";

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
     * Update session ping.
     * 
     * @param pingRequestDto The new ping. (required)
     * @throws ApiException if fails to make API call
     */
    public void syncPlayPing(@org.eclipse.jdt.annotation.Nullable PingRequestDto pingRequestDto) throws ApiException {
        syncPlayPingWithHttpInfo(pingRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayPingRequestBuilder(pingRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable PingRequestDto pingRequestDto) throws ApiException {
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
        syncPlayPreviousItemWithHttpInfo(previousItemRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayPreviousItemRequestBuilder(previousItemRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable PreviousItemRequestDto previousItemRequestDto) throws ApiException {
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
        syncPlayQueueWithHttpInfo(queueRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayQueueRequestBuilder(queueRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable QueueRequestDto queueRequestDto) throws ApiException {
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
        syncPlayReadyWithHttpInfo(readyRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayReadyRequestBuilder(readyRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable ReadyRequestDto readyRequestDto) throws ApiException {
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
        syncPlayRemoveFromPlaylistWithHttpInfo(removeFromPlaylistRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlayRemoveFromPlaylistRequestBuilder(
                removeFromPlaylistRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto)
            throws ApiException {
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
        syncPlaySeekWithHttpInfo(seekRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlaySeekRequestBuilder(seekRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable SeekRequestDto seekRequestDto) throws ApiException {
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
        syncPlaySetIgnoreWaitWithHttpInfo(ignoreWaitRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetIgnoreWaitRequestBuilder(ignoreWaitRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable IgnoreWaitRequestDto ignoreWaitRequestDto) throws ApiException {
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
        syncPlaySetNewQueueWithHttpInfo(playRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetNewQueueRequestBuilder(playRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable PlayRequestDto playRequestDto) throws ApiException {
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
        syncPlaySetPlaylistItemWithHttpInfo(setPlaylistItemRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetPlaylistItemRequestBuilder(setPlaylistItemRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable SetPlaylistItemRequestDto setPlaylistItemRequestDto)
            throws ApiException {
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
        syncPlaySetRepeatModeWithHttpInfo(setRepeatModeRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetRepeatModeRequestBuilder(setRepeatModeRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable SetRepeatModeRequestDto setRepeatModeRequestDto) throws ApiException {
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
        syncPlaySetShuffleModeWithHttpInfo(setShuffleModeRequestDto);
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
        HttpRequest.Builder localVarRequestBuilder = syncPlaySetShuffleModeRequestBuilder(setShuffleModeRequestDto);
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
            @org.eclipse.jdt.annotation.Nullable SetShuffleModeRequestDto setShuffleModeRequestDto)
            throws ApiException {
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
        syncPlayStopWithHttpInfo();
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayStopWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayStopRequestBuilder();
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

    private HttpRequest.Builder syncPlayStopRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Stop";

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
     * Request unpause in SyncPlay group.
     * 
     * @throws ApiException if fails to make API call
     */
    public void syncPlayUnpause() throws ApiException {
        syncPlayUnpauseWithHttpInfo();
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> syncPlayUnpauseWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = syncPlayUnpauseRequestBuilder();
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

    private HttpRequest.Builder syncPlayUnpauseRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/SyncPlay/Unpause";

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
}
