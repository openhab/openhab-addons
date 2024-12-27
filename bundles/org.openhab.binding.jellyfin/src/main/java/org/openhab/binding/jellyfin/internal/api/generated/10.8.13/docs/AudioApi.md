# AudioApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getAudioStream**](AudioApi.md#getAudioStream) | **GET** /Audio/{itemId}/stream | Gets an audio stream. |
| [**getAudioStreamByContainer**](AudioApi.md#getAudioStreamByContainer) | **GET** /Audio/{itemId}/stream.{container} | Gets an audio stream. |
| [**headAudioStream**](AudioApi.md#headAudioStream) | **HEAD** /Audio/{itemId}/stream | Gets an audio stream. |
| [**headAudioStreamByContainer**](AudioApi.md#headAudioStreamByContainer) | **HEAD** /Audio/{itemId}/stream.{container} | Gets an audio stream. |


<a id="getAudioStream"></a>
# **getAudioStream**
> File getAudioStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets an audio stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AudioApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    AudioApi apiInstance = new AudioApi(defaultClient);
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
    String audioCodec = "audioCodec_example"; // String | Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url's extension. Options: aac, mp3, vorbis, wma.
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
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.getAudioStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AudioApi#getAudioStream");
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
| **audioCodec** | **String**| Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension. Options: aac, mp3, vorbis, wma. | [optional] |
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
| **subtitleMethod** | [**SubtitleDeliveryMethod**](.md)| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | [**EncodingContext**](.md)| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |

<a id="getAudioStreamByContainer"></a>
# **getAudioStreamByContainer**
> File getAudioStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets an audio stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AudioApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    AudioApi apiInstance = new AudioApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String container = "container_example"; // String | The audio container.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment lenght.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url's extension. Options: aac, mp3, vorbis, wma.
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
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamporphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.getAudioStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AudioApi#getAudioStreamByContainer");
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
| **container** | **String**| The audio container. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment lenght. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension. Options: aac, mp3, vorbis, wma. | [optional] |
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
| **subtitleMethod** | [**SubtitleDeliveryMethod**](.md)| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamporphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | [**EncodingContext**](.md)| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |

<a id="headAudioStream"></a>
# **headAudioStream**
> File headAudioStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets an audio stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AudioApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    AudioApi apiInstance = new AudioApi(defaultClient);
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
    String audioCodec = "audioCodec_example"; // String | Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url's extension. Options: aac, mp3, vorbis, wma.
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
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.headAudioStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AudioApi#headAudioStream");
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
| **audioCodec** | **String**| Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension. Options: aac, mp3, vorbis, wma. | [optional] |
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
| **subtitleMethod** | [**SubtitleDeliveryMethod**](.md)| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamorphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | [**EncodingContext**](.md)| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |

<a id="headAudioStreamByContainer"></a>
# **headAudioStreamByContainer**
> File headAudioStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets an audio stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.AudioApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    AudioApi apiInstance = new AudioApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String container = "container_example"; // String | The audio container.
    Boolean _static = true; // Boolean | Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false.
    String params = "params_example"; // String | The streaming parameters.
    String tag = "tag_example"; // String | The tag.
    String deviceProfileId = "deviceProfileId_example"; // String | Optional. The dlna device profile id to utilize.
    String playSessionId = "playSessionId_example"; // String | The play session id.
    String segmentContainer = "segmentContainer_example"; // String | The segment container.
    Integer segmentLength = 56; // Integer | The segment lenght.
    Integer minSegments = 56; // Integer | The minimum number of segments.
    String mediaSourceId = "mediaSourceId_example"; // String | The media version id, if playing an alternate version.
    String deviceId = "deviceId_example"; // String | The device id of the client requesting. Used to stop encoding processes when needed.
    String audioCodec = "audioCodec_example"; // String | Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url's extension. Options: aac, mp3, vorbis, wma.
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
    Boolean requireNonAnamorphic = true; // Boolean | Optional. Whether to require a non anamporphic stream.
    Integer transcodingMaxAudioChannels = 56; // Integer | Optional. The maximum number of audio channels to transcode.
    Integer cpuCoreLimit = 56; // Integer | Optional. The limit of how many cpu cores to use.
    String liveStreamId = "liveStreamId_example"; // String | The live stream id.
    Boolean enableMpegtsM2TsMode = true; // Boolean | Optional. Whether to enable the MpegtsM2Ts mode.
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.headAudioStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AudioApi#headAudioStreamByContainer");
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
| **container** | **String**| The audio container. | |
| **_static** | **Boolean**| Optional. If true, the original file will be streamed statically without any encoding. Use either no url extension or the original file extension. true/false. | [optional] |
| **params** | **String**| The streaming parameters. | [optional] |
| **tag** | **String**| The tag. | [optional] |
| **deviceProfileId** | **String**| Optional. The dlna device profile id to utilize. | [optional] |
| **playSessionId** | **String**| The play session id. | [optional] |
| **segmentContainer** | **String**| The segment container. | [optional] |
| **segmentLength** | **Integer**| The segment lenght. | [optional] |
| **minSegments** | **Integer**| The minimum number of segments. | [optional] |
| **mediaSourceId** | **String**| The media version id, if playing an alternate version. | [optional] |
| **deviceId** | **String**| The device id of the client requesting. Used to stop encoding processes when needed. | [optional] |
| **audioCodec** | **String**| Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select using the url&#39;s extension. Options: aac, mp3, vorbis, wma. | [optional] |
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
| **subtitleMethod** | [**SubtitleDeliveryMethod**](.md)| Optional. Specify the subtitle delivery method. | [optional] [enum: Encode, Embed, External, Hls, Drop] |
| **maxRefFrames** | **Integer**| Optional. | [optional] |
| **maxVideoBitDepth** | **Integer**| Optional. The maximum video bit depth. | [optional] |
| **requireAvc** | **Boolean**| Optional. Whether to require avc. | [optional] |
| **deInterlace** | **Boolean**| Optional. Whether to deinterlace the video. | [optional] |
| **requireNonAnamorphic** | **Boolean**| Optional. Whether to require a non anamporphic stream. | [optional] |
| **transcodingMaxAudioChannels** | **Integer**| Optional. The maximum number of audio channels to transcode. | [optional] |
| **cpuCoreLimit** | **Integer**| Optional. The limit of how many cpu cores to use. | [optional] |
| **liveStreamId** | **String**| The live stream id. | [optional] |
| **enableMpegtsM2TsMode** | **Boolean**| Optional. Whether to enable the MpegtsM2Ts mode. | [optional] |
| **videoCodec** | **String**| Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url&#39;s extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv. | [optional] |
| **subtitleCodec** | **String**| Optional. Specify a subtitle codec to encode to. | [optional] |
| **transcodeReasons** | **String**| Optional. The transcoding reason. | [optional] |
| **audioStreamIndex** | **Integer**| Optional. The index of the audio stream to use. If omitted the first audio stream will be used. | [optional] |
| **videoStreamIndex** | **Integer**| Optional. The index of the video stream to use. If omitted the first video stream will be used. | [optional] |
| **context** | [**EncodingContext**](.md)| Optional. The MediaBrowser.Model.Dlna.EncodingContext. | [optional] [enum: Streaming, Static] |
| **streamOptions** | [**Map&lt;String, String&gt;**](String.md)| Optional. The streaming options. | [optional] |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: audio/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audio stream returned. |  -  |

