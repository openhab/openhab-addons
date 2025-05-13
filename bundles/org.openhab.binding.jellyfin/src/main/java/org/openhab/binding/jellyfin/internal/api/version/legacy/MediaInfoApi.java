package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.LiveStreamResponse;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.OpenLiveStreamDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.PlaybackInfoDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.PlaybackInfoResponse;
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
public class MediaInfoApi {
    private ApiClient apiClient;

    public MediaInfoApi() {
        this(new ApiClient());
    }

    @Autowired
    public MediaInfoApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Closes a media source.
     * 
     * <p>
     * <b>204</b> - Livestream closed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param liveStreamId The livestream id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec closeLiveStreamRequestCreation(String liveStreamId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'liveStreamId' is set
        if (liveStreamId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'liveStreamId' when calling closeLiveStream",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/LiveStreams/Close", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Closes a media source.
     * 
     * <p>
     * <b>204</b> - Livestream closed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param liveStreamId The livestream id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> closeLiveStream(String liveStreamId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return closeLiveStreamRequestCreation(liveStreamId).bodyToMono(localVarReturnType);
    }

    /**
     * Closes a media source.
     * 
     * <p>
     * <b>204</b> - Livestream closed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param liveStreamId The livestream id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> closeLiveStreamWithHttpInfo(String liveStreamId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return closeLiveStreamRequestCreation(liveStreamId).toEntity(localVarReturnType);
    }

    /**
     * Closes a media source.
     * 
     * <p>
     * <b>204</b> - Livestream closed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param liveStreamId The livestream id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec closeLiveStreamWithResponseSpec(String liveStreamId) throws WebClientResponseException {
        return closeLiveStreamRequestCreation(liveStreamId);
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * <p>
     * <b>200</b> - Test buffer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param size The bitrate. Defaults to 102400.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getBitrateTestBytesRequestCreation(Integer size) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "size", size));

        final String[] localVarAccepts = { "application/octet-stream" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Playback/BitrateTest", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * <p>
     * <b>200</b> - Test buffer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param size The bitrate. Defaults to 102400.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getBitrateTestBytes(Integer size) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getBitrateTestBytesRequestCreation(size).bodyToMono(localVarReturnType);
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * <p>
     * <b>200</b> - Test buffer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param size The bitrate. Defaults to 102400.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getBitrateTestBytesWithHttpInfo(Integer size) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getBitrateTestBytesRequestCreation(size).toEntity(localVarReturnType);
    }

    /**
     * Tests the network with a request with the size of the bitrate.
     * 
     * <p>
     * <b>200</b> - Test buffer returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param size The bitrate. Defaults to 102400.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getBitrateTestBytesWithResponseSpec(Integer size) throws WebClientResponseException {
        return getBitrateTestBytesRequestCreation(size);
    }

    /**
     * Gets live playback media info for an item.
     * 
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @return PlaybackInfoResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPlaybackInfoRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getPlaybackInfo",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getPlaybackInfo",
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

        ParameterizedTypeReference<PlaybackInfoResponse> localVarReturnType = new ParameterizedTypeReference<PlaybackInfoResponse>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/PlaybackInfo", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets live playback media info for an item.
     * 
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @return PlaybackInfoResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PlaybackInfoResponse> getPlaybackInfo(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<PlaybackInfoResponse> localVarReturnType = new ParameterizedTypeReference<PlaybackInfoResponse>() {
        };
        return getPlaybackInfoRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets live playback media info for an item.
     * 
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @return ResponseEntity&lt;PlaybackInfoResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PlaybackInfoResponse>> getPlaybackInfoWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<PlaybackInfoResponse> localVarReturnType = new ParameterizedTypeReference<PlaybackInfoResponse>() {
        };
        return getPlaybackInfoRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets live playback media info for an item.
     * 
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPlaybackInfoWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return getPlaybackInfoRequestCreation(itemId, userId);
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param mediaSourceId The media source id.
     * @param liveStreamId The livestream id.
     * @param autoOpenLiveStream Whether to auto open the livestream.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param enableTranscoding Whether to enable transcoding. Default: true.
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true.
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true.
     * @param playbackInfoDto The playback info.
     * @return PlaybackInfoResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPostedPlaybackInfoRequestCreation(UUID itemId, UUID userId, Integer maxStreamingBitrate,
            Long startTimeTicks, Integer audioStreamIndex, Integer subtitleStreamIndex, Integer maxAudioChannels,
            String mediaSourceId, String liveStreamId, Boolean autoOpenLiveStream, Boolean enableDirectPlay,
            Boolean enableDirectStream, Boolean enableTranscoding, Boolean allowVideoStreamCopy,
            Boolean allowAudioStreamCopy, PlaybackInfoDto playbackInfoDto) throws WebClientResponseException {
        Object postBody = playbackInfoDto;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling getPostedPlaybackInfo",
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
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxStreamingBitrate", maxStreamingBitrate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "autoOpenLiveStream", autoOpenLiveStream));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableDirectPlay", enableDirectPlay));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableDirectStream", enableDirectStream));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTranscoding", enableTranscoding));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowVideoStreamCopy", allowVideoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowAudioStreamCopy", allowAudioStreamCopy));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<PlaybackInfoResponse> localVarReturnType = new ParameterizedTypeReference<PlaybackInfoResponse>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/PlaybackInfo", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param mediaSourceId The media source id.
     * @param liveStreamId The livestream id.
     * @param autoOpenLiveStream Whether to auto open the livestream.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param enableTranscoding Whether to enable transcoding. Default: true.
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true.
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true.
     * @param playbackInfoDto The playback info.
     * @return PlaybackInfoResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PlaybackInfoResponse> getPostedPlaybackInfo(UUID itemId, UUID userId, Integer maxStreamingBitrate,
            Long startTimeTicks, Integer audioStreamIndex, Integer subtitleStreamIndex, Integer maxAudioChannels,
            String mediaSourceId, String liveStreamId, Boolean autoOpenLiveStream, Boolean enableDirectPlay,
            Boolean enableDirectStream, Boolean enableTranscoding, Boolean allowVideoStreamCopy,
            Boolean allowAudioStreamCopy, PlaybackInfoDto playbackInfoDto) throws WebClientResponseException {
        ParameterizedTypeReference<PlaybackInfoResponse> localVarReturnType = new ParameterizedTypeReference<PlaybackInfoResponse>() {
        };
        return getPostedPlaybackInfoRequestCreation(itemId, userId, maxStreamingBitrate, startTimeTicks,
                audioStreamIndex, subtitleStreamIndex, maxAudioChannels, mediaSourceId, liveStreamId,
                autoOpenLiveStream, enableDirectPlay, enableDirectStream, enableTranscoding, allowVideoStreamCopy,
                allowAudioStreamCopy, playbackInfoDto).bodyToMono(localVarReturnType);
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param mediaSourceId The media source id.
     * @param liveStreamId The livestream id.
     * @param autoOpenLiveStream Whether to auto open the livestream.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param enableTranscoding Whether to enable transcoding. Default: true.
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true.
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true.
     * @param playbackInfoDto The playback info.
     * @return ResponseEntity&lt;PlaybackInfoResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PlaybackInfoResponse>> getPostedPlaybackInfoWithHttpInfo(UUID itemId, UUID userId,
            Integer maxStreamingBitrate, Long startTimeTicks, Integer audioStreamIndex, Integer subtitleStreamIndex,
            Integer maxAudioChannels, String mediaSourceId, String liveStreamId, Boolean autoOpenLiveStream,
            Boolean enableDirectPlay, Boolean enableDirectStream, Boolean enableTranscoding,
            Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, PlaybackInfoDto playbackInfoDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<PlaybackInfoResponse> localVarReturnType = new ParameterizedTypeReference<PlaybackInfoResponse>() {
        };
        return getPostedPlaybackInfoRequestCreation(itemId, userId, maxStreamingBitrate, startTimeTicks,
                audioStreamIndex, subtitleStreamIndex, maxAudioChannels, mediaSourceId, liveStreamId,
                autoOpenLiveStream, enableDirectPlay, enableDirectStream, enableTranscoding, allowVideoStreamCopy,
                allowAudioStreamCopy, playbackInfoDto).toEntity(localVarReturnType);
    }

    /**
     * Gets live playback media info for an item.
     * For backwards compatibility parameters can be sent via Query or Body, with Query having higher precedence. Query
     * parameters are obsolete.
     * <p>
     * <b>200</b> - Playback info returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param userId The user id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param mediaSourceId The media source id.
     * @param liveStreamId The livestream id.
     * @param autoOpenLiveStream Whether to auto open the livestream.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param enableTranscoding Whether to enable transcoding. Default: true.
     * @param allowVideoStreamCopy Whether to allow to copy the video stream. Default: true.
     * @param allowAudioStreamCopy Whether to allow to copy the audio stream. Default: true.
     * @param playbackInfoDto The playback info.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPostedPlaybackInfoWithResponseSpec(UUID itemId, UUID userId, Integer maxStreamingBitrate,
            Long startTimeTicks, Integer audioStreamIndex, Integer subtitleStreamIndex, Integer maxAudioChannels,
            String mediaSourceId, String liveStreamId, Boolean autoOpenLiveStream, Boolean enableDirectPlay,
            Boolean enableDirectStream, Boolean enableTranscoding, Boolean allowVideoStreamCopy,
            Boolean allowAudioStreamCopy, PlaybackInfoDto playbackInfoDto) throws WebClientResponseException {
        return getPostedPlaybackInfoRequestCreation(itemId, userId, maxStreamingBitrate, startTimeTicks,
                audioStreamIndex, subtitleStreamIndex, maxAudioChannels, mediaSourceId, liveStreamId,
                autoOpenLiveStream, enableDirectPlay, enableDirectStream, enableTranscoding, allowVideoStreamCopy,
                allowAudioStreamCopy, playbackInfoDto);
    }

    /**
     * Opens a media source.
     * 
     * <p>
     * <b>200</b> - Media source opened.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param openToken The open token.
     * @param userId The user id.
     * @param playSessionId The play session id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param itemId The item id.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param openLiveStreamDto The open live stream dto.
     * @return LiveStreamResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec openLiveStreamRequestCreation(String openToken, UUID userId, String playSessionId,
            Integer maxStreamingBitrate, Long startTimeTicks, Integer audioStreamIndex, Integer subtitleStreamIndex,
            Integer maxAudioChannels, UUID itemId, Boolean enableDirectPlay, Boolean enableDirectStream,
            OpenLiveStreamDto openLiveStreamDto) throws WebClientResponseException {
        Object postBody = openLiveStreamDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "openToken", openToken));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxStreamingBitrate", maxStreamingBitrate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemId", itemId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableDirectPlay", enableDirectPlay));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableDirectStream", enableDirectStream));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<LiveStreamResponse> localVarReturnType = new ParameterizedTypeReference<LiveStreamResponse>() {
        };
        return apiClient.invokeAPI("/LiveStreams/Open", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Opens a media source.
     * 
     * <p>
     * <b>200</b> - Media source opened.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param openToken The open token.
     * @param userId The user id.
     * @param playSessionId The play session id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param itemId The item id.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param openLiveStreamDto The open live stream dto.
     * @return LiveStreamResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<LiveStreamResponse> openLiveStream(String openToken, UUID userId, String playSessionId,
            Integer maxStreamingBitrate, Long startTimeTicks, Integer audioStreamIndex, Integer subtitleStreamIndex,
            Integer maxAudioChannels, UUID itemId, Boolean enableDirectPlay, Boolean enableDirectStream,
            OpenLiveStreamDto openLiveStreamDto) throws WebClientResponseException {
        ParameterizedTypeReference<LiveStreamResponse> localVarReturnType = new ParameterizedTypeReference<LiveStreamResponse>() {
        };
        return openLiveStreamRequestCreation(openToken, userId, playSessionId, maxStreamingBitrate, startTimeTicks,
                audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId, enableDirectPlay, enableDirectStream,
                openLiveStreamDto).bodyToMono(localVarReturnType);
    }

    /**
     * Opens a media source.
     * 
     * <p>
     * <b>200</b> - Media source opened.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param openToken The open token.
     * @param userId The user id.
     * @param playSessionId The play session id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param itemId The item id.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param openLiveStreamDto The open live stream dto.
     * @return ResponseEntity&lt;LiveStreamResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<LiveStreamResponse>> openLiveStreamWithHttpInfo(String openToken, UUID userId,
            String playSessionId, Integer maxStreamingBitrate, Long startTimeTicks, Integer audioStreamIndex,
            Integer subtitleStreamIndex, Integer maxAudioChannels, UUID itemId, Boolean enableDirectPlay,
            Boolean enableDirectStream, OpenLiveStreamDto openLiveStreamDto) throws WebClientResponseException {
        ParameterizedTypeReference<LiveStreamResponse> localVarReturnType = new ParameterizedTypeReference<LiveStreamResponse>() {
        };
        return openLiveStreamRequestCreation(openToken, userId, playSessionId, maxStreamingBitrate, startTimeTicks,
                audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId, enableDirectPlay, enableDirectStream,
                openLiveStreamDto).toEntity(localVarReturnType);
    }

    /**
     * Opens a media source.
     * 
     * <p>
     * <b>200</b> - Media source opened.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param openToken The open token.
     * @param userId The user id.
     * @param playSessionId The play session id.
     * @param maxStreamingBitrate The maximum streaming bitrate.
     * @param startTimeTicks The start time in ticks.
     * @param audioStreamIndex The audio stream index.
     * @param subtitleStreamIndex The subtitle stream index.
     * @param maxAudioChannels The maximum number of audio channels.
     * @param itemId The item id.
     * @param enableDirectPlay Whether to enable direct play. Default: true.
     * @param enableDirectStream Whether to enable direct stream. Default: true.
     * @param openLiveStreamDto The open live stream dto.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec openLiveStreamWithResponseSpec(String openToken, UUID userId, String playSessionId,
            Integer maxStreamingBitrate, Long startTimeTicks, Integer audioStreamIndex, Integer subtitleStreamIndex,
            Integer maxAudioChannels, UUID itemId, Boolean enableDirectPlay, Boolean enableDirectStream,
            OpenLiveStreamDto openLiveStreamDto) throws WebClientResponseException {
        return openLiveStreamRequestCreation(openToken, userId, playSessionId, maxStreamingBitrate, startTimeTicks,
                audioStreamIndex, subtitleStreamIndex, maxAudioChannels, itemId, enableDirectPlay, enableDirectStream,
                openLiveStreamDto);
    }
}
