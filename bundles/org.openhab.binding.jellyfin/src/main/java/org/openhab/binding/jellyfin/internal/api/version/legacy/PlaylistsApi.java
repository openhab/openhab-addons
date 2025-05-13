package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.CreatePlaylistDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.PlaylistCreationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PlaylistsApi {
    private ApiClient apiClient;

    public PlaylistsApi() {
        this(new ApiClient());
    }

    @Autowired
    public PlaylistsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Adds items to a playlist.
     * 
     * <p>
     * <b>204</b> - Items added to playlist.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addToPlaylistRequestCreation(UUID playlistId, List<UUID> ids, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling addToPlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Items", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Adds items to a playlist.
     * 
     * <p>
     * <b>204</b> - Items added to playlist.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> addToPlaylist(UUID playlistId, List<UUID> ids, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addToPlaylistRequestCreation(playlistId, ids, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Adds items to a playlist.
     * 
     * <p>
     * <b>204</b> - Items added to playlist.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> addToPlaylistWithHttpInfo(UUID playlistId, List<UUID> ids, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addToPlaylistRequestCreation(playlistId, ids, userId).toEntity(localVarReturnType);
    }

    /**
     * Adds items to a playlist.
     * 
     * <p>
     * <b>204</b> - Items added to playlist.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addToPlaylistWithResponseSpec(UUID playlistId, List<UUID> ids, UUID userId)
            throws WebClientResponseException {
        return addToPlaylistRequestCreation(playlistId, ids, userId);
    }

    /**
     * Creates a new playlist.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The playlist name.
     * @param ids The item ids.
     * @param userId The user id.
     * @param mediaType The media type.
     * @param createPlaylistDto The create playlist payload.
     * @return PlaylistCreationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createPlaylistRequestCreation(String name, List<UUID> ids, UUID userId, String mediaType,
            CreatePlaylistDto createPlaylistDto) throws WebClientResponseException {
        Object postBody = createPlaylistDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaType", mediaType));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<PlaylistCreationResult> localVarReturnType = new ParameterizedTypeReference<PlaylistCreationResult>() {
        };
        return apiClient.invokeAPI("/Playlists", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates a new playlist.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The playlist name.
     * @param ids The item ids.
     * @param userId The user id.
     * @param mediaType The media type.
     * @param createPlaylistDto The create playlist payload.
     * @return PlaylistCreationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PlaylistCreationResult> createPlaylist(String name, List<UUID> ids, UUID userId, String mediaType,
            CreatePlaylistDto createPlaylistDto) throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistCreationResult> localVarReturnType = new ParameterizedTypeReference<PlaylistCreationResult>() {
        };
        return createPlaylistRequestCreation(name, ids, userId, mediaType, createPlaylistDto)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Creates a new playlist.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The playlist name.
     * @param ids The item ids.
     * @param userId The user id.
     * @param mediaType The media type.
     * @param createPlaylistDto The create playlist payload.
     * @return ResponseEntity&lt;PlaylistCreationResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PlaylistCreationResult>> createPlaylistWithHttpInfo(String name, List<UUID> ids,
            UUID userId, String mediaType, CreatePlaylistDto createPlaylistDto) throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistCreationResult> localVarReturnType = new ParameterizedTypeReference<PlaylistCreationResult>() {
        };
        return createPlaylistRequestCreation(name, ids, userId, mediaType, createPlaylistDto)
                .toEntity(localVarReturnType);
    }

    /**
     * Creates a new playlist.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The playlist name.
     * @param ids The item ids.
     * @param userId The user id.
     * @param mediaType The media type.
     * @param createPlaylistDto The create playlist payload.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createPlaylistWithResponseSpec(String name, List<UUID> ids, UUID userId, String mediaType,
            CreatePlaylistDto createPlaylistDto) throws WebClientResponseException {
        return createPlaylistRequestCreation(name, ids, userId, mediaType, createPlaylistDto);
    }

    /**
     * Gets the original items of a playlist.
     * 
     * <p>
     * <b>200</b> - Original playlist returned.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param userId User id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPlaylistItemsRequestCreation(UUID playlistId, UUID userId, Integer startIndex,
            Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling getPlaylistItems",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'userId' when calling getPlaylistItems",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Items", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets the original items of a playlist.
     * 
     * <p>
     * <b>200</b> - Original playlist returned.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param userId User id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getPlaylistItems(UUID playlistId, UUID userId, Integer startIndex,
            Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getPlaylistItemsRequestCreation(playlistId, userId, startIndex, limit, fields, enableImages,
                enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the original items of a playlist.
     * 
     * <p>
     * <b>200</b> - Original playlist returned.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param userId User id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getPlaylistItemsWithHttpInfo(UUID playlistId, UUID userId,
            Integer startIndex, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getPlaylistItemsRequestCreation(playlistId, userId, startIndex, limit, fields, enableImages,
                enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Gets the original items of a playlist.
     * 
     * <p>
     * <b>200</b> - Original playlist returned.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param userId User id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPlaylistItemsWithResponseSpec(UUID playlistId, UUID userId, Integer startIndex,
            Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getPlaylistItemsRequestCreation(playlistId, userId, startIndex, limit, fields, enableImages,
                enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Moves a playlist item.
     * 
     * <p>
     * <b>204</b> - Item moved to new index.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param itemId The item id.
     * @param newIndex The new index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec moveItemRequestCreation(String playlistId, String itemId, Integer newIndex)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException("Missing the required parameter 'playlistId' when calling moveItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling moveItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'newIndex' is set
        if (newIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'newIndex' when calling moveItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);
        pathParams.put("itemId", itemId);
        pathParams.put("newIndex", newIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Items/{itemId}/Move/{newIndex}", HttpMethod.POST,
                pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Moves a playlist item.
     * 
     * <p>
     * <b>204</b> - Item moved to new index.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param itemId The item id.
     * @param newIndex The new index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> moveItem(String playlistId, String itemId, Integer newIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return moveItemRequestCreation(playlistId, itemId, newIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Moves a playlist item.
     * 
     * <p>
     * <b>204</b> - Item moved to new index.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param itemId The item id.
     * @param newIndex The new index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> moveItemWithHttpInfo(String playlistId, String itemId, Integer newIndex)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return moveItemRequestCreation(playlistId, itemId, newIndex).toEntity(localVarReturnType);
    }

    /**
     * Moves a playlist item.
     * 
     * <p>
     * <b>204</b> - Item moved to new index.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param itemId The item id.
     * @param newIndex The new index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec moveItemWithResponseSpec(String playlistId, String itemId, Integer newIndex)
            throws WebClientResponseException {
        return moveItemRequestCreation(playlistId, itemId, newIndex);
    }

    /**
     * Removes items from a playlist.
     * 
     * <p>
     * <b>204</b> - Items removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec removeFromPlaylistRequestCreation(String playlistId, List<String> entryIds)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling removeFromPlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "entryIds", entryIds));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Items", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Removes items from a playlist.
     * 
     * <p>
     * <b>204</b> - Items removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> removeFromPlaylist(String playlistId, List<String> entryIds) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeFromPlaylistRequestCreation(playlistId, entryIds).bodyToMono(localVarReturnType);
    }

    /**
     * Removes items from a playlist.
     * 
     * <p>
     * <b>204</b> - Items removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> removeFromPlaylistWithHttpInfo(String playlistId, List<String> entryIds)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeFromPlaylistRequestCreation(playlistId, entryIds).toEntity(localVarReturnType);
    }

    /**
     * Removes items from a playlist.
     * 
     * <p>
     * <b>204</b> - Items removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec removeFromPlaylistWithResponseSpec(String playlistId, List<String> entryIds)
            throws WebClientResponseException {
        return removeFromPlaylistRequestCreation(playlistId, entryIds);
    }
}
