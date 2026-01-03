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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.CreatePlaylistDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PlaylistCreationResult;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PlaylistDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PlaylistUserPermissions;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.UpdatePlaylistDto;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.UpdatePlaylistUserDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaylistsApi {
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
     * Adds items to a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param ids Item id, comma delimited. (optional)
     * @param userId The userId. (optional)
     * @throws ApiException if fails to make API call
     */
    public void addItemToPlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        addItemToPlaylist(playlistId, ids, userId, null);
    }

    /**
     * Adds items to a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param ids Item id, comma delimited. (optional)
     * @param userId The userId. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void addItemToPlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        addItemToPlaylistWithHttpInfo(playlistId, ids, userId, headers);
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
    public ApiResponse<Void> addItemToPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return addItemToPlaylistWithHttpInfo(playlistId, ids, userId, null);
    }

    /**
     * Adds items to a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param ids Item id, comma delimited. (optional)
     * @param userId The userId. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> addItemToPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addItemToPlaylistRequestBuilder(playlistId, ids, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addItemToPlaylist", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder addItemToPlaylistRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
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
    public PlaylistCreationResult createPlaylist(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable MediaType mediaType,
            @org.eclipse.jdt.annotation.Nullable CreatePlaylistDto createPlaylistDto) throws ApiException {
        return createPlaylist(name, ids, userId, mediaType, createPlaylistDto, null);
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
     * @param headers Optional headers to include in the request
     * @return PlaylistCreationResult
     * @throws ApiException if fails to make API call
     */
    public PlaylistCreationResult createPlaylist(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable MediaType mediaType,
            @org.eclipse.jdt.annotation.Nullable CreatePlaylistDto createPlaylistDto, Map<String, String> headers)
            throws ApiException {
        ApiResponse<PlaylistCreationResult> localVarResponse = createPlaylistWithHttpInfo(name, ids, userId, mediaType,
                createPlaylistDto, headers);
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
            @org.eclipse.jdt.annotation.Nullable String name, @org.eclipse.jdt.annotation.Nullable List<UUID> ids,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable MediaType mediaType,
            @org.eclipse.jdt.annotation.Nullable CreatePlaylistDto createPlaylistDto) throws ApiException {
        return createPlaylistWithHttpInfo(name, ids, userId, mediaType, createPlaylistDto, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;PlaylistCreationResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaylistCreationResult> createPlaylistWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String name, @org.eclipse.jdt.annotation.Nullable List<UUID> ids,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable MediaType mediaType,
            @org.eclipse.jdt.annotation.Nullable CreatePlaylistDto createPlaylistDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = createPlaylistRequestBuilder(name, ids, userId, mediaType,
                createPlaylistDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("createPlaylist", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<PlaylistCreationResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                PlaylistCreationResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<PlaylistCreationResult>() {
                        });

                return new ApiResponse<PlaylistCreationResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder createPlaylistRequestBuilder(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable MediaType mediaType,
            @org.eclipse.jdt.annotation.Nullable CreatePlaylistDto createPlaylistDto, Map<String, String> headers)
            throws ApiException {

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(createPlaylistDto);
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
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @return PlaylistDto
     * @throws ApiException if fails to make API call
     */
    public PlaylistDto getPlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId) throws ApiException {
        return getPlaylist(playlistId, null);
    }

    /**
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param headers Optional headers to include in the request
     * @return PlaylistDto
     * @throws ApiException if fails to make API call
     */
    public PlaylistDto getPlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId, Map<String, String> headers)
            throws ApiException {
        ApiResponse<PlaylistDto> localVarResponse = getPlaylistWithHttpInfo(playlistId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @return ApiResponse&lt;PlaylistDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaylistDto> getPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId)
            throws ApiException {
        return getPlaylistWithHttpInfo(playlistId, null);
    }

    /**
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;PlaylistDto&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaylistDto> getPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistRequestBuilder(playlistId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylist", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<PlaylistDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                            null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                PlaylistDto responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<PlaylistDto>() {
                        });

                return new ApiResponse<PlaylistDto>(localVarResponse.statusCode(), localVarResponse.headers().map(),
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

    private HttpRequest.Builder getPlaylistRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

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
    public BaseItemDtoQueryResult getPlaylistItems(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable Integer startIndex,
            @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes) throws ApiException {
        return getPlaylistItems(playlistId, userId, startIndex, limit, fields, enableImages, enableUserData,
                imageTypeLimit, enableImageTypes, null);
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
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getPlaylistItems(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable Integer startIndex,
            @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getPlaylistItemsWithHttpInfo(playlistId, userId,
                startIndex, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes, headers);
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
            @org.eclipse.jdt.annotation.NonNull UUID playlistId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes) throws ApiException {
        return getPlaylistItemsWithHttpInfo(playlistId, userId, startIndex, limit, fields, enableImages, enableUserData,
                imageTypeLimit, enableImageTypes, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getPlaylistItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID playlistId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistItemsRequestBuilder(playlistId, userId, startIndex,
                limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylistItems", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getPlaylistItemsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable Integer startIndex,
            @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes, Map<String, String> headers)
            throws ApiException {
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
     * Get a playlist user.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @return PlaylistUserPermissions
     * @throws ApiException if fails to make API call
     */
    public PlaylistUserPermissions getPlaylistUser(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getPlaylistUser(playlistId, userId, null);
    }

    /**
     * Get a playlist user.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @return PlaylistUserPermissions
     * @throws ApiException if fails to make API call
     */
    public PlaylistUserPermissions getPlaylistUser(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<PlaylistUserPermissions> localVarResponse = getPlaylistUserWithHttpInfo(playlistId, userId,
                headers);
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
            @org.eclipse.jdt.annotation.NonNull UUID playlistId, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        return getPlaylistUserWithHttpInfo(playlistId, userId, null);
    }

    /**
     * Get a playlist user.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;PlaylistUserPermissions&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<PlaylistUserPermissions> getPlaylistUserWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID playlistId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistUserRequestBuilder(playlistId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylistUser", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<PlaylistUserPermissions>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                PlaylistUserPermissions responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<PlaylistUserPermissions>() {
                        });

                return new ApiResponse<PlaylistUserPermissions>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getPlaylistUserRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
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
     * Get a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @return List&lt;PlaylistUserPermissions&gt;
     * @throws ApiException if fails to make API call
     */
    public List<PlaylistUserPermissions> getPlaylistUsers(@org.eclipse.jdt.annotation.NonNull UUID playlistId)
            throws ApiException {
        return getPlaylistUsers(playlistId, null);
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;PlaylistUserPermissions&gt;
     * @throws ApiException if fails to make API call
     */
    public List<PlaylistUserPermissions> getPlaylistUsers(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<PlaylistUserPermissions>> localVarResponse = getPlaylistUsersWithHttpInfo(playlistId, headers);
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
            @org.eclipse.jdt.annotation.NonNull UUID playlistId) throws ApiException {
        return getPlaylistUsersWithHttpInfo(playlistId, null);
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;PlaylistUserPermissions&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<PlaylistUserPermissions>> getPlaylistUsersWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID playlistId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPlaylistUsersRequestBuilder(playlistId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPlaylistUsers", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<PlaylistUserPermissions>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<PlaylistUserPermissions> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody,
                                new TypeReference<List<PlaylistUserPermissions>>() {
                                });

                return new ApiResponse<List<PlaylistUserPermissions>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getPlaylistUsersRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylistUsers");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Playlists/{playlistId}/Users".replace("{playlistId}",
                ApiClient.urlEncode(playlistId.toString()));

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
     * Moves a playlist item.
     * 
     * @param playlistId The playlist id. (required)
     * @param itemId The item id. (required)
     * @param newIndex The new index. (required)
     * @throws ApiException if fails to make API call
     */
    public void moveItem(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull Integer newIndex)
            throws ApiException {
        moveItem(playlistId, itemId, newIndex, null);
    }

    /**
     * Moves a playlist item.
     * 
     * @param playlistId The playlist id. (required)
     * @param itemId The item id. (required)
     * @param newIndex The new index. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void moveItem(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull Integer newIndex,
            Map<String, String> headers) throws ApiException {
        moveItemWithHttpInfo(playlistId, itemId, newIndex, headers);
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
    public ApiResponse<Void> moveItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull Integer newIndex)
            throws ApiException {
        return moveItemWithHttpInfo(playlistId, itemId, newIndex, null);
    }

    /**
     * Moves a playlist item.
     * 
     * @param playlistId The playlist id. (required)
     * @param itemId The item id. (required)
     * @param newIndex The new index. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> moveItemWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull Integer newIndex,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = moveItemRequestBuilder(playlistId, itemId, newIndex, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("moveItem", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder moveItemRequestBuilder(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.NonNull String itemId, @org.eclipse.jdt.annotation.NonNull Integer newIndex,
            Map<String, String> headers) throws ApiException {
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
     * Removes items from a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param entryIds The item ids, comma delimited. (optional)
     * @throws ApiException if fails to make API call
     */
    public void removeItemFromPlaylist(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.Nullable List<String> entryIds) throws ApiException {
        removeItemFromPlaylist(playlistId, entryIds, null);
    }

    /**
     * Removes items from a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param entryIds The item ids, comma delimited. (optional)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void removeItemFromPlaylist(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.Nullable List<String> entryIds, Map<String, String> headers)
            throws ApiException {
        removeItemFromPlaylistWithHttpInfo(playlistId, entryIds, headers);
    }

    /**
     * Removes items from a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param entryIds The item ids, comma delimited. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeItemFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.Nullable List<String> entryIds) throws ApiException {
        return removeItemFromPlaylistWithHttpInfo(playlistId, entryIds, null);
    }

    /**
     * Removes items from a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param entryIds The item ids, comma delimited. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeItemFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.Nullable List<String> entryIds, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeItemFromPlaylistRequestBuilder(playlistId, entryIds,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeItemFromPlaylist", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder removeItemFromPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull String playlistId,
            @org.eclipse.jdt.annotation.Nullable List<String> entryIds, Map<String, String> headers)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Remove a user from a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
     */
    public void removeUserFromPlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        removeUserFromPlaylist(playlistId, userId, null);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void removeUserFromPlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        removeUserFromPlaylistWithHttpInfo(playlistId, userId, headers);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeUserFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return removeUserFromPlaylistWithHttpInfo(playlistId, userId, null);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> removeUserFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeUserFromPlaylistRequestBuilder(playlistId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeUserFromPlaylist", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder removeUserFromPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull UUID playlistId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            Map<String, String> headers) throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     * Updates a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. (required)
     * @throws ApiException if fails to make API call
     */
    public void updatePlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistDto updatePlaylistDto) throws ApiException {
        updatePlaylist(playlistId, updatePlaylistDto, null);
    }

    /**
     * Updates a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updatePlaylist(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistDto updatePlaylistDto, Map<String, String> headers)
            throws ApiException {
        updatePlaylistWithHttpInfo(playlistId, updatePlaylistDto, headers);
    }

    /**
     * Updates a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updatePlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistDto updatePlaylistDto) throws ApiException {
        return updatePlaylistWithHttpInfo(playlistId, updatePlaylistDto, null);
    }

    /**
     * Updates a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updatePlaylistWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistDto updatePlaylistDto, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updatePlaylistRequestBuilder(playlistId, updatePlaylistDto,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updatePlaylist", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder updatePlaylistRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistDto updatePlaylistDto, Map<String, String> headers)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updatePlaylistDto);
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
     * Modify a user of a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto. (required)
     * @throws ApiException if fails to make API call
     */
    public void updatePlaylistUser(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistUserDto updatePlaylistUserDto) throws ApiException {
        updatePlaylistUser(playlistId, userId, updatePlaylistUserDto, null);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void updatePlaylistUser(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistUserDto updatePlaylistUserDto,
            Map<String, String> headers) throws ApiException {
        updatePlaylistUserWithHttpInfo(playlistId, userId, updatePlaylistUserDto, headers);
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
    public ApiResponse<Void> updatePlaylistUserWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistUserDto updatePlaylistUserDto) throws ApiException {
        return updatePlaylistUserWithHttpInfo(playlistId, userId, updatePlaylistUserDto, null);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> updatePlaylistUserWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistUserDto updatePlaylistUserDto,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = updatePlaylistUserRequestBuilder(playlistId, userId,
                updatePlaylistUserDto, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("updatePlaylistUser", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder updatePlaylistUserRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UpdatePlaylistUserDto updatePlaylistUserDto,
            Map<String, String> headers) throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(updatePlaylistUserDto);
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
