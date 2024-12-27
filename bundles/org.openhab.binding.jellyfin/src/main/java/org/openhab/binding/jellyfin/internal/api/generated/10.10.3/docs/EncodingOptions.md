

# EncodingOptions

Class EncodingOptions.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**encodingThreadCount** | **Integer** | Gets or sets the thread count used for encoding. |  [optional] |
|**transcodingTempPath** | **String** | Gets or sets the temporary transcoding path. |  [optional] |
|**fallbackFontPath** | **String** | Gets or sets the path to the fallback font. |  [optional] |
|**enableFallbackFont** | **Boolean** | Gets or sets a value indicating whether to use the fallback font. |  [optional] |
|**enableAudioVbr** | **Boolean** | Gets or sets a value indicating whether audio VBR is enabled. |  [optional] |
|**downMixAudioBoost** | **Double** | Gets or sets the audio boost applied when downmixing audio. |  [optional] |
|**downMixStereoAlgorithm** | **DownMixStereoAlgorithms** | Gets or sets the algorithm used for downmixing audio to stereo. |  [optional] |
|**maxMuxingQueueSize** | **Integer** | Gets or sets the maximum size of the muxing queue. |  [optional] |
|**enableThrottling** | **Boolean** | Gets or sets a value indicating whether throttling is enabled. |  [optional] |
|**throttleDelaySeconds** | **Integer** | Gets or sets the delay after which throttling happens. |  [optional] |
|**enableSegmentDeletion** | **Boolean** | Gets or sets a value indicating whether segment deletion is enabled. |  [optional] |
|**segmentKeepSeconds** | **Integer** | Gets or sets seconds for which segments should be kept before being deleted. |  [optional] |
|**hardwareAccelerationType** | **HardwareAccelerationType** | Gets or sets the hardware acceleration type. |  [optional] |
|**encoderAppPath** | **String** | Gets or sets the FFmpeg path as set by the user via the UI. |  [optional] |
|**encoderAppPathDisplay** | **String** | Gets or sets the current FFmpeg path being used by the system and displayed on the transcode page. |  [optional] |
|**vaapiDevice** | **String** | Gets or sets the VA-API device. |  [optional] |
|**qsvDevice** | **String** | Gets or sets the QSV device. |  [optional] |
|**enableTonemapping** | **Boolean** | Gets or sets a value indicating whether tonemapping is enabled. |  [optional] |
|**enableVppTonemapping** | **Boolean** | Gets or sets a value indicating whether VPP tonemapping is enabled. |  [optional] |
|**enableVideoToolboxTonemapping** | **Boolean** | Gets or sets a value indicating whether videotoolbox tonemapping is enabled. |  [optional] |
|**tonemappingAlgorithm** | **TonemappingAlgorithm** | Gets or sets the tone-mapping algorithm. |  [optional] |
|**tonemappingMode** | **TonemappingMode** | Gets or sets the tone-mapping mode. |  [optional] |
|**tonemappingRange** | **TonemappingRange** | Gets or sets the tone-mapping range. |  [optional] |
|**tonemappingDesat** | **Double** | Gets or sets the tone-mapping desaturation. |  [optional] |
|**tonemappingPeak** | **Double** | Gets or sets the tone-mapping peak. |  [optional] |
|**tonemappingParam** | **Double** | Gets or sets the tone-mapping parameters. |  [optional] |
|**vppTonemappingBrightness** | **Double** | Gets or sets the VPP tone-mapping brightness. |  [optional] |
|**vppTonemappingContrast** | **Double** | Gets or sets the VPP tone-mapping contrast. |  [optional] |
|**h264Crf** | **Integer** | Gets or sets the H264 CRF. |  [optional] |
|**h265Crf** | **Integer** | Gets or sets the H265 CRF. |  [optional] |
|**encoderPreset** | **EncoderPreset** | Gets or sets the encoder preset. |  [optional] |
|**deinterlaceDoubleRate** | **Boolean** | Gets or sets a value indicating whether the framerate is doubled when deinterlacing. |  [optional] |
|**deinterlaceMethod** | **DeinterlaceMethod** | Gets or sets the deinterlace method. |  [optional] |
|**enableDecodingColorDepth10Hevc** | **Boolean** | Gets or sets a value indicating whether 10bit HEVC decoding is enabled. |  [optional] |
|**enableDecodingColorDepth10Vp9** | **Boolean** | Gets or sets a value indicating whether 10bit VP9 decoding is enabled. |  [optional] |
|**enableDecodingColorDepth10HevcRext** | **Boolean** | Gets or sets a value indicating whether 8/10bit HEVC RExt decoding is enabled. |  [optional] |
|**enableDecodingColorDepth12HevcRext** | **Boolean** | Gets or sets a value indicating whether 12bit HEVC RExt decoding is enabled. |  [optional] |
|**enableEnhancedNvdecDecoder** | **Boolean** | Gets or sets a value indicating whether the enhanced NVDEC is enabled. |  [optional] |
|**preferSystemNativeHwDecoder** | **Boolean** | Gets or sets a value indicating whether the system native hardware decoder should be used. |  [optional] |
|**enableIntelLowPowerH264HwEncoder** | **Boolean** | Gets or sets a value indicating whether the Intel H264 low-power hardware encoder should be used. |  [optional] |
|**enableIntelLowPowerHevcHwEncoder** | **Boolean** | Gets or sets a value indicating whether the Intel HEVC low-power hardware encoder should be used. |  [optional] |
|**enableHardwareEncoding** | **Boolean** | Gets or sets a value indicating whether hardware encoding is enabled. |  [optional] |
|**allowHevcEncoding** | **Boolean** | Gets or sets a value indicating whether HEVC encoding is enabled. |  [optional] |
|**allowAv1Encoding** | **Boolean** | Gets or sets a value indicating whether AV1 encoding is enabled. |  [optional] |
|**enableSubtitleExtraction** | **Boolean** | Gets or sets a value indicating whether subtitle extraction is enabled. |  [optional] |
|**hardwareDecodingCodecs** | **List&lt;String&gt;** | Gets or sets the codecs hardware encoding is used for. |  [optional] |
|**allowOnDemandMetadataBasedKeyframeExtractionForExtensions** | **List&lt;String&gt;** | Gets or sets the file extensions on-demand metadata based keyframe extraction is enabled for. |  [optional] |



