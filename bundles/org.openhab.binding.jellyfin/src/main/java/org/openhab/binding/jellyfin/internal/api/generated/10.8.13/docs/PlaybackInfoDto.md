

# PlaybackInfoDto

Plabyback info dto.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**userId** | **UUID** | Gets or sets the playback userId. |  [optional] |
|**maxStreamingBitrate** | **Integer** | Gets or sets the max streaming bitrate. |  [optional] |
|**startTimeTicks** | **Long** | Gets or sets the start time in ticks. |  [optional] |
|**audioStreamIndex** | **Integer** | Gets or sets the audio stream index. |  [optional] |
|**subtitleStreamIndex** | **Integer** | Gets or sets the subtitle stream index. |  [optional] |
|**maxAudioChannels** | **Integer** | Gets or sets the max audio channels. |  [optional] |
|**mediaSourceId** | **String** | Gets or sets the media source id. |  [optional] |
|**liveStreamId** | **String** | Gets or sets the live stream id. |  [optional] |
|**deviceProfile** | [**DeviceProfile**](DeviceProfile.md) | A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain device is able to play.  &lt;br /&gt;  Specifically, it defines the supported &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\&quot;&gt;containers&lt;/see&gt; and  &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\&quot;&gt;codecs&lt;/see&gt; (video and/or audio, including codec profiles and levels)  the device is able to direct play (without transcoding or remuxing),  as well as which &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\&quot;&gt;containers/codecs to transcode to&lt;/see&gt; in case it isn&#39;t. |  [optional] |
|**enableDirectPlay** | **Boolean** | Gets or sets a value indicating whether to enable direct play. |  [optional] |
|**enableDirectStream** | **Boolean** | Gets or sets a value indicating whether to enable direct stream. |  [optional] |
|**enableTranscoding** | **Boolean** | Gets or sets a value indicating whether to enable transcoding. |  [optional] |
|**allowVideoStreamCopy** | **Boolean** | Gets or sets a value indicating whether to enable video stream copy. |  [optional] |
|**allowAudioStreamCopy** | **Boolean** | Gets or sets a value indicating whether to allow audio stream copy. |  [optional] |
|**autoOpenLiveStream** | **Boolean** | Gets or sets a value indicating whether to auto open the live stream. |  [optional] |



