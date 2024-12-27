

# ClientCapabilitiesDto

Client capabilities dto.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**playableMediaTypes** | **List&lt;String&gt;** | Gets or sets the list of playable media types. |  [optional] |
|**supportedCommands** | **List&lt;GeneralCommandType&gt;** | Gets or sets the list of supported commands. |  [optional] |
|**supportsMediaControl** | **Boolean** | Gets or sets a value indicating whether session supports media control. |  [optional] |
|**supportsContentUploading** | **Boolean** | Gets or sets a value indicating whether session supports content uploading. |  [optional] |
|**messageCallbackUrl** | **String** | Gets or sets the message callback url. |  [optional] |
|**supportsPersistentIdentifier** | **Boolean** | Gets or sets a value indicating whether session supports a persistent identifier. |  [optional] |
|**supportsSync** | **Boolean** | Gets or sets a value indicating whether session supports sync. |  [optional] |
|**deviceProfile** | [**DeviceProfile**](DeviceProfile.md) | A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain device is able to play.  &lt;br /&gt;  Specifically, it defines the supported &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\&quot;&gt;containers&lt;/see&gt; and  &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\&quot;&gt;codecs&lt;/see&gt; (video and/or audio, including codec profiles and levels)  the device is able to direct play (without transcoding or remuxing),  as well as which &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\&quot;&gt;containers/codecs to transcode to&lt;/see&gt; in case it isn&#39;t. |  [optional] |
|**appStoreUrl** | **String** | Gets or sets the app store url. |  [optional] |
|**iconUrl** | **String** | Gets or sets the icon url. |  [optional] |



