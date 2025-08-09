package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

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

public class PlaylistsApi {
    private ApiClient apiClient;

    public PlaylistsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PlaylistsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get the API client
     *
     * @return API client
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client
     *
     * @param apiClient an instance of API client
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Adds items to a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param ids Item id, comma delimited. (optional)
     * @param userId The userId. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Items added to playlist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Items added to playlist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> addItemToPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull List<UUID> ids, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling addItemToPlaylist");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Items".replaceAll("\\{playlistId}",
                apiClient.escapeString(playlistId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "ids", ids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaylistsApi.addItemToPlaylist", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Playlist created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public PlaylistCreationResult createPlaylist(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull List<UUID> ids, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull MediaType mediaType,
            @org.eclipse.jdt.annotation.NonNull CreatePlaylistDto createPlaylistDto) throws ApiException {
        return createPlaylistWithHttpInfo(name, ids, userId, mediaType, createPlaylistDto).getData();
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Playlist created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<PlaylistCreationResult> createPlaylistWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String name, @org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull MediaType mediaType,
            @org.eclipse.jdt.annotation.NonNull CreatePlaylistDto createPlaylistDto) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "name", name));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "ids", ids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaType", mediaType));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<PlaylistCreationResult> localVarReturnType = new GenericType<PlaylistCreationResult>() {
        };
        return apiClient.invokeAPI("PlaylistsApi.createPlaylist", "/Playlists", "POST", localVarQueryParams,
                createPlaylistDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @return PlaylistDto
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>The playlist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public PlaylistDto getPlaylist(@org.eclipse.jdt.annotation.Nullable UUID playlistId) throws ApiException {
        return getPlaylistWithHttpInfo(playlistId).getData();
    }

    /**
     * Get a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @return ApiResponse&lt;PlaylistDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>The playlist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<PlaylistDto> getPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId)
            throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylist");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}".replaceAll("\\{playlistId}",
                apiClient.escapeString(playlistId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<PlaylistDto> localVarReturnType = new GenericType<PlaylistDto>() {
        };
        return apiClient.invokeAPI("PlaylistsApi.getPlaylist", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Original playlist returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getPlaylistItems(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes) throws ApiException {
        return getPlaylistItemsWithHttpInfo(playlistId, userId, startIndex, limit, fields, enableImages, enableUserData,
                imageTypeLimit, enableImageTypes).getData();
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Original playlist returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getPlaylistItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID playlistId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes) throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylistItems");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Items".replaceAll("\\{playlistId}",
                apiClient.escapeString(playlistId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("PlaylistsApi.getPlaylistItems", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get a playlist user.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @return PlaylistUserPermissions
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User permission found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public PlaylistUserPermissions getPlaylistUser(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getPlaylistUserWithHttpInfo(playlistId, userId).getData();
    }

    /**
     * Get a playlist user.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @return ApiResponse&lt;PlaylistUserPermissions&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>User permission found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<PlaylistUserPermissions> getPlaylistUserWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID playlistId, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylistUser");
        }
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling getPlaylistUser");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Users/{userId}"
                .replaceAll("\\{playlistId}", apiClient.escapeString(playlistId.toString()))
                .replaceAll("\\{userId}", apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<PlaylistUserPermissions> localVarReturnType = new GenericType<PlaylistUserPermissions>() {
        };
        return apiClient.invokeAPI("PlaylistsApi.getPlaylistUser", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @return List&lt;PlaylistUserPermissions&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Found shares.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<PlaylistUserPermissions> getPlaylistUsers(@org.eclipse.jdt.annotation.Nullable UUID playlistId)
            throws ApiException {
        return getPlaylistUsersWithHttpInfo(playlistId).getData();
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @return ApiResponse&lt;List&lt;PlaylistUserPermissions&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Found shares.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<PlaylistUserPermissions>> getPlaylistUsersWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID playlistId) throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getPlaylistUsers");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Users".replaceAll("\\{playlistId}",
                apiClient.escapeString(playlistId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<PlaylistUserPermissions>> localVarReturnType = new GenericType<List<PlaylistUserPermissions>>() {
        };
        return apiClient.invokeAPI("PlaylistsApi.getPlaylistUsers", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Moves a playlist item.
     * 
     * @param playlistId The playlist id. (required)
     * @param itemId The item id. (required)
     * @param newIndex The new index. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Item moved to new index.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Item moved to new index.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> moveItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable Integer newIndex)
            throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling moveItem");
        }
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling moveItem");
        }
        if (newIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'newIndex' when calling moveItem");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Items/{itemId}/Move/{newIndex}"
                .replaceAll("\\{playlistId}", apiClient.escapeString(playlistId.toString()))
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{newIndex}", apiClient.escapeString(newIndex.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaylistsApi.moveItem", localVarPath, "POST", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Removes items from a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param entryIds The item ids, comma delimited. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Items removed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Items removed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> removeItemFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.NonNull List<String> entryIds) throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling removeItemFromPlaylist");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Items".replaceAll("\\{playlistId}",
                apiClient.escapeString(playlistId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "entryIds", entryIds));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaylistsApi.removeItemFromPlaylist", localVarPath, "DELETE", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User permissions removed from playlist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>No playlist or user permissions found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized access.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User permissions removed from playlist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>No playlist or user permissions found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized access.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> removeUserFromPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling removeUserFromPlaylist");
        }
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling removeUserFromPlaylist");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Users/{userId}"
                .replaceAll("\\{playlistId}", apiClient.escapeString(playlistId.toString()))
                .replaceAll("\\{userId}", apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaylistsApi.removeUserFromPlaylist", localVarPath, "DELETE", new ArrayList<>(),
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates a playlist.
     * 
     * @param playlistId The playlist id. (required)
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Playlist updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Playlist updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updatePlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistDto updatePlaylistDto) throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling updatePlaylist");
        }
        if (updatePlaylistDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updatePlaylistDto' when calling updatePlaylist");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}".replaceAll("\\{playlistId}",
                apiClient.escapeString(playlistId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaylistsApi.updatePlaylist", localVarPath, "POST", new ArrayList<>(),
                updatePlaylistDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * @param playlistId The playlist id. (required)
     * @param userId The user id. (required)
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User&#39;s permissions modified.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User&#39;s permissions modified.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Access forbidden.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Playlist not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> updatePlaylistUserWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID playlistId,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UpdatePlaylistUserDto updatePlaylistUserDto) throws ApiException {
        // Check required parameters
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling updatePlaylistUser");
        }
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling updatePlaylistUser");
        }
        if (updatePlaylistUserDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updatePlaylistUserDto' when calling updatePlaylistUser");
        }

        // Path parameters
        String localVarPath = "/Playlists/{playlistId}/Users/{userId}"
                .replaceAll("\\{playlistId}", apiClient.escapeString(playlistId.toString()))
                .replaceAll("\\{userId}", apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaylistsApi.updatePlaylistUser", localVarPath, "POST", new ArrayList<>(),
                updatePlaylistUserDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }
}
