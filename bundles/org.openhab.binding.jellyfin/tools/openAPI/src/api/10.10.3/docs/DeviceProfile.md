

# DeviceProfile

A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain device is able to play.  <br />  Specifically, it defines the supported <see cref=\"P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\">containers</see> and  <see cref=\"P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\">codecs</see> (video and/or audio, including codec profiles and levels)  the device is able to direct play (without transcoding or remuxing),  as well as which <see cref=\"P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\">containers/codecs to transcode to</see> in case it isn't.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Gets or sets the name of this device profile. |  [optional] |
|**id** | **String** | Gets or sets the Id. |  [optional] |
|**identification** | [**DeviceIdentification**](DeviceIdentification.md) | Gets or sets the Identification. |  [optional] |
|**friendlyName** | **String** | Gets or sets the friendly name of the device profile, which can be shown to users. |  [optional] |
|**manufacturer** | **String** | Gets or sets the manufacturer of the device which this profile represents. |  [optional] |
|**manufacturerUrl** | **String** | Gets or sets an url for the manufacturer of the device which this profile represents. |  [optional] |
|**modelName** | **String** | Gets or sets the model name of the device which this profile represents. |  [optional] |
|**modelDescription** | **String** | Gets or sets the model description of the device which this profile represents. |  [optional] |
|**modelNumber** | **String** | Gets or sets the model number of the device which this profile represents. |  [optional] |
|**modelUrl** | **String** | Gets or sets the ModelUrl. |  [optional] |
|**serialNumber** | **String** | Gets or sets the serial number of the device which this profile represents. |  [optional] |
|**enableAlbumArtInDidl** | **Boolean** | Gets or sets a value indicating whether EnableAlbumArtInDidl. |  [optional] |
|**enableSingleAlbumArtLimit** | **Boolean** | Gets or sets a value indicating whether EnableSingleAlbumArtLimit. |  [optional] |
|**enableSingleSubtitleLimit** | **Boolean** | Gets or sets a value indicating whether EnableSingleSubtitleLimit. |  [optional] |
|**supportedMediaTypes** | **String** | Gets or sets the SupportedMediaTypes. |  [optional] |
|**userId** | **String** | Gets or sets the UserId. |  [optional] |
|**albumArtPn** | **String** | Gets or sets the AlbumArtPn. |  [optional] |
|**maxAlbumArtWidth** | **Integer** | Gets or sets the MaxAlbumArtWidth. |  [optional] |
|**maxAlbumArtHeight** | **Integer** | Gets or sets the MaxAlbumArtHeight. |  [optional] |
|**maxIconWidth** | **Integer** | Gets or sets the maximum allowed width of embedded icons. |  [optional] |
|**maxIconHeight** | **Integer** | Gets or sets the maximum allowed height of embedded icons. |  [optional] |
|**maxStreamingBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for all streamed content. |  [optional] |
|**maxStaticBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for statically streamed content (&#x3D; direct played files). |  [optional] |
|**musicStreamingTranscodingBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for transcoded music streams. |  [optional] |
|**maxStaticMusicBitrate** | **Integer** | Gets or sets the maximum allowed bitrate for statically streamed (&#x3D; direct played) music files. |  [optional] |
|**sonyAggregationFlags** | **String** | Gets or sets the content of the aggregationFlags element in the urn:schemas-sonycom:av namespace. |  [optional] |
|**protocolInfo** | **String** | Gets or sets the ProtocolInfo. |  [optional] |
|**timelineOffsetSeconds** | **Integer** | Gets or sets the TimelineOffsetSeconds. |  [optional] |
|**requiresPlainVideoItems** | **Boolean** | Gets or sets a value indicating whether RequiresPlainVideoItems. |  [optional] |
|**requiresPlainFolders** | **Boolean** | Gets or sets a value indicating whether RequiresPlainFolders. |  [optional] |
|**enableMSMediaReceiverRegistrar** | **Boolean** | Gets or sets a value indicating whether EnableMSMediaReceiverRegistrar. |  [optional] |
|**ignoreTranscodeByteRangeRequests** | **Boolean** | Gets or sets a value indicating whether IgnoreTranscodeByteRangeRequests. |  [optional] |
|**xmlRootAttributes** | [**List&lt;XmlAttribute&gt;**](XmlAttribute.md) | Gets or sets the XmlRootAttributes. |  [optional] |
|**directPlayProfiles** | [**List&lt;DirectPlayProfile&gt;**](DirectPlayProfile.md) | Gets or sets the direct play profiles. |  [optional] |
|**transcodingProfiles** | [**List&lt;TranscodingProfile&gt;**](TranscodingProfile.md) | Gets or sets the transcoding profiles. |  [optional] |
|**containerProfiles** | [**List&lt;ContainerProfile&gt;**](ContainerProfile.md) | Gets or sets the container profiles. |  [optional] |
|**codecProfiles** | [**List&lt;CodecProfile&gt;**](CodecProfile.md) | Gets or sets the codec profiles. |  [optional] |
|**responseProfiles** | [**List&lt;ResponseProfile&gt;**](ResponseProfile.md) | Gets or sets the ResponseProfiles. |  [optional] |
|**subtitleProfiles** | [**List&lt;SubtitleProfile&gt;**](SubtitleProfile.md) | Gets or sets the subtitle profiles. |  [optional] |



