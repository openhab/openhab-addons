

# DeviceProfile

A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain device is able to play.  <br />  Specifically, it defines the supported <see cref=\"P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\">containers</see> and  <see cref=\"P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\">codecs</see> (video and/or audio, including codec profiles and levels)  the device is able to direct play (without transcoding or remuxing),  as well as which <see cref=\"P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\">containers/codecs to transcode to</see> in case it isn't.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Gets or sets the name of this device profile. User profiles must have a unique name. |  [optional] |
|**id** | **UUID** | Gets or sets the unique internal identifier. |  [optional] |
|**maxStreamingBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for all streamed content. |  [optional] |
|**maxStaticBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for statically streamed content (&#x3D; direct played files). |  [optional] |
|**musicStreamingTranscodingBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for transcoded music streams. |  [optional] |
|**maxStaticMusicBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for statically streamed (&#x3D; direct played) music files. |  [optional] |
|**directPlayProfiles** | [**List&lt;DirectPlayProfile&gt;**](DirectPlayProfile.md) | Gets or sets the direct play profiles. |  [optional] |
|**transcodingProfiles** | [**List&lt;TranscodingProfile&gt;**](TranscodingProfile.md) | Gets or sets the transcoding profiles. |  [optional] |
|**containerProfiles** | [**List&lt;ContainerProfile&gt;**](ContainerProfile.md) | Gets or sets the container profiles. Failing to meet these optional conditions causes transcoding to occur. |  [optional] |
|**codecProfiles** | [**List&lt;CodecProfile&gt;**](CodecProfile.md) | Gets or sets the codec profiles. |  [optional] |
|**subtitleProfiles** | [**List&lt;SubtitleProfile&gt;**](SubtitleProfile.md) | Gets or sets the subtitle profiles. |  [optional] |



