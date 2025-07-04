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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CreatePlaylistDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaylistCreationResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaylistDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaylistUserPermissions;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdatePlaylistDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdatePlaylistUserDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaylistsApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public PlaylistsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PlaylistsApi(ApiClient apiClient) {
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
     * Adds items to a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param ids Item id, comma delimited. (optional)
     * @param userId The userId. (optional)
     * @throws ApiException if fails to make API call
     */
    public void addItemToPlaylist(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> ids, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        addItemToPlaylistWithHttpInfo(playlistId, ids, userId);
    }

    /**
     * Adds items to a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param ids Item id, comma delimited. (optional)
     * @param userId The userId. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addItemToPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> ids, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addItemToPlaylistRequestBuilder(playlistId, ids, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addItemToPlaylist", localVarResponse);
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

    private HttpRequest.Builder addItemToPlaylistRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> ids, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling addItemToPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Items".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "ids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "ids", ids));
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Creates a new playlist.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * 
     * @param name The playlist name. (optional)
     * @param ids The item ids. (optional)
     * @param userId The user id. (optional)
     * @param mediaType The media type. (optional)
     * @param createPlaylistDto The create playlist payload. (optional)
     * @return PlaylistCreationResult
     * @throws ApiException if fails to make API call
     */
    public PlaylistCreationResult createPlaylist(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull List<UUID> ids, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull MediaType mediaType,
            @org.eclipse.jdt.annotation.NonNull CreatePlaylistDto createPlaylistDto) throws ApiException {
        ApiResponse<PlaylistCreationResult> localVarResponse = createPlaylistWithHttpInfo(name, ids, userId, mediaType,
                createPlaylistDto);
        return localVarResponse.getData();
    }

    /**
     * Creates a new playlist.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * 
     * @param name The playlist name. (optional)
     * @param ids The item ids. (optional)
     * @param userId The user id. (optional)
     * @param mediaType The media type. (optional)
     * @param createPlaylistDto The create playlist payload. (optional)
     * @return ApiResponse&lt;PlaylistCreationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaylistCreationResult> createPlaylistWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String name, @org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull MediaType mediaType,
            @org.eclipse.jdt.annotation.NonNull CreatePlaylistDto createPlaylistDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createPlaylistRequestBuilder(name, ids, userId, mediaType,
                createPlaylistDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<PlaylistCreationResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<PlaylistCreationResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<PlaylistCreationResult>() {
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

    private HttpRequest.Builder createPlaylistRequestBuilder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull List<UUID> ids, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull MediaType mediaType,
            @org.eclipse.jdt.annotation.NonNull CreatePlaylistDto createPlaylistDto) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "name";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("name", name));
        localVarQueryParameterBaseName = "ids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "ids", ids));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "mediaType";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaType", mediaType));

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
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(createPlaylistDto);
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
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @return PlaylistDto
     * @throws ApiException if fails to make API call
     */
    public PlaylistDto getPlaylist(@org.eclipse.jdt.annotation.Nullable UUID playlistId) throws ApiException {
        ApiResponse<PlaylistDto> localVarResponse = getPlaylistWithHttpInfo(playlistId);
        return localVarResponse.getData();
    }

    /**
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @return ApiResponse&lt;PlaylistDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaylistDto> getPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistRequestBuilder(playlistId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<PlaylistDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<PlaylistDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<PlaylistDto>() {
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

    private HttpRequest.Builder getPlaylistRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID playlistId)
            throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

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
     * Gets the original items of a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId User id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getPlaylistItems(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getPlaylistItemsWithHttpInfo(playlistId, userId,
                startIndex, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
        return localVarResponse.getData();
    }

    /**
     * Gets the original items of a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId User id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getPlaylistItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID playlistId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistItemsRequestBuilder(playlistId, userId, startIndex,
                limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylistItems", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<BaseItemDtoQueryResult>() {
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

    private HttpRequest.Builder getPlaylistItemsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes) throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylistItems");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Items".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));

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
     * Get a playlist user.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @return PlaylistUserPermissions
     * @throws ApiException if fails to make API call
     */
    public PlaylistUserPermissions getPlaylistUser(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        ApiResponse<PlaylistUserPermissions> localVarResponse = getPlaylistUserWithHttpInfo(playlistId, userId);
        return localVarResponse.getData();
    }

    /**
     * Get a playlist user.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @return ApiResponse&lt;PlaylistUserPermissions&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaylistUserPermissions> getPlaylistUserWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID playlistId, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistUserRequestBuilder(playlistId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylistUser", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<PlaylistUserPermissions>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<PlaylistUserPermissions>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<PlaylistUserPermissions>() {
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

    private HttpRequest.Builder getPlaylistUserRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylistUser");
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getPlaylistUser");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Users/{userId}"
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()))
                .replace("{userId}", ApiClient.urlEncode(userId.toString()));

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
     * Get a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @return List&lt;PlaylistUserPermissions&gt;
     * @throws ApiException if fails to make API call
     */
    public List<PlaylistUserPermissions> getPlaylistUsers(@org.eclipse.jdt.annotation.Nullable UUID playlistId)
            throws ApiException {
        ApiResponse<List<PlaylistUserPermissions>> localVarResponse = getPlaylistUsersWithHttpInfo(playlistId);
        return localVarResponse.getData();
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @return ApiResponse&lt;List&lt;PlaylistUserPermissions&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<PlaylistUserPermissions>> getPlaylistUsersWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID playlistId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistUsersRequestBuilder(playlistId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylistUsers", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<PlaylistUserPermissions>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<PlaylistUserPermissions>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<PlaylistUserPermissions>>() {
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

    private HttpRequest.Builder getPlaylistUsersRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID playlistId)
            throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylistUsers");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Users".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

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
     * Moves a playlist item.
     * 
     * @param playlistId The playlist id. (required)
     * @param itemId The item id. (required)
     * @param newIndex The new index. (required)
     * @throws ApiException if fails to make API call
     */
    public void moveItem(@org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable Integer newIndex)
            throws ApiException {
        moveItemWithHttpInfo(playlistId, itemId, newIndex);
    }

    /**
     * Moves a playlist item.
     * 
     * @param playlistId The playlist id. (required)
     * @param itemId The item id. (required)
     * @param newIndex The new index. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> moveItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable Integer newIndex)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = moveItemRequestBuilder(playlistId, itemId, newIndex);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("moveItem", localVarResponse);
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

    private HttpRequest.Builder moveItemRequestBuilder(@org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable Integer newIndex)
            throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling moveItem");
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling moveItem");
        }
        // verify the required parameter 'newIndex' is set
        if (newIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'newIndex' when calling moveItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Items/{itemId}/Move/{newIndex}"
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()))
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{newIndex}", ApiClient.urlEncode(newIndex.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
     * Removes items from a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param entryIds The item ids, comma delimited. (optional)
     * @throws ApiException if fails to make API call
     */
    public void removeItemFromPlaylist(@org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.NonNull List<String> entryIds) throws ApiException {
        removeItemFromPlaylistWithHttpInfo(playlistId, entryIds);
    }

    /**
     * Removes items from a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param entryIds The item ids, comma delimited. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeItemFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.NonNull List<String> entryIds) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeItemFromPlaylistRequestBuilder(playlistId, entryIds);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeItemFromPlaylist", localVarResponse);
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

    private HttpRequest.Builder removeItemFromPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.NonNull List<String> entryIds) throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling removeItemFromPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Items".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "entryIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "entryIds", entryIds));

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
     * Remove a user from a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
     */
    public void removeUserFromPlaylist(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        removeUserFromPlaylistWithHttpInfo(playlistId, userId);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeUserFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeUserFromPlaylistRequestBuilder(playlistId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeUserFromPlaylist", localVarResponse);
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

    private HttpRequest.Builder removeUserFromPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID playlistId, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling removeUserFromPlaylist");
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling removeUserFromPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Users/{userId}"
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()))
                .replace("{userId}", ApiClient.urlEncode(userId.toString()));

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
     * Updates a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. (required)
     * @throws ApiException if fails to make API call
     */
    public void updatePlaylist(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistDto updatePlaylistDto) throws ApiException {
        updatePlaylistWithHttpInfo(playlistId, updatePlaylistDto);
    }

    /**
     * Updates a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updatePlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistDto updatePlaylistDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updatePlaylistRequestBuilder(playlistId, updatePlaylistDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updatePlaylist", localVarResponse);
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

    private HttpRequest.Builder updatePlaylistRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistDto updatePlaylistDto) throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling updatePlaylist");
        }
        // verify the required parameter 'updatePlaylistDto' is set
        if (updatePlaylistDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updatePlaylistDto' when calling updatePlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updatePlaylistDto);
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
     * Modify a user of a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto. (required)
     * @throws ApiException if fails to make API call
     */
    public void updatePlaylistUser(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistUserDto updatePlaylistUserDto) throws ApiException {
        updatePlaylistUserWithHttpInfo(playlistId, userId, updatePlaylistUserDto);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updatePlaylistUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistUserDto updatePlaylistUserDto) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updatePlaylistUserRequestBuilder(playlistId, userId,
                updatePlaylistUserDto);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updatePlaylistUser", localVarResponse);
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

    private HttpRequest.Builder updatePlaylistUserRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistUserDto updatePlaylistUserDto) throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling updatePlaylistUser");
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updatePlaylistUser");
        }
        // verify the required parameter 'updatePlaylistUserDto' is set
        if (updatePlaylistUserDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updatePlaylistUserDto' when calling updatePlaylistUser");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Users/{userId}"
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()))
                .replace("{userId}", ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updatePlaylistUserDto);
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
