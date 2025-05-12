package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.EncodingContext;
import java.io.File;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SubtitleDeliveryMethod;
import java.util.UUID;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class VideosApi {
    private ApiClient apiClient;

    public VideosApi() {
        this(new ApiClient());
    }

    @Autowired
    public VideosApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Removes alternate video sources.
     * 
     * <p><b>204</b> - Alternate sources deleted.
     * <p><b>404</b> - Video not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteAlternateSourcesRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling deleteAlternateSources", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/AlternateSources", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Removes alternate video sources.
     * 
     * <p><b>204</b> - Alternate sources deleted.
     * <p><b>404</b> - Video not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteAlternateSources(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteAlternateSourcesRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Removes alternate video sources.
     * 
     * <p><b>204</b> - Alternate sources deleted.
     * <p><b>404</b> - Video not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteAlternateSourcesWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteAlternateSourcesRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Removes alternate video sources.
     * 
     * <p><b>204</b> - Alternate sources deleted.
     * <p><b>404</b> - Video not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteAlternateSourcesWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return deleteAlternateSourcesRequestCreation(itemId);
    }

    /**
     * Gets additional parts for a video.
     * 
     * <p><b>200</b> - Additional parts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAdditionalPartRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getAdditionalPart", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/AdditionalParts", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets additional parts for a video.
     * 
     * <p><b>200</b> - Additional parts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getAdditionalPart(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getAdditionalPartRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets additional parts for a video.
     * 
     * <p><b>200</b> - Additional parts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getAdditionalPartWithHttpInfo(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getAdditionalPartRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets additional parts for a video.
     * 
     * <p><b>200</b> - Additional parts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAdditionalPartWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return getAdditionalPartRequestCreation(itemId, userId);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getVideoStreamRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getVideoStream", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "container", container));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "static", _static));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "params", params));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceProfileId", deviceProfileId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentContainer", segmentContainer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentLength", segmentLength));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minSegments", minSegments));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioCodec", audioCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAutoStreamCopy", enableAutoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowVideoStreamCopy", allowVideoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowAudioStreamCopy", allowAudioStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "breakOnNonKeyFrames", breakOnNonKeyFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioSampleRate", audioSampleRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioBitDepth", maxAudioBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioBitRate", audioBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioChannels", audioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "profile", profile));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "level", level));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "framerate", framerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxFramerate", maxFramerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "copyTimestamps", copyTimestamps));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoBitRate", videoBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleMethod", subtitleMethod));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxRefFrames", maxRefFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxVideoBitDepth", maxVideoBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireAvc", requireAvc));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deInterlace", deInterlace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireNonAnamorphic", requireNonAnamorphic));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cpuCoreLimit", cpuCoreLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoCodec", videoCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleCodec", subtitleCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodeReasons", transcodeReasons));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoStreamIndex", videoStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "context", context));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "streamOptions", streamOptions));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAudioVbrEncoding", enableAudioVbrEncoding));
        
        final String[] localVarAccepts = { 
            "video/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/stream", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getVideoStream(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getVideoStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getVideoStreamWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getVideoStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getVideoStreamWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return getVideoStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getVideoStreamByContainerRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getVideoStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new WebClientResponseException("Missing the required parameter 'container' when calling getVideoStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("container", container);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "static", _static));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "params", params));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceProfileId", deviceProfileId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentContainer", segmentContainer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentLength", segmentLength));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minSegments", minSegments));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioCodec", audioCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAutoStreamCopy", enableAutoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowVideoStreamCopy", allowVideoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowAudioStreamCopy", allowAudioStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "breakOnNonKeyFrames", breakOnNonKeyFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioSampleRate", audioSampleRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioBitDepth", maxAudioBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioBitRate", audioBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioChannels", audioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "profile", profile));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "level", level));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "framerate", framerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxFramerate", maxFramerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "copyTimestamps", copyTimestamps));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoBitRate", videoBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleMethod", subtitleMethod));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxRefFrames", maxRefFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxVideoBitDepth", maxVideoBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireAvc", requireAvc));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deInterlace", deInterlace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireNonAnamorphic", requireNonAnamorphic));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cpuCoreLimit", cpuCoreLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoCodec", videoCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleCodec", subtitleCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodeReasons", transcodeReasons));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoStreamIndex", videoStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "context", context));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "streamOptions", streamOptions));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAudioVbrEncoding", enableAudioVbrEncoding));
        
        final String[] localVarAccepts = { 
            "video/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/stream.{container}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getVideoStreamByContainer(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getVideoStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getVideoStreamByContainerWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getVideoStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getVideoStreamByContainerWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return getVideoStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headVideoStreamRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headVideoStream", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "container", container));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "static", _static));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "params", params));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceProfileId", deviceProfileId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentContainer", segmentContainer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentLength", segmentLength));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minSegments", minSegments));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioCodec", audioCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAutoStreamCopy", enableAutoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowVideoStreamCopy", allowVideoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowAudioStreamCopy", allowAudioStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "breakOnNonKeyFrames", breakOnNonKeyFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioSampleRate", audioSampleRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioBitDepth", maxAudioBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioBitRate", audioBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioChannels", audioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "profile", profile));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "level", level));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "framerate", framerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxFramerate", maxFramerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "copyTimestamps", copyTimestamps));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoBitRate", videoBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleMethod", subtitleMethod));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxRefFrames", maxRefFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxVideoBitDepth", maxVideoBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireAvc", requireAvc));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deInterlace", deInterlace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireNonAnamorphic", requireNonAnamorphic));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cpuCoreLimit", cpuCoreLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoCodec", videoCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleCodec", subtitleCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodeReasons", transcodeReasons));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoStreamIndex", videoStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "context", context));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "streamOptions", streamOptions));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAudioVbrEncoding", enableAudioVbrEncoding));
        
        final String[] localVarAccepts = { 
            "video/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/stream", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headVideoStream(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headVideoStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headVideoStreamWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headVideoStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headVideoStreamWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return headVideoStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headVideoStreamByContainerRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headVideoStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new WebClientResponseException("Missing the required parameter 'container' when calling headVideoStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("container", container);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "static", _static));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "params", params));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceProfileId", deviceProfileId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentContainer", segmentContainer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentLength", segmentLength));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minSegments", minSegments));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioCodec", audioCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAutoStreamCopy", enableAutoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowVideoStreamCopy", allowVideoStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "allowAudioStreamCopy", allowAudioStreamCopy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "breakOnNonKeyFrames", breakOnNonKeyFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioSampleRate", audioSampleRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioBitDepth", maxAudioBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioBitRate", audioBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioChannels", audioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "profile", profile));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "level", level));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "framerate", framerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxFramerate", maxFramerate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "copyTimestamps", copyTimestamps));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoBitRate", videoBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleMethod", subtitleMethod));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxRefFrames", maxRefFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxVideoBitDepth", maxVideoBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireAvc", requireAvc));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deInterlace", deInterlace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "requireNonAnamorphic", requireNonAnamorphic));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cpuCoreLimit", cpuCoreLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "liveStreamId", liveStreamId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoCodec", videoCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleCodec", subtitleCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodeReasons", transcodeReasons));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "videoStreamIndex", videoStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "context", context));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "streamOptions", streamOptions));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableAudioVbrEncoding", enableAudioVbrEncoding));
        
        final String[] localVarAccepts = { 
            "video/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/stream.{container}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headVideoStreamByContainer(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headVideoStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headVideoStreamByContainerWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headVideoStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets a video stream.
     * 
     * <p><b>200</b> - Video stream returned.
     * @param itemId The item id.
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
     * @param params The streaming parameters.
     * @param tag The tag.
     * @param deviceProfileId Optional. The dlna device profile id to utilize.
     * @param playSessionId The play session id.
     * @param segmentContainer The segment container.
     * @param segmentLength The segment length.
     * @param minSegments The minimum number of segments.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension.
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url.
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2.
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param width Optional. The fixed horizontal resolution of the encoded video.
     * @param height Optional. The fixed vertical resolution of the encoded video.
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video.
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video.
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream.
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode.
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use.
     * @param liveStreamId The live stream id.
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode.
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension.
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to.
     * @param transcodeReasons Optional. The transcoding reason.
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be used.
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext.
     * @param streamOptions Optional. The streaming options.
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headVideoStreamByContainerWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer maxWidth, Integer maxHeight, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return headVideoStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }

    /**
     * Merges videos into a single record.
     * 
     * <p><b>204</b> - Videos merged.
     * <p><b>400</b> - Supply at least 2 video ids.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param ids Item id list. This allows multiple, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec mergeVersionsRequestCreation(List<UUID> ids) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'ids' is set
        if (ids == null) {
            throw new WebClientResponseException("Missing the required parameter 'ids' when calling mergeVersions", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Videos/MergeVersions", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Merges videos into a single record.
     * 
     * <p><b>204</b> - Videos merged.
     * <p><b>400</b> - Supply at least 2 video ids.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param ids Item id list. This allows multiple, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> mergeVersions(List<UUID> ids) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return mergeVersionsRequestCreation(ids).bodyToMono(localVarReturnType);
    }

    /**
     * Merges videos into a single record.
     * 
     * <p><b>204</b> - Videos merged.
     * <p><b>400</b> - Supply at least 2 video ids.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param ids Item id list. This allows multiple, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> mergeVersionsWithHttpInfo(List<UUID> ids) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return mergeVersionsRequestCreation(ids).toEntity(localVarReturnType);
    }

    /**
     * Merges videos into a single record.
     * 
     * <p><b>204</b> - Videos merged.
     * <p><b>400</b> - Supply at least 2 video ids.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param ids Item id list. This allows multiple, comma delimited.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec mergeVersionsWithResponseSpec(List<UUID> ids) throws WebClientResponseException {
        return mergeVersionsRequestCreation(ids);
    }
}
