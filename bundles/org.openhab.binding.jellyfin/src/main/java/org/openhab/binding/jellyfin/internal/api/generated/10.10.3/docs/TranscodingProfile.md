

# TranscodingProfile

A class for transcoding profile information.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**container** | **String** | Gets or sets the container. |  [optional] |
|**type** | **DlnaProfileType** | Gets or sets the DLNA profile type. |  [optional] |
|**videoCodec** | **String** | Gets or sets the video codec. |  [optional] |
|**audioCodec** | **String** | Gets or sets the audio codec. |  [optional] |
|**protocol** | **MediaStreamProtocol** | Media streaming protocol.  Lowercase for backwards compatibility. |  [optional] |
|**estimateContentLength** | **Boolean** | Gets or sets a value indicating whether the content length should be estimated. |  [optional] |
|**enableMpegtsM2TsMode** | **Boolean** | Gets or sets a value indicating whether M2TS mode is enabled. |  [optional] |
|**transcodeSeekInfo** | **TranscodeSeekInfo** | Gets or sets the transcoding seek info mode. |  [optional] |
|**copyTimestamps** | **Boolean** | Gets or sets a value indicating whether timestamps should be copied. |  [optional] |
|**context** | **EncodingContext** | Gets or sets the encoding context. |  [optional] |
|**enableSubtitlesInManifest** | **Boolean** | Gets or sets a value indicating whether subtitles are allowed in the manifest. |  [optional] |
|**maxAudioChannels** | **String** | Gets or sets the maximum audio channels. |  [optional] |
|**minSegments** | **Integer** | Gets or sets the minimum amount of segments. |  [optional] |
|**segmentLength** | **Integer** | Gets or sets the segment length. |  [optional] |
|**breakOnNonKeyFrames** | **Boolean** | Gets or sets a value indicating whether breaking the video stream on non-keyframes is supported. |  [optional] |
|**conditions** | [**List&lt;ProfileCondition&gt;**](ProfileCondition.md) | Gets or sets the profile conditions. |  [optional] |
|**enableAudioVbrEncoding** | **Boolean** | Gets or sets a value indicating whether variable bitrate encoding is supported. |  [optional] |



