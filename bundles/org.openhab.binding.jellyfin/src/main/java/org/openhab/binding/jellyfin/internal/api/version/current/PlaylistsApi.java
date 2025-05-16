package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.CreatePlaylistDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlaylistCreationResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlaylistDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlaylistUserPermissions;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UpdatePlaylistDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UpdatePlaylistUserDto;
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

import reactor.core.publisher.Flux;
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
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addItemToPlaylistRequestCreation(UUID playlistId, List<UUID> ids, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling addItemToPlaylist",
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

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
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
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> addItemToPlaylist(UUID playlistId, List<UUID> ids, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addItemToPlaylistRequestCreation(playlistId, ids, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Adds items to a playlist.
     * 
     * <p>
     * <b>204</b> - Items added to playlist.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> addItemToPlaylistWithHttpInfo(UUID playlistId, List<UUID> ids, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addItemToPlaylistRequestCreation(playlistId, ids, userId).toEntity(localVarReturnType);
    }

    /**
     * Adds items to a playlist.
     * 
     * <p>
     * <b>204</b> - Items added to playlist.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param ids Item id, comma delimited.
     * @param userId The userId.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addItemToPlaylistWithResponseSpec(UUID playlistId, List<UUID> ids, UUID userId)
            throws WebClientResponseException {
        return addItemToPlaylistRequestCreation(playlistId, ids, userId);
    }

    /**
     * Creates a new playlist.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Playlist created.
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
    private ResponseSpec createPlaylistRequestCreation(String name, List<UUID> ids, UUID userId, MediaType mediaType,
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
     * <b>200</b> - Playlist created.
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
    public Mono<PlaylistCreationResult> createPlaylist(String name, List<UUID> ids, UUID userId, MediaType mediaType,
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
     * <b>200</b> - Playlist created.
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
            UUID userId, MediaType mediaType, CreatePlaylistDto createPlaylistDto) throws WebClientResponseException {
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
     * <b>200</b> - Playlist created.
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
    public ResponseSpec createPlaylistWithResponseSpec(String name, List<UUID> ids, UUID userId, MediaType mediaType,
            CreatePlaylistDto createPlaylistDto) throws WebClientResponseException {
        return createPlaylistRequestCreation(name, ids, userId, mediaType, createPlaylistDto);
    }

    /**
     * Get a playlist.
     * 
     * <p>
     * <b>200</b> - The playlist.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @return PlaylistDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPlaylistRequestCreation(UUID playlistId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException("Missing the required parameter 'playlistId' when calling getPlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<PlaylistDto> localVarReturnType = new ParameterizedTypeReference<PlaylistDto>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get a playlist.
     * 
     * <p>
     * <b>200</b> - The playlist.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @return PlaylistDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PlaylistDto> getPlaylist(UUID playlistId) throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistDto> localVarReturnType = new ParameterizedTypeReference<PlaylistDto>() {
        };
        return getPlaylistRequestCreation(playlistId).bodyToMono(localVarReturnType);
    }

    /**
     * Get a playlist.
     * 
     * <p>
     * <b>200</b> - The playlist.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @return ResponseEntity&lt;PlaylistDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PlaylistDto>> getPlaylistWithHttpInfo(UUID playlistId)
            throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistDto> localVarReturnType = new ParameterizedTypeReference<PlaylistDto>() {
        };
        return getPlaylistRequestCreation(playlistId).toEntity(localVarReturnType);
    }

    /**
     * Get a playlist.
     * 
     * <p>
     * <b>200</b> - The playlist.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playlistId The playlist id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPlaylistWithResponseSpec(UUID playlistId) throws WebClientResponseException {
        return getPlaylistRequestCreation(playlistId);
    }

    /**
     * Gets the original items of a playlist.
     * 
     * <p>
     * <b>200</b> - Original playlist returned.
     * <p>
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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
     * Get a playlist user.
     * 
     * <p>
     * <b>200</b> - User permission found.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @return PlaylistUserPermissions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPlaylistUserRequestCreation(UUID playlistId, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling getPlaylistUser",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getPlaylistUser",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);
        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<PlaylistUserPermissions> localVarReturnType = new ParameterizedTypeReference<PlaylistUserPermissions>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Users/{userId}", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Get a playlist user.
     * 
     * <p>
     * <b>200</b> - User permission found.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @return PlaylistUserPermissions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PlaylistUserPermissions> getPlaylistUser(UUID playlistId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistUserPermissions> localVarReturnType = new ParameterizedTypeReference<PlaylistUserPermissions>() {
        };
        return getPlaylistUserRequestCreation(playlistId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Get a playlist user.
     * 
     * <p>
     * <b>200</b> - User permission found.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @return ResponseEntity&lt;PlaylistUserPermissions&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PlaylistUserPermissions>> getPlaylistUserWithHttpInfo(UUID playlistId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistUserPermissions> localVarReturnType = new ParameterizedTypeReference<PlaylistUserPermissions>() {
        };
        return getPlaylistUserRequestCreation(playlistId, userId).toEntity(localVarReturnType);
    }

    /**
     * Get a playlist user.
     * 
     * <p>
     * <b>200</b> - User permission found.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPlaylistUserWithResponseSpec(UUID playlistId, UUID userId)
            throws WebClientResponseException {
        return getPlaylistUserRequestCreation(playlistId, userId);
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * <p>
     * <b>200</b> - Found shares.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @return List&lt;PlaylistUserPermissions&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPlaylistUsersRequestCreation(UUID playlistId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling getPlaylistUsers",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<PlaylistUserPermissions> localVarReturnType = new ParameterizedTypeReference<PlaylistUserPermissions>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Users", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * <p>
     * <b>200</b> - Found shares.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @return List&lt;PlaylistUserPermissions&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<PlaylistUserPermissions> getPlaylistUsers(UUID playlistId) throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistUserPermissions> localVarReturnType = new ParameterizedTypeReference<PlaylistUserPermissions>() {
        };
        return getPlaylistUsersRequestCreation(playlistId).bodyToFlux(localVarReturnType);
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * <p>
     * <b>200</b> - Found shares.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @return ResponseEntity&lt;List&lt;PlaylistUserPermissions&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<PlaylistUserPermissions>>> getPlaylistUsersWithHttpInfo(UUID playlistId)
            throws WebClientResponseException {
        ParameterizedTypeReference<PlaylistUserPermissions> localVarReturnType = new ParameterizedTypeReference<PlaylistUserPermissions>() {
        };
        return getPlaylistUsersRequestCreation(playlistId).toEntityList(localVarReturnType);
    }

    /**
     * Get a playlist&#39;s users.
     * 
     * <p>
     * <b>200</b> - Found shares.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPlaylistUsersWithResponseSpec(UUID playlistId) throws WebClientResponseException {
        return getPlaylistUsersRequestCreation(playlistId);
    }

    /**
     * Moves a playlist item.
     * 
     * <p>
     * <b>204</b> - Item moved to new index.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
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
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
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
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec removeItemFromPlaylistRequestCreation(String playlistId, List<String> entryIds)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling removeItemFromPlaylist",
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

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
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
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> removeItemFromPlaylist(String playlistId, List<String> entryIds)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeItemFromPlaylistRequestCreation(playlistId, entryIds).bodyToMono(localVarReturnType);
    }

    /**
     * Removes items from a playlist.
     * 
     * <p>
     * <b>204</b> - Items removed.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> removeItemFromPlaylistWithHttpInfo(String playlistId, List<String> entryIds)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeItemFromPlaylistRequestCreation(playlistId, entryIds).toEntity(localVarReturnType);
    }

    /**
     * Removes items from a playlist.
     * 
     * <p>
     * <b>204</b> - Items removed.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param entryIds The item ids, comma delimited.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec removeItemFromPlaylistWithResponseSpec(String playlistId, List<String> entryIds)
            throws WebClientResponseException {
        return removeItemFromPlaylistRequestCreation(playlistId, entryIds);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User permissions removed from playlist.
     * <p>
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - No playlist or user permissions found.
     * <p>
     * <b>401</b> - Unauthorized access.
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec removeUserFromPlaylistRequestCreation(UUID playlistId, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling removeUserFromPlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'userId' when calling removeUserFromPlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);
        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Users/{userId}", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User permissions removed from playlist.
     * <p>
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - No playlist or user permissions found.
     * <p>
     * <b>401</b> - Unauthorized access.
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> removeUserFromPlaylist(UUID playlistId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeUserFromPlaylistRequestCreation(playlistId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User permissions removed from playlist.
     * <p>
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - No playlist or user permissions found.
     * <p>
     * <b>401</b> - Unauthorized access.
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> removeUserFromPlaylistWithHttpInfo(UUID playlistId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeUserFromPlaylistRequestCreation(playlistId, userId).toEntity(localVarReturnType);
    }

    /**
     * Remove a user from a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User permissions removed from playlist.
     * <p>
     * <b>403</b> - Forbidden
     * <p>
     * <b>404</b> - No playlist or user permissions found.
     * <p>
     * <b>401</b> - Unauthorized access.
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec removeUserFromPlaylistWithResponseSpec(UUID playlistId, UUID userId)
            throws WebClientResponseException {
        return removeUserFromPlaylistRequestCreation(playlistId, userId);
    }

    /**
     * Updates a playlist.
     * 
     * <p>
     * <b>204</b> - Playlist updated.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updatePlaylistRequestCreation(UUID playlistId, UpdatePlaylistDto updatePlaylistDto)
            throws WebClientResponseException {
        Object postBody = updatePlaylistDto;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling updatePlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'updatePlaylistDto' is set
        if (updatePlaylistDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'updatePlaylistDto' when calling updatePlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates a playlist.
     * 
     * <p>
     * <b>204</b> - Playlist updated.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updatePlaylist(UUID playlistId, UpdatePlaylistDto updatePlaylistDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updatePlaylistRequestCreation(playlistId, updatePlaylistDto).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a playlist.
     * 
     * <p>
     * <b>204</b> - Playlist updated.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updatePlaylistWithHttpInfo(UUID playlistId, UpdatePlaylistDto updatePlaylistDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updatePlaylistRequestCreation(playlistId, updatePlaylistDto).toEntity(localVarReturnType);
    }

    /**
     * Updates a playlist.
     * 
     * <p>
     * <b>204</b> - Playlist updated.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param updatePlaylistDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistDto id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updatePlaylistWithResponseSpec(UUID playlistId, UpdatePlaylistDto updatePlaylistDto)
            throws WebClientResponseException {
        return updatePlaylistRequestCreation(playlistId, updatePlaylistDto);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User&#39;s permissions modified.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updatePlaylistUserRequestCreation(UUID playlistId, UUID userId,
            UpdatePlaylistUserDto updatePlaylistUserDto) throws WebClientResponseException {
        Object postBody = updatePlaylistUserDto;
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playlistId' when calling updatePlaylistUser",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'userId' when calling updatePlaylistUser",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'updatePlaylistUserDto' is set
        if (updatePlaylistUserDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'updatePlaylistUserDto' when calling updatePlaylistUser",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("playlistId", playlistId);
        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Playlists/{playlistId}/Users/{userId}", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User&#39;s permissions modified.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updatePlaylistUser(UUID playlistId, UUID userId, UpdatePlaylistUserDto updatePlaylistUserDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updatePlaylistUserRequestCreation(playlistId, userId, updatePlaylistUserDto)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User&#39;s permissions modified.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updatePlaylistUserWithHttpInfo(UUID playlistId, UUID userId,
            UpdatePlaylistUserDto updatePlaylistUserDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updatePlaylistUserRequestCreation(playlistId, userId, updatePlaylistUserDto)
                .toEntity(localVarReturnType);
    }

    /**
     * Modify a user of a playlist&#39;s users.
     * 
     * <p>
     * <b>204</b> - User&#39;s permissions modified.
     * <p>
     * <b>403</b> - Access forbidden.
     * <p>
     * <b>404</b> - Playlist not found.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param playlistId The playlist id.
     * @param userId The user id.
     * @param updatePlaylistUserDto The Jellyfin.Api.Models.PlaylistDtos.UpdatePlaylistUserDto.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updatePlaylistUserWithResponseSpec(UUID playlistId, UUID userId,
            UpdatePlaylistUserDto updatePlaylistUserDto) throws WebClientResponseException {
        return updatePlaylistUserRequestCreation(playlistId, userId, updatePlaylistUserDto);
    }
}
