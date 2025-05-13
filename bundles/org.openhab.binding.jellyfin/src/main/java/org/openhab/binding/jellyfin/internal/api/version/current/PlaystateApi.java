package org.openhab.binding.jellyfin.internal.api.version.current;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlayMethod;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlaybackProgressInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlaybackStartInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlaybackStopInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.RepeatMode;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UserItemDataDto;
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
public class PlaystateApi {
    private ApiClient apiClient;

    public PlaystateApi() {
        this(new ApiClient());
    }

    @Autowired
    public PlaystateApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Marks an item as played for user.
     * 
     * <p>
     * <b>200</b> - Item marked as played.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param datePlayed Optional. The date the item was played.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec markPlayedItemRequestCreation(UUID itemId, UUID userId, OffsetDateTime datePlayed)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling markPlayedItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "datePlayed", datePlayed));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("/UserPlayedItems/{itemId}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Marks an item as played for user.
     * 
     * <p>
     * <b>200</b> - Item marked as played.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param datePlayed Optional. The date the item was played.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> markPlayedItem(UUID itemId, UUID userId, OffsetDateTime datePlayed)
            throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return markPlayedItemRequestCreation(itemId, userId, datePlayed).bodyToMono(localVarReturnType);
    }

    /**
     * Marks an item as played for user.
     * 
     * <p>
     * <b>200</b> - Item marked as played.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param datePlayed Optional. The date the item was played.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> markPlayedItemWithHttpInfo(UUID itemId, UUID userId,
            OffsetDateTime datePlayed) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return markPlayedItemRequestCreation(itemId, userId, datePlayed).toEntity(localVarReturnType);
    }

    /**
     * Marks an item as played for user.
     * 
     * <p>
     * <b>200</b> - Item marked as played.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param datePlayed Optional. The date the item was played.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec markPlayedItemWithResponseSpec(UUID itemId, UUID userId, OffsetDateTime datePlayed)
            throws WebClientResponseException {
        return markPlayedItemRequestCreation(itemId, userId, datePlayed);
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * <p>
     * <b>200</b> - Item marked as unplayed.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec markUnplayedItemRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling markUnplayedItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("/UserPlayedItems/{itemId}", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * <p>
     * <b>200</b> - Item marked as unplayed.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> markUnplayedItem(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return markUnplayedItemRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * <p>
     * <b>200</b> - Item marked as unplayed.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> markUnplayedItemWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return markUnplayedItemRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * <p>
     * <b>200</b> - Item marked as unplayed.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec markUnplayedItemWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return markUnplayedItemRequestCreation(itemId, userId);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * <p>
     * <b>204</b> - Play progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param volumeLevel Scale of 0-100.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param repeatMode The repeat mode.
     * @param isPaused Indicates if the player is paused.
     * @param isMuted Indicates if the player is muted.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec onPlaybackProgressRequestCreation(UUID itemId, String mediaSourceId, Long positionTicks,
            Integer audioStreamIndex, Integer subtitleStreamIndex, Integer volumeLevel, PlayMethod playMethod,
            String liveStreamId, String playSessionId, RepeatMode repeatMode, Boolean isPaused, Boolean isMuted)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling onPlaybackProgress",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "positionTicks", positionTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "volumeLevel", volumeLevel));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playMethod", playMethod));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "repeatMode", repeatMode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPaused", isPaused));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMuted", isMuted));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/PlayingItems/{itemId}/Progress", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * <p>
     * <b>204</b> - Play progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param volumeLevel Scale of 0-100.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param repeatMode The repeat mode.
     * @param isPaused Indicates if the player is paused.
     * @param isMuted Indicates if the player is muted.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> onPlaybackProgress(UUID itemId, String mediaSourceId, Long positionTicks,
            Integer audioStreamIndex, Integer subtitleStreamIndex, Integer volumeLevel, PlayMethod playMethod,
            String liveStreamId, String playSessionId, RepeatMode repeatMode, Boolean isPaused, Boolean isMuted)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return onPlaybackProgressRequestCreation(itemId, mediaSourceId, positionTicks, audioStreamIndex,
                subtitleStreamIndex, volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused,
                isMuted).bodyToMono(localVarReturnType);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * <p>
     * <b>204</b> - Play progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param volumeLevel Scale of 0-100.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param repeatMode The repeat mode.
     * @param isPaused Indicates if the player is paused.
     * @param isMuted Indicates if the player is muted.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> onPlaybackProgressWithHttpInfo(UUID itemId, String mediaSourceId,
            Long positionTicks, Integer audioStreamIndex, Integer subtitleStreamIndex, Integer volumeLevel,
            PlayMethod playMethod, String liveStreamId, String playSessionId, RepeatMode repeatMode, Boolean isPaused,
            Boolean isMuted) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return onPlaybackProgressRequestCreation(itemId, mediaSourceId, positionTicks, audioStreamIndex,
                subtitleStreamIndex, volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused,
                isMuted).toEntity(localVarReturnType);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * <p>
     * <b>204</b> - Play progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param volumeLevel Scale of 0-100.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param repeatMode The repeat mode.
     * @param isPaused Indicates if the player is paused.
     * @param isMuted Indicates if the player is muted.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec onPlaybackProgressWithResponseSpec(UUID itemId, String mediaSourceId, Long positionTicks,
            Integer audioStreamIndex, Integer subtitleStreamIndex, Integer volumeLevel, PlayMethod playMethod,
            String liveStreamId, String playSessionId, RepeatMode repeatMode, Boolean isPaused, Boolean isMuted)
            throws WebClientResponseException {
        return onPlaybackProgressRequestCreation(itemId, mediaSourceId, positionTicks, audioStreamIndex,
                subtitleStreamIndex, volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused,
                isMuted);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * <p>
     * <b>204</b> - Play start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param canSeek Indicates if the client can seek.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec onPlaybackStartRequestCreation(UUID itemId, String mediaSourceId, Integer audioStreamIndex,
            Integer subtitleStreamIndex, PlayMethod playMethod, String liveStreamId, String playSessionId,
            Boolean canSeek) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling onPlaybackStart",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playMethod", playMethod));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "canSeek", canSeek));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/PlayingItems/{itemId}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * <p>
     * <b>204</b> - Play start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param canSeek Indicates if the client can seek.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> onPlaybackStart(UUID itemId, String mediaSourceId, Integer audioStreamIndex,
            Integer subtitleStreamIndex, PlayMethod playMethod, String liveStreamId, String playSessionId,
            Boolean canSeek) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return onPlaybackStartRequestCreation(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod,
                liveStreamId, playSessionId, canSeek).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * <p>
     * <b>204</b> - Play start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param canSeek Indicates if the client can seek.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> onPlaybackStartWithHttpInfo(UUID itemId, String mediaSourceId,
            Integer audioStreamIndex, Integer subtitleStreamIndex, PlayMethod playMethod, String liveStreamId,
            String playSessionId, Boolean canSeek) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return onPlaybackStartRequestCreation(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod,
                liveStreamId, playSessionId, canSeek).toEntity(localVarReturnType);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * <p>
     * <b>204</b> - Play start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param playMethod The play method.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @param canSeek Indicates if the client can seek.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec onPlaybackStartWithResponseSpec(UUID itemId, String mediaSourceId, Integer audioStreamIndex,
            Integer subtitleStreamIndex, PlayMethod playMethod, String liveStreamId, String playSessionId,
            Boolean canSeek) throws WebClientResponseException {
        return onPlaybackStartRequestCreation(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod,
                liveStreamId, playSessionId, canSeek);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param nextMediaType The next media type that will play.
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec onPlaybackStoppedRequestCreation(UUID itemId, String mediaSourceId, String nextMediaType,
            Long positionTicks, String liveStreamId, String playSessionId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling onPlaybackStopped",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nextMediaType", nextMediaType));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "positionTicks", positionTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/PlayingItems/{itemId}", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param nextMediaType The next media type that will play.
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> onPlaybackStopped(UUID itemId, String mediaSourceId, String nextMediaType, Long positionTicks,
            String liveStreamId, String playSessionId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return onPlaybackStoppedRequestCreation(itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId,
                playSessionId).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param nextMediaType The next media type that will play.
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> onPlaybackStoppedWithHttpInfo(UUID itemId, String mediaSourceId,
            String nextMediaType, Long positionTicks, String liveStreamId, String playSessionId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return onPlaybackStoppedRequestCreation(itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId,
                playSessionId).toEntity(localVarReturnType);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param mediaSourceId The id of the MediaSource.
     * @param nextMediaType The next media type that will play.
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms.
     * @param liveStreamId The live stream id.
     * @param playSessionId The play session id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec onPlaybackStoppedWithResponseSpec(UUID itemId, String mediaSourceId, String nextMediaType,
            Long positionTicks, String liveStreamId, String playSessionId) throws WebClientResponseException {
        return onPlaybackStoppedRequestCreation(itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId,
                playSessionId);
    }

    /**
     * Pings a playback session.
     * 
     * <p>
     * <b>204</b> - Playback session pinged.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playSessionId Playback session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec pingPlaybackSessionRequestCreation(String playSessionId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'playSessionId' is set
        if (playSessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playSessionId' when calling pingPlaybackSession",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Playing/Ping", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Pings a playback session.
     * 
     * <p>
     * <b>204</b> - Playback session pinged.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playSessionId Playback session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> pingPlaybackSession(String playSessionId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return pingPlaybackSessionRequestCreation(playSessionId).bodyToMono(localVarReturnType);
    }

    /**
     * Pings a playback session.
     * 
     * <p>
     * <b>204</b> - Playback session pinged.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playSessionId Playback session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> pingPlaybackSessionWithHttpInfo(String playSessionId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return pingPlaybackSessionRequestCreation(playSessionId).toEntity(localVarReturnType);
    }

    /**
     * Pings a playback session.
     * 
     * <p>
     * <b>204</b> - Playback session pinged.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playSessionId Playback session id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec pingPlaybackSessionWithResponseSpec(String playSessionId) throws WebClientResponseException {
        return pingPlaybackSessionRequestCreation(playSessionId);
    }

    /**
     * Reports playback progress within a session.
     * 
     * <p>
     * <b>204</b> - Playback progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackProgressInfo The playback progress info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec reportPlaybackProgressRequestCreation(PlaybackProgressInfo playbackProgressInfo)
            throws WebClientResponseException {
        Object postBody = playbackProgressInfo;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Playing/Progress", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Reports playback progress within a session.
     * 
     * <p>
     * <b>204</b> - Playback progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackProgressInfo The playback progress info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> reportPlaybackProgress(PlaybackProgressInfo playbackProgressInfo)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportPlaybackProgressRequestCreation(playbackProgressInfo).bodyToMono(localVarReturnType);
    }

    /**
     * Reports playback progress within a session.
     * 
     * <p>
     * <b>204</b> - Playback progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackProgressInfo The playback progress info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> reportPlaybackProgressWithHttpInfo(PlaybackProgressInfo playbackProgressInfo)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportPlaybackProgressRequestCreation(playbackProgressInfo).toEntity(localVarReturnType);
    }

    /**
     * Reports playback progress within a session.
     * 
     * <p>
     * <b>204</b> - Playback progress recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackProgressInfo The playback progress info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec reportPlaybackProgressWithResponseSpec(PlaybackProgressInfo playbackProgressInfo)
            throws WebClientResponseException {
        return reportPlaybackProgressRequestCreation(playbackProgressInfo);
    }

    /**
     * Reports playback has started within a session.
     * 
     * <p>
     * <b>204</b> - Playback start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStartInfo The playback start info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec reportPlaybackStartRequestCreation(PlaybackStartInfo playbackStartInfo)
            throws WebClientResponseException {
        Object postBody = playbackStartInfo;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Playing", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Reports playback has started within a session.
     * 
     * <p>
     * <b>204</b> - Playback start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStartInfo The playback start info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> reportPlaybackStart(PlaybackStartInfo playbackStartInfo) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportPlaybackStartRequestCreation(playbackStartInfo).bodyToMono(localVarReturnType);
    }

    /**
     * Reports playback has started within a session.
     * 
     * <p>
     * <b>204</b> - Playback start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStartInfo The playback start info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> reportPlaybackStartWithHttpInfo(PlaybackStartInfo playbackStartInfo)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportPlaybackStartRequestCreation(playbackStartInfo).toEntity(localVarReturnType);
    }

    /**
     * Reports playback has started within a session.
     * 
     * <p>
     * <b>204</b> - Playback start recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStartInfo The playback start info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec reportPlaybackStartWithResponseSpec(PlaybackStartInfo playbackStartInfo)
            throws WebClientResponseException {
        return reportPlaybackStartRequestCreation(playbackStartInfo);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStopInfo The playback stop info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec reportPlaybackStoppedRequestCreation(PlaybackStopInfo playbackStopInfo)
            throws WebClientResponseException {
        Object postBody = playbackStopInfo;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Playing/Stopped", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStopInfo The playback stop info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> reportPlaybackStopped(PlaybackStopInfo playbackStopInfo) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportPlaybackStoppedRequestCreation(playbackStopInfo).bodyToMono(localVarReturnType);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStopInfo The playback stop info.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> reportPlaybackStoppedWithHttpInfo(PlaybackStopInfo playbackStopInfo)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportPlaybackStoppedRequestCreation(playbackStopInfo).toEntity(localVarReturnType);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * <p>
     * <b>204</b> - Playback stop recorded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playbackStopInfo The playback stop info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec reportPlaybackStoppedWithResponseSpec(PlaybackStopInfo playbackStopInfo)
            throws WebClientResponseException {
        return reportPlaybackStoppedRequestCreation(playbackStopInfo);
    }
}
