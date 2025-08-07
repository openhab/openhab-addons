package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.LiveStreamResponse;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.OpenLiveStreamDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaybackInfoResponse;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaInfoApi {
    private ApiClient apiClient;

    public MediaInfoApi() {
        this(Configuration.getDefaultApiClient());
    }

    public MediaInfoApi(ApiClient apiClient) {
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
     * Closes a media source.
     * 
     * @param liveStreamId The livestream id. (required)
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
     *                        <td>Livestream closed.</td>
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
    public void closeLiveStream(@org.eclipse.jdt.annotation.Nullable String liveStreamId) throws ApiException {
        closeLiveStreamWithHttpInfo(liveStreamId);
    }

    /**
     * Closes a media source.
     * 
     * @param liveStreamId The livestream id. (required)
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
     *                        <td>Livestream closed.</td>
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
    public ApiResponse<Void> closeLiveStreamWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String liveStreamId)
            throws ApiException {
        // Check required parameters
        if (liveStreamId == null) {
            throw new ApiException(400, "Missing the required parameter 'liveStreamId' when calling closeLiveStream");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("MediaInfoApi.closeLiveStream", "/LiveStreams/Close", "POST", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * @param size The bitrate. Defaults to 102400. (optional, default to 102400)
     * @return File
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
     *                        <td>Test buffer returned.</td>
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
    public File getBitrateTestBytes(@org.eclipse.jdt.annotation.NonNull Integer size) throws ApiException {
        return getBitrateTestBytesWithHttpInfo(size).getData();
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * @param size The bitrate. Defaults to 102400. (optional, default to 102400)
     * @return ApiResponse&lt;File&gt;
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
     *                        <td>Test buffer returned.</td>
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
    public ApiResponse<File> getBitrateTestBytesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull Integer size)
            throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "size", size));

        String localVarAccept = apiClient.selectHeaderAccept("application/octet-stream");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("MediaInfoApi.getBitrateTestBytes", "/Playback/BitrateTest", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets live playback media info for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @return PlaybackInfoResponse
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
     *                        <td>Playback info returned.</td>
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
    public PlaybackInfoResponse getPlaybackInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getPlaybackInfoWithHttpInfo(itemId, userId).getData();
    }

    /**
     * Gets live playback media info for an item.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @return ApiResponse&lt;PlaybackInfoResponse&gt;
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
     *                        <td>Playback info returned.</td>
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
    public ApiResponse<PlaybackInfoResponse> getPlaybackInfoWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getPlaybackInfo");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/PlaybackInfo".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<PlaybackInfoResponse> localVarReturnType = new GenericType<PlaybackInfoResponse>() {
        };
        return apiClient.invokeAPI("MediaInfoApi.getPlaybackInfo", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param liveStreamId The livestream id. (optional)
     * @param autoOpenLiveStream Whether to auto open the livestream. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param enableTranscoding Whether to enable transcoding. Default: true. (optional)
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true. (optional)
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true. (optional)
     * @param playbackInfoDto The playback info. (optional)
     * @return PlaybackInfoResponse
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
     *                        <td>Playback info returned.</td>
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
    public PlaybackInfoResponse getPostedPlaybackInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean autoOpenLiveStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTranscoding,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull PlaybackInfoDto playbackInfoDto) throws ApiException {
        return getPostedPlaybackInfoWithHttpInfo(itemId, userId, maxStreamingBitrate, startTimeTicks, audioStreamIndex,
                subtitleStreamIndex, maxAudioChannels, mediaSourceId, liveStreamId, autoOpenLiveStream,
                enableDirectPlay, enableDirectStream, enableTranscoding, allowVideoStreamCopy, allowAudioStreamCopy,
                playbackInfoDto).getData();
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * 
     * @param itemId The item id. (required)
     * @param userId The user id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param liveStreamId The livestream id. (optional)
     * @param autoOpenLiveStream Whether to auto open the livestream. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param enableTranscoding Whether to enable transcoding. Default: true. (optional)
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true. (optional)
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true. (optional)
     * @param playbackInfoDto The playback info. (optional)
     * @return ApiResponse&lt;PlaybackInfoResponse&gt;
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
     *                        <td>Playback info returned.</td>
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
    public ApiResponse<PlaybackInfoResponse> getPostedPlaybackInfoWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean autoOpenLiveStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTranscoding,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull PlaybackInfoDto playbackInfoDto) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getPostedPlaybackInfo");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/PlaybackInfo".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "autoOpenLiveStream", autoOpenLiveStream));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableDirectPlay", enableDirectPlay));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableDirectStream", enableDirectStream));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTranscoding", enableTranscoding));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowAudioStreamCopy", allowAudioStreamCopy));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<PlaybackInfoResponse> localVarReturnType = new GenericType<PlaybackInfoResponse>() {
        };
        return apiClient.invokeAPI("MediaInfoApi.getPostedPlaybackInfo", localVarPath, "POST", localVarQueryParams,
                playbackInfoDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Opens a media source.
     * 
     * @param openToken The open token. (optional)
     * @param userId The user id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param itemId The item id. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param alwaysBurnInSubtitleWhenTranscoding Always burn-in subtitle when transcoding. (optional)
     * @param openLiveStreamDto The open live stream dto. (optional)
     * @return LiveStreamResponse
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
     *                        <td>Media source opened.</td>
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
    public LiveStreamResponse openLiveStream(@org.eclipse.jdt.annotation.NonNull String openToken,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding,
            @org.eclipse.jdt.annotation.NonNull OpenLiveStreamDto openLiveStreamDto) throws ApiException {
        return openLiveStreamWithHttpInfo(openToken, userId, playSessionId, maxStreamingBitrate, startTimeTicks,
                audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId, enableDirectPlay, enableDirectStream,
                alwaysBurnInSubtitleWhenTranscoding, openLiveStreamDto).getData();
    }

    /**
     * Opens a media source.
     * 
     * @param openToken The open token. (optional)
     * @param userId The user id. (optional)
     * @param playSessionId The play session id. (optional)
     * @param maxStreamingBitrate The maximum streaming bitrate. (optional)
     * @param startTimeTicks The start time in ticks. (optional)
     * @param audioStreamIndex The audio stream index. (optional)
     * @param subtitleStreamIndex The subtitle stream index. (optional)
     * @param maxAudioChannels The maximum number of audio channels. (optional)
     * @param itemId The item id. (optional)
     * @param enableDirectPlay Whether to enable direct play. Default: true. (optional)
     * @param enableDirectStream Whether to enable direct stream. Default: true. (optional)
     * @param alwaysBurnInSubtitleWhenTranscoding Always burn-in subtitle when transcoding. (optional)
     * @param openLiveStreamDto The open live stream dto. (optional)
     * @return ApiResponse&lt;LiveStreamResponse&gt;
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
     *                        <td>Media source opened.</td>
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
    public ApiResponse<LiveStreamResponse> openLiveStreamWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String openToken, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding,
            @org.eclipse.jdt.annotation.NonNull OpenLiveStreamDto openLiveStreamDto) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "openToken", openToken));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "itemId", itemId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableDirectPlay", enableDirectPlay));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableDirectStream", enableDirectStream));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "alwaysBurnInSubtitleWhenTranscoding",
                alwaysBurnInSubtitleWhenTranscoding));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<LiveStreamResponse> localVarReturnType = new GenericType<LiveStreamResponse>() {
        };
        return apiClient.invokeAPI("MediaInfoApi.openLiveStream", "/LiveStreams/Open", "POST", localVarQueryParams,
                openLiveStreamDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
