# VideosApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteAlternateSources**](VideosApi.md#deleteAlternateSources) | **DELETE** /Videos/{itemId}/AlternateSources | Removes alternate video sources. |
| [**getAdditionalPart**](VideosApi.md#getAdditionalPart) | **GET** /Videos/{itemId}/AdditionalParts | Gets additional parts for a video. |
| [**getVideoStream**](VideosApi.md#getVideoStream) | **GET** /Videos/{itemId}/stream | Gets a video stream. |
| [**getVideoStreamByContainer**](VideosApi.md#getVideoStreamByContainer) | **GET** /Videos/{itemId}/stream.{container} | Gets a video stream. |
| [**headVideoStream**](VideosApi.md#headVideoStream) | **HEAD** /Videos/{itemId}/stream | Gets a video stream. |
| [**headVideoStreamByContainer**](VideosApi.md#headVideoStreamByContainer) | **HEAD** /Videos/{itemId}/stream.{container} | Gets a video stream. |
| [**mergeVersions**](VideosApi.md#mergeVersions) | **POST** /Videos/MergeVersions | Merges videos into a single record. |


<a id="deleteAlternateSources"></a>
# **deleteAlternateSources**
> deleteAlternateSources(itemId)

Removes alternate video sources.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    VideosApi apiInstance = new VideosApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    try {
      apiInstance.deleteAlternateSources(itemId);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideosApi#deleteAlternateSources");
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

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Alternate sources deleted. |  -  |
| **404** | Video not found. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getAdditionalPart"></a>
# **getAdditionalPart**
> BaseItemDtoQueryResult getAdditionalPart(itemId, userId)

Gets additional parts for a video.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    VideosApi apiInstance = new VideosApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    UUID userId = UUID.randomUUID(); // UUID | Optional. Filter by user id, and attach user data.
    try {
      BaseItemDtoQueryResult result = apiInstance.getAdditionalPart(itemId, userId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideosApi#getAdditionalPart");
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
| **userId** | **UUID**| Optional. Filter by user id, and attach user data. | [optional] |

### Return type

[**BaseItemDtoQueryResult**](BaseItemDtoQueryResult.md)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Additional parts returned. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

<a id="getVideoStream"></a>
# **getVideoStream**
> File getVideoStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets a video stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    VideosApi apiInstance = new VideosApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String container = "container_example"; // String | The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
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
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.getVideoStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideosApi#getVideoStream");
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
| **container** | **String**| The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. | [optional] |
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
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
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
 - **Accept**: video/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |

<a id="getVideoStreamByContainer"></a>
# **getVideoStreamByContainer**
> File getVideoStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets a video stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    VideosApi apiInstance = new VideosApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String container = "container_example"; // String | The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
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
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.getVideoStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideosApi#getVideoStreamByContainer");
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
| **container** | **String**| The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. | |
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
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
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
 - **Accept**: video/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |

<a id="headVideoStream"></a>
# **headVideoStream**
> File headVideoStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets a video stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    VideosApi apiInstance = new VideosApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String container = "container_example"; // String | The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
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
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.headVideoStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideosApi#headVideoStream");
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
| **container** | **String**| The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. | [optional] |
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
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
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
 - **Accept**: video/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |

<a id="headVideoStreamByContainer"></a>
# **headVideoStreamByContainer**
> File headVideoStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions)

Gets a video stream.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    VideosApi apiInstance = new VideosApi(defaultClient);
    UUID itemId = UUID.randomUUID(); // UUID | The item id.
    String container = "container_example"; // String | The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv.
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
    String videoCodec = "videoCodec_example"; // String | Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
    String subtitleCodec = "subtitleCodec_example"; // String | Optional. Specify a subtitle codec to encode to.
    String transcodeReasons = "transcodeReasons_example"; // String | Optional. The transcoding reason.
    Integer audioStreamIndex = 56; // Integer | Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
    Integer videoStreamIndex = 56; // Integer | Optional. The index of the video stream to use. If omitted the first video stream will be used.
    EncodingContext context = EncodingContext.fromValue("Streaming"); // EncodingContext | Optional. The MediaBrowser.Model.Dlna.EncodingContext.
    Map<String, String> streamOptions = new HashMap(); // Map<String, String> | Optional. The streaming options.
    try {
      File result = apiInstance.headVideoStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideosApi#headVideoStreamByContainer");
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
| **container** | **String**| The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg, avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. | |
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
| **maxWidth** | **Integer**| Optional. The maximum horizontal resolution of the encoded video. | [optional] |
| **maxHeight** | **Integer**| Optional. The maximum vertical resolution of the encoded video. | [optional] |
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
 - **Accept**: video/*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Video stream returned. |  -  |

<a id="mergeVersions"></a>
# **mergeVersions**
> mergeVersions(ids)

Merges videos into a single record.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.auth.*;
import org.openapitools.client.models.*;
import org.openapitools.client.api.VideosApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");
    
    // Configure API key authorization: CustomAuthentication
    ApiKeyAuth CustomAuthentication = (ApiKeyAuth) defaultClient.getAuthentication("CustomAuthentication");
    CustomAuthentication.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //CustomAuthentication.setApiKeyPrefix("Token");

    VideosApi apiInstance = new VideosApi(defaultClient);
    List<UUID> ids = Arrays.asList(); // List<UUID> | Item id list. This allows multiple, comma delimited.
    try {
      apiInstance.mergeVersions(ids);
    } catch (ApiException e) {
      System.err.println("Exception when calling VideosApi#mergeVersions");
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
| **ids** | [**List&lt;UUID&gt;**](UUID.md)| Item id list. This allows multiple, comma delimited. | |

### Return type

null (empty response body)

### Authorization

[CustomAuthentication](../README.md#CustomAuthentication)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, application/json; profile=CamelCase, application/json; profile=PascalCase

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Videos merged. |  -  |
| **400** | Supply at least 2 video ids. |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |

