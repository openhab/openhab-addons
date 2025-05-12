package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.EncodingContext;
import java.io.File;
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
public class AudioApi {
    private ApiClient apiClient;

    public AudioApi() {
        this(new ApiClient());
    }

    @Autowired
    public AudioApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    private ResponseSpec getAudioStreamRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getAudioStream", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/stream", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    public Mono<File> getAudioStream(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getAudioStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    public Mono<ResponseEntity<File>> getAudioStreamWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getAudioStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    public ResponseSpec getAudioStreamWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return getAudioStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    private ResponseSpec getAudioStreamByContainerRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getAudioStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new WebClientResponseException("Missing the required parameter 'container' when calling getAudioStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/stream.{container}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    public Mono<File> getAudioStreamByContainer(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getAudioStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    public Mono<ResponseEntity<File>> getAudioStreamByContainerWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getAudioStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    public ResponseSpec getAudioStreamByContainerWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return getAudioStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    private ResponseSpec headAudioStreamRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headAudioStream", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/stream", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    public Mono<File> headAudioStream(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headAudioStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    public Mono<ResponseEntity<File>> headAudioStreamWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headAudioStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
    public ResponseSpec headAudioStreamWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return headAudioStreamRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    private ResponseSpec headAudioStreamByContainerRequestCreation(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headAudioStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new WebClientResponseException("Missing the required parameter 'container' when calling headAudioStreamByContainer", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/stream.{container}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    public Mono<File> headAudioStreamByContainer(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headAudioStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    public Mono<ResponseEntity<File>> headAudioStreamByContainerWithHttpInfo(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headAudioStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding).toEntity(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * @param itemId The item id.
     * @param container The audio container.
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
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
     * @param subtitleMethod Optional. Specify the subtitle delivery method.
     * @param maxRefFrames Optional.
     * @param maxVideoBitDepth Optional. The maximum video bit depth.
     * @param requireAvc Optional. Whether to require avc.
     * @param deInterlace Optional. Whether to deinterlace the video.
     * @param requireNonAnamorphic Optional. Whether to require a non anamporphic stream.
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
    public ResponseSpec headAudioStreamByContainerWithResponseSpec(UUID itemId, String container, Boolean _static, String params, String tag, String deviceProfileId, String playSessionId, String segmentContainer, Integer segmentLength, Integer minSegments, String mediaSourceId, String deviceId, String audioCodec, Boolean enableAutoStreamCopy, Boolean allowVideoStreamCopy, Boolean allowAudioStreamCopy, Boolean breakOnNonKeyFrames, Integer audioSampleRate, Integer maxAudioBitDepth, Integer audioBitRate, Integer audioChannels, Integer maxAudioChannels, String profile, String level, Float framerate, Float maxFramerate, Boolean copyTimestamps, Long startTimeTicks, Integer width, Integer height, Integer videoBitRate, Integer subtitleStreamIndex, SubtitleDeliveryMethod subtitleMethod, Integer maxRefFrames, Integer maxVideoBitDepth, Boolean requireAvc, Boolean deInterlace, Boolean requireNonAnamorphic, Integer transcodingMaxAudioChannels, Integer cpuCoreLimit, String liveStreamId, Boolean enableMpegtsM2TsMode, String videoCodec, String subtitleCodec, String transcodeReasons, Integer audioStreamIndex, Integer videoStreamIndex, EncodingContext context, Map<String, String> streamOptions, Boolean enableAudioVbrEncoding) throws WebClientResponseException {
        return headAudioStreamByContainerRequestCreation(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
    }
}
