

# TrickplayOptions

Class TrickplayOptions.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**enableHwAcceleration** | **Boolean** | Gets or sets a value indicating whether or not to use HW acceleration. |  [optional] |
|**enableHwEncoding** | **Boolean** | Gets or sets a value indicating whether or not to use HW accelerated MJPEG encoding. |  [optional] |
|**enableKeyFrameOnlyExtraction** | **Boolean** | Gets or sets a value indicating whether to only extract key frames.  Significantly faster, but is not compatible with all decoders and/or video files. |  [optional] |
|**scanBehavior** | **TrickplayScanBehavior** | Gets or sets the behavior used by trickplay provider on library scan/update. |  [optional] |
|**processPriority** | **ProcessPriorityClass** | Gets or sets the process priority for the ffmpeg process. |  [optional] |
|**interval** | **Integer** | Gets or sets the interval, in ms, between each new trickplay image. |  [optional] |
|**widthResolutions** | **List&lt;Integer&gt;** | Gets or sets the target width resolutions, in px, to generates preview images for. |  [optional] |
|**tileWidth** | **Integer** | Gets or sets number of tile images to allow in X dimension. |  [optional] |
|**tileHeight** | **Integer** | Gets or sets number of tile images to allow in Y dimension. |  [optional] |
|**qscale** | **Integer** | Gets or sets the ffmpeg output quality level. |  [optional] |
|**jpegQuality** | **Integer** | Gets or sets the jpeg quality to use for image tiles. |  [optional] |
|**processThreads** | **Integer** | Gets or sets the number of threads to be used by ffmpeg. |  [optional] |



