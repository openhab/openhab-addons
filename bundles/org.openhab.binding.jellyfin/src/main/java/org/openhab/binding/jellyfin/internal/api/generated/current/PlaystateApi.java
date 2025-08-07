package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlayMethod;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackProgressInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackStartInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackStopInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RepeatMode;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserItemDataDto;

import jakarta.ws.rs.core.GenericType;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaystateApi {
    private ApiClient apiClient;

    public PlaystateApi() {
        this(Configuration.getDefaultApiClient());
    }

    public PlaystateApi(ApiClient apiClient) {
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
     * Marks an item as played for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param datePlayed Optional. The date the item was played. (optional)
     * @return UserItemDataDto
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
     *                        <td>Item marked as played.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
    public UserItemDataDto markPlayedItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime datePlayed) throws ApiException {
        return markPlayedItemWithHttpInfo(itemId, userId, datePlayed).getData();
    }

    /**
     * Marks an item as played for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @param datePlayed Optional. The date the item was played. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
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
     *                        <td>Item marked as played.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
    public ApiResponse<UserItemDataDto> markPlayedItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime datePlayed) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling markPlayedItem");
        }

        // Path parameters
        String localVarPath = "/UserPlayedItems/{itemId}".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "datePlayed", datePlayed));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<UserItemDataDto> localVarReturnType = new GenericType<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("PlaystateApi.markPlayedItem", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return UserItemDataDto
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
     *                        <td>Item marked as unplayed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
    public UserItemDataDto markUnplayedItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return markUnplayedItemWithHttpInfo(itemId, userId).getData();
    }

    /**
     * Marks an item as unplayed for user.
     * 
     * @param itemId Item id. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;UserItemDataDto&gt;
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
     *                        <td>Item marked as unplayed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
    public ApiResponse<UserItemDataDto> markUnplayedItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling markUnplayedItem");
        }

        // Path parameters
        String localVarPath = "/UserPlayedItems/{itemId}".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<UserItemDataDto> localVarReturnType = new GenericType<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("PlaystateApi.markUnplayedItem", localVarPath, "DELETE", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param volumeLevel Scale of 0-100. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param repeatMode The repeat mode. (optional)
     * @param isPaused Indicates if the player is paused. (optional, default to false)
     * @param isMuted Indicates if the player is muted. (optional, default to false)
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
     *                        <td>Play progress recorded.</td>
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
    public void onPlaybackProgress(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer volumeLevel,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode,
            @org.eclipse.jdt.annotation.NonNull Boolean isPaused, @org.eclipse.jdt.annotation.NonNull Boolean isMuted)
            throws ApiException {
        onPlaybackProgressWithHttpInfo(itemId, mediaSourceId, positionTicks, audioStreamIndex, subtitleStreamIndex,
                volumeLevel, playMethod, liveStreamId, playSessionId, repeatMode, isPaused, isMuted);
    }

    /**
     * Reports a session&#39;s playback progress.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param positionTicks Optional. The current position, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param volumeLevel Scale of 0-100. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param repeatMode The repeat mode. (optional)
     * @param isPaused Indicates if the player is paused. (optional, default to false)
     * @param isMuted Indicates if the player is muted. (optional, default to false)
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
     *                        <td>Play progress recorded.</td>
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
    public ApiResponse<Void> onPlaybackProgressWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer volumeLevel,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode,
            @org.eclipse.jdt.annotation.NonNull Boolean isPaused, @org.eclipse.jdt.annotation.NonNull Boolean isMuted)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling onPlaybackProgress");
        }

        // Path parameters
        String localVarPath = "/PlayingItems/{itemId}/Progress".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "positionTicks", positionTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "volumeLevel", volumeLevel));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playMethod", playMethod));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "repeatMode", repeatMode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isPaused", isPaused));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMuted", isMuted));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaystateApi.onPlaybackProgress", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param canSeek Indicates if the client can seek. (optional, default to false)
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
     *                        <td>Play start recorded.</td>
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
    public void onPlaybackStart(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Boolean canSeek) throws ApiException {
        onPlaybackStartWithHttpInfo(itemId, mediaSourceId, audioStreamIndex, subtitleStreamIndex, playMethod,
                liveStreamId, playSessionId, canSeek);
    }

    /**
     * Reports that a session has begun playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param playMethod The play method. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param canSeek Indicates if the client can seek. (optional, default to false)
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
     *                        <td>Play start recorded.</td>
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
    public ApiResponse<Void> onPlaybackStartWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull PlayMethod playMethod,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Boolean canSeek) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling onPlaybackStart");
        }

        // Path parameters
        String localVarPath = "/PlayingItems/{itemId}".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playMethod", playMethod));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "canSeek", canSeek));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaystateApi.onPlaybackStart", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param nextMediaType The next media type that will play. (optional)
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
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
     *                        <td>Playback stop recorded.</td>
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
    public void onPlaybackStopped(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String nextMediaType,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId) throws ApiException {
        onPlaybackStoppedWithHttpInfo(itemId, mediaSourceId, nextMediaType, positionTicks, liveStreamId, playSessionId);
    }

    /**
     * Reports that a session has stopped playing an item.
     * 
     * @param itemId Item id. (required)
     * @param mediaSourceId The id of the MediaSource. (optional)
     * @param nextMediaType The next media type that will play. (optional)
     * @param positionTicks Optional. The position, in ticks, where playback stopped. 1 tick &#x3D; 10000 ms. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param playSessionId The play session id. (optional)
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
     *                        <td>Playback stop recorded.</td>
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
    public ApiResponse<Void> onPlaybackStoppedWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String nextMediaType,
            @org.eclipse.jdt.annotation.NonNull Long positionTicks,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling onPlaybackStopped");
        }

        // Path parameters
        String localVarPath = "/PlayingItems/{itemId}".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nextMediaType", nextMediaType));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "positionTicks", positionTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaystateApi.onPlaybackStopped", localVarPath, "DELETE", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Pings a playback session.
     * 
     * @param playSessionId Playback session id. (required)
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
     *                        <td>Playback session pinged.</td>
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
    public void pingPlaybackSession(@org.eclipse.jdt.annotation.Nullable String playSessionId) throws ApiException {
        pingPlaybackSessionWithHttpInfo(playSessionId);
    }

    /**
     * Pings a playback session.
     * 
     * @param playSessionId Playback session id. (required)
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
     *                        <td>Playback session pinged.</td>
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
    public ApiResponse<Void> pingPlaybackSessionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String playSessionId)
            throws ApiException {
        // Check required parameters
        if (playSessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playSessionId' when calling pingPlaybackSession");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "playSessionId", playSessionId));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaystateApi.pingPlaybackSession", "/Sessions/Playing/Ping", "POST",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Reports playback progress within a session.
     * 
     * @param playbackProgressInfo The playback progress info. (optional)
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
     *                        <td>Playback progress recorded.</td>
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
    public void reportPlaybackProgress(@org.eclipse.jdt.annotation.NonNull PlaybackProgressInfo playbackProgressInfo)
            throws ApiException {
        reportPlaybackProgressWithHttpInfo(playbackProgressInfo);
    }

    /**
     * Reports playback progress within a session.
     * 
     * @param playbackProgressInfo The playback progress info. (optional)
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
     *                        <td>Playback progress recorded.</td>
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
    public ApiResponse<Void> reportPlaybackProgressWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackProgressInfo playbackProgressInfo) throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaystateApi.reportPlaybackProgress", "/Sessions/Playing/Progress", "POST",
                new ArrayList<>(), playbackProgressInfo, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Reports playback has started within a session.
     * 
     * @param playbackStartInfo The playback start info. (optional)
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
     *                        <td>Playback start recorded.</td>
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
    public void reportPlaybackStart(@org.eclipse.jdt.annotation.NonNull PlaybackStartInfo playbackStartInfo)
            throws ApiException {
        reportPlaybackStartWithHttpInfo(playbackStartInfo);
    }

    /**
     * Reports playback has started within a session.
     * 
     * @param playbackStartInfo The playback start info. (optional)
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
     *                        <td>Playback start recorded.</td>
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
    public ApiResponse<Void> reportPlaybackStartWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackStartInfo playbackStartInfo) throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaystateApi.reportPlaybackStart", "/Sessions/Playing", "POST", new ArrayList<>(),
                playbackStartInfo, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * @param playbackStopInfo The playback stop info. (optional)
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
     *                        <td>Playback stop recorded.</td>
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
    public void reportPlaybackStopped(@org.eclipse.jdt.annotation.NonNull PlaybackStopInfo playbackStopInfo)
            throws ApiException {
        reportPlaybackStoppedWithHttpInfo(playbackStopInfo);
    }

    /**
     * Reports playback has stopped within a session.
     * 
     * @param playbackStopInfo The playback stop info. (optional)
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
     *                        <td>Playback stop recorded.</td>
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
    public ApiResponse<Void> reportPlaybackStoppedWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PlaybackStopInfo playbackStopInfo) throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("PlaystateApi.reportPlaybackStopped", "/Sessions/Playing/Stopped", "POST",
                new ArrayList<>(), playbackStopInfo, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }
}
