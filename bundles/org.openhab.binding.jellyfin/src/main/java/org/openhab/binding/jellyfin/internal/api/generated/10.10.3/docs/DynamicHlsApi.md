# DynamicHlsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getHlsAudioSegment**](DynamicHlsApi.md#getHlsAudioSegment) | **GET** /Audio/{itemId}/hls1/{playlistId}/{segmentId}.{container} | Gets a video stream using HTTP live streaming. |
| [**getHlsVideoSegment**](DynamicHlsApi.md#getHlsVideoSegment) | **GET** /Videos/{itemId}/hls1/{playlistId}/{segmentId}.{container} | Gets a video stream using HTTP live streaming. |
| [**getLiveHlsStream**](DynamicHlsApi.md#getLiveHlsStream) | **GET** /Videos/{itemId}/live.m3u8 | Gets a hls live stream. |
| [**getMasterHlsAudioPlaylist**](DynamicHlsApi.md#getMasterHlsAudioPlaylist) | **GET** /Audio/{itemId}/master.m3u8 | Gets an audio hls playlist stream. |
| [**getMasterHlsVideoPlaylist**](DynamicHlsApi.md#getMasterHlsVideoPlaylist) | **GET** /Videos/{itemId}/master.m3u8 | Gets a video hls playlist stream. |
| [**getVariantHlsAudioPlaylist**](DynamicHlsApi.md#getVariantHlsAudioPlaylist) | **GET** /Audio/{itemId}/main.m3u8 | Gets an audio stream using HTTP live streaming. |
| [**getVariantHlsVideoPlaylist**](DynamicHlsApi.md#getVariantHlsVideoPlaylist) | **GET** /Videos/{itemId}/main.m3u8 | Gets a video stream using HTTP live streaming. |
| [**headMasterHlsAudioPlaylist**](DynamicHlsApi.md#headMasterHlsAudioPlaylist) | **HEAD** /Audio/{itemId}/master.m3u8 | Gets an audio hls playlist stream. |
| [**headMasterHlsVideoPlaylist**](DynamicHlsApi.md#headMasterHlsVideoPlaylist) | **HEAD** /Videos/{itemId}/master.m3u8 | Gets a video hls playlist stream. |


<a id="getHlsAudioSegment"></a>
# **getHlsAudioSegment**
> File getHlsAudioSegment(itemId, playlistId, segmentId, container, runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding)

Gets a video stream using HTTP live streaming.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String playlistId = "playlistId_example"; // String | The playlist id.
    Integer segmentId = 56; // Integer | The segment id.
    String container = "container_example"; // String | The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
    Long runtimeTicks = 56L; // Long | The position of the requested segment in ticks.
    Long actualSegmentLengthTicks = 56L; // Long | The length of the requested segment in ticks.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer maxStreamingBitrate = 56; // Integer | Optional. The maximum streaming bitrate.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    try {
      File result = apiInstance.getHlsAudioSegment(itemId, playlistId, segmentId, container, runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#getHlsAudioSegment");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **playlistId** | **String**| The playlist id. | |
| **segmentId** | **Integer**| The segment id. | |
| **container** | **String**| The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. | |
| **runtimeTicks** | **Long**| The position of the requested segment in ticks. | |
| **actualSegmentLengthTicks** | **Long**| The length of the requested segment in ticks. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **maxStreamingBitrate** | **Integer**| Optional. The maximum streaming bitrate. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getHlsVideoSegment"></a>
# **getHlsVideoSegment**
> File getHlsVideoSegment(itemId, playlistId, segmentId, container, runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding)

Gets a video stream using HTTP live streaming.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String playlistId = "playlistId_example"; // String | The playlist id.
    Integer segmentId = 56; // Integer | The segment id.
    String container = "container_example"; // String | The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
    Long runtimeTicks = 56L; // Long | The position of the requested segment in ticks.
    Long actualSegmentLengthTicks = 56L; // Long | The length of the requested segment in ticks.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The desired segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer maxWidth = 56; // Integer | Optional. The maximum horizontal resolution of the encoded video.
    Integer maxHeight = 56; // Integer | Optional. The maximum vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    Boolean alwaysBurnInSubtitleWhenTranscoding = false; // Boolean | Whether to always burn in subtitles when transcoding.
    try {
      File result = apiInstance.getHlsVideoSegment(itemId, playlistId, segmentId, container, runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#getHlsVideoSegment");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **playlistId** | **String**| The playlist id. | |
| **segmentId** | **Integer**| The segment id. | |
| **container** | **String**| The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. | |
| **runtimeTicks** | **Long**| The position of the requested segment in ticks. | |
| **actualSegmentLengthTicks** | **Long**| The length of the requested segment in ticks. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The desired segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |
| **alwaysBurnInSubtitleWhenTranscoding** | **Boolean**| Whether to always burn in subtitles when transcoding. | [optional] [default to false] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: video/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getLiveHlsStream"></a>
# **getLiveHlsStream**
> File getLiveHlsStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, maxWidth, maxHeight, enableSubtitlesInManifest, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding)

Gets a hls live stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String container = "container_example"; // String | The audio container.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Integer maxWidth = 56; // Integer | Optional. The max width.
    Integer maxHeight = 56; // Integer | Optional. The max height.
    Boolean enableSubtitlesInManifest = true; // Boolean | Optional. Whether to enable subtitles in the manifest.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    Boolean alwaysBurnInSubtitleWhenTranscoding = false; // Boolean | Whether to always burn in subtitles when transcoding.
    try {
      File result = apiInstance.getLiveHlsStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, maxWidth, maxHeight, enableSubtitlesInManifest, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#getLiveHlsStream");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **container** | **String**| The audio container. | [optional] |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **maxWidth** | **Integer**| Optional. The max width. | [optional] |
| **maxHeight** | **Integer**| Optional. The max height. | [optional] |
| **enableSubtitlesInManifest** | **Boolean**| Optional. Whether to enable subtitles in the manifest. | [optional] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |
| **alwaysBurnInSubtitleWhenTranscoding** | **Boolean**| Whether to always burn in subtitles when transcoding. | [optional] [default to false] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Hls live stream retrieved. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMasterHlsAudioPlaylist"></a>
# **getMasterHlsAudioPlaylist**
> File getMasterHlsAudioPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableAudioVbrEncoding)

Gets an audio hls playlist stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer maxStreamingBitrate = 56; // Integer | Optional. The maximum streaming bitrate.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAdaptiveBitrateStreaming = true; // Boolean | Enable adaptive bitrate streaming.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    try {
      File result = apiInstance.getMasterHlsAudioPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableAudioVbrEncoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#getMasterHlsAudioPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **maxStreamingBitrate** | **Integer**| Optional. The maximum streaming bitrate. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAdaptiveBitrateStreaming** | **Boolean**| Enable adaptive bitrate streaming. | [optional] [default to true] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getMasterHlsVideoPlaylist"></a>
# **getMasterHlsVideoPlaylist**
> File getMasterHlsVideoPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding)

Gets a video hls playlist stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer maxWidth = 56; // Integer | Optional. The maximum horizontal resolution of the encoded video.
    Integer maxHeight = 56; // Integer | Optional. The maximum vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAdaptiveBitrateStreaming = true; // Boolean | Enable adaptive bitrate streaming.
    Boolean enableTrickplay = true; // Boolean | Enable trickplay image playlists being added to master playlist.
    Boolean enableAudioVbrEncoding = true; // Boolean | Whether to enable Audio Encoding.
    Boolean alwaysBurnInSubtitleWhenTranscoding = false; // Boolean | Whether to always burn in subtitles when transcoding.
    try {
      File result = apiInstance.getMasterHlsVideoPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#getMasterHlsVideoPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAdaptiveBitrateStreaming** | **Boolean**| Enable adaptive bitrate streaming. | [optional] [default to true] |
| **enableTrickplay** | **Boolean**| Enable trickplay image playlists being added to master playlist. | [optional] [default to true] |
| **enableAudioVbrEncoding** | **Boolean**| Whether to enable Audio Encoding. | [optional] [default to true] |
| **alwaysBurnInSubtitleWhenTranscoding** | **Boolean**| Whether to always burn in subtitles when transcoding. | [optional] [default to false] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getVariantHlsAudioPlaylist"></a>
# **getVariantHlsAudioPlaylist**
> File getVariantHlsAudioPlaylist(itemId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding)

Gets an audio stream using HTTP live streaming.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer maxStreamingBitrate = 56; // Integer | Optional. The maximum streaming bitrate.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    try {
      File result = apiInstance.getVariantHlsAudioPlaylist(itemId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#getVariantHlsAudioPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **maxStreamingBitrate** | **Integer**| Optional. The maximum streaming bitrate. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getVariantHlsVideoPlaylist"></a>
# **getVariantHlsVideoPlaylist**
> File getVariantHlsVideoPlaylist(itemId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding)

Gets a video stream using HTTP live streaming.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer maxWidth = 56; // Integer | Optional. The maximum horizontal resolution of the encoded video.
    Integer maxHeight = 56; // Integer | Optional. The maximum vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    Boolean alwaysBurnInSubtitleWhenTranscoding = false; // Boolean | Whether to always burn in subtitles when transcoding.
    try {
      File result = apiInstance.getVariantHlsVideoPlaylist(itemId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#getVariantHlsVideoPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |
| **alwaysBurnInSubtitleWhenTranscoding** | **Boolean**| Whether to always burn in subtitles when transcoding. | [optional] [default to false] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="headMasterHlsAudioPlaylist"></a>
# **headMasterHlsAudioPlaylist**
> File headMasterHlsAudioPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableAudioVbrEncoding)

Gets an audio hls playlist stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer maxStreamingBitrate = 56; // Integer | Optional. The maximum streaming bitrate.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAdaptiveBitrateStreaming = true; // Boolean | Enable adaptive bitrate streaming.
    Boolean enableAudioVbrEncoding = true; // Boolean | Optional. Whether to enable Audio Encoding.
    try {
      File result = apiInstance.headMasterHlsAudioPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableAudioVbrEncoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#headMasterHlsAudioPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **maxStreamingBitrate** | **Integer**| Optional. The maximum streaming bitrate. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAdaptiveBitrateStreaming** | **Boolean**| Enable adaptive bitrate streaming. | [optional] [default to true] |
| **enableAudioVbrEncoding** | **Boolean**| Optional. Whether to enable Audio Encoding. | [optional] [default to true] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="headMasterHlsVideoPlaylist"></a>
# **headMasterHlsVideoPlaylist**
> File headMasterHlsVideoPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding)

Gets a video hls playlist stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DynamicHlsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    DynamicHlsApi apiInstance = new DynamicHlsApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment length.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify an audio codec to encode to, e.g. mp3.
    Boolean enableAutoStreamCopy = true; // Boolean | Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true.
    Boolean allowVideoStreamCopy = true; // Boolean | Whether or not to allow copying of the video stream url.
    Boolean allowAudioStreamCopy = true; // Boolean | Whether or not to allow copying of the audio stream url.
    Boolean breakOnNonKeyFrames = true; // Boolean | Optional. Whether to break on non key frames.
    Integer audioSampleRate = 56; // Integer | Optional. Specify a specific audio sample rate, e.g. 44100.
    Integer maxAudioBitDepth = 56; // Integer | Optional. The maximum audio bit depth.
    Integer audioBitRate = 56; // Integer | Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
    Integer audioChannels = 56; // Integer | Optional. Specify a specific number of audio channels to encode to, e.g. 2.
    Integer maxAudioChannels = 56; // Integer | Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
    String profile = "profile_example"; // String | Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
    String level = "level_example"; // String | Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
    Float framerate = 3.4F; // Float | Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Float maxFramerate = 3.4F; // Float | Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements.
    Boolean copyTimestamps = true; // Boolean | Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
    Long startTimeTicks = 56L; // Long | Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
    Integer width = 56; // Integer | Optional. The fixed horizontal resolution of the encoded video.
    Integer height = 56; // Integer | Optional. The fixed vertical resolution of the encoded video.
    Integer maxWidth = 56; // Integer | Optional. The maximum horizontal resolution of the encoded video.
    Integer maxHeight = 56; // Integer | Optional. The maximum vertical resolution of the encoded video.
    Integer videoBitRate = 56; // Integer | Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults.
    Integer subtitleStreamIndex = 56; // Integer | Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
    SubtitleDeliveryMethod subtitleMethod = SubtitleDeliveryMethod.fromValue("Encode"); // SubtitleDeliveryMethod | Optional. Specify the subtitle delivery method.
    Integer maxRefFrames = 56; // Integer | Optional.
    Integer maxVideoBitDepth = 56; // Integer | Optional. The maximum video bit depth.
    Boolean requireAvc = true; // Boolean | Optional. Whether to require avc.
    Boolean deInterlace = true; // Boolean | Optional. Whether to deinterlace the video.
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamorphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    Boolean enableAdaptiveBitrateStreaming = true; // Boolean | Enable adaptive bitrate streaming.
    Boolean enableTrickplay = true; // Boolean | Enable trickplay image playlists being added to master playlist.
    Boolean enableAudioVbrEncoding = true; // Boolean | Whether to enable Audio Encoding.
    Boolean alwaysBurnInSubtitleWhenTranscoding = false; // Boolean | Whether to always burn in subtitles when transcoding.
    try {
      File result = apiInstance.headMasterHlsVideoPlaylist(itemId, mediaSourceId, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DynamicHlsApi#headMasterHlsVideoPlaylist");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **itemId** | **UUID**| The item id. | |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment length. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify an audio codec to encode to, e.g. mp3. | [optional] |
| **enableAutoStreamCopy** | **Boolean**| Whether or not to allow automatic stream copy if requested values match the original source. Defaults to true. | [optional] |
| **allowVideoStreamCopy** | **Boolean**| Whether or not to allow copying of the video stream url. | [optional] |
| **allowAudioStreamCopy** | **Boolean**| Whether or not to allow copying of the audio stream url. | [optional] |
| **breakOnNonKeyFrames** | **Boolean**| Optional. Whether to break on non key frames. | [optional] |
| **audioSampleRate** | **Integer**| Optional. Specify a specific audio sample rate, e.g. 44100. | [optional] |
| **maxAudioBitDepth** | **Integer**| Optional. The maximum audio bit depth. | [optional] |
| **audioBitRate** | **Integer**| Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults. | [optional] |
| **audioChannels** | **Integer**| Optional. Specify a specific number of audio channels to encode to, e.g. 2. | [optional] |
| **maxAudioChannels** | **Integer**| Optional. Specify a maximum number of audio channels to encode to, e.g. 2. | [optional] |
| **profile** | **String**| Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high. | [optional] |
| **level** | **String**| Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. | [optional] |
| **framerate** | **Float**| Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **maxFramerate** | **Float**| Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be omitted unless the device has specific requirements. | [optional] |
| **copyTimestamps** | **Boolean**| Whether or not to copy timestamps when transcoding with an offset. Defaults to false. | [optional] |
| **startTimeTicks** | **Long**| Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. | [optional] |
| **width** | **Integer**| Optional. The fixed horizontal resolution of the encoded video. | [optional] |
| **height** | **Integer**| Optional. The fixed vertical resolution of the encoded video. | [optional] |
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
| **videoBitRate** | **Integer**| Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to encoder defaults. | [optional] |
| **subtitleStreamIndex** | **Integer**| Optional. The index of the subtitle stream to use. If omitted no subtitles will be used. | [optional] |
| **subtitleMethod** | **SubtitleDeliveryMethod**| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | **EncodingContext**| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |
| **enableAdaptiveBitrateStreaming** | **Boolean**| Enable adaptive bitrate streaming. | [optional] [default to true] |
| **enableTrickplay** | **Boolean**| Enable trickplay image playlists being added to master playlist. | [optional] [default to true] |
| **enableAudioVbrEncoding** | **Boolean**| Whether to enable Audio Encoding. | [optional] [default to true] |
| **alwaysBurnInSubtitleWhenTranscoding** | **Boolean**| Whether to always burn in subtitles when transcoding. | [optional] [default to false] |

### Return type

[**File**](File.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/x-mpegURL

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

